#!/usr/bin/env python3
"""Run a bounded concurrent Pentaho repository import/read check."""

from __future__ import annotations

import argparse
import concurrent.futures
import getpass
import http.cookiejar
import io
import json
import math
import os
import posixpath
import re
import statistics
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path
from typing import Any, Callable


MAX_READERS = 4
MAX_ROUNDS = 20
MAX_DOWNLOADS_PER_READER = 25
MAX_CHILDREN_PER_READER = 20
MAX_REQUEST_TIMEOUT = 30.0
MAX_IMPORT_BYTES = 64 * 1024
MAX_RESPONSE_BYTES = 4 * 1024 * 1024
MIN_READER_DELAY = 0.1


class HarnessError(Exception):
    """An expected harness or HTTP workflow failure."""


def normalize_base_url(value: str) -> str:
    parsed = urllib.parse.urlsplit(value)
    if parsed.scheme not in {"http", "https"} or not parsed.netloc:
        raise ValueError("base URL must be an absolute http(s) URL")
    if parsed.username or parsed.password or parsed.query or parsed.fragment:
        raise ValueError("base URL must not contain credentials, query, or fragment")
    return value.rstrip("/")


def repository_path_id(repository_path: str) -> str:
    if not repository_path.startswith("/"):
        raise ValueError("repository paths must start with '/'")
    segments = repository_path.strip("/").split("/")
    if not segments or any(not part or part in {".", ".."} for part in segments):
        raise ValueError("repository path contains an empty or traversal segment")
    return ":" + ":".join(segments)


def validate_limits(
    readers: int,
    rounds: int,
    downloads_per_reader: int,
    children_per_reader: int,
    timeout: float,
    reader_delay: float,
) -> None:
    if not 1 <= readers <= MAX_READERS:
        raise ValueError(f"readers must be between 1 and {MAX_READERS}")
    if not 1 <= rounds <= MAX_ROUNDS:
        raise ValueError(f"rounds must be between 1 and {MAX_ROUNDS}")
    if not 0 <= downloads_per_reader <= MAX_DOWNLOADS_PER_READER:
        raise ValueError(f"downloads per reader must be between 0 and {MAX_DOWNLOADS_PER_READER}")
    if not 0 <= children_per_reader <= MAX_CHILDREN_PER_READER:
        raise ValueError(f"children per reader must be between 0 and {MAX_CHILDREN_PER_READER}")
    if downloads_per_reader + children_per_reader == 0:
        raise ValueError("each reader must perform at least one read")
    if not 1 <= timeout <= MAX_REQUEST_TIMEOUT:
        raise ValueError(f"timeout must be between 1 and {MAX_REQUEST_TIMEOUT:g} seconds")
    if not MIN_READER_DELAY <= reader_delay <= 5:
        raise ValueError(f"reader delay must be between {MIN_READER_DELAY:g} and 5 seconds")
    request_count = rounds + readers * (downloads_per_reader + children_per_reader)
    if request_count > 200:
        raise ValueError("workload exceeds the 200-request safety cap")


def csv_payload(marker: str) -> bytes:
    return f"run_id,sequence\n{marker},0\n".encode("utf-8")


def multipart_body(fields: dict[str, str], filename: str, payload: bytes) -> tuple[str, bytes]:
    if not re.fullmatch(r"[A-Za-z0-9._-]+", filename):
        raise ValueError("test filename may contain only letters, digits, dot, underscore, and hyphen")
    if len(payload) > MAX_IMPORT_BYTES:
        raise ValueError(f"test payload exceeds {MAX_IMPORT_BYTES} bytes")

    boundary = "----PentahoRepoCheck" + uuid.uuid4().hex
    body = bytearray()
    for name, value in fields.items():
        body.extend(f"--{boundary}\r\n".encode("ascii"))
        body.extend(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode("ascii"))
        body.extend(value.encode("utf-8"))
        body.extend(b"\r\n")
    body.extend(f"--{boundary}\r\n".encode("ascii"))
    body.extend(
        f'Content-Disposition: form-data; name="fileUpload"; filename="{filename}"\r\n'.encode("ascii")
    )
    body.extend(b"Content-Type: text/csv; charset=UTF-8\r\n\r\n")
    body.extend(payload)
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("ascii"))
    return boundary, bytes(body)


