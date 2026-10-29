# Repository concurrency harness

Standard-library Python scripts that stress the JCR repository of a running Pentaho Server over HTTP (REST and the `unifiedRepository` SOAP service). They were written for BISERVER-15820 (JCR version-read/content-write coordination, PR #6396) and are meant to be reused whenever repository locking, `JcrRepositoryFileDao`, or the Kettle PUR plugin changes.

| Script | Purpose |
| --- | --- |
| `jcr-concurrency-endpoint-bench.py` | One shared file: N writers plus readers. Latency, throughput, errors. |
| `jcr-concurrency-tree-scenarios.py` | Folder-tree move, trash/restore and ACL-hidden descendants under concurrent reads and writes. |
| `jcr-concurrency-bulk.py` | Mixed workload over every code path touched by the lock change, with stall watchdog and post-run audit. |
| `jcr-concurrency-lost-update-probe.py` | Counts accepted vs rejected overwrites of one versioned file at 1, 2, 4 and 8 writers. |
| `jcr-concurrency-payload.csv` | Default import payload. |

All scripts need only Python 3. They create data under disposable folders in `/home/admin` (the benchmark overwrites `/home/admin/<filename>` and leaves it), send credentials with HTTP Basic over plain HTTP, and must be run against localhost or a trusted disposable test server. Pass the password with `--password "$PENTAHO_PASSWORD"` (the bulk script and the probe also read the `PENTAHO_PASSWORD` environment variable). Common options: `--host`, `--port`, `--context`, `--username`, `--label` (required), `--log` (server log; any JCR signature written after the run started fails the run).

A CSV payload is used on purpose. With a KTR payload, concurrent readers and a writer hit a separate Kettle lock cycle (`PurRepository` read lock vs. shared-objects node lock, PDI-21058), so KTR cannot serve as a throughput comparison. Do not use `.txt`; the importer answers HTTP 200 with `INVALID_MIME_TYPE` (the script flags it).

## A/B procedure (stock vs patched)

1. Unzip the same suite build twice (stock and patched), each with its own database state, and run one at a time on port 8080.
2. On the patched copy, replace only the jars under test, keeping the original file names. For BISERVER-15820 that is `pentaho-platform-repository` (and `pentaho-platform-extensions` for the HTTP 409 check) in `tomcat/webapps/pentaho/WEB-INF/lib`, and `pdi-pur-plugin-core` in `pentaho-solutions/system/pdi-pur-plugin`.
3. Wait until the repository has finished initializing before starting a run.
4. Run the same commands against each server with a distinct `--label` and `--log`, and compare the JSON.
5. Before drawing conclusions, confirm that the jar on the patched server is the one you built (checksum) and that the PR head matches the build.

## Single-file benchmark

```sh
python3 jcr-concurrency-endpoint-bench.py --payload jcr-concurrency-payload.csv \
  --filename perf-shared.csv --workers 1,2,4 --rounds 20 --readers 2 --reader-delay 0.1 \
  --password "$PENTAHO_PASSWORD" --log /path/to/pentaho.log --label patched > patched-bench.json
```

Endpoints: `POST /api/repo/files/import` (overwrite), `GET /api/repo/files/{id}/download`, `GET /api/repo/files/{id}/children`. Each writer round runs `get_file`, `get_children`, then `post_import` followed by a read-back `get_file` that checks a per-round marker. Readers loop `get_file` and `get_children` until the writers finish. Output: per-operation p50/p95/p99/mean latency, throughput, errors and JCR log signatures. Exit code 1 on any error or signature. Options: `--workers` (comma list), `--rounds`, `--readers`, `--request-timeout`, `--reader-delay`, `--payload`, `--filename`.

## Folder-tree scenarios

One mover thread repeatedly moves or trashes/restores a folder tree while readers read its descendants and writers overwrite non-versioned files in the folders the tree moves between. Everything is created under a disposable `/home/admin/perf-tree-*` folder and deleted at the end (`--keep` to retain it).

| Scenario | Mover | Concurrent load |
| --- | --- | --- |
| `move` | `PUT /files/{dest}/move` toggling the tree between `a` and `b` | readers: children and download of descendants; writers: non-versioned sibling imports in `a` and `b` |
| `trash` | `PUT /files/delete` then `PUT /files/restore` | same, plus `GET /files/deleted` (trashed descendants) |
| `acl` | same as `trash` | readers run as `--restricted-user`, which cannot see `hidden.csv`; needs `--restricted-user/--restricted-password` |

```sh
python3 jcr-concurrency-tree-scenarios.py --label patched --password "$PENTAHO_PASSWORD" \
  --restricted-user suzy --restricted-password password --log /path/to/pentaho.log
```

Pass criteria: no HTTP 5xx, no timeout or connection error, every mover call succeeds, the mover finishes (no deadlock), and no JCR signature (`InvalidItemStateException`, `could not find child`, closed session, deadlock) in `--log`. 404/403 on readers and writers are expected while the tree is in flight and are reported per operation in `http_status`. The `acl` request body is built from `RepositoryFileAclDto` XML and has not been verified against every server version; if setup fails the scenario is reported as skipped with the HTTP status.

## Bulk mixed workload

All roles run at once for `--duration` seconds:

| Role | Operation (DAO call) |
| --- | --- |
| version-writer | overwrite versioned files, REST import (`updateFile`) |
| version-restorer | restore an older version, SOAP (`restoreFileAtVersion`) |
| version-deleter | delete a non-current version, SOAP (`deleteFileAtVersion`) |
| version-reader | version summaries, file/data at a version, SOAP and REST download |
| mover | move a folder tree between three folders (`moveFile`) |
| trasher | trash and undelete a tree (`deleteFile`/`undeleteFile`) |
| copier | copy a tree to a fresh folder, then delete it permanently (`copyFile`, `permanentlyDeleteFile`) |
| churn | create folder, create file, delete permanently |
| reader | children, properties, download, tree listing, trash listing |
| acl-writer | rewrite ACLs of the hot files (only with `--acl-on-hot`; not covered by the lock change) |

```sh
python3 jcr-concurrency-bulk.py --label patched --duration 300 --server-pid <tomcat pid> \
  --password "$PENTAHO_PASSWORD" --log /path/to/pentaho.log > patched-bulk.json
```

Role counts are set with `--writers`, `--restorers`, `--deleters`, `--readers`, `--copiers` and `--churners`; data size with `--files`, `--hot-files`, `--seed-versions` and `--keep-versions`.

Pass criteria: no HTTP 5xx on REST calls, no timeout, no stall, no unclassified SOAP fault, no JCR signature in the log, and a post-run audit that finds every seeded file exactly once, readable, with a consistent version history. SOAP faults that name a missing, locked or duplicate item are counted as expected races between roles. A stall watchdog (`--stall-seconds`, default 45) dumps server threads with `jstack` when `--server-pid` is set and flags worker threads that do not finish as a possible deadlock.

## Lost-update probe

```sh
python3 jcr-concurrency-lost-update-probe.py --label patched --log /path/to/pentaho.log --seconds 20
```

For 1, 2, 4 and 8 writers on one file, and 8 writers on 8 files, it reports `attempts`, `accepted` (new versions), `rejected` (log lines containing `update to this file has occurred since its last read`) and `unexplained = attempts - accepted - rejected`. A non-zero `unexplained` means a silent loss or another failure. The rejection text is the one raised by the BISERVER-15820 guard; on a build without it `rejected` stays 0.

## BISERVER-15820 reference results

Stock suite-release 11.1 #454 vs the same build with the PR #6396 jars, 2026-10-29, on one local machine. Treat the numbers as relative, not absolute. Full tables and interpretation are in `docs/BISERVER-15820-JCR-VERSION-READ-WRITE-COORDINATION.md`.

| Workload | Stock | Patched |
| --- | --- | --- |
| Single file, 1/2/4 writers, 2 readers (req/s) | 68.9 / 78.6 / 94.4 | 78.6 / 102.0 / 125.3 |
| Bulk, 300 s, 48 threads: total requests, req/s | 49,979, 166.1 | 91,064, 302.2 |
| Bulk: harness errors, JCR log lines | 20, 20 | 1, 0 |

The one patched harness error was a trash-listing HTTP 500 found by this workload and fixed in the same PR. Copy is slower on the patched build (p50 897 to 2,278 ms) because of tree-lease serialization.

## Not covered

KTR import and load concurrency (PDI-21058), clustered repositories, and databases other than the one used for the reference runs.
