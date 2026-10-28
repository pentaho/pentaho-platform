import io
import json
import re
import threading
import unittest
import zipfile
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from types import SimpleNamespace
from unittest.mock import patch

import repository_concurrency as harness  # type: ignore[reportMissingImports]


class RepositoryConcurrencyTest(unittest.TestCase):
    def test_normalizes_base_url_with_context_path(self):
        self.assertEqual(
            "https://pentaho.example.test/analytics/pentaho",
            harness.normalize_base_url("https://pentaho.example.test/analytics/pentaho/"),
        )

    def test_rejects_base_url_credentials_and_query(self):
        for value in (
            "https://user:secret@pentaho.example.test/pentaho",
            "https://pentaho.example.test/pentaho?debug=true",
            "ftp://pentaho.example.test/pentaho",
        ):
            with self.subTest(value=value), self.assertRaises(ValueError):
                harness.normalize_base_url(value)

    def test_converts_repository_path_to_path_id(self):
        self.assertEqual(":home:qa_user:run.csv", harness.repository_path_id("/home/qa_user/run.csv"))

    def test_rejects_repository_path_traversal(self):
        with self.assertRaises(ValueError):
            harness.repository_path_id("/home/../admin")

    def test_workload_limits_enforce_reader_cap(self):
        with self.assertRaises(ValueError):
            harness.validate_limits(5, 20, 25, 20, 30, 0.1)
        harness.validate_limits(4, 20, 25, 20, 30, 0.1)

    def test_multipart_contains_import_directory_and_file(self):
        boundary, body = harness.multipart_body(
            {
                "importDir": "/home/qa_user",
                "overwriteFile": "true",
                "fileNameOverride": "run.csv",
            },
            "run.csv",
            b"run_id,sequence\nabc,0\n",
        )
        self.assertIn(f'name="importDir"'.encode(), body)
        self.assertIn(b"/home/qa_user", body)
        self.assertIn(b'name="fileUpload"; filename="run.csv"', body)
        self.assertIn(b'name="fileNameOverride"', body)
        self.assertIn(b"\r\n\r\nrun.csv\r\n", body)
        self.assertIn(b"abc,0", body)
        self.assertIn(boundary.encode(), body)

    def test_children_xml_must_list_target(self):
        xml = b"<repositoryFileDtoes><repositoryFileDto><name>run.csv</name></repositoryFileDto></repositoryFileDtoes>"
        self.assertTrue(harness.parse_children_contains(xml, "run.csv"))
        self.assertFalse(harness.parse_children_contains(xml, "other.csv"))

    def test_download_zip_marker_is_verified(self):
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, "w") as archive:
            archive.writestr("run.csv", "run_id,sequence\nabc-123,0\n")
        self.assertTrue(harness.download_contains_marker(buffer.getvalue(), {"abc-123"}))
        self.assertFalse(harness.download_contains_marker(buffer.getvalue(), {"missing"}))

    def test_live_workflow_against_local_mock_server(self):
        state = {"filename": None, "payload": b""}
        state_lock = threading.Lock()

        class Handler(BaseHTTPRequestHandler):
            def log_message(self, _format, *_args):
                return

            def do_POST(self):
                if self.path == "/app/j_spring_security_check":
                    self.rfile.read(int(self.headers["Content-Length"]))
                    self.send_response(302)
                    self.send_header("Set-Cookie", "JSESSIONID=local-test; Path=/app")
                    self.send_header("Location", "/app/Home")
                    self.end_headers()
                    return
                if self.path != "/app/api/repo/files/import" or "JSESSIONID=local-test" not in self.headers.get("Cookie", ""):
                    self.send_error(401)
                    return
                body = self.rfile.read(int(self.headers["Content-Length"]))
                filename = re.search(rb'filename="([^"]+)"', body)
                marker = re.search(rb"run_id,sequence\r?\n([^,\r\n]+),0", body)
                if not filename or not marker:
                    self.send_error(400)
                    return
                with state_lock:
                    state["filename"] = filename.group(1).decode("ascii")
                    state["payload"] = marker.group(0).replace(b"\r", b"")
                self.send_response(200)
                self.end_headers()
                self.wfile.write(b"OK")

            def do_GET(self):
                if self.path == "/app/Home":
                    self.send_response(200)
                    self.end_headers()
                    return
                if "JSESSIONID=local-test" not in self.headers.get("Cookie", ""):
                    self.send_error(401)
                    return
                if self.path.endswith("/children"):
                    with state_lock:
                        filename = state["filename"]
                    name = f"<name>{filename}</name>" if filename else ""
                    body = f"<repositoryFileDtoes><repositoryFileDto>{name}</repositoryFileDto></repositoryFileDtoes>".encode()
                    self.send_response(200)
                    self.send_header("Content-Type", "application/xml")
                    self.end_headers()
                    self.wfile.write(body)
                    return
                if self.path.endswith("/download"):
                    with state_lock:
                        filename = state["filename"]
                        payload = state["payload"]
                    if not filename:
                        self.send_error(404)
                        return
                    buffer = io.BytesIO()
                    with zipfile.ZipFile(buffer, "w") as archive:
                        archive.writestr(filename, payload)
                    self.send_response(200)
                    self.send_header("Content-Type", "application/zip")
                    self.end_headers()
                    self.wfile.write(buffer.getvalue())
                    return
                self.send_error(404)

        server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        server_thread = threading.Thread(target=server.serve_forever, daemon=True)
        server_thread.start()
        args = SimpleNamespace(
            base_url=f"http://127.0.0.1:{server.server_port}/app",
            target_dir="/home/qa_user",
            readers=2,
            rounds=2,
            downloads_per_reader=2,
            children_per_reader=1,
            timeout=5.0,
            reader_delay=0.1,
            dry_run=False,
            authorized=True,
        )
        try:
            with patch.dict("os.environ", {"PENTAHO_USERNAME": "tester", "PENTAHO_PASSWORD": "not-a-real-password"}):
                report = harness.run_workload(args)
        finally:
            server.shutdown()
            server.server_close()
            server_thread.join(timeout=2)

        self.assertEqual(0, report["total_errors"], json.dumps(report["errors"], indent=2))
        self.assertEqual(8, report["request_count"])
        self.assertTrue(report["final_readback"]["ok"])
        self.assertTrue(report["target_path"].startswith("/home/qa_user/repo-load-"))


if __name__ == "__main__":
    unittest.main()