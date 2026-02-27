/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.services;

import java.io.OutputStream;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class AuditHelperTest extends BaseTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testAuditFailures() {
    startTest();

    OutputStream outputStream = getOutputStream( "testAudit", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_PARAMETERS );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$

    AuditHelper.audit( null, session, "Type", "This is a message", "Values", 34, this ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertTrue( true );
    finishTest();
  }

  public void testAuditFailures2() {
    startTest();
    AuditHelper.audit( null, "admin", "ViewAction", "String", "334234", "Type", "This is a message", "Values", 34, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        this );
    assertTrue( true );
    finishTest();
  }

  public void testAuditFailures3() {
    startTest();
    AuditHelper.audit( "342323", "admin", null, "String", "334234", "Type", "This is a message", "Values", 34, this ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    assertTrue( true );
    finishTest();
  }

  public void testAuditFailures4() {
    startTest();
    AuditHelper.audit( "342323", "admin", "ViewAction", "String", null, "Type", "This is a message", "Values", 34, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        this );
    assertTrue( true );
    finishTest();
  }

}
