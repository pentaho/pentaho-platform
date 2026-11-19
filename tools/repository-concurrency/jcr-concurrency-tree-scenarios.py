#!/usr/bin/env python3
"""Folder-tree concurrency scenarios for the JCR repository lock changes (BISERVER-15820).

Complements jcr-concurrency-endpoint-bench.py (single shared file, writers vs readers). Here one mover
thread repeatedly moves or trashes/restores a folder tree while readers read its descendants and writers
overwrite non-versioned files in the folders the tree moves between.

Scenarios (--scenarios, comma list):
  move   move the tree between two folders; readers on descendants, writers on non-versioned siblings
  trash  delete (to trash) and restore the tree; readers also list the trash
  acl    like trash/move, but a second user reads descendants hidden from them by ACL (needs --restricted-user)

Pass criteria: no HTTP 5xx, no timeout/connection error, no mover failure, no JCR signature in the server log.
404/403 on readers and writers are expected while the tree is in flight and are only counted.
"""
import argparse
import base64
import http.client
import json
import os
import re
import statistics
import threading
import time
import urllib.parse
import uuid
from collections import Counter

LOG_SIGNATURES = (
    "invaliditemstateexception", "could not find child", "session has been closed",
    "unable to save repository element", "file data must not be null", "deadlock",
    "nosuchitemstateexception",
)


def percentile(values, fraction):
    ordered = sorted(values)
    if not ordered:
        return None
    return round(ordered[min(len(ordered) - 1, int((len(ordered) - 1) * fraction))] * 1000, 3)


def colon(path):
    return urllib.parse.quote(":" + path.strip("/").replace("/", ":"), safe="")


class Metrics:
    def __init__(self):
        self.lock = threading.Lock()
        self.latencies = {}
        self.statuses = {}
        self.errors = []

    def record(self, operation, seconds, status):
        with self.lock:
            self.latencies.setdefault(operation, []).append(seconds)
            self.statuses.setdefault(operation, Counter())[status] += 1

    def error(self, message):
        with self.lock:
            self.errors.append(message)


class Session:
    def __init__(self, args, username, password, metrics):
        self.args = args
        self.metrics = metrics
        token = base64.b64encode(("%s:%s" % (username, password)).encode()).decode("ascii")
        self.headers = {"Authorization": "Basic " + token, "Accept": "*/*"}
        self.connection = None

    def request(self, operation, method, path, body=None, content_type=None, accept=None,
                tolerate=(), strict=False):
        """strict: any status other than 200/204 is an error (mover and setup calls)."""
        headers = dict(self.headers)
        if content_type:
            headers["Content-Type"] = content_type
        if accept:
            headers["Accept"] = accept
        started = time.perf_counter()
        status, payload = 0, b""
        try:
            if self.connection is None:
                self.connection = http.client.HTTPConnection(
                    self.args.host, self.args.port, timeout=self.args.request_timeout)
            self.connection.request(method, self.args.context + path, body=body, headers=headers)
            response = self.connection.getresponse()
            payload = response.read()
            status = response.status
        except Exception as error:
            self.metrics.error("%s %s -> %s: %s" % (method, path, type(error).__name__, error))
            try:
                self.connection.close()
            finally:
                self.connection = None
        self.metrics.record(operation, time.perf_counter() - started, status)
        if status >= 500:
            self.metrics.error("%s %s -> HTTP %d: %s" % (
                method, path, status, payload[:200].decode("utf-8", "replace")))
        elif strict and status not in (200, 204):
            self.metrics.error("%s %s -> unexpected HTTP %d" % (method, path, status))
        elif status in (401,):
            self.metrics.error("%s %s -> HTTP 401" % (method, path))
        return status, payload


def multipart(fields, filename, content):
    boundary = "----jcr-tree-%s" % uuid.uuid4().hex
    parts = []
    for name, value in fields.items():
        parts += [("--%s\r\nContent-Disposition: form-data; name=\"%s\"\r\n\r\n" % (boundary, name)).encode(),
                  value.encode(), b"\r\n"]
    parts += [("--%s\r\nContent-Disposition: form-data; name=\"fileUpload\"; filename=\"%s\"\r\n"
               "Content-Type: application/octet-stream\r\n\r\n" % (boundary, filename)).encode(),
              content, b"\r\n", ("--%s--\r\n" % boundary).encode()]
    return b"".join(parts), "multipart/form-data; boundary=" + boundary


