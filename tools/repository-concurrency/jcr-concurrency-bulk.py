#!/usr/bin/env python3
"""Bulk mixed-workload stress for every code path changed by BISERVER-15820 (JcrRepositoryFileDao).

All roles run at once for --duration seconds against disjoint-and-overlapping parts of one tree:

  version-writer     overwrite versioned files (updateFile)                  REST import
  version-restorer   restore a random older version (restoreFileAtVersion)   SOAP
  version-deleter    delete a non-current version (deleteFileAtVersion)      SOAP
  version-reader     version summaries, file/data at a version               SOAP + REST download
  mover              move a folder tree between three folders (moveFile)     REST
  trasher            trash and undelete a folder tree (deleteFile/undelete)  REST
  copier             copy a tree into a fresh folder, then delete it forever (copyFile, permanentlyDeleteFile)
  churn              create folder, create file, delete permanently (createFolder/createFile/permanentlyDeleteFile)
  reader             children, properties, download, tree listing, trash listing (getFile/getData/getChildren)
  acl-writer         rewrite ACLs of the hot files (aclDao paths under the same locks)

Pass criteria: no HTTP 5xx on REST calls, no timeout, no stall (watchdog dumps server threads with jstack),
no unclassified SOAP fault, no JCR signature in the server log, and the post-run audit finds every seeded
file exactly once, readable, with a consistent version history.
SOAP faults that name a missing/locked/duplicate item are counted as expected races between roles.
"""
import argparse
import base64
import http.client
import json
import os
import random
import re
import subprocess
import threading
import time
import urllib.parse
import uuid
from collections import Counter
from importlib import util as importlib_util

HERE = os.path.dirname(os.path.abspath(__file__))
_spec = importlib_util.spec_from_file_location("tree_scenarios", os.path.join(HERE, "jcr-concurrency-tree-scenarios.py"))
base = importlib_util.module_from_spec(_spec)
_spec.loader.exec_module(base)
colon, import_file, file_id, Metrics, Session, percentile = (
    base.colon, base.import_file, base.file_id, base.Metrics, base.Session, base.percentile)

SOAP_NS = "http://jaxws.webservices.unified.repository2.platform.pentaho.org/"
EXPECTED_FAULT = re.compile(
    r"not\s*found|no such|does not exist|doesn't exist|already exist|locked|itemnotfound|pathnotfound|"
    r"nosuchitem|invalid(ated)? .*version|version.*(not|no longer)|cannot remove|constraint|access.?denied", re.I)


class Soap:
    def __init__(self, args, username, password, metrics):
        self.args, self.metrics = args, metrics
        token = base64.b64encode(("%s:%s" % (username, password)).encode()).decode("ascii")
        self.headers = {"Content-Type": "text/xml; charset=utf-8", "SOAPAction": "", "Authorization": "Basic " + token}
        self.connection = None
        self.faults = Counter()

    def call(self, operation, *arguments, confirm=None):
        """confirm: called on an unclassified fault; returning True marks it as an expected race."""
        inner = "".join("<arg%d>%s</arg%d>" % (index, value, index) for index, value in enumerate(arguments))
        envelope = ('<?xml version="1.0"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" '
                    'xmlns:ns="%s"><soapenv:Body><ns:%s>%s</ns:%s></soapenv:Body></soapenv:Envelope>'
                    % (SOAP_NS, operation, inner, operation))
        started, status, text = time.perf_counter(), 0, ""
        try:
            if self.connection is None:
                self.connection = http.client.HTTPConnection(self.args.host, self.args.port,
                                                             timeout=self.args.request_timeout)
            self.connection.request("POST", self.args.context + "/webservices/unifiedRepository",
                                    envelope.encode(), self.headers)
            response = self.connection.getresponse()
            status, text = response.status, response.read().decode("utf-8", "replace")
        except Exception as error:
            self.metrics.error("SOAP %s -> %s: %s" % (operation, type(error).__name__, error))
            try:
                self.connection.close()
            finally:
                self.connection = None
        self.metrics.record("soap_" + operation, time.perf_counter() - started, status)
        if status == 500:
            fault = re.search(r"<faultstring>(.*?)</faultstring>", text, re.S)
            fault = fault.group(1)[:300] if fault else text[:300]
            if EXPECTED_FAULT.search(fault) or (confirm and confirm()):
                self.metrics.record("expected_fault", 0, "soap_" + operation)
            else:
                self.metrics.error("SOAP %s fault: %s" % (operation, fault))
        elif status != 200:
            self.metrics.error("SOAP %s -> HTTP %d" % (operation, status))
        return status, text


