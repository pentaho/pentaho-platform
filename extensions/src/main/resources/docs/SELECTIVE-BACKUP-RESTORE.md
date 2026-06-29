# Selective backup and restore with profiles and advanced options

This document describes the **selective** backup and restore capability for the
Pentaho Repository, which extends the standard full backup/restore described in
[Backup and restore Pentaho repositories](https://docs.pentaho.com/pdia-admin/10.2-admin/manage-the-pentaho-system/manage-the-pentaho-repository/backup-and-restore-pentaho-repositories).

In addition to the classic "back up everything" workflow, you can now:

- Back up or restore **only a subset** of the repository using a named **profile**
  (for example, only security, only data sources, or only schedules).
- Drive every option from a **configuration file** instead of long command lines.
- Store the server password in an **encrypted** form.
- Reveal fine-grained, per-component switches through an **advanced help** view.

All commands are run with the `import-export` script from the root of a running
Pentaho Server (`import-export.bat` on Windows, `import-export.sh` on Linux/macOS).
You must have administrator permissions on the server to perform a backup or restore.

---

## What a backup contains

A full backup exports all content from the Pentaho Repository into a single ZIP file,
which includes:

- Users and roles
- All files (dashboards, reports, etc.)
- Schedules
- Data connections
- Mondrian schemas
- Metadata entries
- A manifest file (`exportManifest.xml`)

A **selective** backup writes the same ZIP structure but limits its contents to the
components selected by the chosen profile.

---

## Prerequisites

- A **running** Pentaho Server (the backup/restore REST endpoints must be reachable).
- **Administrator** credentials.
- The `import-export` script located in the Pentaho Server install directory.

---

## Standard full backup and restore (baseline)

These continue to work exactly as before.

**Backup (Linux/macOS):**

```bash
./import-export.sh --backup --url=http://localhost:8080/pentaho \
  --username=admin --password=password \
  --file-path=/home/Downloads/backup.zip \
  --logfile=/temp/logfile.log
```

**Restore (Linux/macOS):**

```bash
./import-export.sh --restore --url=http://localhost:8080/pentaho \
  --username=admin --password=password \
  --file-path=/home/Downloads/backup.zip \
  --overwrite=true --logfile=/temp/logfile.log
```

On Windows, use `import-export.bat` with the same arguments and Windows paths.

> The operation flag (`--backup` or `--restore`) must always be on the command line.
> If a required argument is missing, the script prints the missing requirement.

---

## Configuration file and precedence

Rather than typing long command lines, you can put options in a `.properties`
configuration file and pass it with `--config` (`-cfg`).

```bash
./import-export.sh --backup --config=example-backup-security.properties
```

Each setting is resolved in this order (first match wins):

1. **Command-line option** — e.g. `--file-path=./backups/today.zip`
2. **Config file** — the file given with `--config` / `-cfg`
3. **Environment variable** — `PENTAHO_<OPTION>` (uppercase, `-` → `_`),
   e.g. `--file-path` → `PENTAHO_FILE_PATH`, `--password` → `PENTAHO_PASSWORD`

The config file location itself is resolved from, in order:

1. The `--config` / `-cfg` option
2. The `PENTAHO_IE_CONFIG` environment variable
3. A default of `~/.pentaho-backup/import-export.properties` (used only if it exists)

A typical config file:

```properties
# Connection
url=http://localhost:8080/pentaho
username=admin
password=password

# What to back up
backup-profile=SECURITY

# Output archive (must end in .zip)
file-path=./backups/pentaho-security-backup.zip

# Logging
logLevel=INFO
logfile=./logs/pentaho-security-backup.log
stream-logs=true
```

---

## Encrypted passwords

To avoid storing a plaintext password in a config file or shell history, you can
supply an **encrypted** password. The script automatically decrypts it before
authenticating against the REST API. Plaintext passwords remain fully supported.

### Generate an encrypted value

Use the bundled Pentaho encryption tool:

```bash
./encr.sh -kettle <yourPassword>          # encr.bat on Windows
# Example output:
#   Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde
```

### Use it

In a config file (no quoting needed):

```properties
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde
```

On the command line (the value contains a space, so **quote it**):

```bash
./import-export.sh --backup --config=example-backup-security.properties \
  --password="Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde"
```

As an environment variable:

```bash
export PENTAHO_PASSWORD='Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde'
```

> This is reversible obfuscation, not a one-way hash. It prevents casual exposure
> in files and logs; protect the file and host accordingly (for example `chmod 600`).

---

## Step 1: Selective backup with profiles

Set `backup-profile=<PROFILE>` in the config file (or pass
`--backup-profile=<PROFILE>` on the command line). Profile names are case-insensitive.

| Profile          | Backs up                                              |
|------------------|-------------------------------------------------------|
| `FULL_SYSTEM`    | Everything (content, security, data sources, schedules, settings) |
| `CONTENT_ONLY`   | Repository content (dashboards, reports). Use `--include-generated-content` to include/exclude generated output |
| `SECURITY`       | Users and roles only                                  |
| `DATA_SOURCE`    | Data sources, metastore, Mondrian                     |
| `SCHEDULES`      | Scheduled jobs only                                   |
| `SETTINGS`       | User settings, email/group configuration              |

Examples:

```bash
# Security only (users, roles, ACLs)
./import-export.sh --backup --config=example-backup-security.properties

# Data sources only
./import-export.sh --backup --backup-profile=DATA_SOURCE \
  --url=http://localhost:8080/pentaho --username=admin --password=password \
  --file-path=./backups/datasources.zip

# Content without generated output (recommended for a live server)
./import-export.sh --backup --backup-profile=CONTENT_ONLY \
  --include-generated-content=false \
  --url=http://localhost:8080/pentaho --username=admin --password=password \
  --file-path=./backups/content.zip
```

### Generated content

For `CONTENT_ONLY` (and `FULL_SYSTEM`), `--include-generated-content` controls
whether files produced by scheduled reports are included:

- `--include-generated-content=true` — include generated output (larger, slower).
- `--include-generated-content=false` — exclude it (recommended on a running server).

---

## Step 2: Selective restore with profiles

Restore mirrors backup. Set `restore-profile=<PROFILE>` (or pass
`--restore-profile=<PROFILE>`), and point `file-path` at the archive to restore.

```properties
url=http://localhost:8080/pentaho
username=admin
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde

restore-profile=SECURITY
file-path=./backups/pentaho-security-backup.zip

# Restore behaviour
overwrite=true               # replace existing items
applyAclSettings=true        # apply ACLs from the archive
overwriteAclSettings=false   # do not wipe ACLs that are absent from the archive
```

```bash
./import-export.sh --restore --config=example-restore-security.properties
```

---

## Preview a command (dry run)

Add `--dry-run` (or `-dry`) to any backup or restore command to see exactly what
would run **without executing anything**. It resolves every option using the
normal precedence (command line > config file > `PENTAHO_*` environment), shows
the source of each value, masks the password, and prints a copy-paste-ready
command:

```bash
./import-export.sh --backup --backup-profile=SECURITY \
  --config=example-backup-security.properties --dry-run
```

```
=== DRY RUN: no changes will be made ===
Operation : BACKUP
Precedence: command line > config file > PENTAHO_* environment

Resolved options:
  --backup-profile           = SECURITY                  [cli]
  --password                 = ********                  [config]
  --url                      = http://localhost:8080/pentaho  [config]
  --username                 = admin                     [env (PENTAHO_USERNAME)]

Translated command (password masked):
  import-export.sh --backup-profile=SECURITY --password=******** --url=http://localhost:8080/pentaho --username=admin
```

Use it to confirm which config file, profile, and credentials a command will pick
up before running a real backup or (destructive) restore.

---

## Advanced options

Profiles are the recommended, headline interface. For fine-grained control there
are also **granular per-component switches**. To keep the default help readable,
these are **hidden from `--help`**; show them with `--help-advanced`:

```bash
./import-export.sh --help-advanced
```

Granular backup switches (each takes `true`/`false`):

| Flag | Component |
|------|-----------|
| `--backup-content` (`-bc`)      | Content (dashboards, reports) |
| `--backup-users` (`-bu`)        | Users and roles |
| `--backup-datasources` (`-bd`)  | Database connections and data sources |
| `--backup-metastore` (`-bm`)    | Metastore configuration |
| `--backup-schedules` (`-bs`)    | Job schedules |
| `--backup-settings` (`-bset`)   | User settings and preferences |
| `--backup-mondrian` (`-bmo`)    | Mondrian analysis schemas |

The matching restore switches are `--restore-content` (`-rc`),
`--restore-users` (`-ru`), `--restore-datasources` (`-rd`),
`--restore-metastore` (`-rm`), `--restore-schedules` (`-rs`),
`--restore-settings` (`-rset`), and `--restore-mondrian` (`-rmo`).

> **Profile vs. granular flags are mutually exclusive.** If a profile
> (`--backup-profile` / `--restore-profile`) is supplied, the granular component
> flags are ignored. Use the granular flags only when you need a custom combination
> that no profile covers (for example, content **and** data sources but **not** users).

---

## Common option reference

| Option | Description |
|--------|-------------|
| `--backup` / `--restore` | Operation (always on the command line) |
| `--config` / `-cfg`      | Path to a `.properties` configuration file |
| `--url`                  | Pentaho Server base URL |
| `--username`             | Administrator user name |
| `--password`             | Password (plaintext or `Encrypted ...`) |
| `--file-path`            | Archive path (must end in `.zip`) |
| `--backup-profile` / `--restore-profile` | Predefined profile (see table) |
| `--include-generated-content` | Include scheduled-report output (`true`/`false`) |
| `--overwrite`            | Replace existing items on restore (`true`/`false`) |
| `--applyAclSettings`     | Apply ACLs from the archive on restore |
| `--overwriteAclSettings` | Replace existing ACLs on restore |
| `--logfile`              | Log file path |
| `--logLevel`             | Log level (e.g. `INFO`, `DEBUG`) |
| `--stream-logs`          | Stream server-side progress to the console (`true`/`false`) |
| `--dry-run` (`-dry`)     | Resolve and print the effective command (and the source of each value) **without executing** |
| `--help`                 | Print help (profiles and common options) |
| `--help-advanced`        | Print help including the granular component flags |

---

## Operational notes

- **`FULL_SYSTEM` and `CONTENT_ONLY` with generated content** can contend with the
  live scheduler. If a scheduled report writes generated content into the repository
  while a backup is streaming, the backup can fail (for example with
  `StaleItemStateException: ... has been modified externally` or
  `IOException: write beyond end of stream`). Run these when the scheduler is idle,
  or back up content with `--include-generated-content=false`.
- **Run one backup/restore at a time.** Concurrent runs (or orphaned processes) can
  contend for repository transactions and produce an
  `Invalid ClientResponse received in performREST()` error.
- **Use `--stream-logs=true`** so long-running operations show progress instead of
  appearing to hang.
- A successful backup reports `Response Status: 200` and writes the ZIP file; the
  archive always contains at least `exportManifest.xml`.
