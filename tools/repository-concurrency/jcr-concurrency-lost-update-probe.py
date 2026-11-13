#!/usr/bin/env python3
"""Count accepted vs rejected overwrites of versioned files at several writer counts.

accepted = versions created; rejected = lost-update-guard lines in the server log;
unexplained = attempts - accepted - rejected (silent loss or other failure).
"""
import argparse
import os
import re
import threading
import time
import uuid
from importlib import util

HERE = os.path.dirname(os.path.abspath(__file__))
spec = util.spec_from_file_location("bulk", os.path.join(HERE, "jcr-concurrency-bulk.py"))
bulk = util.module_from_spec(spec)
spec.loader.exec_module(bulk)

GUARD = "update to this file has occurred since its last read"


def log_count(path):
    with open(path, "rb") as handle:
        return handle.read().count(GUARD.encode())


def run_case(args, payload, writers, shared, seconds, pause):
    metrics = bulk.base.Metrics()
    admin = bulk.Session(args, args.username, args.password, metrics)
    soap = bulk.Soap(args, args.username, args.password, metrics)
    root = "%s/lup-%s" % (args.base_dir, uuid.uuid4().hex[:8])
    admin.request("mk", "PUT", "/api/repo/dirs/" + bulk.colon(root), strict=True)
    names = ["f0.csv"] if shared else ["f%d.csv" % i for i in range(writers)]
    ids = {}
    for name in names:
        for seed in range(3):
            bulk.import_file(admin, "seed", root, name, payload + ("\nseed-%d\n" % seed).encode())
        ids[name] = bulk.file_id(admin, root + "/" + name)

    def versions(name):
        soap.connection = None
        status, text = soap.call("getVersionSummaries", ids[name])
        numbers = [int(v.split(".")[-1]) for v in bulk.tags(text, "id") if re.match(r"^[0-9.]+$", v)]
        return max(numbers) if numbers else -1

    before = sum(versions(n) for n in names)
    log_before = log_count(args.log)
    stop, attempts, lock, codes = threading.Event(), [0], threading.Lock(), {}

    def worker(index):
        session = bulk.Session(args, args.username, args.password, metrics)
        name = names[0] if shared else names[index]
        while not stop.is_set():
            status, _ = bulk.import_file(session, "write", root, name,
                                         payload + ("\nw-%s\n" % uuid.uuid4().hex).encode())
            with lock:
                attempts[0] += 1
                codes[status] = codes.get(status, 0) + 1
            if pause:
                time.sleep(pause)

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(writers)]
    started = time.time()
    for t in threads:
        t.start()
    time.sleep(seconds)
    stop.set()
    for t in threads:
        t.join()
    elapsed = time.time() - started
    time.sleep(1)
    accepted = sum(versions(n) for n in names) - before
    rejected = log_count(args.log) - log_before
    return {"writers": writers, "files": len(names), "attempts": attempts[0], "accepted": accepted,
            "rejected": rejected, "codes": codes, "unexplained": attempts[0] - accepted - rejected,
            "attempts_per_s": round(attempts[0] / elapsed, 1),
            "reject_pct": round(100.0 * rejected / max(attempts[0], 1), 1)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument("--context", default="/pentaho")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default=os.environ.get("PENTAHO_PASSWORD", "password"))
    parser.add_argument("--base-dir", default="/home/admin")
    parser.add_argument("--request-timeout", type=float, default=60)
    parser.add_argument("--label", required=True)
    parser.add_argument("--log", required=True)
    parser.add_argument("--seconds", type=int, default=20)
    parser.add_argument("--pause", type=float, default=0.0)
    args = parser.parse_args()
    with open(os.path.join(HERE, "jcr-concurrency-payload.csv"), "rb") as handle:
        payload = handle.read()
    print(args.label, "pause=%s" % args.pause)
    for writers, shared in ((1, True), (2, True), (4, True), (8, True), (8, False)):
        print(run_case(args, payload, writers, shared, args.seconds, args.pause), flush=True)


if __name__ == "__main__":
    main()
