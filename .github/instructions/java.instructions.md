---
description: 'Pentaho-style Java coding standards and project conventions derived from pentaho-kettle and pentaho-coding-standards'
applyTo: '**/src/main/java/**/*.java'
---

# Pentaho Java Standards

Use these instructions for Java code in Pentaho-style projects, especially Maven multi-module codebases that follow the conventions used in `pentaho-kettle` and `pentaho-coding-standards`.

## General Instructions

- Preserve the existing module and package structure. In Pentaho projects, reusable logic belongs in lower-level modules such as `core`, runtime orchestration belongs in `engine`, UI code belongs in `ui`, and integration coverage belongs in dedicated integration-test modules.
- Keep new code compatible with JDK17. When editing existing code, update language features to JDK17 where it can be done without breaking existing tests or introducing inconsistent style.
- Follow standard Maven layout: production code in `src/main/java`, tests in `src/test/java`, and integration tests in the repository's established integration-test module or directory.
- Prefer extending existing Pentaho abstractions, utilities, exception types, and logging APIs over introducing parallel patterns or frameworks.
- Preserve existing license headers and legal notices in touched files. For new files, match the header style already used in the surrounding module.

## Formatting and Code Style

- Use spaces, never tabs.
- Use 2-space indentation.
- Put opening braces on the same line for types, methods, constructors, and control flow.
- Include a space inside parentheses and after commas, matching Pentaho formatter settings. Examples: `if ( value != null )`, `method( arg1, arg2 )`.
- Use braces for all `if`, `else`, `for`, `while`, and `do` blocks, even for single-line statements.
- Do not leave trailing whitespace.
- End files with a newline.
- Keep imports explicit. Do not use wildcard imports.
- Remove unused and redundant imports.
- Do not use `sun.*` or other JDK-internal packages.
- Keep statements one per line.

## Naming and File Organization

- Keep package names lowercase and aligned to the Maven module and domain
- Use descriptive class names that match file names.
- Use meaningful field and method names that reflect ETL, plugin, repository, VFS, transformation, or job concepts already present in the codebase.
- Prefer one top-level type per file.
- Keep helper logic close to the domain class that owns it; avoid creating utility classes unless the behavior is truly shared.

## Internationalization and User-Facing Strings

- When editing a class that already uses Pentaho i18n, continue that pattern instead of introducing hard-coded user-facing strings.
- For localized classes, keep or add the `PKG` marker used by `BaseMessages`, for example:

```java
private static Class<?> PKG = MyClass.class; // for i18n purposes, needed by Translator2!!
```

- Use `BaseMessages.getString( PKG, "Key" )` or the existing package-based lookup helpers for messages shown to users, surfaced in exceptions, or logged as user-visible status.
- Reuse the existing message bundle and key naming style in the module instead of inventing inconsistent keys.

## Logging Conventions

- When a class already participates in Pentaho logging, preserve the existing logging object pattern rather than introducing SLF4J or direct console output.
- Code for modules that are not part of the Kettle architecture should use SLF4J for logging
- If LogChannel is already in use by surrounding code, continue to use it and do not introduce SLF4J or other logging frameworks in the same class or module.
- Create log channels with the most appropriate subject:
  - `new LogChannel( this )` when the object itself is the logging source
  - `new LogChannel( subjectName )` for static or utility-style contexts
  - `new LogChannel( subject, parentObject )` when parent logging context matters
- Use the established Pentaho log levels intentionally:
  - `logError` for failures
  - `logBasic` for user-relevant progress
  - `logDetailed` for detailed execution information
  - `logDebug` for deep diagnostics
  - `logRowlevel` only for row-level tracing
- Avoid `System.out.println` and `System.err.println` except in existing bootstrap, CLI, or legacy code paths that already use console output.

## Exceptions and Error Handling

- Throw the most specific existing Pentaho exception type available, such as `KettleException`, `KettleXMLException`, or `KettlePluginException`.
- Preserve the original cause when wrapping exceptions.
- Do not swallow exceptions silently.
- Keep error messages actionable and consistent with existing project wording.
- When a method already uses repository-specific exception handling patterns, extend that pattern instead of normalizing it to a generic runtime exception.

## Security and Data Handling

- Treat request parameters, transformation metadata, log text, XML content, and other externally sourced values as untrusted.
- Escape values written into HTML with `Encode.forHtml( ... )`.
- Escape values written into XML with `Encode.forXml( ... )` and XML attributes with `Encode.forXmlAttribute( ... )`.
- Do not concatenate unescaped user-controlled values into servlet output, HTML fragments, XML, or log-rendered UI content.
- Reuse existing security libraries already present in the build, such as OWASP Encoder, before adding new dependencies.
- Be careful with filesystem, JNDI, plugin loading, and environment initialization code; preserve existing initialization order and cleanup behavior.

## Dependencies and Build Practices

- Assume Maven is the build system unless the repository says otherwise.
- Prefer dependency versions managed by the parent POM or shared properties instead of hard-coding versions in module POMs.
- When adding a dependency, first check whether the project already uses an equivalent library.
- Avoid introducing duplicate logging frameworks or alternate utility libraries when the repository already has an established choice.
- Follow the repository's exclusion patterns when they are used to control transitive dependencies.
- Keep test-only dependencies in test scope.

## Test-Aware Development

- Follow the detailed rules in `javatesting.instructions.md` for unit and integration tests.
- When production code affects global Kettle or PDI state, design it so tests can initialize and restore the environment cleanly.
- For code that interacts with Kettle environment initialization, plugin registries, VFS, logging stores, or locale/timezone-sensitive behavior, keep testability in mind and preserve the existing reset hooks and rules.

## Good Example

```java
private static Class<?> PKG = MyService.class; // for i18n purposes, needed by Translator2!!

private final LogChannelInterface log = new LogChannel( this );

public void process( String name ) throws KettleException {
  if ( name == null ) {
    throw new KettleException( BaseMessages.getString( PKG, "MyService.NameMissing" ) );
  }

  log.logDetailed( BaseMessages.getString( PKG, "MyService.Processing", name ) );
}
```

## Avoid

- Introducing 4-space indentation or tabs in Pentaho-formatted files
- Using wildcard imports
- Adding hard-coded user-facing strings to classes that already use `BaseMessages`
- Introducing new logging patterns where `LogChannel` is already the norm
- Writing raw request data directly into HTML or XML responses without OWASP encoding
- Using newer Java language features that exceed the repository's configured Java version

## Validation

- Format code with the Pentaho formatter settings from `pentaho-coding-standards` when the repository uses them.
- Run unit tests with `mvn verify`.
- Run integration tests with `mvn verify -DrunITs` when the repository stores ITs under `src/it` and uses that pattern.
- Run `mvn checkstyle:check` when the repository wires Checkstyle into the build or provides the Pentaho Checkstyle rules.
- If you modify logging, i18n, servlet output, or environment initialization code, verify both behavior and escaping/reset semantics in tests.

