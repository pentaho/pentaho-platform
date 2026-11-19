#!/usr/bin/env python3
import argparse
import base64
import http.client
import io
import json
import os
import re
import statistics
import threading
import time
import urllib.parse
import uuid
import zipfile
from concurrent.futures import ThreadPoolExecutor


class Metrics:
    def __init__(self):
        self.lock = threading.Lock()
        self.latencies = {"get_file": [], "get_children": [], "post_import": []}
        self.errors = []

    def add_latency(self, operation, seconds):
        with self.lock:
            self.latencies[operation].append(seconds)

    def add_error(self, message):
        with self.lock:
            self.errors.append(message)


def percentile(values, percentile_value):
    ordered = sorted(values)
    if not ordered:
        return None
    index = min(len(ordered) - 1, int((len(ordered) - 1) * percentile_value))
    return round(ordered[index] * 1000, 3)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="localhost")
    parser.add_argument("--port", type=int, default=8080)
    parser.add_argument("--context", default="/pentaho")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default="password")
    parser.add_argument("--payload", default="/tmp/simulate_order_processing.ktr")
    parser.add_argument("--filename", default="perf-shared.ktr")
    parser.add_argument("--workers", default="2,4,8")
    parser.add_argument("--rounds", type=int, default=20)
    parser.add_argument("--readers", type=int, default=4)
    parser.add_argument("--request-timeout", type=float, default=30)
    parser.add_argument("--reader-delay", type=float, default=0.05)
    parser.add_argument("--log", default="")
    parser.add_argument("--label", required=True)
    args = parser.parse_args()

    with open(args.payload, "rb") as payload_file:
        payload = payload_file.read()
    auth = "Basic " + base64.b64encode(
        (args.username + ":" + args.password).encode("utf-8")
    ).decode("ascii")
    file_id = urllib.parse.quote(":home:admin:" + args.filename, safe="")
    children_id = urllib.parse.quote(":home:admin", safe="")
    file_path = args.context + "/api/repo/files/" + file_id
    download_path = file_path + "/download"
    children_path = args.context + "/api/repo/files/" + children_id + "/children"
    base_headers = {"Authorization": auth, "Accept": "*/*"}
    metrics = Metrics()
    run_token = re.sub(r"[^A-Za-z0-9_]+", "_", args.label)
    log_offset = os.path.getsize(args.log) if args.log and os.path.exists(args.log) else 0

    def request(connection, operation, method, path, body=None, content_type=None):
        headers = dict(base_headers)
        if content_type:
            headers["Content-Type"] = content_type
        started = time.perf_counter()
        try:
            connection.request(method, path, body=body, headers=headers)
            response = connection.getresponse()
            response_body = response.read()
            elapsed = time.perf_counter() - started
            metrics.add_latency(operation, elapsed)
            if response.status != 200:
                metrics.add_error("%s %s -> HTTP %d: %s" % (
                    method, path, response.status, response_body[:300].decode("utf-8", "replace")))
            return response.status, response_body
        except Exception as error:
            elapsed = time.perf_counter() - started
            metrics.add_latency(operation, elapsed)
            metrics.add_error("%s %s -> %s: %s" % (method, path, type(error).__name__, error))
            try:
                connection.close()
            except Exception:
                pass
            return 0, b""

    def new_connection():
        return http.client.HTTPConnection(args.host, args.port, timeout=args.request_timeout)

    def import_file(connection, body):
        boundary = "----jcr-bench-%s" % uuid.uuid4().hex
        fields = {
            "importDir": "/home/admin",
            "fileNameOverride": args.filename,
            "overwriteFile": "true",
            "logLevel": "ERROR",
            "retainOwnership": "true",
            "charSet": "UTF-8",
        }
        parts = []
        for name, value in fields.items():
            parts.extend([
                ("--" + boundary + "\r\n").encode("ascii"),
                ("Content-Disposition: form-data; name=\"%s\"\r\n\r\n" % name).encode("ascii"),
                value.encode("utf-8"),
                b"\r\n",
            ])
        parts.extend([
            ("--" + boundary + "\r\n").encode("ascii"),
            ("Content-Disposition: form-data; name=\"fileUpload\"; filename=\"%s\"\r\n" %
             os.path.basename(args.payload)).encode("utf-8"),
            b"Content-Type: application/octet-stream\r\n\r\n",
            body,
            b"\r\n",
            ("--" + boundary + "--\r\n").encode("ascii"),
        ])
        import_path = args.context + "/api/repo/files/import"
        status, response_body = request(
            connection, "post_import", "POST", import_path,
            b"".join(parts), "multipart/form-data; boundary=" + boundary)
        response_text = response_body.decode("utf-8", "replace")
        if status == 200 and re.search(
            r"Could not import|Unable to save repository element|ExceptionOnUpdatingFile|INVALID_MIME_TYPE|<strong[^>]*>\s*ERROR",
                response_text, re.IGNORECASE):
            metrics.add_error("multipart importer response contains an import error")
        return status, response_body

    def marked_payload(worker_number, round_number):
        marker = "%s_worker_%d_round_%d" % (run_token, worker_number, round_number)
        marker_bytes = marker.encode("ascii")
        if not args.filename.lower().endswith(".ktr"):
            return payload + b"\n" + marker_bytes + b"\n", marker_bytes
        description_tag = b"<description/>"
        before_steps, first_step = payload.split(b"<step>", 1)
        step_content, remaining = first_step.split(b"</step>", 1)
        if description_tag not in step_content:
            raise ValueError("First KTR step does not contain an empty description element")
        marked_step = step_content.replace(
            description_tag, b"<description>" + marker_bytes + b"</description>", 1)
        return before_steps + b"<step>" + marked_step + b"</step>" + remaining, marker_bytes

    def downloaded_ktr(response_body):
        with zipfile.ZipFile(io.BytesIO(response_body)) as archive:
            for name in archive.namelist():
                if name.endswith(args.filename):
                    return archive.read(name)
        return b""

    setup_connection = new_connection()
    seed_status, _ = import_file(setup_connection, payload)
    setup_connection.close()
    setup_errors = list(metrics.errors)
    if seed_status != 200 and not setup_errors:
        setup_errors.append("initial multipart repository import returned HTTP %s" % seed_status)

    results = []
    for worker_count in [int(item) for item in args.workers.split(",") if item.strip()]:
        metrics = Metrics()
        begin = time.perf_counter()
        writers_start = threading.Barrier(worker_count)
        readers_started = threading.Barrier(args.readers + 1)
        stop_readers = threading.Event()
        reader_connections = []

        def reader_worker():
            connection = new_connection()
            reader_connections.append(connection)
            readers_started.wait(timeout=60)
            while not stop_readers.is_set():
                request(connection, "get_file", "GET", download_path)
                request(connection, "get_children", "GET", children_path)
                time.sleep(args.reader_delay)
            connection.close()

        def writer_worker(worker_number):
            connection = new_connection()
            try:
                writers_start.wait(timeout=60)
                for round_number in range(args.rounds):
                    request(connection, "get_file", "GET", download_path)
                    request(connection, "get_children", "GET", children_path)
                    writers_start.wait(timeout=60)
                    for turn in range(worker_count):
                        if turn == worker_number:
                            marked_body, marker = marked_payload(worker_number, round_number)
                            status, _ = import_file(connection, marked_body)
                            if status == 200:
                                read_status, read_body = request(connection, "get_file", "GET", download_path)
                                if read_status == 200 and marker not in downloaded_ktr(read_body):
                                    metrics.add_error("read-back missed update marker for worker %d round %d" %
                                                      (worker_number, round_number))
                        writers_start.wait(timeout=60)
            except Exception as error:
                metrics.add_error("writer %d -> %s: %s" % (worker_number, type(error).__name__, error))
            finally:
                connection.close()

        with ThreadPoolExecutor(max_workers=args.readers + worker_count) as executor:
            reader_futures = [executor.submit(reader_worker) for _ in range(args.readers)]
            readers_started.wait(timeout=60)
            writer_futures = [executor.submit(writer_worker, worker) for worker in range(worker_count)]
            for future in writer_futures:
                future.result()
            stop_readers.set()
            for future in reader_futures:
                future.result()

        elapsed = time.perf_counter() - begin
        counts = {name: len(values) for name, values in metrics.latencies.items()}
        throughput = sum(counts.values()) / elapsed if elapsed else 0
        results.append({
            "workers": worker_count,
            "readers": args.readers,
            "rounds_per_writer": args.rounds,
            "elapsed_seconds": round(elapsed, 3),
            "request_count": sum(counts.values()),
            "requests_per_second": round(throughput, 2),
            "operations": {
                name: {
                    "count": len(values),
                    "p50_ms": percentile(values, 0.50),
                    "p95_ms": percentile(values, 0.95),
                    "p99_ms": percentile(values, 0.99),
                    "mean_ms": round(statistics.mean(values) * 1000, 3) if values else None,
                }
                for name, values in metrics.latencies.items()
            },
            "errors": metrics.errors,
        })

    log_matches = []
    if args.log and os.path.exists(args.log):
        with open(args.log, "r", errors="replace") as log_file:
            log_file.seek(log_offset)
            for line in log_file:
                lower = line.lower()
                if ("invaliditemstateexception" in lower or "could not find child" in lower or
                    "session has been closed" in lower or "this session has been closed" in lower or
                    "unable to save repository element" in lower or
                    "file data must not be null" in lower):
                    log_matches.append(line.strip()[:500])

    report = {
        "label": args.label,
        "base_url": "http://%s:%d%s" % (args.host, args.port, args.context),
        "payload_bytes": len(payload),
        "target_filename": args.filename,
        "run_parameters": {
            "worker_counts": args.workers,
            "rounds": args.rounds,
            "readers": args.readers,
            "request_timeout_seconds": args.request_timeout,
            "reader_delay_seconds": args.reader_delay,
        },
        "setup_import": {"http_status": seed_status, "errors": setup_errors},
        "results": results,
        "total_errors": len(setup_errors) + sum(len(result["errors"]) for result in results),
        "target_jcr_errors_in_server_log": log_matches,
    }
    print(json.dumps(report, indent=2))
    if report["total_errors"] or log_matches:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
