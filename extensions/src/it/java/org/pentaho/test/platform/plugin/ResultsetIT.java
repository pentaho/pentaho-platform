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

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

@SuppressWarnings( "nls" )
public class ResultsetIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testResultSet() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetTest.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testJavaScriptResultSet() {
    startTest();
    IRuntimeContext context = run( "/test/rules/JavaScriptResultSetTest.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testResultSetWithoutColumnHeader() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetTest_without_columnheader.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testResultSetWithoutRowValue() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetTest_without_rowvalue.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testResultSet_NullColumnHeader() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetTest_NullColumnHeader.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testResultSetTestWithDifferentDataTypes() {
    startTest();
    IRuntimeContext context = run( "/test/rules/ResultSetTestWithDifferentDataTypes.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public static void main( String[] args ) {
    ResultsetIT test = new ResultsetIT();
    try {
      test.setUp();
      test.testResultSet();
      test.testJavaScriptResultSet();
      test.testResultSetWithoutColumnHeader();
      test.testResultSetWithoutRowValue();
      test.testResultSet_NullColumnHeader();
      test.testResultSetTestWithDifferentDataTypes();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
