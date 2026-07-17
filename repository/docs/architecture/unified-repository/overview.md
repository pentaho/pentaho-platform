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

## 1. Bean composition and call chain

```
Caller
  └─► unifiedRepository                       (ExceptionLoggingDecorator)
        └─► unifiedRepositoryProxy             (ProxyFactoryBean with AOP chain)
              ├─ unifiedRepositoryTransactionInterceptor   (JCR transaction)
              ├─ unifiedRepositoryMethodInterceptor        (Spring Security method security)
              └─► unifiedRepositoryTarget      (DefaultUnifiedRepository)
                    ├─► repositoryFileDao      (JcrRepositoryFileDao)
                    │     └─► JCR session      (Jackrabbit, opened with user credentials)
                    └─► repositoryFileAclDao   (JcrRepositoryFileAclDao)
```

AOP interceptors in `unifiedRepositoryProxy` are applied outermost-first:
the transaction interceptor starts the JCR transaction, then the method security
interceptor performs the ABS check, then the target method executes.

---

