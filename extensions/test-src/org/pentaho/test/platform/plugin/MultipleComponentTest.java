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

import org.pentaho.commons.connection.DataUtilities;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.InvalidParameterException;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.api.util.PentahoChainedException;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.runtime.SimpleRuntimeElement;
import org.pentaho.platform.plugin.services.connections.sql.SQLResultSetException;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.SimpleRuntimeRepository;
import org.pentaho.platform.util.FileHelper;
import org.pentaho.platform.util.JVMParameterProvider;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import java.text.SimpleDateFormat;
//import org.pentaho.platform.api.engine.IActionParameter;

@SuppressWarnings( "nls" )
public class MultipleComponentTest extends BaseTest {

  /**
   * This Test Case runs multiple small action sequences which improve test coverage.
   */

  private static final String HW_TEST_NAME = "MultipleComponentTest_HelloWorld_"; //$NON-NLS-1$

  private static final String HW_TEST_EXTN = ".txt"; //$NON-NLS-1$

  private static final String RC_TEST_NAME = "MultipleComponentTest_ReportCharts_"; //$NON-NLS-1$

  //  private static final String RC_TEST_EXTN = ".html"; //$NON-NLS-1$
  //
  //  private static final String DS_TEST_NAME = "MultipleComponentTest_DynamicSQL_"; //$NON-NLS-1$
  //
  //  private static final String DS_TEST_EXTN = ".html"; //$NON-NLS-1$

  private ByteArrayOutputStream lastStream;

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "jfree-report", "jfree-report" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  public void startTest() {
    super.startTest();
  }

  public void finishTest() {
    super.finishTest();
  }

  public String getBaseUrl() {
    return "http://localhost:9876/pentaho/"; //$NON-NLS-1$
  }

  protected OutputStream getOutputStream( String testName, String extension ) {
    //
    // The reportchart reports are huge (>500k). So, for these, write the
    // output to a file (in test/tmp). Otherwise, use a ByteArrayOutputStream
    //
    if ( testName.indexOf( RC_TEST_NAME ) >= 0 ) {
      return super.getOutputStream( testName, extension );
    } else {
      lastStream = new ByteArrayOutputStream();
      return lastStream;
    }
  }

  protected InputStream getInputStreamFromOutput( String testName, String extension ) {

    //
    // Either read from the file for the report charts, or just
    // return the byte array output stream wrapped as an input stream.
    //
    if ( testName.indexOf( RC_TEST_NAME ) >= 0 ) {
      return super.getInputStreamFromOutput( testName, extension );
    } else {
      return new ByteArrayInputStream( lastStream.toByteArray() );
    }
  }

  public void setUp() {
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
  }

  public IOutputHandler getOutputHandler( OutputStream stream ) {
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( stream, true ); // Allow feedback for this test
    outputHandler.setOutputPreference( IOutputHandler.OUTPUT_TYPE_DEFAULT );
    return outputHandler;
  }

