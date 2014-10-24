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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.DefaultServiceManager;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.uifoundation.component.ActionComponent;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.platform.plugin.CommonUtil;
import org.pentaho.reporting.platform.plugin.cache.NullReportCache;
import org.pentaho.reporting.platform.plugin.cache.ReportCache;
import org.pentaho.reporting.platform.plugin.output.DefaultReportOutputHandlerFactory;
import org.pentaho.reporting.platform.plugin.output.ReportOutputHandlerFactory;
import org.pentaho.reporting.platform.plugin.repository.PentahoNameGenerator;
import org.pentaho.reporting.platform.plugin.repository.TempDirectoryNameGenerator;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.userrole.ws.MockUserRoleListService;
import org.springframework.security.userdetails.MockUserDetailsService;
import org.springframework.security.userdetails.UserDetailsService;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings( "nls" )
public class JFreeReportTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  //private MicroPlatform microPlatform;
  private ClassicEngineBoot c;
  
  //Logger
  private static final Log log = LogFactory.getLog( JFreeReportTest.class );
  
  @Override
public void setUp() {
	  new File("./resource/solution/system/tmp").mkdirs();	  
	  MicroPlatform microPlatform = new MicroPlatform( "./test-src/solution/"); //$NON-NLS-1$
	  microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
	  microPlatform.define(IUnifiedRepository.class, FileSystemBackedUnifiedRepository.class);
	  microPlatform.define(IPluginProvider.class, SystemPathXmlPluginProvider.class);
	  microPlatform.define(IServiceManager.class, DefaultServiceManager.class, IPentahoDefinableObjectFactory.Scope.GLOBAL);
	  microPlatform.define(PentahoNameGenerator.class, TempDirectoryNameGenerator.class, IPentahoDefinableObjectFactory.Scope.GLOBAL);
	  microPlatform.define(IUserRoleListService.class, MockUserRoleListService.class);
	  microPlatform.define(UserDetailsService.class, MockUserDetailsService.class);
	  microPlatform.define(ReportOutputHandlerFactory.class, DefaultReportOutputHandlerFactory.class);
	  microPlatform.define(ReportCache.class, NullReportCache.class);	 	  
	  try {
		microPlatform.start();
		if (!ClassicEngineBoot.getInstance().isBootInProgress()) {
			c = ClassicEngineBoot.getInstance();
			c.start();
		}
	  } catch (PlatformInitializationException e) {
		  e.printStackTrace();
	  }	 	  
	  IPentahoSession session = new StandaloneSession();
	  PentahoSessionHolder.setSession( session );
  }  
  
  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  /*public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "jfree-report", "jfree-report" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }*/

   /*
   public void testJFreeReportMondrian() { 
   startTest(); 
   SimpleParameterProvider parameterProvider = new SimpleParameterProvider(); 
   //parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ 
   parameterProvider.setParameter( "outputType", "text/html" );
   OutputStream outputStream = getOutputStream("ReportingTest.testJFreeReportMondrian", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); 
   StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ 
   IRuntimeContext context = run( "/test-src/solution/test/reporting/MDX_report.xaction", null, false, parameterProvider, outputHandler, session);
   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
   assertEquals(Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success 
   finishTest(); 
   }
   */

  public void testJFreeReportParameterPage1() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "outputType", "text/html" );
    OutputStream outputStream = getOutputStream( "ReportingTest.testJFreeReportParameterPage", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    IRuntimeContext context =
        run(
            "/test-src/solution/test/reporting/jfreereport-reports-test-param.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
    finishTest();
  }

  public void testJFreeReportParameterPage2() {
    startTest();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "type", "html" ); //$NON-NLS-1$ //$NON-NLS-2$
    OutputStream outputStream = getOutputStream( "ReportingTest.testJFreeReportParameterPage2", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    IRuntimeContext context =
        run(
            "/test-src/solution/test/reporting/jfreereport-reports-test-param2.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString( "BaseTest.USER_RUNNING_ACTION_SEQUENCE" ), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
   finishTest();
  }

  
  public void testJFreeReportParameterPage3() { 
	  startTest(); 
	  SimpleParameterProvider parameterProvider = new SimpleParameterProvider(); 
	  parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ 
	  OutputStream outputStream = getOutputStream("ReportingTest.testJFreeReportParameterPage3", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
	  SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); 
	  StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ 
	  IRuntimeContext context = 
			  run( "/test-src/solution/test/reporting/jfreereport-reports-test-param3.xaction", null, false, parameterProvider, outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	  assertEquals(
			  Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success 
	  finishTest(); 
  }
  
   /*public void testJFreeReport1() { 
   startTest(); 
   SimpleParameterProvider parameterProvider = new
   SimpleParameterProvider(); 
   parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ 
   OutputStream outputStream = getOutputStream("ReportingTest.JFreeReporthtml", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
   SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); 
   StandaloneSession session = new
   StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ 
   IRuntimeContext
   context = run( 
		   "/test/reporting/jfreereport-reports-test-1.xaction", null, false, parameterProvider, outputHandler, session ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
   assertEquals(
		   Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success 
   finishTest(); 
   }*/  
  
   /*public void testJFreeReport2() { 
   startTest(); 
   SimpleParameterProvider parameterProvider = new SimpleParameterProvider(); 
   parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$ 
   OutputStream
   outputStream = getOutputStream("ReportingTest.JFreeReport-2-PDF", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
   SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); 
   StandaloneSession session = new
   StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ 
   IRuntimeContext
   context = run( "/test-src/solution/test/reporting/jfreereport-reports-test-2.xaction", null, false, parameterProvider,
   outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
   assertEquals(
   Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success 
   finishTest(); 
   }*/  
    
    /*public void testJFreeReport3() { startTest(); SimpleParameterProvider parameterProvider = new
    SimpleParameterProvider(); parameterProvider.setParameter("type", "xls"); //$NON-NLS-1$ //$NON-NLS-2$ 
    OutputStream outputStream = getOutputStream("ReportingTest.JFreeReportxls", ".xls"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
    StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ 
    IRuntimeContext
    context = run( "/test-src/solution/test/reporting/jfreereport-reports-test-1.xaction", null, false, parameterProvider,
    outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    assertEquals(
    Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
    context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success 
    finishTest(); }*/
    
    /*public void testJFreeReport4() { startTest(); SimpleParameterProvider parameterProvider = new
    SimpleParameterProvider(); parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$ 
    OutputStream
    outputStream = getOutputStream("ReportingTest.JFreeReporthtml", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true); StandaloneSession session = new
    StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$ 
    IRuntimeContext
    context = run( "/test-src/solution/test/reporting/jfreereport-reports-test-1.xaction", null, false, parameterProvider,
    outputHandler, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    assertEquals(
    Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
    context.getStatus()); //$NON-NLS-1$ // TODO need some validation of success 
    finishTest(); }*/
    
    /*public void testJFreeReport5() { startTest(); SimpleParameterProvider parameterProvider = new
    SimpleParameterProvider(); parameterProvider.setParameter("type", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter("solution", getSolutionPath()); //$NON-NLS-1$ //$NON-NLS-2$ 
    IRuntimeContext context
    = run( "/test-src/solution/test/reporting/jfreereport-reports-file.xaction", parameterProvider, "dummy", "txt"); //$NON-NLS-1$
    //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
    assertEquals(
    Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
    context.getStatus()); //$NON-NLS-1$ 
    finishTest(); }*/   
  
   /*public void testJFreeReport6() { 
   startTest(); 
   SimpleParameterProvider parameterProvider = new
   SimpleParameterProvider(); 
   parameterProvider.setParameter("type", "csv"); //$NON-NLS-1$ //$NON-NLS-2$
   IRuntimeContext context = 
   run( "/test-src/solution/test/reporting/jfreereport-reports-file.xaction", parameterProvider, "dummy",
   "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
   assertEquals(
   Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), 
   IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   context.getStatus()); //$NON-NLS-1$ 
   finishTest(); 
   }*/
   
   /* public void testJFreeReport7() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "xls"); //$NON-NLS-1$ //$NON-NLS-2$
   * IRuntimeContext context = run( "test", "reporting", "jfreereport-reports-file.xaction", parameterProvider, "dummy",
   * "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   * 
   * public void testJFreeReport8() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "rtf"); //$NON-NLS-1$ //$NON-NLS-2$
   * IRuntimeContext context = run( "test", "reporting", "jfreereport-reports-file.xaction", parameterProvider, "dummy",
   * "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */

  /*
   * public void testJFreeReport9() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "zip"); //$NON-NLS-1$ //$NON-NLS-2$
   * IRuntimeContext context = run( "test", "reporting", "jfreereport-reports-file.xaction", parameterProvider, "dummy",
   * "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   * 
   * public void testJFreeReport10() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "xml"); //$NON-NLS-1$ //$NON-NLS-2$
   * IRuntimeContext context = run( "test", "reporting", "jfreereport-reports-file.xaction", parameterProvider, "dummy",
   * "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */

  public void testJFreeReportWithChartActionComponent() {
    startTest();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( requestContext.getContextPath() );
    ArrayList messages = new ArrayList();
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    parameterProvider.setParameter( "solution", "test" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "path", "reporting" ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProvider.setParameter( "action", "JFree_XQuery_Chart_report.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$
    ActionComponent component =
        new ActionComponent(
            "samples/reporting/JFree_XQuery_Chart_report.xaction", null, IOutputHandler.OUTPUT_TYPE_DEFAULT, urlFactory, messages ); //$NON-NLS-1$
    component.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameterProvider );
    StandaloneSession session =
        new StandaloneSession( Messages.getInstance().getString( "BaseTest.DEBUG_JUNIT_SESSION" ) ); //$NON-NLS-1$
    component.validate( session, null );
    OutputStream outputStream = getOutputStream( "ReportingTest.testJFreeReportWithChartActionComponent", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
    String content = component.getContent( "text/html" ); //$NON-NLS-1$
    try {
      outputStream.write( content.getBytes() );
    } catch ( Exception e ) {
      //ignore
    }
    finishTest();
  }

  /*
   * public void testJFreeReportPlainText() { startTest(); SimpleParameterProvider parameterProvider = new
   * SimpleParameterProvider(); parameterProvider.setParameter("type", "plaintext"); //$NON-NLS-1$ //$NON-NLS-2$
   * IRuntimeContext context = run( "test", "reporting", "jfreereport-reports-file.xaction", parameterProvider, "dummy",
   * "txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ assertEquals(
   * Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */
  
  protected OutputStream getOutputStream( String testName, String extension ) {
	    OutputStream outputStream = null;
	    try {
	      String tmpDir = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp" ); //$NON-NLS-1$
	      File file = new File( tmpDir );
	      file.mkdirs();
	      String path = PentahoSystem.getApplicationContext().getFileOutputPath( "test/tmp/" + testName + extension ); //$NON-NLS-1$
	      outputStream = new FileOutputStream( path );
	    } catch ( FileNotFoundException e ) {
	      CommonUtil.checkStyleIgnore();
	    }
	    return outputStream;
	  }

  public IRuntimeContext run( String actionPath, String instanceId, boolean persisted,
	    IParameterProvider parameterProvider, IOutputHandler outputHandler, IPentahoSession session ) {
	    List<String> messages = new ArrayList<String>();
	    String baseUrl = ""; //$NON-NLS-1$
	    HashMap<String, IParameterProvider> parameterProviderMap = new HashMap<String, IParameterProvider>();
	    parameterProviderMap.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );
	    ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );

	    IPentahoUrlFactory urlFactory = new SimpleUrlFactory( baseUrl );

	    IRuntimeContext context =
	        solutionEngine.execute( actionPath,
	            "", false, true, instanceId, persisted, parameterProviderMap, outputHandler, null, urlFactory, messages ); //$NON-NLS-1$

	    return context;
	  }

  public static void main( String[] args ) {
    JFreeReportTest test = new JFreeReportTest();

    try {
      test.setUp();

      // test.testJFreeReportMondrian();
      // test.testJFreeReport1();
      // test.testJFreeReport2();
      // test.testJFreeReport3();
      // test.testJFreeReport4();
      // test.testJFreeReport5();
      // test.testJFreeReport6();
      // test.testJFreeReport7();
      // test.testJFreeReport8();
      // test.testJFreeReport9();
      // test.testJFreeReport10();
      test.testJFreeReportWithChartActionComponent();
      test.testJFreeReportParameterPage1();
      test.testJFreeReportParameterPage2();
      test.testJFreeReportParameterPage3();
      // test.testJFreeReportParameterPage3();
      // test.testJFreeReportPlainText();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
