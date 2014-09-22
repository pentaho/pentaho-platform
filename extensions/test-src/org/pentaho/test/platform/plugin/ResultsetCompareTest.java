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
public class ResultsetCompareTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testRSCompareOK() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetCompareTest.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    IActionParameter rtn = context.getOutputParameter( "COMPARERESULT" ); //$NON-NLS-1$
    assertNotNull( rtn );
    String compareResult = rtn.getStringValue();
    assertEquals( compareResult, "No Mismatches" ); //$NON-NLS-1$
    finishTest();
  }

  public void testRSCompareNotOK1() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetCompareTest_error1.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    IActionParameter rtn = context.getOutputParameter( "COMPARERESULT" ); //$NON-NLS-1$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    assertNotNull( rtn );
    String compareResult = rtn.getStringValue();
    assertEquals( compareResult, "Eastern" ); //$NON-NLS-1$
    finishTest();
  }

  public void testRSCompareNotOK2() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetCompareTest_error2.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );

    finishTest();
  }

  public void testRSCompareNotOK3() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetCompareTest_error3.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );

    finishTest();

  }

  public void testRSCompareNotOK4() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetCompareTest_error4.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );

    finishTest();

  }

  public void testRSCompareNotOK5() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetCompareTest_error5.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );

    finishTest();

  }

  public static void main( String[] args ) {
    ResultsetCompareTest test = new ResultsetCompareTest();
    try {
      test.setUp();
      test.testRSCompareOK();
      test.testRSCompareNotOK1();
      test.testRSCompareNotOK2();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
