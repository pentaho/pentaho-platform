/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin;

import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class ResultSetExportComponentTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testRSExportComponent() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetExportTest.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter( "EXPORTRESULT" ); //$NON-NLS-1$
    assertNotNull( "Result is null", rtn ); //$NON-NLS-1$

    // Check that the data, Eastern, is in the result set
    String content = rtn.getStringValue();
    assertNotNull( "Exported content is null", content ); //$NON-NLS-1$

    int containsEastern = content.indexOf( "Eastern" ); //$NON-NLS-1$ 
    assertEquals( "ResultSet export does not contain 'Eastern'", Math.max( containsEastern, -1 ), containsEastern ); //$NON-NLS-1$
    finishTest();
  }

  /*
   * The test removes the result-set element from the action inputs.
   */
  public void testRSExportComponent_error1() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetExportTest_error1.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  /*
   * The test removes outputs and action-outputs. This causes ResultSetExportComponent's validateAction() to return
   * false.
   */
  public void testRSExportComponent_error2() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetExportTest_error2.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public static void main( String[] args ) {
    ResultSetExportComponentTest test = new ResultSetExportComponentTest();
    try {
      test.setUp();
      test.testRSExportComponent();
      test.testRSExportComponent_error1();
      test.testRSExportComponent_error2();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
