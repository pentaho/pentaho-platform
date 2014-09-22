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

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.services.BaseRequestHandler;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.uifoundation.chart.CategoryDatasetChartComponent;
import org.pentaho.platform.uifoundation.chart.ChartHelper;
import org.pentaho.platform.uifoundation.chart.TimeSeriesCollectionChartComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings( "nls" )
public class ChartTest extends BaseTest {
  StandaloneApplicationContext applicationContext = null;

  IPentahoSession session;

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public ChartTest() {
    super( SOLUTION_PATH );
  }

  /*
   * public void testCategoryAreaChart() { runCategoryChartTest(JFreeChartEngine.AREA_CHART_TYPE,
   * "test/charts/areaChart.xml", "testAreaChart"); //$NON-NLS-1$ //$NON-NLS-2$ }
   * 
   * public void testCategoryBarChart() { runCategoryChartTest(JFreeChartEngine.BAR_CHART_TYPE,
   * "test/charts/barChart.xml", "testBarChart"); //$NON-NLS-1$ //$NON-NLS-2$ }
   * 
   * public void testCategoryLineChart() { runCategoryChartTest(JFreeChartEngine.LINE_CHART_TYPE,
   * "test/charts/lineChart.xml", "testLineChart"); //$NON-NLS-1$ //$NON-NLS-2$ }
   * 
   * public void testPieChart() { runPieChartTest(JFreeChartEngine.PIE_CHART_TYPE, "test/charts/pieChart.xml",
   * "testPieChart"); //$NON-NLS-1$ //$NON-NLS-2$ }
   */
  /*
   * public void testChartHelper_DialChart() { String region = "Central"; //$NON-NLS-1$ ArrayList messages = new
   * ArrayList(); SimpleParameterProvider parameters = new SimpleParameterProvider();
   * parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("image-height",
   * "300"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("REGION", region); //$NON-NLS-1$
   * parameters.setParameter("outer-params", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("inner-param", "DEPARTMENT"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter(
   * "drill-url", "SampleDashboard.jsp?region=" + region + "&amp;department={DEPARTMENT}&amp;measure={measure}");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ parameters.setParameter("series-name", "measure"); //$NON-NLS-1$
   * //$NON-NLS-2$ parameters.setParameter("chart-type", "DialChart"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("value", "550"); //$NON-NLS-1$ //$NON-NLS-2$ StringBuffer content = new StringBuffer();
   * StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION"));
   * //$NON-NLS-1$ ChartHelper.doChart("/test-src/solutions/samples/dashboard/departments.widget.xml", parameters,
   * content, session, messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ try { OutputStream os =
   * getOutputStream(SOLUTION_PATH, "ChartTest.testChartHelper_DialChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * os.write(content.toString().getBytes()); } catch (Exception e) {
   * 
   * } }
   * 
   * public void testChartHelper_PieChart() { String region = "Central"; //$NON-NLS-1$ ArrayList messages = new
   * ArrayList(); SimpleParameterProvider parameters = new SimpleParameterProvider();
   * parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("image-height",
   * "300"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("REGION", region); //$NON-NLS-1$
   * parameters.setParameter("outer-params", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("inner-param", "DEPARTMENT"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter(
   * "drill-url", "SampleDashboard.jsp?region=" + region + "&amp;department={DEPARTMENT}&amp;measure={measure}");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ parameters.setParameter("series-name", "measure"); //$NON-NLS-1$
   * //$NON-NLS-2$ parameters.setParameter("chart-type", "PieChart"); //$NON-NLS-1$ //$NON-NLS-2$ StringBuffer content =
   * new StringBuffer(); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * ChartHelper.doChart("/test-src/solutions/samples/dashboard/departments.widget.xml", parameters, content, session,
   * messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ try { OutputStream os = getOutputStream(SOLUTION_PATH,
   * "ChartTest.testChartHelper_PieChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * os.write(content.toString().getBytes()); } catch (Exception e) {
   * 
   * } }
   * 
   * public void testChartHelper_PieGrid() { String region = "Central"; //$NON-NLS-1$ ArrayList messages = new
   * ArrayList(); SimpleParameterProvider parameters = new SimpleParameterProvider();
   * parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("image-height",
   * "300"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("REGION", region); //$NON-NLS-1$
   * parameters.setParameter("outer-params", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("inner-param", "DEPARTMENT"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter(
   * "drill-url", "SampleDashboard.jsp?region=" + region + "&amp;department={DEPARTMENT}&amp;measure={measure}");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ parameters.setParameter("series-name", "measure"); //$NON-NLS-1$
   * //$NON-NLS-2$ parameters.setParameter("chart-type", "PieGrid"); //$NON-NLS-1$ //$NON-NLS-2$ StringBuffer content =
   * new StringBuffer(); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * ChartHelper.doChart(/test-src/solutions/samples/dashboard/departments.widget.xml", parameters, content, session,
   * messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ try { OutputStream os = getOutputStream(SOLUTION_PATH,
   * "ChartTest.testChartHelper_PieGrid", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ os.write(content.toString().getBytes());
   * } catch (Exception e) {
   * 
   * } }
   * 
   * public void testChartHelper_BarChart() { String region = "Central"; //$NON-NLS-1$ ArrayList messages = new
   * ArrayList(); SimpleParameterProvider parameters = new SimpleParameterProvider();
   * parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("image-height",
   * "300"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("REGION", region); //$NON-NLS-1$
   * parameters.setParameter("outer-params", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("inner-param", "DEPARTMENT"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter(
   * "drill-url", "SampleDashboard.jsp?region=" + region + "&amp;department={DEPARTMENT}&amp;measure={measure}");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ parameters.setParameter("series-name", "measure"); //$NON-NLS-1$
   * //$NON-NLS-2$ parameters.setParameter("chart-type", "BarChart"); //$NON-NLS-1$ //$NON-NLS-2$ StringBuffer content =
   * new StringBuffer(); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * ChartHelper.doChart("/test-src/solutions/samples/dashboard/departments.widget.xml", parameters, content, session,
   * messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ try { OutputStream os = getOutputStream(SOLUTION_PATH,
   * "ChartTest.testChartHelper_BarChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * os.write(content.toString().getBytes()); } catch (Exception e) {
   * 
   * } }
   */
  /*
   * public void testChartHelper_LineChart() { String region = "Central"; //$NON-NLS-1$ ArrayList messages = new
   * ArrayList(); SimpleParameterProvider parameters = new SimpleParameterProvider();
   * parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("image-height",
   * "300"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("REGION", region); //$NON-NLS-1$
   * parameters.setParameter("outer-params", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("inner-param", "DEPARTMENT"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter(
   * "drill-url", "SampleDashboard.jsp?region=" + region + "&amp;department={DEPARTMENT}&amp;measure={measure}");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ parameters.setParameter("series-name", "measure"); //$NON-NLS-1$
   * //$NON-NLS-2$ parameters.setParameter("chart-type", "LineChart"); //$NON-NLS-1$ //$NON-NLS-2$ StringBuffer content
   * = new StringBuffer(); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * ChartHelper.doChart("/test-src/solutions/samples/dashboard/departments.widget.xml", parameters, content, session,
   * messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ try { OutputStream os = getOutputStream(SOLUTION_PATH,
   * "ChartTest.testChartHelper_LineChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * os.write(content.toString().getBytes()); } catch (Exception e) {
   * 
   * } }
   */

