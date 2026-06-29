# Pentaho Import / Export — Backup & Restore Guide

Simplified, config-file–driven backup and restore for Pentaho Server 10.2 EE, with
support for **encrypted passwords**, profile-based selective backups, and
`command line > config file > environment variable` precedence.

All commands below are run from the server root. Change into the directory where
your Pentaho Server is installed:

```bash
cd /path/to/pentaho-server
```

---

## 1. Quick start

```bash
# Back up users, roles and ACLs only
./import-export.sh --backup --config=example-backup-security.properties

# Restore them
./import-export.sh --restore --config=example-restore-security.properties
```

The **operation flag (`--backup` / `--restore`) must always be on the command line.**
It is intentionally *not* read from the config file because restore is destructive —
this prevents an innocent-looking properties file from silently overwriting the
repository. Everything else (URL, credentials, profile, output path, logging) can
live in the config file.

---

## 2. Configuration precedence

Each setting is resolved in this order (first match wins):

1. **Command-line option** — e.g. `--file-path=./backups/today.zip`
2. **Config file** — the `.properties` file given with `--config` / `-cfg`
3. **Environment variable** — `PENTAHO_<OPTION>` (uppercased, `-` → `_`),
   e.g. `--file-path` → `PENTAHO_FILE_PATH`, `--password` → `PENTAHO_PASSWORD`

### Where the config file comes from

The config file path itself is resolved from (in order):

1. `--config` / `-cfg` option
2. `PENTAHO_IE_CONFIG` environment variable
3. Default `~/.pentaho-backup/import-export.properties` (only if it exists)

This means you can keep credentials out of every command by exporting:

```bash
export PENTAHO_IE_CONFIG=$HOME/.pentaho-backup/import-export.properties
export PENTAHO_PASSWORD='Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde'
./import-export.sh --backup --backup-profile=SECURITY --file-path=./backups/sec.zip
```

### Previewing the resolved command (`--dry-run`)

To see how precedence resolves for a given invocation **without executing it**,
append `--dry-run` (or `-dry`). The CLI prints every option's effective value, the
source it came from (`cli`, `config`, or `env (NAME)`), and a copy-paste-ready
command with the password masked:

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

This is the quickest way to confirm which config file and credentials a command
will actually use before running a real backup or restore.

---

## 3. Encrypted passwords

### Why
Avoid storing plaintext passwords in config files, shell history, or CI logs.

### How it works
The CLI obfuscates/decrypts the password using the standard Kettle two-way encoder
before it ever reaches the REST API. The relevant code in
`CommandLineProcessor.getPassword()`:

```java
String getPassword() throws ParseException, KettleException {
  if ( !KettleClientEnvironment.isInitialized() ) {
    KettleClientEnvironment.init();
  }
  return Encr.decryptPasswordOptionallyEncrypted(
      getOptionValue( INFO_OPTION_PASSWORD_NAME, true, false ) );
}
```

`Encr.decryptPasswordOptionallyEncrypted(...)`:
- If the value starts with `Encrypted `, it is decoded back to plaintext.
- Otherwise the value is passed through unchanged (**plaintext is still supported**
  for backward compatibility).

The decrypted value is then used for HTTP Basic auth against the REST API.

### Generating an encrypted value
Use the bundled Pentaho tool — the same encoder, so output is compatible:

```bash
./encr.sh -kettle <yourPassword>          # encr.bat on Windows
# Example output:
#   Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde     (this decodes to: password)
```

### Using it

**In a config file** (no quoting needed):

```properties
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde
```

**As a command-line override** — the value contains a space, so **quote it**:

```bash
./import-export.sh --backup --config=example-backup-security.properties \
  --password="Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde"
```

**As an environment variable:**

```bash
export PENTAHO_PASSWORD='Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde'
```

> Note: this is reversible obfuscation (two-way), not a one-way hash. It stops
> shoulder-surfing and casual log/file exposure; it is not a substitute for proper
> secret management. Protect the file/host accordingly (`chmod 600`).

---

## 4. Backup profiles

Set `backup-profile=<PROFILE>` in the config (case-insensitive). Available profiles:

| Profile          | Includes                                              |
|------------------|-------------------------------------------------------|
| `FULL_SYSTEM`    | Everything (content, security, datasources, schedules, settings) |
| `CONTENT_ONLY`   | Repository content (`--include-generated-content` toggles generated output) |
| `SECURITY`       | Users and roles only                                  |
| `DATA_SOURCE`    | Datasources, metastore, Mondrian                      |
| `SCHEDULES`      | Scheduled jobs only                                   |
| `SETTINGS`       | User settings, email/group config                     |

