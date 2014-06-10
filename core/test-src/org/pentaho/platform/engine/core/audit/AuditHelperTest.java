package org.pentaho.platform.engine.core.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.platform.api.engine.IAuditEntry;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

public class AuditHelperTest {

  private static final String SESSION_NAME = "testuser";
  private static final String TEST_TYPE = "testtype";
  private static final String TEST_NAME = "testname";
  private static final String TEST_TEXT = "testtext";
  private static final float TEST_DURATION = 1.23f;

  private StandaloneObjectFactory factory;
  private StandaloneSession session;

  @Mock
  private IRuntimeContext context;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks( this );
    session = new StandaloneSession( SESSION_NAME );
    factory = new StandaloneObjectFactory();
    PentahoSystem.registerObjectFactory( factory );
    factory.defineObject( IAuditEntry.class.getSimpleName(), TestAuditEntry.class.getName(),
        StandaloneObjectFactory.Scope.GLOBAL );
  }

  @After
  public void terDown() {
    session.destroy();
    PentahoSystem.deregisterObjectFactory( factory );
    AuditEntry.getCounts().clear();
  }

  @Test
  public void testAuditHelper() throws Exception {
    when( context.getActionName() ).thenReturn( "testActionName" );
    when( context.getInstanceId() ).thenReturn( "testInstanceId" );
    when( context.getCurrentComponentName() ).thenReturn( "testCurrentComponentName" );
    when( context.getProcessId() ).thenReturn( "testProcessId" );

    AuditHelper.audit( context, session, TEST_TYPE, TEST_NAME, TEST_TEXT, TEST_DURATION, null );

    TestAuditEntry entry = (TestAuditEntry) factory.get( IAuditEntry.class, null );

    assertNotNull( "AuditEntry should not be null", entry );

    assertEquals( context.getInstanceId(), entry.instId );
    assertEquals( context.getProcessId(), entry.jobId );
    assertEquals( context.getActionName(), entry.objId );
    assertEquals( context.getCurrentComponentName(), entry.objType );
    assertEquals( SESSION_NAME, entry.actor );
    assertEquals( TEST_TEXT, entry.messageTxtValue );
    assertEquals( Long.valueOf( 1 ), AuditEntry.getCounts().get( TEST_TYPE ) );
  }

  @Test
  public void testAuditHelperNotValid() throws Exception {
    AuditHelper.audit( null, session, TEST_TYPE, TEST_NAME, TEST_TEXT, TEST_DURATION, null );

    TestAuditEntry entry = (TestAuditEntry) factory.get( IAuditEntry.class, null );

    assertNotNull( "AuditEntry should not be null", entry );

    assertEquals( StringUtils.EMPTY, entry.instId );
    assertEquals( StringUtils.EMPTY, entry.jobId );
    assertEquals( StringUtils.EMPTY, entry.objId );
    assertEquals( StringUtils.EMPTY, entry.objType );
    assertEquals( SESSION_NAME, entry.actor );
    assertEquals( TEST_TEXT, entry.messageTxtValue );
    assertEquals( Long.valueOf( 1 ), AuditEntry.getCounts().get( TEST_TYPE ) );
  }
}