def parse_children_contains(body: bytes, filename: str) -> bool:
    try:
        root = ET.fromstring(body)
    except ET.ParseError as error:
        raise HarnessError("children endpoint did not return repository XML") from error
    return any(
        element.tag.rsplit("}", 1)[-1] == "name" and element.text == filename
        for element in root.iter()
    )


def download_contains_marker(body: bytes, markers: set[str]) -> bool:
    try:
        with zipfile.ZipFile(io.BytesIO(body)) as archive:
            return any(
                marker.encode("utf-8") in archive.read(member)
                for member in archive.namelist()
                if not member.endswith("/")
                for marker in markers
            )
    except (OSError, zipfile.BadZipFile, RuntimeError) as error:
        raise HarnessError("download endpoint did not return a readable ZIP") from error


def import_response_error(body: bytes) -> str | None:
    text = body.decode("utf-8", errors="replace")
    if "INVALID_MIME_TYPE" in text:
        return "server rejected the file MIME type"
    try:
        document = json.loads(text)
    except json.JSONDecodeError:
        document = None
    if isinstance(document, dict):
        for key in ("errors", "error", "exceptions"):
            value = document.get(key)
            if value:
                return f"server import response contains {key}"
    try:
        root = ET.fromstring(body)
    except ET.ParseError:
        return None
    for element in root.iter():
        if element.tag.rsplit("}", 1)[-1].lower() in {"error", "exception"} and (element.text or "").strip():
            return "server import response contains an error"
    return None


class ApiClient:
    def __init__(self, base_url: str, cookie_header: str, timeout: float):
        self.base_url = base_url
        self.cookie_header = cookie_header
        self.timeout = timeout

    def request(self, method: str, path: str, body: bytes | None = None, content_type: str | None = None) -> bytes:
        headers = {"Accept": "application/xml, application/zip, */*", "Cookie": self.cookie_header}
        if content_type:
            headers["Content-Type"] = content_type
        request = urllib.request.Request(self.base_url + path, data=body, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                response_body = response.read(MAX_RESPONSE_BYTES + 1)
                if len(response_body) > MAX_RESPONSE_BYTES:
                    raise HarnessError("server response exceeded the 4 MiB safety limit")
                return response_body
        except urllib.error.HTTPError as error:
            raise HarnessError(f"{method} request returned HTTP {error.code}") from None
        except urllib.error.URLError as error:
            raise HarnessError(f"{method} request failed: {error.reason}") from None


def login(base_url: str, username: str, password: str, timeout: float) -> str:
    cookies = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookies))
    form = urllib.parse.urlencode(
        {"j_username": username, "j_password": password, "locale": "en_US"}
    ).encode("ascii")
    request = urllib.request.Request(
        base_url + "/j_spring_security_check",
        data=form,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )
    try:
        with opener.open(request, timeout=timeout):
            pass
    except urllib.error.HTTPError as error:
        raise HarnessError(f"login request returned HTTP {error.code}") from None
    except urllib.error.URLError as error:
        raise HarnessError(f"login request failed: {error.reason}") from None
    cookie_header = "; ".join(f"{cookie.name}={cookie.value}" for cookie in cookies)
    if not cookie_header:
        raise HarnessError("login did not establish a session cookie")
    return cookie_header


def upload_file(client: ApiClient, target_dir: str, filename: str, marker: str) -> None:
    fields = {
        "importDir": target_dir,
        "overwriteFile": "true",
        "overwriteAclPermissions": "false",
        "applyAclPermissions": "false",
        "retainOwnership": "false",
        "charSet": "UTF-8",
        "logLevel": "ERROR",
        "fileNameOverride": filename,
    }
    boundary, body = multipart_body(fields, filename, csv_payload(marker))
    response = client.request(
        "POST",
        "/api/repo/files/import",
        body,
        f"multipart/form-data; boundary={boundary}",
    )
    error = import_response_error(response)
    if error:
        raise HarnessError(error)