  private IPentahoResultSet getResultSet() {
    Object[][] columnHeaders = new Object[][] { { "COL1", "COL2", "COL3" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    IPentahoMetaData md = new MemoryMetaData( columnHeaders, null );
    MemoryResultSet rs = new MemoryResultSet( md );
    rs.addRow( new Object[] { "DATA1", "DATA2", new Date() } ); //$NON-NLS-1$ //$NON-NLS-2$
    rs.isScrollable();
    return rs;
  }

  private IPentahoResultSet getResultSetFromArray() {
    Object[][] columnHeaders = new Object[][] { { "COL1", "COL2", "COL3" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Object[][] row = new Object[][] { { "VAL1", "VAL2", "VAL3" }, { "VAL4", "VAL5", "VAL6" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    return MemoryResultSet.createFromArrays( columnHeaders, row );
  }

  @SuppressWarnings( "unused" )
  private IPentahoResultSet getResultSetFromList() {
    List columnHeadersList = new ArrayList();
    columnHeadersList.add( "COL1" ); //$NON-NLS-1$
    columnHeadersList.add( "COL2" ); //$NON-NLS-1$
    columnHeadersList.add( "COL3" ); //$NON-NLS-1$
    List rowList = new ArrayList();
    rowList.add( "VAL1" ); //$NON-NLS-1$
    rowList.add( "VAL2" ); //$NON-NLS-1$
    rowList.add( "VAL3" ); //$NON-NLS-1$

    return MemoryResultSet.createFromLists( columnHeadersList, rowList );
  }

  private IPentahoResultSet getResultSet2() {
    Object[][] columnHeaders = new Object[][] { { "COL1", "COL2", "COL3" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    List columnHeadersList = new ArrayList();
    columnHeadersList.add( "COL1" ); //$NON-NLS-1$
    columnHeadersList.add( "COL2" ); //$NON-NLS-1$
    columnHeadersList.add( "COL3" ); //$NON-NLS-1$
    MemoryMetaData md2 = new MemoryMetaData( columnHeaders, null );
    //    emd.getAttribute(1, 1, "COL1"); //$NON-NLS-1$
    // emd.setBusinessColumns(columnHeadersList);
    MemoryResultSet rs = new MemoryResultSet( md2 );
    rs.addRow( new Object[] { "DATA1", "DATA2", new Date() } ); //$NON-NLS-1$ //$NON-NLS-2$
    return rs;
  }

  private IPentahoResultSet getResultSet3() {
    List columnHeadersList = new ArrayList();
    columnHeadersList.add( "COL1" ); //$NON-NLS-1$
    columnHeadersList.add( "COL2" ); //$NON-NLS-1$
    columnHeadersList.add( "COL3" ); //$NON-NLS-1$
    MemoryMetaData md3 = new MemoryMetaData( columnHeadersList );
    MemoryResultSet rs = new MemoryResultSet( md3 );
    rs.addRow( new Object[] { "DATA1", "DATA2", new Date() } ); //$NON-NLS-1$ //$NON-NLS-2$
    return rs;
  }

  public void testHelloWorldComponent() {
    startTest();
    String testName = HW_TEST_NAME + System.currentTimeMillis();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    IRuntimeContext context = run( "/test/platform/HelloWorld.xaction", parameterProvider, testName, HW_TEST_EXTN ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$

    InputStream is = this.getInputStreamFromOutput( testName, HW_TEST_EXTN );
    assertNotNull( is ); // Did the test execute properly...
    String lookingFor = "\nHello World. (2B || !2B) That is the question\n"; //$NON-NLS-1$ //$NON-NLS-2$
    String wasRead = FileHelper.getStringFromInputStream( is );
    assertEquals( wasRead, lookingFor );
    finishTest();
  }

  /*
   * public void testCrosstab() { startTest(); IRuntimeContext context = run("test", "rules", "CrossTabTest.xaction");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ IActionParameter rtn = context.getOutputParameter("rule-result"); //$NON-NLS-1$
   * assertNotNull(rtn); IPentahoResultSet resultset = (IPentahoResultSet) rtn.getValue();
   * assertEquals(resultset.getRowCount(), 7); Object[][] colHeaders = resultset.getMetaData().getColumnHeaders();
   * assertEquals(colHeaders[0][0], "DEPARTMENT"); //$NON-NLS-1$ assertEquals(colHeaders[0][1], "Central"); //$NON-NLS-1$
   * assertEquals(colHeaders[0][2], "Western"); //$NON-NLS-1$ assertEquals(colHeaders[0][3], "Southern"); //$NON-NLS-1$
   * assertEquals(colHeaders[0][4], "Eastern"); //$NON-NLS-1$
   * 
   * finishTest(); }
   */

  public void testMiscExceptionClasses() {
    // Making sure we create an instance of all the Pentaho
    // Exception classes.
    startTest();
    Exception ex = new RepositoryException();
    Exception ex2 = new RepositoryException( "Ignored" ); //$NON-NLS-1$
    Exception ex3 = new RepositoryException( ex2 );
    Exception ex4 = new RepositoryException( "Ignored", ex3 ); //$NON-NLS-1$
    Exception ex5 = new AuditException();
    Exception ex6 = new AuditException( "Ignored" ); //$NON-NLS-1$
    Exception ex7 = new AuditException( "Ignored", ex ); //$NON-NLS-1$
    Exception ex8 = new AuditException( ex4 );
    ex = new ContentException( "Ignored" ); //$NON-NLS-1$
    ex = new ContentException( "Ignored", ex5 ); //$NON-NLS-1$
    ex = new ContentException( ex6 );
    ex = new InvalidParameterException();
    ex = new SQLResultSetException();
    ex = new PentahoChainedException();
    ex = new PentahoChainedException( "Ignored" ); //$NON-NLS-1$
    ex = new PentahoChainedException( "Ignored", ex7 ); //$NON-NLS-1$
    ex = new PentahoChainedException( ex8 );
    finishTest();
  }

  /*
   * public void testReportCharts() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("chart_type", "multipie"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("output-type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ long curTime =
   * System.currentTimeMillis(); String testName = RC_TEST_NAME + "multipie_" + curTime; //$NON-NLS-1$ IRuntimeContext
   * context = run( "test", "reporting/JFreeReportChartTypes", "JFreeReport_Chart_ChartTypes.xaction",
   * parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ InputStream is = this.getInputStreamFromOutput(testName, RC_TEST_EXTN);
   * assertNotNull(is); try { is.close(); } catch (Exception ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "line"); //$NON-NLS-1$ //$NON-NLS-2$ testName = RC_TEST_NAME + "line_"
   * + curTime; //$NON-NLS-1$ context = run( "test", "reporting/JFreeReportChartTypes",
   * "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ is =
   * this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); try { is.close(); } catch (Exception
   * ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "area"); //$NON-NLS-1$ //$NON-NLS-2$ testName = RC_TEST_NAME + "area_"
   * + curTime; //$NON-NLS-1$ context = run( "test", "reporting/JFreeReportChartTypes",
   * "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ is =
   * this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); try { is.close(); } catch (Exception
   * ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "stackedarea"); //$NON-NLS-1$ //$NON-NLS-2$ testName = RC_TEST_NAME +
   * "stackedarea_" + curTime; //$NON-NLS-1$ context = run( "test", "reporting/JFreeReportChartTypes",
   * "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ is =
   * this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); try { is.close(); } catch (Exception
   * ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "bar"); //$NON-NLS-1$ //$NON-NLS-2$ testName = RC_TEST_NAME + "bar_" +
   * curTime; //$NON-NLS-1$ context = run( "test", "reporting/JFreeReportChartTypes",
   * "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ is =
   * this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); try { is.close(); } catch (Exception
   * ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "stackedbar"); //$NON-NLS-1$ //$NON-NLS-2$ testName = RC_TEST_NAME +
   * "stackedbar_" + curTime; //$NON-NLS-1$ context = run( "test", "reporting/JFreeReportChartTypes",
   * "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ is =
   * this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); try { is.close(); } catch (Exception
   * ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "stackedbarpercentages"); //$NON-NLS-1$ //$NON-NLS-2$ testName =
   * RC_TEST_NAME + "stackedbarpercentages_" + curTime; //$NON-NLS-1$ context = run( "test",
   * "reporting/JFreeReportChartTypes", "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName,
   * RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ is = this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is);
   * try { is.close(); } catch (Exception ignored) { }
   * 
   * parameterProvider.setParameter("chart_type", "pie"); //$NON-NLS-1$ //$NON-NLS-2$ testName = RC_TEST_NAME + "pie_" +
   * curTime; //$NON-NLS-1$ context = run( "test", "reporting/JFreeReportChartTypes",
   * "JFreeReport_Chart_ChartTypes.xaction", parameterProvider, testName, RC_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ is =
   * this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); try { is.close(); } catch (Exception
   * ignored) { }
   * 
   * finishTest(); }
   */

  /*
   * public void testDynamicSQLAndSecureFilter() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); // Add no parameters yet for first run String testName = DS_TEST_NAME +
   * "parameterResponse_" + System.currentTimeMillis(); //$NON-NLS-1$ IRuntimeContext context = run( "test",
   * "reporting", "DynamicSQLSample.xaction", parameterProvider, testName, DS_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$
   * //$NON-NLS-3$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // Should have created a Feedback html
   * page in the output. InputStream is = this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is);
   * String feedbackHTML = FileHelper.getStringFromInputStream(is); assertNotNull(feedbackHTML); // Make sure it's
   * feedback HTML we have...
   * assertTrue(feedbackHTML.indexOf("Use this form to supply the parameters required for this content") > 0);
   * //$NON-NLS-1$ assertTrue(feedbackHTML.indexOf("Select: Department or Position") > 0); //$NON-NLS-1$ // OK - now,
   * supply all the parameters and re-run. parameterProvider.setParameter("deptorposn", "department"); //$NON-NLS-1$
   * //$NON-NLS-2$ parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("region", "Central"); //$NON-NLS-1$ //$NON-NLS-2$ testName = DS_TEST_NAME + "report_"
   * + System.currentTimeMillis(); //$NON-NLS-1$ context = run("test", "reporting", "DynamicSQLSample.xaction",
   * parameterProvider, testName, DS_TEST_EXTN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * assertEquals(context.getStatus(), IRuntimeContext.RUNTIME_STATUS_SUCCESS); // Should have created a report html
   * page in the output. is = this.getInputStreamFromOutput(testName, RC_TEST_EXTN); assertNotNull(is); String
   * reportHTML = FileHelper.getStringFromInputStream(is); assertNotNull(reportHTML); // Insure it has information
   * assertTrue(reportHTML.indexOf("Professional Services") > 0); //$NON-NLS-1$
   * assertTrue(reportHTML.indexOf("20,068,039") > 0); //$NON-NLS-1$ finishTest(); }
   */

  public void testSimpleRuntime() {
    // The simple runtime is used by the standalone distribution.
    // This provides some simple tests to exercise the code...
    startTest();
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    IRuntimeRepository srr = new SimpleRuntimeRepository();
    srr.setSession( session );
    IRuntimeElement ele1 = srr.loadElementById( "instanceid", null ); //$NON-NLS-1$
    IRuntimeElement ele2 = srr.newRuntimeElement( "parent", "parentType", true ); //$NON-NLS-1$ //$NON-NLS-2$
    IRuntimeElement ele3 = srr.newRuntimeElement( "parentid", "parentType", "solutionId", true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    SimpleRuntimeElement sre = (SimpleRuntimeElement) srr.loadElementById( "instanceid", null ); //$NON-NLS-1$
    List list = sre.getMessages();
    if ( list != null ) {
      for ( int i = 0; i < list.size(); i++ ) {
        System.out.println( "Message " + ( i + 1 ) + list.get( i ) ); //$NON-NLS-1$
      }

      sre.setParentId( "parentid1" ); //$NON-NLS-1$
      sre.setParentType( "parentidType1" ); //$NON-NLS-1$
      sre.setSolutionId( "solutionId1" ); //$NON-NLS-1$
      sre.setReadOnly( true );
      boolean isReadOnly = sre.getReadOnly();
      String parentId = sre.getParentId();
      String parentIdType = sre.getParentType();
      String solutionId = sre.getSolutionId();
      assertEquals( isReadOnly, true );
      assertEquals( parentId, "parentid1" ); //$NON-NLS-1$
      assertEquals( parentIdType, "parentidType1" ); //$NON-NLS-1$
      assertEquals( solutionId, "solutionId1" ); //$NON-NLS-1$
    }
    int revision = sre.getRevision();
    System.out.println( "Revision Value is" + revision ); //$NON-NLS-1$
    sre.setStringProperty( "parentId", "value" ); //$NON-NLS-1$ //$NON-NLS-2$
    BigDecimal tstBD = new BigDecimal( "2.4" ); //$NON-NLS-1$
    ele2.setBigDecimalProperty( "junkBD", tstBD ); //$NON-NLS-1$
    BigDecimal bd = ele2.getBigDecimalProperty( "junkBD" ); //$NON-NLS-1$
    assertEquals( bd, tstBD );
    Date tstDT = new Date();
    ele3.setDateProperty( "junkDT", tstDT ); //$NON-NLS-1$
    Date dt = ele3.getDateProperty( "junkDT" ); //$NON-NLS-1$
    assertEquals( tstDT, dt );
    List l = new ArrayList();
    l.add( "one" ); //$NON-NLS-1$
    l.add( "two" ); //$NON-NLS-1$
    ele1.setListProperty( "SOMELIST", l ); //$NON-NLS-1$
    assertEquals( ele1.getListProperty( "SOMELIST" ), l ); //$NON-NLS-1$
    Long aLong = new Long( 5 );
    ele2.setLongProperty( "SOMELONG", aLong ); //$NON-NLS-1$
    assertEquals( ele2.getLongProperty( "SOMELONG", null ), aLong ); //$NON-NLS-1$
    assertEquals( ele2.getLongProperty( "SOMELONG", 5 ), 5 ); //$NON-NLS-1$
    Map mapProperty = new HashMap();
    mapProperty.put( "mapKey", "mapValue" ); //$NON-NLS-1$ //$NON-NLS-2$
    ele3.setMapProperty( "SOMEMAP", mapProperty ); //$NON-NLS-1$
    assertEquals( ele3.getMapProperty( "SOMEMAP" ), mapProperty ); //$NON-NLS-1$
    ele1.setStringProperty( "SOMESTRING", "SomeStringValue" ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( ele1.getStringProperty( "SOMESTRING" ), "SomeStringValue" ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( ele2.getStringProperty( "DOESNTEXIST", "SomeDefault" ), "SomeDefault" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    assertNull( ele1.getStringProperty( "DOESNTEXIST" ) ); //$NON-NLS-1$    
    assertNull( ele1.getBigDecimalProperty( "DOESNTEXIST" ) ); //$NON-NLS-1$
    assertNull( ele2.getDateProperty( "DOESNTEXIST" ) ); //$NON-NLS-1$
    assertNull( ele3.getListProperty( "DOESNTEXIST" ) ); //$NON-NLS-1$
    assertEquals( ele2.getLongProperty( "DOESNTEXIST", 2 ), 2 ); //$NON-NLS-1$
    assertEquals( ele2.getLongProperty( "DOESNTEXIST", new Long( 2 ) ), new Long( 2 ) ); //$NON-NLS-1$
    assertEquals( ele1.getBigDecimalProperty( "DOESNTEXIST", new BigDecimal( "0.23" ) ), new BigDecimal( "0.23" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Date tmpDate = new Date();
    assertEquals( ele1.getDateProperty( "DOESNTEXIST", tmpDate ), tmpDate ); //$NON-NLS-1$
    Set parmNames = ele1.getParameterNames();
    assertNotNull( parmNames );

    assertFalse( srr.usesHibernate() );

    assertEquals( ele2.getParameterType( "SOMELONG" ), "java.lang.Long" ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( ele2.getParentType(), "parentType" ); //$NON-NLS-1$
    assertEquals( ele3.getParentId(), "parentid" ); //$NON-NLS-1$
    assertEquals( ele1.getInstanceId(), "instanceid" ); //$NON-NLS-1$

    finishTest();
  }

  public void testDataUtility() {
    startTest();
    IPentahoResultSet rs = getResultSet();
    String xmlString = DataUtilities.getXMLString( rs );
    assertTrue( xmlString.indexOf( "DATA2" ) > 0 ); //$NON-NLS-1$
    finishTest();
  }

  public void testDataUtility1() {
    startTest();
    IPentahoResultSet rs1 = getResultSetFromArray();
    String xmlString1 = DataUtilities.getXMLString( rs1 );
    assertTrue( xmlString1.indexOf( "VAL1" ) > 0 ); //$NON-NLS-1$
    finishTest();
  }

  public void testDataUtility3() {
    startTest();
    IPentahoResultSet rs3 = getResultSet2();
    String xmlString3 = DataUtilities.getXMLString( rs3 );
    assertTrue( xmlString3.indexOf( "DATA2" ) > 0 ); //$NON-NLS-1$

    finishTest();
  }

  public void testDataUtility4() {
    startTest();
    IPentahoResultSet rs4 = getResultSet3();
    String xmlString4 = DataUtilities.getXMLString( rs4 );
    assertTrue( xmlString4.indexOf( "DATA2" ) > 0 ); //$NON-NLS-1$   
    finishTest();
  }

  public void testJVMParameterProvider() {
    startTest();
    System.setProperty( "teststring", "test string" ); //$NON-NLS-1$//$NON-NLS-2$
    System.setProperty( "testlong", "32" ); //$NON-NLS-1$//$NON-NLS-2$
    System.setProperty( "testdecimal", "66.6" ); //$NON-NLS-1$//$NON-NLS-2$
    Date someDate = new Date();
    System.setProperty( "testdate", DateFormat.getInstance().format( someDate ) ); //$NON-NLS-1$

    JVMParameterProvider provider = new JVMParameterProvider();

    //  assertEquals(someDate, provider.getDateParameter("testdate", null) ); //$NON-NLS-1$
    assertEquals( someDate, provider.getDateParameter( "bogus", someDate ) ); //$NON-NLS-1$
    assertEquals( 32, provider.getLongParameter( "testlong", 0 ) ); //$NON-NLS-1$
    assertEquals( "test string", provider.getStringParameter( "teststring", null ) ); //$NON-NLS-1$ //$NON-NLS-2$ 
    assertEquals( new BigDecimal( "66.6" ), provider.getDecimalParameter( "testdecimal", null ) ); //$NON-NLS-1$ //$NON-NLS-2$

    assertEquals( "test string", provider.getParameter( "teststring" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals( "string", provider.getParameterType( "teststring" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Iterator it = provider.getParameterNames();
    while ( it.hasNext() ) {
      String pName = (String) it.next();
      System.out.println( pName + "=" + provider.getStringParameter( pName, null ) ); //$NON-NLS-1$
    }
    finishTest();
  }

  public void testJVMParameterProviderDifferentPath() {
    startTest();
    JVMParameterProvider provider = new JVMParameterProvider();
    assertEquals( 99, provider.getLongParameter( "bogus", 99 ) ); //$NON-NLS-1$
    assertEquals( new BigDecimal( "99" ), provider.getDecimalParameter( "bogus", new BigDecimal( "99" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    finishTest();
  }

  public void testJVMParameterProviderDifferentPath2() {
    startTest();
    JVMParameterProvider provider = new JVMParameterProvider();
    try {
      Object parameterValue = provider.getDecimalParameter( "bogus", null ); //$NON-NLS-1$
      System.out.println( "Paramater Value Received is " + parameterValue ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      assertTrue( "Expected exception was caught", true ); //$NON-NLS-1$
    }
    finishTest();
  }

  /*
   * public void testUIUtil() { List messages = new ArrayList(); messages.add("Error Message One"); //$NON-NLS-1$
   * messages.add("Error Message Two"); //$NON-NLS-1$
   * messages.add("Error: This is the first error Error: message - MultipleComponentTest"); //$NON-NLS-1$ StringBuffer
   * messageBuffer = new StringBuffer(); UIUtil.formatErrorMessage("text/html", "Error Test", messages, messageBuffer);
   * //$NON-NLS-1$ //$NON-NLS-2$ String result = messageBuffer.toString();
   * assertTrue(result.indexOf("Error Message Two")>=0); //$NON-NLS-1$ String msg = UIUtil.getFirstError(messages);
   * assertTrue(msg.indexOf("MultipleComponentTest")>=0); //$NON-NLS-1$ IPentahoResultSet rs = getResultSet();
   * StringBuffer formattedRs = new StringBuffer(); UIUtil.formatResultSetAsHTMLRows(rs, formattedRs);
   * System.out.println(formattedRs.toString()); assertTrue(formattedRs.indexOf("<th>COL3</th>")>=0); //$NON-NLS-1$ }
   */

  public void testPathBasedSystemSettings() {

    PathBasedSystemSettings settings = new PathBasedSystemSettings();
    String cfgPathKey = settings.getSystemSetting( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, "" ); //$NON-NLS-1$
    List list = settings.getSystemSettings( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY );
    for ( int i = 0; i < list.size(); i++ ) {
      System.out.println( "System Settings" + ( i + 1 ) + list.get( i ) ); //$NON-NLS-1$
    }
    System.setProperty( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, "" ); //$NON-NLS-1$
    settings.getSystemSetting( "c:/code", PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, null ); //$NON-NLS-1$
    System.setProperty( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, cfgPathKey );
    assertTrue( true );
  }

  public static void main( String[] args ) {
    MultipleComponentTest test = new MultipleComponentTest();
    try {
      test.setUp();
      /*
                    * test.testHelloWorldComponent(); // test.testCrosstab(); test.testMiscExceptionClasses(); //
                    * test.testReportCharts(); // test.testDynamicSQLAndSecureFilter(); test.testPivotViewComponent();
                    * test.testSimpleRuntime(); test.testDataUtility(); test.testDataUtility1();
                    * test.testDataUtility2(); test.testDataUtility3(); test.testDataUtility4();
                    * test.testJVMParameterProvider(); test.testJVMParameterProviderDifferentPath();
                    * test.testJVMParameterProviderDifferentPath2(); test.testUIUtil();
                    */
      test.testPathBasedSystemSettings();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