def tags(text, name):
    return re.findall(r"<%s>([^<]*)</%s>" % (name, name), text)


def run(args, payload):
    metrics = Metrics()
    admin = Session(args, args.username, args.password, metrics)
    soap_setup = Soap(args, args.username, args.password, metrics)
    root = "%s/perf-bulk-%s" % (args.base_dir.rstrip("/"), uuid.uuid4().hex[:8])
    folders = {name: "%s/%s" % (root, name) for name in ("a", "b", "c", "hot", "scratch", "mtree", "ttree", "ctree")}
    for directory in [root] + list(folders.values()):
        admin.request("setup_mkdir", "PUT", "/api/repo/dirs/" + colon(directory), strict=True)
    for tree in ("mtree", "ttree", "ctree"):
        admin.request("setup_mkdir", "PUT", "/api/repo/dirs/" + colon(folders[tree] + "/sub"), strict=True)
    trees = {}
    for tree in ("mtree", "ttree", "ctree"):
        for index in range(args.files):
            for sub in ("", "/sub"):
                import_file(admin, "setup_import", folders[tree] + sub, "f%d.csv" % index, payload)
    seeded = {tree: set() for tree in ("mtree", "ttree", "ctree")}
    for tree in seeded:
        seeded[tree] = {"%s/f%d.csv" % (sub, index) for index in range(args.files) for sub in ("", "/sub")}
    hot = ["h%d.csv" % index for index in range(args.hot_files)]
    for name in hot:
        for version in range(args.seed_versions):
            import_file(admin, "setup_import", folders["hot"], name, payload + ("\nseed-%d\n" % version).encode())
    ids = {name: file_id(admin, folders["hot"] + "/" + name) for name in hot}
    for tree in seeded:
        trees[tree] = file_id(admin, folders[tree])
    if metrics.errors or not all(ids.values()) or not all(trees.values()):
        return {"root": root, "skipped": True, "errors": metrics.errors or ["setup failed"]}, metrics

    stop = threading.Event()
    progress = [0]
    state = {"mtree": folders["mtree"], "ttree": folders["ttree"]}
    final = {"copies": 0}
    rng_lock = threading.Lock()

    def tick():
        progress[0] += 1

    def pick(seq):
        with rng_lock:
            return random.choice(seq)

    def loop(body):
        def runner():
            session = Session(args, args.username, args.password, metrics)
            soap = Soap(args, args.username, args.password, metrics)
            start.wait(timeout=120)
            while not stop.is_set():
                try:
                    body(session, soap)
                except Exception as error:
                    metrics.error("%s: %s" % (type(error).__name__, error))
                tick()
        return runner

    def version_writer(session, soap):
        name = pick(hot)
        import_file(session, "version_write", folders["hot"], name, payload + ("\nw-%s\n" % uuid.uuid4().hex).encode())

    def version_list(soap, name):
        status, text = soap.call("getVersionSummaries", ids[name])
        return tags(text, "id") if status == 200 else []

    def version_restorer(session, soap):
        name = pick(hot)
        versions = version_list(soap, name)
        versions = [v for v in versions if re.match(r"^[0-9.]+$", v)]
        if len(versions) > 1:
            soap.call("restoreFileAtVersion", ids[name], pick(versions[:-1]), "bulk restore")

    def version_deleter(session, soap):
        name = pick(hot)
        versions = [v for v in version_list(soap, name) if re.match(r"^[0-9.]+$", v)]
        if len(versions) > args.keep_versions:
            soap.call("deleteFileAtVersion", ids[name], versions[1])

    def version_reader(session, soap):
        name = pick(hot)
        versions = [v for v in version_list(soap, name) if re.match(r"^[0-9.]+$", v)]
        if versions:
            version = pick(versions)

            def gone():
                return version not in version_list(soap, name)
            soap.call("getFileAtVersion", ids[name], version, confirm=gone)
            soap.call("getVersionSummary", ids[name], version, confirm=gone)
        session.request("hot_download", "GET", "/api/repo/files/%s/download" % colon(folders["hot"] + "/" + name))

    def mover(session, soap):
        target = pick([folders[k] for k in ("a", "b", "c") if folders[k] != os.path.dirname(state["mtree"])])
        status, _ = session.request("move", "PUT", "/api/repo/files/%s/move" % colon(target), trees["mtree"], "text/plain",
                                    strict=True)
        if status == 200:
            state["mtree"] = target + "/mtree"

    def trasher(session, soap):
        session.request("trash", "PUT", "/api/repo/files/delete", trees["ttree"], "text/plain", strict=True)
        session.request("trash_list", "GET", "/api/repo/files/deleted", accept="application/json")
        session.request("restore", "PUT", "/api/repo/files/restore", trees["ttree"], "text/plain", strict=True)

    def copier(session, soap):
        target = "%s/copy-%s" % (folders["scratch"], uuid.uuid4().hex[:8])
        session.request("copy_mkdir", "PUT", "/api/repo/dirs/" + colon(target), strict=True)
        session.request("copy", "PUT", "/api/repo/files/%s/children?mode=2" % colon(target), trees["ctree"],
                        "text/plain", strict=True)
        target_id = file_id(session, target)
        if target_id:
            session.request("copy_cleanup", "PUT", "/api/repo/files/deletepermanent", target_id, "text/plain")
            final["copies"] += 1

    def churn(session, soap):
        parent = pick([folders[k] for k in ("a", "b", "c", "scratch")])
        directory = "%s/churn-%s" % (parent, uuid.uuid4().hex[:8])
        session.request("churn_mkdir", "PUT", "/api/repo/dirs/" + colon(directory))
        import_file(session, "churn_import", directory, "x.csv", payload)
        directory_id = file_id(session, directory)
        if directory_id:
            session.request("churn_delete", "PUT", "/api/repo/files/deletepermanent", directory_id, "text/plain")

    def reader(session, soap):
        tree = pick(["mtree", "ttree"])
        location = state[tree]
        session.request("read_children", "GET", "/api/repo/files/%s/children" % colon(location), accept="application/json")
        session.request("read_tree", "GET", "/api/repo/files/%s/tree?depth=-1&showHidden=true" % colon(location),
                        accept="application/json")
        session.request("read_descendant", "GET", "/api/repo/files/%s/download" %
                        colon(location + "/sub/f%d.csv" % random.randrange(args.files)))
        session.request("read_properties", "GET", "/api/repo/files/%s/properties" %
                        colon(location + "/f%d.csv" % random.randrange(args.files)), accept="application/json")
        session.request("read_trash", "GET", "/api/repo/files/deleted", accept="application/json")
        soap.call("getFile", location + "/f0.csv")
        time.sleep(args.reader_delay)

    def acl_writer(session, soap):
        if args.acl_on_hot:
            path = folders["hot"] + "/" + pick(hot)
        else:
            path = folders["ctree"] + "/f%d.csv" % random.randrange(args.files)
        target_id = file_id(session, path)
        if not target_id:
            return
        body = ("<repositoryFileAclDto><entriesInheriting>true</entriesInheriting><id>%s</id><owner>%s</owner>"
                "<ownerType>0</ownerType></repositoryFileAclDto>" % (target_id, args.username))
        session.request("acl_write", "PUT", "/api/repo/files/%s/acl" % colon(path), body, "application/xml")

    roles = [("version_writer", version_writer, args.writers), ("version_restorer", version_restorer, args.restorers),
             ("version_deleter", version_deleter, args.deleters), ("version_reader", version_reader, args.readers),
             ("mover", mover, 1), ("trasher", trasher, 1), ("copier", copier, args.copiers),
             ("churn", churn, args.churners), ("reader", reader, args.readers), ("acl_writer", acl_writer, 1)]
    total_threads = sum(count for _, _, count in roles)
    start = threading.Barrier(total_threads + 1)
    threads = []
    for _, body, count in roles:
        threads += [threading.Thread(target=loop(body), daemon=True) for _ in range(count)]

    stall = {"detected": False}

    def watchdog():
        last, last_change = progress[0], time.time()
        while not stop.is_set():
            time.sleep(2)
            if progress[0] != last:
                last, last_change = progress[0], time.time()
            elif time.time() - last_change > args.stall_seconds:
                stall["detected"] = True
                metrics.error("no operation completed for %ds (stall or deadlock)" % args.stall_seconds)
                dump_threads(args, "stall")
                stop.set()
                return

    for thread in threads:
        thread.start()
    threading.Thread(target=watchdog, daemon=True).start()
    start.wait(timeout=120)
    begin = time.perf_counter()
    stop.wait(timeout=args.duration)
    stop.set()
    for thread in threads:
        thread.join(timeout=args.request_timeout + 15)
    elapsed = time.perf_counter() - begin
    alive = [t for t in threads if t.is_alive()]
    if alive and not stall["detected"]:
        metrics.error("%d worker threads did not finish within the request timeout (possible deadlock)" % len(alive))
        dump_threads(args, "unfinished")

    audit_errors = audit(args, payload, Session(args, args.username, args.password, metrics), Soap(args, args.username, args.password, metrics), metrics,
                         folders, seeded, hot, ids, trees)
    metrics.errors.extend(audit_errors)

    if not args.keep:
        cleanup = Session(args, args.username, args.password, base.Metrics())
        root_id = file_id(cleanup, root)
        if root_id:
            cleanup.request("cleanup", "PUT", "/api/repo/files/delete", root_id, "text/plain")
            cleanup.request("cleanup", "PUT", "/api/repo/files/deletepermanent", root_id, "text/plain")

    total = sum(len(values) for values in metrics.latencies.values())
    operations = {}
    for operation, values in sorted(metrics.latencies.items()):
        operations[operation] = {"count": len(values), "p50_ms": percentile(values, 0.5),
                                 "p95_ms": percentile(values, 0.95), "p99_ms": percentile(values, 0.99),
                                 "max_ms": percentile(values, 1.0), "http_status": dict(metrics.statuses[operation])}
    return {"root": root, "elapsed_seconds": round(elapsed, 2), "requests": total,
            "requests_per_second": round(total / elapsed, 2) if elapsed else 0,
            "threads": total_threads, "operations": operations,
            "expected_soap_faults": dict(metrics.statuses.get("expected_fault", {})),
            "errors": metrics.errors}, metrics


