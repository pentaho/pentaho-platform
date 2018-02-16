/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */


package org.pentaho.platform.engine.core;

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
import org.pentaho.platform.engine.core.audit.AuditEntry;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

public class AuditHelperTest {

  private static final String SESSION_NAME  = "testuser";
  private static final String TEST_TYPE     = "testtype";
  private static final String TEST_NAME     = "testname";
  private static final String TEST_TEXT     = "testtext";
  private static final float  TEST_DURATION = 1.23f;

  private StandaloneObjectFactory factory;
  private StandaloneSession       session;

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

    entry.auditAll( "testProcessId", "testInstanceId", "testActionName", "testCurrentComponentName", "testuser", null,
        null, "testtext", null, 0 );

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

    entry.auditAll( "", "", "", "", "testuser", null, null, "testtext", null, 0 );

    assertEquals( StringUtils.EMPTY, entry.instId );
    assertEquals( StringUtils.EMPTY, entry.jobId );
    assertEquals( StringUtils.EMPTY, entry.objId );
    assertEquals( StringUtils.EMPTY, entry.objType );
    assertEquals( SESSION_NAME, entry.actor );
    assertEquals( TEST_TEXT, entry.messageTxtValue );
    assertEquals( Long.valueOf( 1 ), AuditEntry.getCounts().get( TEST_TYPE ) );
  }
}
