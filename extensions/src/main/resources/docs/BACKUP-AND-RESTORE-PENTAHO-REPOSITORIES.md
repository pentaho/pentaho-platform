# Backup and restore Pentaho repositories

A complete backup and restore of your Pentaho repositories can be done through either the command (cmd) window or with the `File Resource` service in the Pentaho REST APIs.

**Note:** If you are on Pentaho version 8.0 or earlier, as a best practice to avoid errors when exporting and importing repository contents, select specific content and not the entire repository. For more information, see [Importing and exporting PDI content with Pentaho 8.0 and earlier](https://docs.pentaho.com/pdia-admin/10.2-admin/troubleshooting-overview-cp/pentaho-repository-issues/importing-and-exporting-8.0-and-earlier).

The backup process exports all content from the Pentaho Repository and creates a ZIP file, which includes:

- Users and roles
- All files (dashboards, reports, etc.)
- Schedules
- Data connections
- Mondrian schemas
- Metadata entries
- A manifest file

All of your content is pulled from this ZIP file when you restore the Pentaho Repository.

You must have appropriate administrator permissions on the server in order to perform a repository backup or restore.

---

## What's new in this release

The `import-export` tool still accepts every command line argument shown below, so existing scripts keep working. In addition, you can now:

- **Drive every option from a configuration file** with `--config=<file>`, so credentials and paths are not repeated on the command line. Only the operation flag (`--backup` / `--restore`) must stay on the command line.
- **Use encrypted passwords.** Generate an obfuscated value with `./encr.sh -kettle <yourPassword>` (`encr.bat` on Windows) and paste the full `Encrypted ...` string into `password=`. Plaintext passwords still work.
- **Back up or restore a subset** of the repository with a profile instead of the whole system (see [Selective backup and restore](#selective-backup-and-restore)).

### Configuration precedence

When the same option is supplied in more than one place, it is resolved in this order (highest wins):

```
command line  >  config file (--config)  >  PENTAHO_* environment variables
```

#### Overriding a value stored in the config file

Because the command line has the highest precedence, you can keep a reusable
config file and override individual options at run time — without editing the
file. Any option you add on the command line replaces the matching key from the
config file; every other key is still read from the file.

For example, using `example-backup-security.properties` but writing to a
timestamped archive and raising the log level for one run:

- **Linux**

  `./import-export.sh --backup --config=example-backup-security.properties --file-path=./backups/security-$(date +%Y%m%d).zip --logLevel=DEBUG`

- **Windows**

  `import-export.bat --backup --config=example-backup-security.properties --file-path=c:/backups/security.zip --logLevel=DEBUG`

Here `file-path` and `logLevel` come from the command line, while `url`,
`username`, `password`, and `backup-profile` are still taken from the config
file. The same pattern works for restore — for example, overriding `overwrite`
on a single run:

`./import-export.sh --restore --config=example-restore-content.properties --overwrite=false`

You can also override with an environment variable (lower precedence than the
command line but higher than nothing set in the file). Environment variables use
the `PENTAHO_` prefix with the option name uppercased and any non-alphanumeric
character replaced by `_` — for example `PENTAHO_PASSWORD` for `password` and
`PENTAHO_FILE_PATH` for `file-path`. The config file path itself can be supplied
through `PENTAHO_IE_CONFIG` instead of `--config`.

#### Previewing the resolved command (`--dry-run`)

To check how the precedence above resolves for a particular invocation **without
running it**, add `--dry-run` (or `-dry`). The tool prints each option's effective
value, where it came from (`cli`, `config`, or `env (NAME)`), and a copy-paste-ready
command with the password masked — then exits without performing the backup or
restore:

```
./import-export.sh --backup --backup-profile=SECURITY --config=example-backup-security.properties --dry-run
```

```
=== DRY RUN: no changes will be made ===
Operation : BACKUP
Precedence: command line > config file > PENTAHO_* environment

Resolved options:
  --backup-profile           = SECURITY                  [cli]
  --password                 = ********                  [config]
  --url                      = http://localhost:8080/pentaho  [config]
  --username                 = admin                     [config]

Translated command (password masked):
  import-export.sh --backup-profile=SECURITY --password=******** --url=http://localhost:8080/pentaho --username=admin
```

### Sample configuration files

Ready-to-edit samples ship in the server root:

| File | Purpose |
| --- | --- |
| `example-backup-full-system.properties` | Full repository backup |
| `example-backup-security.properties` | Users, roles, and ACLs only |
| `example-backup-datasource.properties` | Data connections only |
| `example-backup-content.properties` | Repository files/folders (with generated content) |
| `example-backup-content-no-generated.properties` | Repository files/folders (without generated content) |
| `example-backup-schedules.properties` | Scheduled jobs and triggers only |
| `example-restore-security.properties` | Restore security content |
| `example-restore-content.properties` | Restore repository files/folders |
| `example-restore-schedules.properties` | Restore scheduled jobs and triggers |

---

## Step 1: Back up the Pentaho Repository

Backing up your Pentaho Repository is done through the use of command line arguments. You can customize the provided examples for your server.

If an argument is required for a successful backup and has not been provided, the missing requirement is displayed in the cmd window. Backup results are also displayed in the window.

1. Open a cmd window and point the directory to the install location of your running Pentaho Server.
2. Use the `import-export` script with your arguments for backing up the repository.
3. Press **Enter**.

### Option A — all arguments on the command line

- **Windows**

  `import-export.bat --backup --url=http://localhost:8080/pentaho --username=admin --password=password --file-path=c:/home/Downloads/backup.zip --logfile=c:/temp/logfile.log --logLevel=DEBUG`

- **Linux**

  `./import-export.sh --backup --url=http://localhost:8080/pentaho --username=admin --password=password --file-path=/home/Downloads/backup.zip --logfile=/temp/logfile.log`

### Option B — driven by a configuration file

Put the connection details, file path, and logging options in a properties file and reference it with `--config`. Only the `--backup` flag stays on the command line.

- **Windows**

  `import-export.bat --backup --config=example-backup-full-system.properties`

- **Linux**

  `./import-export.sh --backup --config=example-backup-full-system.properties`

Example `example-backup-full-system.properties`:

```properties
url=http://localhost:8080/pentaho
username=admin
# Generate with: ./encr.sh -kettle <yourPassword>  (plaintext also works)
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde
backup-profile=FULL_SYSTEM
file-path=./backups/pentaho-full-backup.zip
logLevel=INFO
logfile=./logs/pentaho-full-backup.log
```

---

## Step 2: Restore the Pentaho Repository

Restoring your Pentaho Repository is also done through the use of command line arguments. The process is similar to the backup process, except for the differences shown in the provided examples. These examples can be customized for your particular server.

If an argument is required for a successful restore and has not been provided, the missing requirement is displayed in the cmd window. Restore results are also displayed in the window.

1. Open a cmd window and point the directory to the install location of your running Pentaho Server.
2. Use the `import-export` script with your arguments for restoring the repository.
3. Press **Enter**.

### Option A — all arguments on the command line

- **Windows**

  `import-export.bat --restore --url=http://localhost:8080/pentaho --username=admin --password=password --file-path=c:/home/Downloads/backup.zip --overwrite=true --logfile=c:/temp/logfile.log --logLevel=DEBUG`

- **Linux**

  `./import-export.sh --restore --url=http://localhost:8080/pentaho --username=admin --password=password --file-path=/home/Downloads/backup.zip --overwrite=true --logfile=/temp/logfile.log`

### Option B — driven by a configuration file

- **Windows**

  `import-export.bat --restore --config=example-restore-content.properties`

- **Linux**

  `./import-export.sh --restore --config=example-restore-content.properties`

Example `example-restore-content.properties`:

```properties
url=http://localhost:8080/pentaho
username=admin
password=Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde
restore-profile=CONTENT_ONLY
file-path=./backups/pentaho-content-backup.zip
# Restore behaviour
overwrite=true
applyAclSettings=true
retainOwnership=true
logLevel=INFO
logfile=./logs/pentaho-content-restore.log
```

### Restore options

| Option | Description |
| --- | --- |
| `overwrite=true` | Replace items that already exist in the repository. `false` keeps existing items and only adds what is missing. |
| `applyAclSettings=true` | Apply the ACLs stored in the archive to the restored items. |
| `retainOwnership=true` | Keep the original ownership recorded in the archive. |

---

## Selective backup and restore

Instead of the entire repository, you can back up or restore a single area with a profile. Use `--backup-profile` for backup and `--restore-profile` for restore (in a config file, the keys are `backup-profile` and `restore-profile`).

| Profile | Contents |
| --- | --- |
| `FULL_SYSTEM` | Everything (equivalent to a full backup) |
| `CONTENT_ONLY` | Repository files and folders |
| `SECURITY` | Users, roles, and ACLs |
| `DATA_SOURCE` | Data connections |
| `SCHEDULES` | Scheduled jobs and triggers |
| `SETTINGS` | System and user settings |

**Selective backup (Linux):**

`./import-export.sh --backup --url=http://localhost:8080/pentaho --username=admin --password=password --backup-profile=SECURITY --file-path=/home/Downloads/security-backup.zip`

**Selective restore (Linux):**

`./import-export.sh --restore --config=example-restore-security.properties --restore-profile=SECURITY`

**Notes:**

- During a restore, if `restore-profile` is not set it falls back to the `backup-profile` value in the same file, and if neither is set the full archive is restored.
- A profile and the granular per-component flags are mutually exclusive — when a profile is set, the granular flags are ignored. The granular flags are hidden from the default help; run `import-export.sh --help-advanced` to see them.

---

## Run the REST File Resource service

As an alternative to the script, the same backup and restore operations are exposed through the Pentaho REST `File Resource` service. See the Pentaho REST API documentation for the `repo/files/backup` and `repo/files/systemRestore` endpoints.