def audit(args, payload, admin, soap, metrics, folders, seeded, hot, ids, trees):
    """Every seeded file exactly once and readable, trees back in a live location, version history consistent."""
    errors = []
    status, text = admin.request("audit_tree", "GET", "/api/repo/files/%s/tree?depth=-1&showHidden=true" %
                                 colon(os.path.dirname(folders["a"])), accept="application/json")
    if status != 200:
        return ["audit: cannot list the test tree (HTTP %d)" % status]
    found = Counter()

    def walk(node, path):
        entry = node.get("file", node)
        here = path + "/" + entry.get("name", "")
        found[here] += 1
        for child in node.get("children", []) or []:
            walk(child, here)
    try:
        walk(json.loads(text), "")
    except ValueError:
        return ["audit: tree listing is not JSON"]
    for tree, files in seeded.items():
        locations = [path for path in found if path.endswith("/" + tree) and not path.endswith("/.trash/" + tree)]
        if len(locations) != 1:
            errors.append("audit: %s present in %d live locations: %s" % (tree, len(locations), locations))
            continue
        for relative in files:
            count = found.get(locations[0] + relative, 0)
            if count != 1:
                errors.append("audit: %s%s present %d times" % (locations[0], relative, count))
    for name in hot:
        count = sum(1 for path in found if path.endswith("/hot/" + name))
        if count != 1:
            errors.append("audit: hot file %s present %d times" % (name, count))
            continue
        status, text = soap.call("getVersionSummaries", ids[name])
        versions = [v for v in tags(text, "id") if re.match(r"^[0-9.]+$", v)]
        if status != 200 or not versions:
            errors.append("audit: no version history for %s" % name)
            continue
        for version in versions:
            status, _ = soap.call("getFileAtVersion", ids[name], version)
            if status != 200:
                errors.append("audit: %s version %s listed but not readable (HTTP %d)" % (name, version, status))
        status, _ = admin.request("audit_read", "GET", "/api/repo/files/%s/download" % colon(folders["hot"] + "/" + name))
        if status != 200:
            errors.append("audit: current content of %s unreadable (HTTP %d)" % (name, status))
    for leftover in [path for path in found if "/scratch/copy-" in path or "/churn-" in path]:
        errors.append("audit: leftover from a permanently deleted folder: %s" % leftover)
    return errors