def request_record(operation: str, action: Callable[[], None]) -> dict[str, Any]:
    start = time.perf_counter()
    try:
        action()
        return {"operation": operation, "ok": True, "duration_ms": round((time.perf_counter() - start) * 1000, 3)}
    except Exception as error:  # Keep a single failed request from stopping other readers.
        return {
            "operation": operation,
            "ok": False,
            "duration_ms": round((time.perf_counter() - start) * 1000, 3),
            "error": str(error)[:300],
        }


def percentile(values: list[float], quantile: float) -> float | None:
    if not values:
        return None
    ordered = sorted(values)
    index = max(0, math.ceil(quantile * len(ordered)) - 1)
    return round(ordered[index], 3)


def summarize(records: list[dict[str, Any]]) -> dict[str, Any]:
    summary: dict[str, Any] = {}
    operations = sorted({record["operation"] for record in records})
    for operation in operations:
        matching = [record for record in records if record["operation"] == operation]
        durations = [record["duration_ms"] for record in matching]
        summary[operation] = {
            "count": len(matching),
            "successes": sum(record["ok"] for record in matching),
            "p50_ms": percentile(durations, 0.50),
            "p95_ms": percentile(durations, 0.95),
            "p99_ms": percentile(durations, 0.99),
            "mean_ms": round(statistics.mean(durations), 3) if durations else None,
        }
    return summary


def run_workload(args: argparse.Namespace) -> dict[str, Any]:
    base_url = normalize_base_url(args.base_url)
    validate_limits(
        args.readers,
        args.rounds,
        args.downloads_per_reader,
        args.children_per_reader,
        args.timeout,
        args.reader_delay,
    )
    target_dir = args.target_dir.rstrip("/") or "/"
    repository_path_id(target_dir)
    run_id = uuid.uuid4().hex[:12]
    filename = f"repo-load-{run_id}.csv"
    target_path = posixpath.join(target_dir, filename)
    target_file_id = repository_path_id(target_path)
    target_dir_id = urllib.parse.quote(repository_path_id(target_dir), safe=":")
    target_file_url_id = urllib.parse.quote(target_file_id, safe=":")
    markers = {f"{run_id}-{index}" for index in range(args.rounds + 1)}
    setup_marker = f"{run_id}-0"

    if args.dry_run:
        return {
            "dry_run": True,
            "base_url": base_url,
            "target_path": target_path,
            "request_parameters": {
                "writers": 1,
                "rounds": args.rounds,
                "readers": args.readers,
                "downloads_per_reader": args.downloads_per_reader,
                "children_per_reader": args.children_per_reader,
                "measured_request_cap": 200,
            },
        }
    if not args.authorized:
        raise HarnessError("live runs require --authorized to confirm permission to test this server")

    username = os.environ.get("PENTAHO_USERNAME") or input("Pentaho username: ").strip()
    password = os.environ.get("PENTAHO_PASSWORD") or getpass.getpass("Pentaho password: ")
    if not username or not password:
        raise HarnessError("a non-empty Pentaho username and password are required")
    cookie_header = login(base_url, username, password, args.timeout)
    client = ApiClient(base_url, cookie_header, args.timeout)

    children_path = f"/api/repo/files/{target_dir_id}/children"
    initial_children = client.request("GET", children_path)
    try:
        ET.fromstring(initial_children)
    except ET.ParseError as error:
        raise HarnessError("directory check failed; verify credentials, context path, and repository directory") from error

    upload_file(client, target_dir, filename, setup_marker)
    setup_download = client.request("GET", f"/api/repo/files/{target_file_url_id}/download")
    if not download_contains_marker(setup_download, {setup_marker}):
        raise HarnessError("setup import did not produce the expected marker on read-back")

    barrier = threading.Barrier(args.readers + 1)

    def run_writer() -> list[dict[str, Any]]:
        barrier.wait(timeout=args.timeout)
        records = []
        for iteration in range(1, args.rounds + 1):
            marker = f"{run_id}-{iteration}"
            records.append(request_record("post_import", lambda marker=marker: upload_file(client, target_dir, filename, marker)))
        return records

    def run_reader() -> list[dict[str, Any]]:
        barrier.wait(timeout=args.timeout)
        records = []
        operation_count = max(args.downloads_per_reader, args.children_per_reader)
        for index in range(operation_count):
            if index < args.downloads_per_reader:
                records.append(
                    request_record(
                        "get_file",
                        lambda: _check_download(client, target_file_url_id, markers),
                    )
                )
            if index < args.children_per_reader:
                records.append(
                    request_record(
                        "get_children",
                        lambda: _check_children(client, children_path, filename),
                    )
                )
            if index + 1 < operation_count:
                time.sleep(args.reader_delay)
        return records

    start = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.readers + 1) as executor:
        writer_future = executor.submit(run_writer)
        reader_futures = [executor.submit(run_reader) for _ in range(args.readers)]
        records = writer_future.result()
        for future in reader_futures:
            records.extend(future.result())
    elapsed = time.perf_counter() - start
    final_readback = request_record(
        "final_readback",
        lambda: _check_download(client, target_file_url_id, {f"{run_id}-{args.rounds}"}),
    )
    errors = [record for record in records if not record["ok"]]
    if not final_readback["ok"]:
        errors.append(final_readback)
    return {
        "label": f"repository-concurrency-{run_id}",
        "base_url": base_url,
        "target_path": target_path,
        "payload_bytes": len(csv_payload(setup_marker)),
        "run_parameters": {
            "writers": 1,
            "rounds": args.rounds,
            "readers": args.readers,
            "downloads_per_reader": args.downloads_per_reader,
            "children_per_reader": args.children_per_reader,
            "reader_delay_seconds": args.reader_delay,
            "request_timeout_seconds": args.timeout,
        },
        "elapsed_seconds": round(elapsed, 3),
        "request_count": len(records),
        "requests_per_second": round(len(records) / elapsed, 2) if elapsed else None,
        "operations": summarize(records),
        "final_readback": final_readback,
        "total_errors": len(errors),
        "errors": errors[:50],
        "test_file_cleanup": "manual; remove the unique file shown in target_path after review",
    }


