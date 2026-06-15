---
description: 'Java unit and integration testing standards'
applyTo: '**/src/test/**/*.java, **/src/it/**/*.java, **/*Test.java, **/*IT.java'
---

# Java Testing Standards

Write tests in the style already established in the repository. In Pentaho-style projects, tests often mix legacy JUnit 4 coverage with newer JUnit 5 tests, frequently need to protect shared Kettle environment state, and separate unit tests under `src/test` from integration tests under `src/it`.

- Write descriptive test names that explain the expected behavior.
- Add brief comments when they help clarify the scenario being exercised, such as happy-path, edge case, or error-path behavior.
- Prefer small, focused tests over large setup-heavy tests.

## Test Guidelines

- Store unit tests under `src/test` and integration tests under `src/it`.
- Use Mockito 5 and JUnit 5 when the module already supports them.
- If JUnit 4 is already in use in the existing repository, new tests should use JUnit 5 if it can be done without breaking existing tests, otherwise continue to use JUnit 4.
- When mocking static classes with Mockito, scope the static mock to a single test method using try-with-resources.
- When testing the Kettle or PDI environment, use `RestorePDIEnvironment` so environment state is initialized and cleaned up correctly.
- When given the option between mocking an input object or instantiating the real object, use the real object.
- Keep assertions specific. Verify concrete outcomes, not just that code executed.
- Prefer one behavior per test method.

## Integration Test Guidelines

- Put integration tests in `src/it`, separate from unit tests in `src/test`.
- Run integration tests with `mvn verify -DrunITs`.
- Prefer integration tests that exercise real wiring, configuration, and cross-component behavior instead of heavily mocked flows.
- Many existing integration tests may be legacy, outdated, or currently broken. When an existing IT is relevant to the work being done, update it to run successfully whenever practical.
- Preserve the original intent of an existing IT when repairing it. Modernize setup, fixtures, or assertions only as much as needed to keep the test meaningful.
- If product behavior has legitimately changed and an IT's validation must change, add comments that clearly explain why the assertion or expected result was updated.
- Do not silently weaken an integration test just to make it pass. If coverage must narrow because the product contract changed, make that reasoning explicit in the test.
- When changing product code that affects a legacy IT, prefer fixing the IT over ignoring or deleting it.

## Recommended Examples

### Good Example - JUnit 5 with a real object

Use JUnit 5 for new tests when the module already supports it, and prefer instantiating a simple real object instead of mocking everything.

```java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueMetaTest {

  @Test
  void testStorageTypeDefaultsToNormal() {
    // Happy path: verify the default state of a real object.
    ValueMetaString valueMeta = new ValueMetaString( "customerName" );

    assertEquals( ValueMetaInterface.STORAGE_TYPE_NORMAL, valueMeta.getStorageType() );
  }
}
```

### Good Example - Stay with JUnit 4 when the surrounding suite is JUnit 4

If the existing test class or module already relies on JUnit 4 rules, runners, or shared setup, keep the style consistent unless there is a safe migration path.

```java
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionFileNameParserTest {

  private ConnectionFileNameParser parser;

  @Before
  public void setUp() {
    parser = new ConnectionFileNameParser();
  }

  @Test
  public void testParseReturnsExpectedScheme() {
    // Happy path: legacy JUnit 4 test in a legacy suite.
    assertEquals( "file", parser.extractScheme( "file:///tmp/demo.txt" ) );
  }
}
```

### Good Example - Static Mockito mock scoped to one test

This matches the Pentaho pattern used in tests such as `LogChannelTest` and servlet tests.

```java
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvUtilTest {

  @Test
  void testReadsConfiguredDateFormat() {
    // Edge case: isolate the static mock to this test only.
    try ( MockedStatic<EnvUtil> envUtilMock = Mockito.mockStatic( EnvUtil.class ) ) {
      envUtilMock.when( () -> EnvUtil.getSystemProperty( Const.KETTLE_DEFAULT_DATE_FORMAT ) )
        .thenReturn( "yyyy-MM-dd" );

      assertEquals( "yyyy-MM-dd", EnvUtil.getSystemProperty( Const.KETTLE_DEFAULT_DATE_FORMAT ) );
    }
  }
}
```

### Avoid - Static mock shared across tests without tight scope

```java
private static MockedStatic<EnvUtil> envUtilMock;

@BeforeAll
static void setUp() {
  envUtilMock = Mockito.mockStatic( EnvUtil.class );
}
```

Avoid long-lived static mocks unless the surrounding test infrastructure absolutely requires it and the lifecycle is already well established in that suite.

### Good Example - Protecting shared Kettle environment state

Use `RestorePDIEnvironment` in tests that initialize Kettle state, touch plugins, VFS, properties, locale, timezone, or logging registries.

```java
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import static org.junit.Assert.assertTrue;

public class KettleEnvironmentTest {

  @ClassRule
  public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void testEnvironmentInitializesCleanly() throws Exception {
    // Happy path: initialize shared environment with automatic cleanup.
    KettleClientEnvironment.init();

    assertTrue( KettleClientEnvironment.isInitialized() );
  }
}
```

### Avoid - Initializing Kettle environment without cleanup support

```java
@Test
public void testInit() throws Exception {
  KettleClientEnvironment.init();
}
```

Avoid leaving global state behind for other tests.

### Good Example - Prefer a real value object over a mock

```java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConnectionTest {

  @Test
  void testDatabaseMetaStoresAccessType() {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );

    assertEquals( DatabaseMeta.TYPE_ACCESS_NATIVE, meta.getAccessType() );
  }
}
```

### Avoid - Mocking a simple data carrier unnecessarily

```text
DatabaseMeta meta = mock( DatabaseMeta.class );
when( meta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_NATIVE );
```

Prefer mocks for collaborators with heavy side effects, expensive setup, remote access, or behavior that must be isolated.

### Good Example - Integration test in `src/it` that preserves original intent

Keep integration tests focused on real component interaction and document intent when expectations evolve.

```java
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransformationExecutionIT {

  @Test
  public void testCompletedTransformationReportsTerminalStatus() throws Exception {
    // Original intent: verify a successfully executed transformation reaches a terminal success state.
    // Updated validation: the product now reports FINISHED instead of COMPLETE after cleanup changes,
    // so the assertion tracks the current contract while preserving the original purpose of the test.
    String status = executeTransformationAndGetStatus();

    assertEquals( "FINISHED", status );
  }
}
```

### Avoid - Changing a legacy IT assertion without explaining the behavior drift

```text
assertEquals( "FINISHED", status );
```

If the expected value changed from a legacy assertion, explain why in comments so future maintainers understand whether the product contract changed or the old test was stale.

### Avoid - Skipping relevant broken ITs without attempting repair

```text
// Ignore this integration test because it has been failing for years.
```

Prefer repairing relevant ITs so they continue to validate real end-to-end behavior.

## Validation

- Run the narrowest relevant test first, such as a single class or module.
- After changing shared infrastructure code, run nearby tests that may be affected by global state.
- If a test introduces static mocking or environment initialization, verify cleanup behavior explicitly.
- Run unit tests with `mvn verify`.
- Run integration tests with `mvn verify -DrunITs`.
- When you touch code covered by legacy ITs in `src/it`, run the relevant ITs and update them to pass when practical.