def import_file(session, operation, directory, name, content):
    body, content_type = multipart({
        "importDir": directory, "fileNameOverride": name, "overwriteFile": "true",
        "logLevel": "ERROR", "retainOwnership": "true", "charSet": "UTF-8"}, name, content)
    return session.request(operation, "POST", "/api/repo/files/import", body, content_type)


def file_id(session, path):
    status, payload = session.request("setup_properties", "GET", "/api/repo/files/%s/properties" % colon(path),
                                      accept="application/json", strict=True)
    try:
        return json.loads(payload)["id"] if status == 200 else None
    except (ValueError, KeyError):
        session.metrics.error("cannot resolve id of %s" % path)
        return None


RESTRICT_ACL = (
    "<repositoryFileAclDto><entriesInheriting>false</entriesInheriting>"
    "<aces><modifiable>true</modifiable><permissions>4</permissions><recipient>%s</recipient>"
    "<recipientType>0</recipientType></aces><owner>%s</owner><ownerType>0</ownerType></repositoryFileAclDto>")


def run_scenario(args, name, payload, admin_password):
    metrics = Metrics()
    admin = Session(args, args.username, admin_password, metrics)
    root = "%s/perf-tree-%s-%s" % (args.base_dir.rstrip("/"), name, uuid.uuid4().hex[:8])
    side_a, side_b, tree_name = root + "/a", root + "/b", "tree"
    tree_in_a = side_a + "/" + tree_name
    tree_in_b = side_b + "/" + tree_name
    children = ["child-%d.csv" % index for index in range(args.files)]
    hidden = "hidden.csv"
    restricted = name == "acl"

    for directory in (root, side_a, side_b, tree_in_a):
        admin.request("setup_mkdir", "PUT", "/api/repo/dirs/" + colon(directory), strict=True)
    for child in children + ([hidden] if restricted else []):
        import_file(admin, "setup_import", tree_in_a, child, payload)
    for sibling in range(args.writers):
        for side in (side_a, side_b):
            import_file(admin, "setup_import", side, "sibling-%d.csv" % sibling, payload)
    if restricted:
        status, _ = admin.request("setup_acl", "PUT", "/api/repo/files/%s/acl" % colon(tree_in_a + "/" + hidden),
                                  RESTRICT_ACL % (args.username, args.username), "application/xml")
        if status != 200:
            metrics.error("could not restrict %s with an ACL (HTTP %d)" % (hidden, status))
    tree_id = file_id(admin, tree_in_a)

    result = {"scenario": name, "root": root, "setup_errors": list(metrics.errors)}
    if not tree_id or metrics.errors:
        result.update(skipped=True, errors=list(metrics.errors))
        return result

    state = {"location": tree_in_a, "in_trash": False}
    stop = threading.Event()
    start = threading.Barrier(args.readers + args.writers + 2)
    mover_done = threading.Event()

    def mover():
        session = Session(args, args.username, admin_password, metrics)
        start.wait(timeout=60)
        for _ in range(args.rounds):
            if name == "move":
                target = side_b if state["location"] == tree_in_a else side_a
                session.request("move", "PUT", "/api/repo/files/%s/move" % colon(target), tree_id,
                                "text/plain", strict=True)
                state["location"] = target + "/" + tree_name
            else:
                session.request("trash", "PUT", "/api/repo/files/delete", tree_id, "text/plain", strict=True)
                state["in_trash"] = True
                session.request("restore", "PUT", "/api/repo/files/restore", tree_id, "text/plain", strict=True)
                state["in_trash"] = False
        mover_done.set()

    def reader(user, password):
        session = Session(args, user, password, metrics)
        start.wait(timeout=60)
        names = children + ([hidden] if restricted else [])
        index = 0
        while not stop.is_set():
            location = state["location"]
            session.request("read_children", "GET", "/api/repo/files/%s/children" % colon(location),
                            accept="application/json")
            session.request("read_descendant", "GET", "/api/repo/files/%s/download" %
                            colon(location + "/" + names[index % len(names)]))
            if name == "trash":
                session.request("read_trash", "GET", "/api/repo/files/deleted", accept="application/json")
            index += 1
            time.sleep(args.reader_delay)

    def writer(number):
        session = Session(args, args.username, admin_password, metrics)
        start.wait(timeout=60)
        round_number = 0
        while not stop.is_set():
            side = side_a if round_number % 2 == 0 else side_b
            import_file(session, "write_nonversioned", side, "sibling-%d.csv" % number,
                        payload + ("\nw%d-%d\n" % (number, round_number)).encode())
            round_number += 1

    threads = [threading.Thread(target=mover)]
    threads += [threading.Thread(target=writer, args=(number,)) for number in range(args.writers)]
    reader_users = [(args.username, admin_password)]
    if restricted:
        reader_users = [(args.restricted_user, args.restricted_password)] * args.readers
    else:
        reader_users = reader_users * args.readers
    threads += [threading.Thread(target=reader, args=credentials) for credentials in reader_users]
    begin = time.perf_counter()
    for thread in threads:
        thread.daemon = True
        thread.start()
    start.wait(timeout=60)
    mover_done.wait(timeout=args.rounds * args.request_timeout * 2 + 60)
    stop.set()
    for thread in threads:
        thread.join(timeout=args.request_timeout + 10)
    elapsed = time.perf_counter() - begin
    if not mover_done.is_set():
        metrics.error("mover did not finish (possible deadlock or stalled request)")

    cleanup = Session(args, args.username, admin_password, Metrics())
    if not args.keep:
        root_id = file_id(cleanup, root)
        if root_id:
            cleanup.request("cleanup", "PUT", "/api/repo/files/delete", root_id, "text/plain")
            cleanup.request("cleanup", "PUT", "/api/repo/files/deletepermanent", root_id, "text/plain")

    total = sum(len(values) for values in metrics.latencies.values())
    result.update({
        "elapsed_seconds": round(elapsed, 3),
        "requests_per_second": round(total / elapsed, 2) if elapsed else 0,
        "operations": {
            operation: {
                "count": len(values), "p50_ms": percentile(values, 0.5), "p95_ms": percentile(values, 0.95),
                "p99_ms": percentile(values, 0.99), "mean_ms": round(statistics.mean(values) * 1000, 3),
                "http_status": dict(metrics.statuses[operation]),
            } for operation, values in sorted(metrics.latencies.items())},
        "errors": metrics.errors,
    })
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument("--context", default="/pentaho")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default=os.environ.get("PENTAHO_PASSWORD", "password"))
    parser.add_argument("--restricted-user", default="")
    parser.add_argument("--restricted-password", default="")
    parser.add_argument("--payload", default=os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                                          "jcr-concurrency-payload.csv"))
    parser.add_argument("--base-dir", default="/home/admin")
    parser.add_argument("--scenarios", default="move,trash,acl")
    parser.add_argument("--rounds", type=int, default=20, help="mover iterations per scenario")
    parser.add_argument("--files", type=int, default=5, help="descendant files in the moved tree")
    parser.add_argument("--readers", type=int, default=4)
    parser.add_argument("--writers", type=int, default=2, help="non-versioned sibling writers")
    parser.add_argument("--reader-delay", type=float, default=0.05)
    parser.add_argument("--request-timeout", type=float, default=30)
    parser.add_argument("--log", default="")
    parser.add_argument("--keep", action="store_true", help="leave the generated folders in the repository")
    parser.add_argument("--label", required=True)
    args = parser.parse_args()

    with open(args.payload, "rb") as payload_file:
        payload = payload_file.read()
    log_offset = os.path.getsize(args.log) if args.log and os.path.exists(args.log) else 0

    results = []
    for name in [item.strip() for item in args.scenarios.split(",") if item.strip()]:
        if name == "acl" and not (args.restricted_user and args.restricted_password):
            results.append({"scenario": "acl", "skipped": True,
                            "errors": [], "note": "needs --restricted-user and --restricted-password"})
            continue
        results.append(run_scenario(args, name, payload, args.password))

    log_matches = []
    if args.log and os.path.exists(args.log):
        with open(args.log, "r", errors="replace") as log_file:
            log_file.seek(log_offset)
            log_matches = [line.strip()[:500] for line in log_file
                           if any(signature in line.lower() for signature in LOG_SIGNATURES)]

    total_errors = sum(len(result.get("errors", [])) for result in results)
    print(json.dumps({"label": args.label, "results": results, "total_errors": total_errors,
                      "jcr_errors_in_server_log": log_matches}, indent=2))
    if total_errors or log_matches:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