def dump_threads(args, reason):
    pid = args.server_pid
    if not pid:
        try:
            pid = subprocess.check_output(["pgrep", "-f", "catalina[.]base"], text=True).split()[0]
        except Exception:
            return
    target = os.path.join(args.output_dir, "jstack-%s-%d.txt" % (reason, int(time.time())))
    try:
        with open(target, "w") as handle:
            subprocess.run(["jstack", str(pid)], stdout=handle, timeout=60, check=False)
        print("thread dump written to " + target)
    except Exception as error:
        print("jstack failed: %s" % error)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument("--context", default="/pentaho")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default=os.environ.get("PENTAHO_PASSWORD", "password"))
    parser.add_argument("--payload", default=os.path.join(HERE, "jcr-concurrency-payload.csv"))
    parser.add_argument("--base-dir", default="/home/admin")
    parser.add_argument("--duration", type=int, default=120, help="seconds the workload runs")
    parser.add_argument("--files", type=int, default=6, help="files per folder level in each moved/trashed/copied tree")
    parser.add_argument("--hot-files", type=int, default=4, help="versioned files hit by version roles")
    parser.add_argument("--seed-versions", type=int, default=4)
    parser.add_argument("--keep-versions", type=int, default=2, help="the deleter never prunes below this many")
    parser.add_argument("--writers", type=int, default=4)
    parser.add_argument("--restorers", type=int, default=2)
    parser.add_argument("--deleters", type=int, default=2)
    parser.add_argument("--readers", type=int, default=6)
    parser.add_argument("--copiers", type=int, default=2)
    parser.add_argument("--churners", type=int, default=3)
    parser.add_argument("--reader-delay", type=float, default=0.02)
    parser.add_argument("--request-timeout", type=float, default=60)
    parser.add_argument("--stall-seconds", type=int, default=45)
    parser.add_argument("--server-pid", default="")
    parser.add_argument("--output-dir", default="/tmp")
    parser.add_argument("--log", default="")
    parser.add_argument("--acl-on-hot", action="store_true",
                        help="also race ACL updates against versioned writes (not covered by the lock change)")
    parser.add_argument("--keep", action="store_true")
    parser.add_argument("--label", required=True)
    args = parser.parse_args()

    with open(args.payload, "rb") as payload_file:
        payload = payload_file.read()
    log_offset = os.path.getsize(args.log) if args.log and os.path.exists(args.log) else 0
    result, _ = run(args, payload)

    log_matches = []
    if args.log and os.path.exists(args.log):
        with open(args.log, "r", errors="replace") as log_file:
            log_file.seek(log_offset)
            log_matches = [line.strip()[:500] for line in log_file
                           if any(signature in line.lower() for signature in base.LOG_SIGNATURES)]
    print(json.dumps({"label": args.label, "result": result, "jcr_errors_in_server_log": log_matches}, indent=2))
    if result.get("errors") or log_matches:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
