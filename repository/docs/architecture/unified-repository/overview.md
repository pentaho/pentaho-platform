---
type: architecture
title: Unified Repository Access Control Overview
description: Bean composition and call chain for IUnifiedRepository access-control enforcement across its layers.
status: active
timestamp: 2026-07-17T00:00:00Z
---

# Unified Repository Access Control Analysis

> Source files analysed:
> - `repository.spring.xml` – Spring bean configuration (AOP proxies, interceptors, ACL voters, ABS bindings)
> - `DefaultUnifiedRepository.java` – public API implementation
> - `ExceptionLoggingDecorator.java` – outermost `unifiedRepository` bean
> - `JcrRepositoryFileDao.java` – JCR DAO (file operations)
> - `JcrRepositoryFileAclDao.java` – JCR DAO (ACL operations)
> - `DefaultDeleteHelper.java` – JCR DAO helper for delete/undelete (source of `RepositoryFileDaoFileExistsException`/`RepositoryFileDaoReferentialIntegrityException`)
> - `RepositoryAccessVoterManager.java` – file-level voter manager

---

## Bean composition and call chain

```mermaid
flowchart TD
    Caller --> ELD["unifiedRepository<br/>(ExceptionLoggingDecorator)"]
    ELD --> Proxy["unifiedRepositoryProxy<br/>(ProxyFactoryBean, AOP chain)"]
    Proxy -->|"1: outermost"| TxI["unifiedRepositoryTransactionInterceptor<br/>(JCR transaction)"]
    TxI -->|"2"| SecI["unifiedRepositoryMethodInterceptor<br/>(Spring Security method security)"]
    SecI -->|"3: target"| Target["unifiedRepositoryTarget<br/>(DefaultUnifiedRepository)"]
    Target --> FileDao["repositoryFileDao<br/>(JcrRepositoryFileDao)"]
    Target --> AclDao["repositoryFileAclDao<br/>(JcrRepositoryFileAclDao)"]
    FileDao --> JCR["JCR session<br/>(Jackrabbit, opened with user credentials)"]
```

AOP interceptors in `unifiedRepositoryProxy` are applied outermost-first:
the transaction interceptor starts the JCR transaction, then the method security
interceptor performs the ABS check, then the target method executes.

---

