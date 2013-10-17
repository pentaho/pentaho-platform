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

//import org.pentaho.commons.connection.IPentahoMetaData;
//import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( "nls" )
public class RulesTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testScriptRule() {
    startTest();

    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "customer", "Acme" ); //$NON-NLS-1$ //$NON-NLS-2$

    IRuntimeContext context = run( "/test/rules/script_rule1.xaction", parameterProvider ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    assertNotNull(
        Messages.getInstance().getString( "RulesTest.ERROR_0001_NULL_RESULT" ), context.getOutputParameter( "rule-result" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals(
        Messages.getInstance().getString( "RulesTest.ERROR_0002_WRONG_RESULT" ), "Central", context.getOutputParameter( "rule-result" ).getStringValue() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    info( Messages.getInstance().getString(
        "RulesTest.DEBUG_0003_SCRIPT_RULE_SUCCESS", context.getOutputParameter( "rule-result" ).getStringValue() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    finishTest();
  }

  public void testScriptRuleError1() {
    startTest();
    info( Messages.getInstance().getString( "RulesTest.USER_ERRORS_EXPECTED_SCRIPT_NOT_DEFINED" ) ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/script_rule_error1.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$

    assertNotNull(
        Messages.getInstance().getString( "RulesTest.ERROR_0004_NULL_OUTPUT_OBJECT" ), context.getOutputParameter( "rule-result" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertEquals(
        Messages.getInstance().getString( "RulesTest.ERROR_0005_RESULT_WHEN_NULL_EXPECTED" ), null, context.getOutputParameter( "rule-result" ).getStringValue() ); //$NON-NLS-1$//$NON-NLS-2$
    finishTest();
  }

  public void testScriptRuleError2() {
    startTest();
    info( Messages.getInstance().getString( "RulesTest.USER_ERRORS_EXPECTED_OUTPUT_NOT_DEFINED" ) ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/script_rule_error2.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$

    assertEquals(
        Messages.getInstance().getString( "RulesTest.ERROR_0006_RESULT_WHEN_ERROR_EXPECTED" ), false, context.getOutputNames().contains( "rule-result" ) ); //$NON-NLS-1$//$NON-NLS-2$
    finishTest();
  }

  public void testScriptRuleError3() {
    startTest();
    info( Messages.getInstance().getString( "RulesTest.USER_ERRORS_EXPECTED_SCRIPT_INVALID" ) ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/script_rule_error3.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$

    assertEquals(
        Messages.getInstance().getString( "RulesTest.ERROR_0005_RESULT_WHEN_NULL_EXPECTED" ), null, context.getOutputParameter( "rule-result" ).getStringValue() ); //$NON-NLS-1$//$NON-NLS-2$
    finishTest();
  }

  public void testScriptRuleError4() {
    startTest();

    info( "This should generate errors because the input to the script has a minus sign in the input name." ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/script_rule_error4.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    assertEquals(
        Messages.getInstance().getString( "RulesTest.ERROR_0005_RESULT_WHEN_NULL_EXPECTED" ), null, context.getOutputParameter( "rule-result" ).getStringValue() ); //$NON-NLS-1$//$NON-NLS-2$
    finishTest();
  }

  public void testScriptCompoundResult() {
    startTest();
    info( Messages.getInstance().getString( "RulesTest.USER_ERRORS_EXPECTED_SCRIPT_INVALID" ) ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/rules/script_rule3.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    assertNotNull( context.getOutputParameter( "fruit" ) ); //$NON-NLS-1$
    assertNotNull( context.getOutputParameter( "veg" ) ); //$NON-NLS-1$
    assertEquals( "bad", "apples", context.getOutputParameter( "fruit" ).getStringValue() ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( "bad", "carrots", context.getOutputParameter( "veg" ).getStringValue() ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    finishTest();
  }

  /*
   * public void testQueryRule() { startTest();
   * 
   * IPentahoResultSet resultSet = null; try { IRuntimeContext context = run("test", "rules", "query_rule1.xaction");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$
   * 
   * Object result = context.getOutputParameter("rule-result").getValue(); //$NON-NLS-1$
   * assertNotNull(Messages.getInstance().getString("RulesTest.ERROR_0001_NULL_RESULT"), result); //$NON-NLS-1$
   * 
   * assertTrue( Messages.getInstance().getString("RulesTest.ERROR_0007_LOOKUP_RULE_INVALID_RESULT"), (result instanceof
   * IPentahoResultSet)); //$NON-NLS-1$
   * 
   * resultSet = (IPentahoResultSet) result; IPentahoMetaData metaData = resultSet.getMetaData(); Object
   * columnHeaders[][] = metaData.getColumnHeaders();
   * 
   * String columnHeader = columnHeaders[0][0].toString();
   * 
   * assertEquals(Messages.getInstance().getString("RulesTest.ERROR_0009_LOOKUP_RULE_COLUMN_MISSING"), "POSITIONTITLE",
   * columnHeader); //$NON-NLS-1$ //$NON-NLS-2$
   * 
   * Object row[] = resultSet.next();
   * assertNotNull(Messages.getInstance().getString("RulesTest.ERROR_0007_LOOKUP_RULE_INVALID_RESULT"), row);
   * //$NON-NLS-1$
   * 
   * info(Messages.getInstance().getString("RulesTest.DEBUG_LOOKUP_RULE_SUCCESS", row[0].toString())); //$NON-NLS-1$
   * 
   * } finally { if (resultSet != null) { resultSet.closeConnection(); } }
   * 
   * finishTest(); }
   */

  /*
   * public void testQueryRule2() { startTest();
   * 
   * IPentahoResultSet resultSet = null; try { IRuntimeContext context = run("test", "rules", "query_rule2.xaction");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$
   * 
   * Object result = context.getOutputParameter("rule-result").getValue(); //$NON-NLS-1$
   * assertNotNull(Messages.getInstance().getString("RulesTest.ERROR_0001_NULL_RESULT"), result); //$NON-NLS-1$
   * 
   * assertTrue( Messages.getInstance().getString("RulesTest.ERROR_0007_LOOKUP_RULE_INVALID_RESULT"), (result instanceof
   * IPentahoResultSet)); //$NON-NLS-1$
   * 
   * resultSet = (IPentahoResultSet) result; IPentahoMetaData metaData = resultSet.getMetaData(); Object
   * columnHeaders[][] = metaData.getColumnHeaders();
   * 
   * String regionHeader = columnHeaders[0][0].toString(); String departmentHeader = columnHeaders[0][1].toString();
   * 
   * assertEquals(Messages.getInstance().getString("RulesTest.ERROR_0009_LOOKUP_RULE_COLUMN_MISSING"), "REGION",
   * regionHeader); //$NON-NLS-1$ //$NON-NLS-2$ assertEquals(
   * Messages.getInstance().getString("RulesTest.ERROR_0009_LOOKUP_RULE_COLUMN_MISSING"), "DEPARTMENT",
   * departmentHeader); //$NON-NLS-1$ //$NON-NLS-2$
   * 
   * Object row[] = resultSet.next(); while (row != null) {
   * 
   * String region = row[0].toString(); String department = row[1].toString();
   * assertNotNull(Messages.getInstance().getString("RulesTest.ERROR_0009_LOOKUP_RULE_COLUMN_MISSING"), region);
   * //$NON-NLS-1$ assertNotNull(Messages.getInstance().getString("RulesTest.ERROR_0009_LOOKUP_RULE_COLUMN_MISSING"),
   * department); //$NON-NLS-1$
   * 
   * info(region + ", " + department); //$NON-NLS-1$ row = resultSet.next(); }
   * 
   * } finally { if (resultSet != null) { resultSet.closeConnection(); } }
   * 
   * finishTest(); }
   */

  public static void main( String[] args ) {
    RulesTest test = new RulesTest();
    test.setUp();
    try {
      test.testScriptRule();
      test.testScriptRuleError1();
      test.testScriptRuleError2();
      test.testScriptRuleError3();
      test.testScriptRuleError4();
      test.testScriptCompoundResult();
      // test.testQueryRule();
      // test.testQueryRule2();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }

  }

}
