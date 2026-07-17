# Unified Repository Architecture

## Overview

- [Unified Repository Access Control Overview](overview.md) - Bean composition and call chain for IUnifiedRepository access-control enforcement across its layers.

## Layers

In caller-to-storage order:

1. [ExceptionLoggingDecorator Layer](layer-exception-logging-decorator.md) - How the `ExceptionLoggingDecorator` (`unifiedRepository` bean) surfaces and translates repository exceptions.
2. [Method Interceptor Layer](layer-method-interceptor.md) - AOP method-level security enforced by `unifiedRepositoryMethodInterceptor`, including protected methods, ABS actions, and uncovered methods.
3. [DefaultUnifiedRepository Target Bean](layer-default-unified-repository.md) - Role of `DefaultUnifiedRepository` as the target bean behind the interceptor and decorator layers.
4. [JcrRepositoryFileDao Layer](layer-jcr-repository-file-dao.md) - File-path-level access control in `JcrRepositoryFileDao`, including the access voter, native ACL enforcement, kiosk mode, read/write behavior, and magic ACE caveats.
5. [JcrRepositoryFileAclDao hasAccess Layer](layer-jcr-repository-file-acl-dao.md) - How `JcrRepositoryFileAclDao.hasAccess()` evaluates permissions.
6. [Jackrabbit JCR Session Layer](layer-jackrabbit-jcr-session.md) - Native Jackrabbit JCR session behavior underlying repository access control.
7. [JcrTemplate Exception Translation Layer](layer-jcr-template-exception-translation.md) - How `JcrTemplate` translates JCR exceptions between the native session and the `ExceptionLoggingDecorator`.

## Design Observations

- [Unified Repository Access Control Design Observations](design-observations.md) - Key design observations explaining why IUnifiedRepository's access-control layers behave the way they do.
