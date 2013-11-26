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

//import org.pentaho.commons.connection.IPentahoResultSet;
//import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class SQLExecuteComponentTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  /*
   * public void testSQLExecuteComponentSingleStatement() { startTest();
   * info("Expected: Successful execution with one row of data"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "rules", "sqlexecute_rule1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("rule-result"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultset = (IPentahoResultSet) rtn.getValue();
   * 
   * assertEquals(resultset.getRowCount(), 1);
   * 
   * assertEquals("Expected first row to contain a 1 in the first column.", resultset.getValueAt(0, 0), new Integer(1));
   * //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  /*
   * public void testSQLExecuteComponentMultipleStatements() { startTest();
   * info("Expected: Successful execution with 5 rows of data in the result. First row should have a negative #");
   * //$NON-NLS-1$ IRuntimeContext context = run("test", "rules", "sqlexecute_rule2.xaction"); //$NON-NLS-1$
   * //$NON-NLS-2$ //$NON-NLS-3$ assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("rule-result"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultset = (IPentahoResultSet) rtn.getValue();
   * 
   * assertEquals(resultset.getRowCount(), 5);
   * 
   * Integer rsVal = (Integer) resultset.getValueAt(0, 0);
   * assertTrue("Expected first row to contain a negative number in the first column.", (rsVal.intValue() < 0));
   * //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  /*
   * public void testSQLExecuteForceSingle() { startTest();
   * info("Expected: Successful execution with 1 row of data in the result - expected value in column is 0");
   * //$NON-NLS-1$ IRuntimeContext context = run("test", "rules", "sqlexecute_rule3.xaction"); //$NON-NLS-1$
   * //$NON-NLS-2$ //$NON-NLS-3$ assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("rule-result"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultset = (IPentahoResultSet) rtn.getValue();
   * 
   * assertEquals(resultset.getRowCount(), 1);
   * 
   * Integer rsVal = (Integer) resultset.getValueAt(0, 0);
   * assertTrue("Expected first row to contain a zero first column.", (rsVal.intValue() == 0)); //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  /*
   * public void testSQLExecuteComponentMultipleStatementsNewSeparator() { startTest();
   * info("Expected: Successful execution with 5 rows of data in the result. First row should have a negative #");
   * //$NON-NLS-1$ IRuntimeContext context = run("test", "rules", "sqlexecute_rule4.xaction"); //$NON-NLS-1$
   * //$NON-NLS-2$ //$NON-NLS-3$ assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("rule-result"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultset = (IPentahoResultSet) rtn.getValue();
   * 
   * assertEquals(resultset.getRowCount(), 5);
   * 
   * Integer rsVal = (Integer) resultset.getValueAt(0, 0);
   * assertTrue("Expected first row to contain a negative number in the first column.", (rsVal.intValue() < 0));
   * //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  public void testSQLExecuteErrorDropTable() {
    startTest();
    info( "Expected: Failed execution - should error out at the drop table statement" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/sqlexecute_error1.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
  }

  public void testSQLExecuteErrorSQLWithSemiColons() {
    startTest();
    info( "Expected: Failed execution - should error out because of the semi-colon" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/sqlexecute_error2.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
  }

  public void testSQLExecuteErrorDropTableSingleStatement() {
    startTest();
    info( "Expected: Failed execution - should error out at the drop table statement" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/sqlexecute_error3.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
  }

  public static void main( String[] args ) {
    SQLExecuteComponentTest test = new SQLExecuteComponentTest();
    try {
      test.setUp();
      // test.testSQLExecuteComponentSingleStatement();
      // test.testSQLExecuteComponentMultipleStatements();
      // test.testSQLExecuteForceSingle();
      // test.testSQLExecuteComponentMultipleStatementsNewSeparator();
      test.testSQLExecuteErrorDropTable();
      test.testSQLExecuteErrorSQLWithSemiColons();
      test.testSQLExecuteErrorDropTableSingleStatement();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
