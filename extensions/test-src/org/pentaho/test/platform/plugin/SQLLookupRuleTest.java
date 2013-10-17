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
//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class SQLLookupRuleTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  /*
   * public void testSQLLookupSingleStatement() { startTest(); info("Expected: Successful lookup with one row of data");
   * //$NON-NLS-1$ IRuntimeContext context = run("test", "rules", "query_rule1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("rule-result"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPentahoResultSet resultset = (IPentahoResultSet) rtn.getValue(); assertEquals(resultset.getRowCount(), 1);
   * assertEquals("Expected first row to contain a 1 in the first column.", resultset.getValueAt(0, 0), new Integer(1));
   * //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  public void testDummyTest() {
  }

  public static void main( String[] args ) {
    SQLLookupRuleTest test = new SQLLookupRuleTest();
    try {
      test.setUp();
      // test.testSQLLookupSingleStatement();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
