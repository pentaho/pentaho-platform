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


package org.pentaho.test.platform.plugin;

//import org.pentaho.commons.connection.IPentahoResultSet;
//import org.pentaho.platform.api.engine.IActionParameter;
//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

@SuppressWarnings( "nls" )
public class SQLLookupRuleIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

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
    SQLLookupRuleIT test = new SQLLookupRuleIT();
    try {
      test.setUp();
      // test.testSQLLookupSingleStatement();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
