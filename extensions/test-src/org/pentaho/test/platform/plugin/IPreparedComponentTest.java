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

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.util.HashMap;

/**
 * This class tests SQL, MDX, HQL, and XQuery PreparedComponent functionality.
 * 
 * @author Will Gorman
 * 
 */
@SuppressWarnings( "nls" )
public class IPreparedComponentTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testIPreparedComponentSQLAvailable() {
    startTest();
    info( "Expected: Successful execution with object available" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/ipreparedcomponents/ipreparedcomponent_sql_available.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter( "prepared_component" ); //$NON-NLS-1$
    assertNotNull( rtn );
    IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();

    assertNotNull( preparedComponent );

    finishTest();
  }

  /*
   * public void testIPreparedComponentSQLTempTables() { startTest();
   * info("Expected: Successful execution with one row of data"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_sql_temptables.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn1 = context.getOutputParameter("a_result"); //$NON-NLS-1$ assertNotNull(rtn1); IPentahoResultSet
   * resultset1 = (IPentahoResultSet) rtn1.getValue(); assertEquals(1, resultset1.getRowCount()); assertEquals(
   * "Expected first row of 'a_result' to contain a 1 in the first column.", new Integer(1), resultset1.getValueAt(0,
   * 0)); //$NON-NLS-1$
   * 
   * IActionParameter rtn2 = context.getOutputParameter("no_results"); //$NON-NLS-1$ assertNotNull(rtn2);
   * IPentahoResultSet resultset2 = (IPentahoResultSet) rtn2.getValue(); assertEquals(1, resultset2.getRowCount());
   * assertEquals( "Expected first row of 'no_results' to contain a 0 in the first column.", new Integer(0),
   * resultset2.getValueAt(0, 0)); //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  /*
   * public void testIPreparedComponentSQLPrepareLater() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_sql_preparelater.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn1 = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn1);
   * IPreparedComponent preparedComponent1 = (IPreparedComponent) rtn1.getValue(); assertNotNull(preparedComponent1);
   * IPentahoResultSet resultset1 = preparedComponent1.executePrepared(null); assertEquals(1, resultset1.getRowCount());
   * Number val1 = (Number) resultset1.getValueAt(0, 0);
   * 
   * IActionParameter rtn2 = context.getOutputParameter("second_prepared_component"); //$NON-NLS-1$ assertNotNull(rtn2);
   * IPreparedComponent preparedComponent2 = (IPreparedComponent) rtn2.getValue(); assertNotNull(preparedComponent2);
   * HashMap map = new HashMap(); map.put("DEPARTMENT", "Sales"); //$NON-NLS-1$ //$NON-NLS-2$ map.put("REGION",
   * "Eastern"); //$NON-NLS-1$ //$NON-NLS-2$ IPentahoResultSet resultset2 = preparedComponent2.executePrepared(map);
   * assertEquals(1, resultset2.getRowCount()); Number val2 = (Number) resultset2.getValueAt(0, 0);
   * 
   * assertEquals("Values from the first and second query should be equal", val1, val2); //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  /*
   * public void testIPreparedComponentMDXAvailable() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_mdx_available.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();
   * 
   * assertNotNull(preparedComponent);
   * 
   * finishTest(); }
   */

  /*
   * public void testIPreparedComponentMDXShareConnection() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_mdx_shareconn.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();
   * 
   * assertNotNull(preparedComponent);
   * 
   * IPentahoResultSet resultset = preparedComponent.executePrepared(null); assertNotNull(resultset);
   * assertTrue(resultset.getRowCount() >= 1); Object val1 = resultset.getValueAt(0, 0); assertNotNull(val1);
   * 
   * finishTest(); }
   */