  /*
   * public void testChartHelper_AreaChart() { String region = "Central"; //$NON-NLS-1$ ArrayList messages = new
   * ArrayList(); SimpleParameterProvider parameters = new SimpleParameterProvider();
   * parameters.setParameter("image-width", "450"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("image-height",
   * "300"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("REGION", region); //$NON-NLS-1$
   * parameters.setParameter("outer-params", "REGION"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("inner-param", "DEPARTMENT"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("connection", "SampleData"); //$NON-NLS-1$ //$NON-NLS-2$ parameters.setParameter("query",
   * "select REGION, DEPARTMENT, MANAGER_NAME, EMAIL from DEPARTMENT_MANAGERS"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter( "drill-url", "SampleDashboard.jsp?region=" + region +
   * "&amp;department={DEPARTMENT}&amp;measure={measure}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   * parameters.setParameter("series-name", "measure"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameters.setParameter("chart-type", "AreaChart"); //$NON-NLS-1$ //$NON-NLS-2$ StringBuffer content = new
   * StringBuffer(); StandaloneSession session = new
   * StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * ChartHelper.doChart("/test-src/solutions/samples/dashboard/departments.widget.xml", parameters, content, session,
   * messages, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ try { OutputStream os = getOutputStream(SOLUTION_PATH,
   * "ChartTest.testChartHelper_AreaChart", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * os.write(content.toString().getBytes()); } catch (Exception e) {
   * 
   * } }
   */

