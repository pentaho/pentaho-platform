# Pentaho Repository Concurrency Harness

This standard-library Python tool runs a small, bounded import/read workload against one Pentaho server. It uploads a unique CSV file, repeatedly overwrites that file while readers download it and list its directory, verifies each downloaded ZIP contains a marker written by the run, and writes latency/error results as JSON.

Use it only against a Pentaho server you own or are explicitly authorized to test. It sends repository writes and leaves one uniquely named CSV file in the selected directory for manual cleanup.

## Requirements

- Python 3.9 or newer
- Network access to the Pentaho REST API
- A non-admin Pentaho account with permission to import and read files in the chosen repository directory; use least privilege
- The standard Pentaho form-login endpoint (`/j_spring_security_check`); deployments using SSO or a custom authentication flow need an approved session-login adaptation

No third-party Python packages are required. TLS certificate verification remains enabled; configure the system trust store or `SSL_CERT_FILE` for private certificates rather than disabling verification.

## Preview the workload

The base URL includes the host, optional port, and application context path. For example:

```text
python3 tools/repository-concurrency/repository_concurrency.py \
  --base-url https://pentaho.example.test/analytics/pentaho \
  --target-dir /home/qa_user \
  --dry-run
```

The default workload is one writer with 20 imports and four concurrent readers, each performing 25 downloads and 20 directory listings. It makes 200 measured requests, plus a small login/setup/read-back sequence. The harness caps readers at 4, imports at 20, measured requests at 200, and request timeouts at 30 seconds. Reader pacing has a minimum of 100 ms.

## Run against an authorized server

Set the username in the environment. If `PENTAHO_PASSWORD` is not set, the script prompts for the password without echoing it. The account does not need the Pentaho Administrator role; it only needs import/read access to the selected directory. Do not put passwords in command arguments or committed files.

```sh
export PENTAHO_USERNAME=qa_user
python3 tools/repository-concurrency/repository_concurrency.py \
  --base-url https://pentaho.example.test/analytics/pentaho \
  --target-dir /home/qa_user \
  --authorized \
  --output repository-concurrency-results.json
```

The `--authorized` flag is required for network activity. Override workload counts only downward from the documented caps. Use a disposable directory; each run creates a filename such as `repo-load-a1b2c3d4e5f6.csv` and reports its repository path. Remove that file manually after reviewing the run.

## Read the report

The JSON report includes the base URL, test path, measured request counts, elapsed time, requests per second, per-operation p50/p95/p99 latency, and errors. It contains no credentials or session cookies. The exact final marker is checked in a separate read-back after the measured workload, so a successful-looking import that was not persisted is reported as a failure.

The tool cannot inspect server logs, so it cannot report JCR stack signatures. For a deadlock investigation, collect server logs and thread dumps separately. This bounded workload is a regression/performance probe, not a production-capacity benchmark; results from one run are not statistically conclusive.

## Local checks

```sh
python3 -m unittest discover -s tools/repository-concurrency -p 'test_*.py'
python3 tools/repository-concurrency/repository_concurrency.py \
  --base-url https://pentaho.example.test/pentaho \
  --target-dir /home/qa_user --dry-run
```