  /*
   * public void testIPreparedComponentMDXPrepareLater() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_mdx_preparelater.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn1 = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn1);
   * IPreparedComponent preparedComponent1 = (IPreparedComponent) rtn1.getValue(); assertNotNull(preparedComponent1);
   * IPentahoResultSet resultset1 = preparedComponent1.executePrepared(null); assertNotNull(resultset1);
   * assertTrue(resultset1.getRowCount() >= 1); Object val1 = resultset1.getValueAt(0, 0);
   * 
   * IActionParameter rtn2 = context.getOutputParameter("second_prepared_component"); //$NON-NLS-1$ assertNotNull(rtn2);
   * IPreparedComponent preparedComponent2 = (IPreparedComponent) rtn2.getValue(); assertNotNull(preparedComponent2);
   * HashMap map = new HashMap(); map.put("productline", "Classic Cars"); //$NON-NLS-1$ //$NON-NLS-2$ IPentahoResultSet
   * resultset2 = preparedComponent2.executePrepared(map); assertTrue(resultset2.getRowCount() >= 1);
   * assertEquals(resultset1.getRowCount(), resultset2.getRowCount());
   * 
   * Object val2 = resultset2.getValueAt(0, 0);
   * 
   * assertEquals("Values from the first and second query should be equal", val1, val2); //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  public void testIPreparedComponentXQueryAvailable() {
    startTest();
    info( "Expected: Successful execution with object available" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/ipreparedcomponents/ipreparedcomponent_xquery_available.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter( "prepared_component" ); //$NON-NLS-1$
    assertNotNull( rtn );
    IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();

    assertNotNull( preparedComponent );

    finishTest();
  }

  public void testIPreparedComponentXQueryAvailableErrorNoDocument() {
    startTest();
    IRuntimeContext context =
        run( "/test/ipreparedcomponents/ipreparedcomponent_xquery_available_error_nodocument.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn = context.getOutputParameter( "prepared_component" ); //$NON-NLS-1$
    assertNotNull( rtn );
    IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();

    assertNull( preparedComponent );

    finishTest();
  }

  public void testIPreparedComponentXQueryAvailableErrorNoPrepareComponent() {
    startTest();
    info( "Expected: Successful execution with object available" ); //$NON-NLS-1$
    IRuntimeContext context =
        run( "/test/ipreparedcomponents/ipreparedcomponent_xquery_available_error_nopreparedcomponent.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus() ); //$NON-NLS-1$

    finishTest();
  }

  public void testIPreparedComponentXQueryPrepareLater() {
    startTest();
    info( "Expected: Successful execution with object available" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/ipreparedcomponents/ipreparedcomponent_xquery_preparelater.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn1 = context.getOutputParameter( "prepared_component" ); //$NON-NLS-1$
    assertNotNull( rtn1 );
    IPreparedComponent preparedComponent1 = (IPreparedComponent) rtn1.getValue();
    assertNotNull( preparedComponent1 );
    IPentahoResultSet resultset1 = preparedComponent1.executePrepared( null );
    assertTrue( resultset1.getRowCount() >= 1 );
    Object val1 = resultset1.getValueAt( 0, 0 );

    IActionParameter rtn2 = context.getOutputParameter( "second_prepared_component" ); //$NON-NLS-1$
    assertNotNull( rtn2 );
    IPreparedComponent preparedComponent2 = (IPreparedComponent) rtn2.getValue();
    assertNotNull( preparedComponent2 );
    HashMap map = new HashMap();
    map.put( "POSITIONTITLE", "Engineer" ); //$NON-NLS-1$ //$NON-NLS-2$
    IPentahoResultSet resultset2 = preparedComponent2.executePrepared( map );
    assertTrue( resultset2.getRowCount() >= 1 );
    assertEquals( resultset1.getRowCount(), resultset2.getRowCount() );

    Object val2 = resultset2.getValueAt( 0, 0 );

    assertEquals( "Values from the first and second query should be equal", val1, val2 ); //$NON-NLS-1$

    finishTest();
  }

  public void testIPreparedComponentXQueryShareConnection() {
    startTest();
    info( "Expected: Successful execution with object available" ); //$NON-NLS-1$
    IRuntimeContext context = run( "/test/ipreparedcomponents/ipreparedcomponent_xquery_shareconn.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    IActionParameter rtn1 = context.getOutputParameter( "prepared_component" ); //$NON-NLS-1$
    assertNotNull( rtn1 );
    IPreparedComponent preparedComponent1 = (IPreparedComponent) rtn1.getValue();
    assertNotNull( preparedComponent1 );
    IPentahoResultSet resultset1 = preparedComponent1.executePrepared( null );
    assertTrue( resultset1.getRowCount() >= 1 );
    Object val1 = resultset1.getValueAt( 0, 0 );
    assertNotNull( val1 );

    finishTest();
  }

  /*
   * public void testIPreparedComponentHQLAvailable() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_hql_available.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn);
   * IPreparedComponent preparedComponent = (IPreparedComponent) rtn.getValue();
   * 
   * assertNotNull(preparedComponent);
   * 
   * finishTest(); }
   * 
   * public void testIPreparedComponentHQLPrepareLater() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_hql_preparelater.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn1 = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn1);
   * IPreparedComponent preparedComponent1 = (IPreparedComponent) rtn1.getValue(); assertNotNull(preparedComponent1);
   * IPentahoResultSet resultset1 = preparedComponent1.executePrepared(null); assertTrue(resultset1.getRowCount() >= 1);
   * Object val1 = resultset1.getValueAt(0, 0);
   * 
   * IActionParameter rtn2 = context.getOutputParameter("second_prepared_component"); //$NON-NLS-1$ assertNotNull(rtn2);
   * IPreparedComponent preparedComponent2 = (IPreparedComponent) rtn2.getValue(); assertNotNull(preparedComponent2);
   * HashMap map = new HashMap(); map.put("mimetypeval", "text/html"); //$NON-NLS-1$ //$NON-NLS-2$ IPentahoResultSet
   * resultset2 = preparedComponent2.executePrepared(map); assertTrue(resultset2.getRowCount() >= 1);
   * assertEquals(resultset1.getRowCount(), resultset2.getRowCount());
   * 
   * Object val2 = resultset2.getValueAt(0, 0);
   * 
   * assertEquals("Values from the first and second query should be equal", val1, val2); //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  /*
   * public void testIPreparedComponentHQLShareConnection() { startTest();
   * info("Expected: Successful execution with object available"); //$NON-NLS-1$ IRuntimeContext context = run("test",
   * "ipreparedcomponents", "ipreparedcomponent_hql_shareconn.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
   * 
   * IActionParameter rtn1 = context.getOutputParameter("prepared_component"); //$NON-NLS-1$ assertNotNull(rtn1);
   * IPreparedComponent preparedComponent1 = (IPreparedComponent) rtn1.getValue(); assertNotNull(preparedComponent1);
   * IPentahoResultSet resultset1 = preparedComponent1.executePrepared(null); assertTrue(resultset1.getRowCount() >= 1);
   * Object val1 = resultset1.getValueAt(0, 0);
   * 
   * assertNotNull(val1);
   * 
   * finishTest(); }
   */

  public static void main( String[] args ) {
    IPreparedComponentTest test = new IPreparedComponentTest();
    try {
      test.setUp();
      // test.testIPreparedComponentSQLTempTables();
      // test.testIPreparedComponentSQLAvailable();
      // test.testIPreparedComponentSQLPrepareLater();
      // test.testIPreparedComponentMDXAvailable();
      // test.testIPreparedComponentMDXPrepareLater();
      // test.testIPreparedComponentMDXShareConnection();
      test.testIPreparedComponentXQueryAvailable();
      test.testIPreparedComponentXQueryAvailableErrorNoDocument();
      test.testIPreparedComponentXQueryAvailableErrorNoPrepareComponent();
      test.testIPreparedComponentXQueryPrepareLater();
      test.testIPreparedComponentXQueryShareConnection();
      // test.testIPreparedComponentHQLAvailable();
      // test.testIPreparedComponentHQLPrepareLater();
      // test.testIPreparedComponentHQLShareConnection();

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
