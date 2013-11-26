/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.OutputStream;

@SuppressWarnings( "nls" )
public class AuditHelperTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-res/solution";

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
    AuditHelper
        .audit( null, "admin", "ViewAction", "String", "334234", "Type", "This is a message", "Values", 34, this ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
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
    AuditHelper
        .audit( "342323", "admin", "ViewAction", "String", null, "Type", "This is a message", "Values", 34, this ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    assertTrue( true );
    finishTest();
  }

}