def _check_download(client: ApiClient, file_id: str, markers: set[str]) -> None:
    body = client.request("GET", f"/api/repo/files/{file_id}/download")
    if not download_contains_marker(body, markers):
        raise HarnessError("download ZIP did not contain a marker written by this run")


def _check_children(client: ApiClient, children_path: str, filename: str) -> None:
    body = client.request("GET", children_path)
    if not parse_children_contains(body, filename):
        raise HarnessError("children response did not list the test file")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", required=True, help="Pentaho base URL, including context path, e.g. https://host/pentaho")
    parser.add_argument("--target-dir", required=True, help="Existing repository directory for the unique test file, e.g. /home/qa_user")
    parser.add_argument("--authorized", action="store_true", help="Confirm you are authorized to run this bounded workload against the server")
    parser.add_argument("--rounds", type=int, default=20, help=f"Sequential imports; maximum {MAX_ROUNDS} (default: 20)")
    parser.add_argument("--readers", type=int, default=4, help=f"Concurrent readers; maximum {MAX_READERS} (default: 4)")
    parser.add_argument("--downloads-per-reader", type=int, default=25, help=f"Downloads per reader; maximum {MAX_DOWNLOADS_PER_READER} (default: 25)")
    parser.add_argument("--children-per-reader", type=int, default=20, help=f"Directory listings per reader; maximum {MAX_CHILDREN_PER_READER} (default: 20)")
    parser.add_argument("--reader-delay", type=float, default=0.1, help="Delay between reader operations in seconds; minimum 0.1 (default: 0.1)")
    parser.add_argument("--timeout", type=float, default=30.0, help=f"Per-request timeout in seconds; maximum {MAX_REQUEST_TIMEOUT:g} (default: 30)")
    parser.add_argument("--output", type=Path, help="Write the JSON report to this path instead of stdout")
    parser.add_argument("--dry-run", action="store_true", help="Print the bounded plan without contacting the server")
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    try:
        report = run_workload(args)
    except (HarnessError, ValueError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2
    output = json.dumps(report, indent=2, sort_keys=True)
    if args.output:
        args.output.write_text(output + "\n", encoding="utf-8")
        print(f"Report written to {args.output}")
        print(f"Target file: {report.get('target_path', '(dry run)')}")
    else:
        print(output)
    return 0 if report.get("total_errors", 0) == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())