Profiles are the **headline interface** for selecting what to back up or restore.
For fine-grained control there are also granular per-component flags
(`--backup-content`, `--backup-users`, `--backup-datasources`, `--backup-metastore`,
`--backup-mondrian`, `--backup-schedules`, `--backup-settings`, and the matching
`--restore-*` flags). These are **hidden from the default `--help`** to keep it
clean; run `./import-export.sh --help-advanced` to see them. Note: a profile and
the granular flags are mutually exclusive — if `--backup-profile` is set, the
granular flags are ignored.

---

## 5. Example configuration files

These ship in the server directory and all use the encrypted password by default.

| File | Purpose | Status |
|------|---------|--------|
| [example-backup-security.properties](example-backup-security.properties) | Backup users/roles/ACLs | Verified HTTP 200 (9 users / 9 roles) |
| [example-restore-security.properties](example-restore-security.properties) | Restore users/roles/ACLs | overwrite + ACL options set |
| [example-backup-datasource.properties](example-backup-datasource.properties) | Backup datasources | Verified HTTP 200 (~82 KB) |
| [example-backup-schedules.properties](example-backup-schedules.properties) | Backup schedules | Verified HTTP 200 (~91 KB) |
| [example-backup-content.properties](example-backup-content.properties) | Content **with** generated content | Heavy/slow — run when scheduler idle |
| [example-backup-content-no-generated.properties](example-backup-content-no-generated.properties) | Content **without** generated content | Recommended for live servers |
| [example-backup-full-system.properties](example-backup-full-system.properties) | Full system backup | Run when scheduler idle |

### Annotated example — `example-backup-security.properties`

```properties
# Connection (required)
url=http://localhost:8080/pentaho
username=admin
# Generate with: ./encr.sh -kettle <yourPassword>   (plaintext also works)
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde

# What to back up: users and roles only
backup-profile=SECURITY

# Output archive (must end in .zip)
file-path=./backups/pentaho-security-backup.zip

# Logging
logLevel=INFO
logfile=./logs/pentaho-security-backup.log
stream-logs=true
```

### Restore example — `example-restore-security.properties`

```properties
url=http://localhost:8080/pentaho
username=admin
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde

restore-profile=SECURITY
file-path=./backups/pentaho-security-backup.zip

# Restore behaviour
overwrite=true               # replace existing items
applyAclSettings=true        # apply ACLs from the archive
overwriteAclSettings=false   # do not wipe ACLs not present in the archive
```

---

## 6. Common overrides

```bash
# Timestamped output file
./import-export.sh --backup --config=example-backup-security.properties \
  --file-path=./backups/security-$(date +%Y%m%d-%H%M%S).zip

# Encrypted password override on the command line (quote it)
./import-export.sh --backup --config=example-backup-security.properties \
  --password="Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde"

# Change profile on the fly
./import-export.sh --backup --config=example-backup-security.properties \
  --backup-profile=DATA_SOURCE --file-path=./backups/ds.zip
```

---

## 7. Operational notes

- **`FULL_SYSTEM` and `CONTENT_ONLY` (with generated content)** can collide with the
  live scheduler. If a scheduled report writes generated content into the JCR while a
  full/content backup is streaming, the backup can fail with
  `StaleItemStateException: ... has been modified externally` /
  `IOException: write beyond end of stream`. Run these when the scheduler is idle, or
  use `example-backup-content-no-generated.properties`.
- **Run one CLI backup at a time.** Concurrent CLI backups (or leftover orphan
  `CommandLineProcessor` processes) compete for repository transactions and can
  produce `ERROR_0002 - Invalid ClientResponse received in performREST()`.
- **The `import-export.sh` wrapper must keep `-Dpentaho.disable.karaf=true`.** Without
  it the CLI boots OSGi/Karaf and appears to hang. The genuine server script already
  includes this flag plus the Java 11/17 `--add-opens` arguments.
- **`stream-logs=true`** streams server-side progress to the console so long backups
  do not look frozen.

---

## 8. Verification summary (2026-06-27)

| Test | Result |
|------|--------|
| `encr.sh -kettle password` | `Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde` |
| Encrypted password in config file | HTTP 200, valid archive |
| Encrypted password via `--password=` override | HTTP 200, valid archive |
| Security backup | HTTP 200 — 9 users / 9 roles |
| Datasource backup | HTTP 200 — ~82 KB |
| Schedules backup | HTTP 200 — ~91 KB |
| Plaintext password (legacy) | Still works (backward compatible) |