  public void testChartHelper_Null() {
    String region = "Central"; //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    parameters.setParameter( "image-width", "450" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter( "image-height", "300" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter( "REGION", region ); //$NON-NLS-1$
    parameters.setParameter( "outer-params", "REGION" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter( "inner-param", "DEPARTMENT" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter(
        "drill-url", "SampleDashboard.jsp?region=" + region + "&amp;department={DEPARTMENT}&amp;measure={measure}" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    parameters.setParameter( "series-name", "measure" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.setParameter( "chart-type", "" ); //$NON-NLS-1$ //$NON-NLS-2$      
    StringBuffer content = new StringBuffer();
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    ChartHelper.doChart( "/samples/dashboard/departments.widget.xml", parameters, content, session, messages, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    try {
      OutputStream os = getOutputStream( "ChartTest.testChartHelper_Null", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
      os.write( content.toString().getBytes() );
    } catch ( Exception e ) {
      //ignore
    }
  }

  /*
   * public void testChartComponent_AreaChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "area"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_Area", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_BarChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "bar"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_Bar", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_BubbleChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "bubble"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_Bubble", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session =
   * new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_BarLineComboChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "barline"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_BarLineCombo", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession
   * session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   * 
   * public void testChartComponent_DialChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "dial"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_Dial", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_LineChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "line"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_Line", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_PieChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "pie"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_Pie", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_PieGridChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "piegrid"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_PieGrid", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session =
   * new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/samples/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testChartComponent_TimeSeriesChart() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "timeseries"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_TimeSeries", ".html"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session
   * = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testTimeSeriesChartCollection() { SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream
   * outputStream = getOutputStream(SOLUTION_PATH, "Chart_TimeSeries", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  /*
   * public void testXYZChart() { SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
   * parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ OutputStream outputStream =
   * getOutputStream(SOLUTION_PATH, "Chart_XYZ", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   * parameterProvider.setParameter("chart_type", "xy"); //$NON-NLS-1$ //$NON-NLS-2$ StandaloneSession session = new
   * StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); IRuntimeContext context = run(
   * SOLUTION_PATH + "/test/charts/", "ChartComponent_ChartTypes.xaction", parameterProvider, outputHandler);
   * //$NON-NLS-1$ assertEquals( Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"),
   * IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success
   * }
   */

  public void runCategoryChartTest( int chartType, String path, String testName ) {

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    int chartWidth = 400;
    int chartHeight = 400;
    CategoryDatasetChartComponent categoryChartComponent =
        new CategoryDatasetChartComponent( chartType, path, chartWidth, chartHeight, urlFactory, messages );

    categoryChartComponent.setLoggingLevel( ILogger.ERROR );
    categoryChartComponent.setByRow( true );

    categoryChartComponent.setDataAction( "/test-src/solution/test/rules/department_stats.xaction", "rule-result" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    OutputStream outputStream = getOutputStream( "ChartTest." + testName, ".html" ); //$NON-NLS-1$//$NON-NLS-2$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParameters );
    parameterProviders.put( IParameterProvider.SCOPE_SESSION, sessionParameters );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    UserSession usession = new UserSession( "Admin", Locale.US, true, requestParameters ); //$NON-NLS-1$
    usession.doStartupActions( requestParameters );
    UserSession usessionNoAuth = new UserSession( "Admin", Locale.US, requestParameters ); //$NON-NLS-1$
    usessionNoAuth.doStartupActions( requestParameters );
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, false );
    BaseRequestHandler requestHandler = new BaseRequestHandler( session, null, outputHandler, null, urlFactory );

    try {
      categoryChartComponent.validate( session, requestHandler );
      categoryChartComponent.handleRequest( outputStream, requestHandler, contentType, parameterProviders );
    } catch ( IOException e ) {
      e.printStackTrace();
    }

  }

  /*
   * public void runPieChartTest(int chartType, String path, String testName) { SimpleUrlFactory urlFactory = new
   * SimpleUrlFactory("/testurl?"); //$NON-NLS-1$ ArrayList messages = new ArrayList();
   * 
   * int chartWidth = 400; int chartHeight = 400; PieDatasetChartComponent pieChartComponent = new
   * PieDatasetChartComponent(chartType, path, chartWidth, chartHeight, urlFactory, messages);
   * 
   * pieChartComponent.setLoggingLevel(ILogger.ERROR); pieChartComponent.setByRow(true);
   * 
   * pieChartComponent.setDataAction("/test-src/solutions/test/rules/department_stats.xaction", "rule-result");
   * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
   * 
   * OutputStream outputStream = getOutputStream(SOLUTION_PATH, "ChartTest." + testName, ".html");
   * //$NON-NLS-1$//$NON-NLS-2$ String contentType = "text/html"; //$NON-NLS-1$
   * 
   * SimpleParameterProvider requestParameters = new SimpleParameterProvider(); SimpleParameterProvider
   * sessionParameters = new SimpleParameterProvider();
   * 
   * HashMap parameterProviders = new HashMap(); parameterProviders.put(IParameterProvider.SCOPE_REQUEST,
   * requestParameters); parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters); StandaloneSession
   * session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
   * 
   * SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false); BaseRequestHandler requestHandler
   * = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);
   * 
   * try { pieChartComponent.validate(session, requestHandler); pieChartComponent.handleRequest(outputStream,
   * requestHandler, contentType, parameterProviders); } catch (IOException e) { e.printStackTrace(); }
   * 
   * }
   */

  public void runTimeSeriesChartTest( int chartType, String path, String testName ) {

    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    int chartWidth = 400;
    int chartHeight = 400;
    TimeSeriesCollectionChartComponent timeSeriesChartComponent =
        new TimeSeriesCollectionChartComponent( chartType, path, chartWidth, chartHeight, urlFactory, messages );

    timeSeriesChartComponent.setLoggingLevel( ILogger.ERROR );
    timeSeriesChartComponent.setDataAction( "/test/rules/department_stats.xaction", "rule-result" ); //$NON-NLS-1$ //$NON-NLS-2$

    OutputStream outputStream = getOutputStream( "ChartTest." + testName, ".html" ); //$NON-NLS-1$//$NON-NLS-2$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParameters );
    parameterProviders.put( IParameterProvider.SCOPE_SESSION, sessionParameters );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, false );
    BaseRequestHandler requestHandler = new BaseRequestHandler( session, null, outputHandler, null, urlFactory );

    try {
      timeSeriesChartComponent.validate( session, requestHandler );
      timeSeriesChartComponent.handleRequest( outputStream, requestHandler, contentType, parameterProviders );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

}
