# Pentaho Import/Export System - Comprehensive Architecture & Workflow Guide

## Table of Contents

1. [Executive Overview](#executive-overview)
2. [System Architecture](#system-architecture)
3. [Import Workflow](#import-workflow)
4. [Export Workflow](#export-workflow)
5. [Helper System](#helper-system)
6. [Data Structures](#data-structures)
7. [Service Layer Integration](#service-layer-integration)
8. [Repository Access Patterns](#repository-access-patterns)
9. [Error Handling](#error-handling)

---

## Executive Overview

### Purpose

Pentaho's import/export system enables administrators to:
- **Backup** complete platform configurations and artifacts
- **Restore** from backups for disaster recovery
- **Migrate** between Pentaho instances
- **Selective restore** specific components (users, schedules, reports, etc.)
- **Replicate** environments (dev → test → prod)

### Key Components

| Component | Role |
|-----------|------|
| **FileResource** | REST API entry point |
| **FileService** | Request routing and coordination |
| **PentahoPlatformImporter/Exporter** | Top-level orchestrators |
| **SolutionImportHandler/ExportHandler** | Central managers |
| **Import/Export Helpers** | Specialized processors (users, schedules, files, metadata) |
| **Service Layer** | Data access (IUserRoleDao, IUnifiedRepository, etc.) |
| **Repository** | JCR backend storage |

### Data Flow Overview

```
USER REQUEST (REST API)
    ↓
ROUTING (FileService)
    ↓
ORCHESTRATION (SolutionImportHandler/ExportHandler)
    ↓
PARALLEL/SEQUENTIAL PROCESSING (Helpers)
    ↓
SERVICE LAYER (DAOs, Managers)
    ↓
REPOSITORY (JCR, File System)
    ↓
RESPONSE (Success/Error Report)
```

---

## System Architecture

### High-Level Design Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                     REST API Layer                          │
│  FileResource.selectiveRestore / selectiveExport            │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌─────────────────────────────────────────────────────────────┐
│                 Routing Layer                               │
│  FileService → Request validation & file extraction         │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
        ┌──────────────┴──────────────┐
        ↓                             ↓
┌──────────────────────┐    ┌──────────────────────┐
│ IMPORT PIPELINE      │    │ EXPORT PIPELINE      │
└──────────────────────┘    └──────────────────────┘
        ↓                             ↓
┌──────────────────────┐    ┌──────────────────────┐
│ PentahoPlatformImp   │    │ PentahoPlatformExp   │
└──────────┬───────────┘    └────────┬─────────────┘
           ↓                         ↓
┌──────────────────────┐    ┌──────────────────────┐
│ SolutionImportHand   │    │ SolutionExportHand   │
│ - Manifest parsing   │    │ - Manifest creation  │
│ - Helper registry    │    │ - Helper registry    │
│ - Error handling     │    │ - Error handling     │
└──────────┬───────────┘    └────────┬─────────────┘
           ↓                         ↓
    ┌──────┴─────────┐       ┌──────┴─────────┐
    ↓  ↓  ↓  ↓  ↓    ↓       ↓  ↓  ↓  ↓  ↓    ↓
  ┌────────────────────────────────────────────────┐
  │         Helper Chain (Sequential)              │
  │  1. RepositoryFile     4. Metastore            │
  │  2. UsersAndRoles      5. Datasource           │
  │  3. Schedule                                   │
  └────────────┬─────────────────────────────────┘
               ↓
  ┌────────────────────────────────────────────────┐
  │         Service Layer (DAOs/Managers)          │
  │  IUserRoleDao          IUnifiedRepository       │
  │  ITenantManager        ISchedulerService       │
  │  IUserSettingService   IDatasourceService      │
  └────────────┬─────────────────────────────────┘
               ↓
  ┌────────────────────────────────────────────────┐
  │         JCR Repository / File System           │
  │  Persistent Storage Layer                      │
  └────────────────────────────────────────────────┘
```

### Component Responsibilities

#### REST Tier
```
FileResource
├─ selectiveRestore(file)
│  └─ Called via: POST /pentaho/api/repo/files/import
├─ selectiveExport(path, type)
│  └─ Called via: GET /pentaho/api/repo/files/export
└─ Delegates to FileService
```

#### Service Tier
```
FileService
├─ selectiveRestore()
│  ├─ Extracts ZIP file
│  ├─ Validates manifest
│  └─ Creates PentahoPlatformImporter
├─ selectiveExport()
│  ├─ Validates path
│  ├─ Creates PentahoPlatformExporter
│  └─ Returns ZIP stream
└─ Error handling & logging
```

#### Orchestration Tier
```
PentahoPlatformImporter/Exporter
├─ Creates appropriate Handler
├─ Configures handler with options
├─ Triggers processing
└─ Returns status/result

SolutionImportHandler/ExportHandler
├─ Registers all helpers
├─ Manages execution sequence
├─ Aggregates results
├─ Handles errors
└─ Generates reports
```

#### Helper Tier
```
Each Helper specializes in one domain:
├─ RepositoryFile: Artifacts, reports, dashboards
├─ UsersAndRoles: Users, groups, role assignments
├─ Schedule: Scheduled jobs (cron, triggers)
├─ Metastore: Metadata definitions
└─ Datasource: Connection definitions
```

---

## Import Workflow

### Phase 1: Request Reception & Validation

```
HTTP POST /pentaho/api/repo/files/import
├─ Content-Type: multipart/form-data
├─ Body: ZIP file containing backup
└─ Headers: Authentication token

↓

FileResource.selectiveRestore()
├─ Authenticate user
├─ Parse multipart request
├─ Extract ZIP file
├─ Locate manifest.xml
└─ Validate XML structure
```

### Phase 2: Handler Initialization

```
FileService.selectiveRestore()
├─ Extract file path
├─ Read manifest
├─ Create PentahoPlatformImporter
│  └─ new PentahoPlatformImporter()
└─ Call: importFile(manifest, options)
    ↓
PentahoPlatformImporter.importFile()
├─ Parse zip archive
├─ Read ExportManifest
├─ Create SolutionImportHandler
├─ Set handler properties:
│  ├─ isOverwriteFile (true/false)
│  ├─ isPerformingRestore (true)
│  ├─ Logger instance
│  └─ Manifest data
└─ Call: handler.importFile()
    ↓
SolutionImportHandler.importFile()
├─ Register all import helpers
├─ Configure each helper
└─ Call: runImportHelpers()
```

### Phase 3: Helper Registration & Sequencing

```
SolutionImportHandler.runImportHelpers()

Registered helpers (in order):
1. RepositoryFileImportHelper
   ├─ Imports folder structure
   ├─ Imports all artifacts (reports, dashboards)
   └─ Restores file permissions

2. UsersAndRolesImportHelper ← [FIXED IN THIS SESSION]
   ├─ Imports users with credentials
   ├─ Imports roles and permissions
   ├─ Maps users to roles
   ├─ Creates user home folders (ITenantManager)
   ├─ Imports user-specific settings (IUserSettingService)
   └─ Handles: NullPointerException fix (importUserSettings now receives handler)

3. ScheduleImportUtil
   ├─ Creates scheduler jobs
   ├─ Sets up cron expressions
   ├─ Associates owners
   └─ Handles: EnterpriseSchedulerService permission checks

4. MetastoreImportHelper
   ├─ Imports metadata definitions
   ├─ Restores analysis schema
   └─ Configures datasource metadata

5. DatasourceImportHelper
   ├─ Imports connection definitions
   ├─ Restores credentials (encrypted)
   └─ Validates connectivity
```

### Phase 4: Detailed User/Role Import Process

This is where the critical fix was applied:

```
UsersAndRolesImportHelper.importUsers()
└─ For each user in manifest:
   ├─ Call: importUserAndRole(username, user, roleMap, handler)
   │  ├─ Validate username not duplicate
   │  ├─ Create user with IUserRoleDao.createUser()
   │  │  └─ JCR Session accessed here
   │  ├─ Create home folder with ITenantManager.createUserHomeFolder()
   │  └─ Call: importUserSettings(user, handler) ← [FIXED]
   │     ├─ Null check: if (handler == null) return;
   │     ├─ Get IUserSettingService
   │     ├─ For each user setting:
   │     │  ├─ Check: handler.isPerformingRestore() ← [NOW WORKS]
   │     │  ├─ Check: handler.isOverwriteFile()
   │     │  └─ Call: userSettingService.setUserSetting()
   │     └─ Return successfully

UsersAndRolesImportHelper.importRoles()
└─ For each role in manifest:
   ├─ Create role with IUserRoleDao.createRole()
   └─ Set permissions with IRoleAuthorizationPolicyRoleBindingDao

UsersAndRolesImportHelper.mapRolesToUsers()
└─ For each role-user mapping:
   ├─ Get role
   ├─ Get user
   └─ Add user to role
```

### Phase 5: Schedule Import (Where NullPointerException Occurred)

```
ScheduleImportUtil.doImport()
├─ For each schedule in manifest:
│  ├─ Call: importScheduleOwnerUser(username, manifest, handler)
│  │  └─ Delegates to: UsersAndRolesImportHelper.importScheduleOwnerUser()
│  │     ├─ Call: importUserAndRole() ← Must pass handler!
│  │     └─ Call: importUserSettings() ← Must pass handler!
│  │        └─ [BEFORE FIX] handler was null → NullPointerException
│  │        └─ [AFTER FIX] handler is passed parameter → Works!
│  │
│  ├─ Create scheduler job with SchedulerResource.createJob()
│  │  ├─ Validate user permissions
│  │  ├─ Check: EnterpriseSchedulerService.hasSchedulingPermission()
│  │  │  └─ Retrieves user roles via JCR
│  │  └─ Create job in scheduler
│  │
│  └─ Set job triggers/cron expression
│
└─ Return: success or error
```

### Phase 6: Result Aggregation & Reporting

```
SolutionImportHandler.runImportHelpers() completes
├─ Collect results from each helper
├─ Aggregate statistics:
│  ├─ Total items processed
│  ├─ Successful imports
│  ├─ Failed imports
│  └─ Error details
├─ Generate summary report
└─ Log results

Return to FileService
├─ Build response:
│  ├─ Status: SUCCESS/PARTIAL/FAILED
│  ├─ Message: Summary text
│  ├─ Statistics: Counts
│  └─ Errors: Details if any
└─ HTTP Response (200/500)
```

---

## Export Workflow

### Phase 1: Request Reception

```
HTTP GET /pentaho/api/repo/files/export?path=&type=
├─ path: Repository path to export
├─ type: selective/full
└─ Headers: Authentication

↓

FileResource.selectiveExport()
├─ Authenticate user
├─ Validate path exists
├─ Check permissions
└─ Call: FileService.selectiveExport()
```

### Phase 2: Handler Initialization

```
FileService.selectiveExport()
├─ Validate export path
├─ Create PentahoPlatformExporter
└─ Call: exportFile()
    ↓
PentahoPlatformExporter.exportFile()
├─ Create SolutionExportHandler
├─ Set handler properties:
│  ├─ isPerformingRestore (false)
│  ├─ Logger instance
│  └─ Export path
└─ Call: handler.exportFile()
    ↓
SolutionExportHandler.exportFile()
├─ Create ExportManifest
├─ Register all export helpers
└─ Call: runExportHelpers()
```

### Phase 3: Helper Registration & Sequencing

```
SolutionExportHandler.runExportHelpers()

Registered helpers (in order):
1. RepositoryFileExportHelper
   ├─ Exports folder structure
   ├─ Exports all artifacts (reports, dashboards)
   └─ Exports file permissions

2. UsersAndRolesExportHelper
   ├─ Exports all users (without passwords)
   ├─ Exports all roles
   ├─ Exports user-role mappings
   └─ Exports user-specific settings

3. ScheduleExportUtil
   ├─ Exports scheduled jobs
   ├─ Exports cron expressions
   └─ Exports job configurations

4. MetastoreExportHelper
   ├─ Exports metadata definitions
   └─ Exports analysis schema

5. DatasourceExportHelper
   ├─ Exports connection definitions
   ├─ Exports credentials (encrypted)
   └─ Validates connectivity
```

### Phase 4: Manifest Creation

```
During export, each helper populates ExportManifest:

ExportManifest structure:
├─ Version: "1.0"
├─ Repository files
│  └─ List of all exported artifacts with metadata
├─ Users
│  └─ User definitions (no passwords exported)
├─ Roles
│  └─ Role definitions with permissions
├─ User-Role mappings
│  └─ Which users have which roles
├─ Schedules
│  └─ Schedule definitions with owners
├─ Metadata
│  └─ Metadata definitions
└─ Datasources
   └─ Connection definitions (encrypted)
```

### Phase 5: ZIP Package Creation

```
SolutionExportHandler completes
├─ Finalize manifest.xml
├─ Add to ZIP archive
├─ Add all exported files to ZIP
├─ Compress package
└─ Return as HTTP stream

FileService.selectiveExport()
├─ Set response headers:
│  ├─ Content-Type: application/zip
│  ├─ Content-Disposition: attachment; filename=backup.zip
│  └─ Content-Length: size
└─ Stream ZIP to client
```

---

## Helper System

### Architecture

```
IImportHelper (Interface)
├─ Method: importFile(manifest, options)
├─ Implementation: Each specialized helper
└─ Lifecycle: Created, registered, executed

IExportHelper (Interface)
├─ Method: exportFile(path, options)
├─ Populates: ExportManifest
└─ Lifecycle: Created, registered, executed
```

### Helper Execution Model

#### Sequential Execution (Current)
```
Helper 1 completes → Helper 2 starts → Helper 3 starts → ...
└─ Ensures dependencies are satisfied
```

#### Execution Order Rationale
```
1. RepositoryFile FIRST
   └─ Must restore folder structure before users create home folders

2. UsersAndRoles SECOND
   └─ Users must exist before schedule owners can be resolved
   └─ Roles must exist before permissions can be assigned

3. Schedule THIRD
   └─ Requires users and roles to exist
   └─ Requires folder structure for job configurations

4. Metastore FOURTH
   └─ Can be independent but may reference users

5. Datasource FIFTH
   └─ Can be independent, typically last
```

### Helper Implementation Template

```java
public class SpecializedImportHelper implements IImportHelper {
    
    private SolutionImportHandler handler;
    private ExportManifest manifest;
    
    @Override
    public void importFile(ExportManifest manifest, ...) {
        this.manifest = manifest;
        
        // 1. Validate
        if (manifest == null || manifest.getSpecializedData() == null) {
            return;
        }
        
        // 2. Process
        for (SpecializedData data : manifest.getSpecializedData()) {
            try {
                importSingleItem(data);
                recordSuccess();
            } catch (Exception e) {
                handler.getLogger().error("Failed: " + e.getMessage());
                recordFailure();
            }
        }
        
        // 3. Report
        logSummary();
    }
    
    private void importSingleItem(SpecializedData data) {
        // Specialized business logic
        // Access services via PentahoSystem.get()
        // Use handler for logging and context
        // Check: handler.isPerformingRestore()
        // Check: handler.isOverwriteFile()
    }
}
```

---

## Data Structures

### ExportManifest

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest version="1.0">
    
    <repository-files>
        <file-entry>
            <path>/public/reports/sales.prpt</path>
            <type>REPORT</type>
            <description>Sales Report</description>
            <owner>admin</owner>
            <created>2026-05-28T10:00:00Z</created>
        </file-entry>
        <!-- More files -->
    </repository-files>
    
    <users>
        <user>
            <username>user1</username>
            <password-encrypted>true</password-encrypted>
            <description>Test User</description>
            <email>user1@example.com</email>
            <enabled>true</enabled>
        </user>
        <!-- More users -->
    </users>
    
    <roles>
        <role>
            <name>admin</name>
            <description>Administrator</description>
            <permissions>
                <permission>MANAGE_SETTINGS</permission>
                <permission>ADMINISTER</permission>
            </permissions>
        </role>
        <!-- More roles -->
    </roles>
    
    <user-role-mappings>
        <mapping>
            <username>user1</username>
            <role>report_viewer</role>
        </mapping>
        <!-- More mappings -->
    </user-role-mappings>
    
    <schedules>
        <schedule>
            <job-id>job-123</job-id>
            <job-name>Daily Sales Report</job-name>
            <owner>admin</owner>
            <cron-expression>0 0 1 * * ?</cron-expression>
            <report-path>/public/reports/sales.prpt</report-path>
        </schedule>
        <!-- More schedules -->
    </schedules>
    
    <metadata>
        <domain>
            <domain-id>sales</domain-id>
            <description>Sales Data Model</description>
        </domain>
        <!-- More metadata -->
    </metadata>
    
    <datasources>
        <datasource>
            <name>sales_db</name>
            <type>JDBC</type>
            <driver>org.postgresql.Driver</driver>
            <connection-url>jdbc:postgresql://localhost/pentaho</connection-url>
            <username>pentaho</username>
            <password-encrypted>true</password-encrypted>
        </datasource>
        <!-- More datasources -->
    </datasources>
    
</manifest>
```

### Data Flow Through Helpers

```
ZIP File Extracted
├─ manifest.xml → Parsed into ExportManifest object
├─ artifacts/ → Extracted to temp directory
├─ users.xml → Parsed into UserExport list
├─ roles.xml → Parsed into RoleExport list
├─ schedules.xml → Parsed into ScheduleExport list
├─ metadata/ → Metadata definitions
└─ datasources.xml → Connection definitions

ExportManifest Object
└─ Used throughout import process
   ├─ Referenced by each helper
   ├─ Updated with progress
   └─ Queried for data during import
```

---

## Service Layer Integration

### User & Role Management

```
IUserRoleDao
├─ createUser(ITenant, String username, String password, String email)
│  └─ Creates new user in repository
├─ createRole(ITenant, String name)
│  └─ Creates new role in repository
├─ setUserRoles(ITenant, String username, List<String> roles)
│  └─ Maps user to roles
└─ getUser(ITenant, String username)
   └─ Retrieves user from repository

ITenantManager
├─ createTenant(ITenant)
│  └─ Creates tenant structure
└─ createUserHomeFolder(ITenant, String username)
   └─ Creates /home/username folder

IUserSettingService
├─ setUserSetting(String username, String settingName, String settingValue)
│  └─ Stores user-specific settings
└─ getUserSetting(String username, String settingName, String defaultValue)
   └─ Retrieves user settings

IRoleAuthorizationPolicyRoleBindingDao
├─ setRoleBindings(ITenant, String role, List<String> permissions)
│  └─ Assigns permissions to role
└─ getRoleBindings(ITenant, String role)
   └─ Retrieves role permissions
```

### File & Artifact Management

```
IUnifiedRepository
├─ getFile(String path)
│  └─ Retrieves file metadata and content
├─ createFile(String path, RepositoryFile file)
│  └─ Creates new file/folder
├─ updateFile(RepositoryFile file, InputStream data, String comment)
│  └─ Updates existing file
└─ deleteFile(String path)
   └─ Deletes file/folder

RepositoryFile
├─ id: Unique identifier
├─ path: Repository path
├─ name: File name
├─ folder: Is directory
├─ owner: User who owns file
├─ created: Creation timestamp
├─ modified: Last modified timestamp
├─ permissions: Access control list
└─ content: File data (lazy-loaded)
```

### Schedule Management

```
SchedulerService / SchedulerResource
├─ createJob(JobScheduleRequest request)
│  └─ Creates new scheduled job
├─ getJobs()
│  └─ Lists all jobs
├─ deleteJob(String jobId)
│  └─ Deletes scheduled job
└─ pauseJob(String jobId)
   └─ Pauses job execution

EnterpriseSchedulerService
├─ hasSchedulingPermission(String username)
│  └─ Checks if user can create schedules
├─ resolveScheduleOwner(String username)
│  └─ Resolves owner from username
└─ getRolesForUser(String username)
   └─ Gets user's roles (access JCR)

CompositeUserRoleListService
└─ getRolesForUser(String username)
   └─ Aggregates roles from multiple sources
      └─ Calls JcrUserRoleDao.getUserRoles()
         └─ Access JCR via GuavaCachePoolPentahoJcrSessionFactory
```

### Datasource Management

```
IDatasourceService
├─ createDatasource(Datasource ds)
│  └─ Creates new datasource
├─ getDatasource(String name)
│  └─ Retrieves datasource
└─ updateDatasource(Datasource ds)
   └─ Updates datasource configuration

Datasource
├─ name: Connection name
├─ driver: JDBC driver class
├─ connectionUrl: Database connection string
├─ username: Database user
├─ password: Database password (encrypted)
├─ databaseType: DB type (PostgreSQL, MySQL, etc.)
└─ maxPoolSize: Connection pool size
```

### Metadata Management

```
IMetaStore
├─ createElement(IMetaStoreElement element)
│  └─ Creates metadata element
├─ getElement(String namespace, String type, String name)
│  └─ Retrieves metadata element
└─ updateElement(IMetaStoreElement element)
   └─ Updates metadata

IMetaStoreElementType
├─ name: Element type name
├─ attributes: List of attributes
└─ Used by: Analysis schema, data models, etc.
```

