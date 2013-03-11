/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.actionsequence.OutputDef;
import org.pentaho.platform.engine.services.solution.SolutionCompare;
import org.pentaho.platform.engine.services.solution.SolutionHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

//import java.math.BigDecimal;
//import java.util.Date;
//import java.util.Map;
//import org.pentaho.platform.engine.services.solution.SolutionEngineAgent;

@SuppressWarnings("nls")
public class SolutionTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testSolutionCompare() {
    startTest();

    SolutionCompare compare = new SolutionCompare();
    String path1 = PentahoSystem.getApplicationContext().getSolutionPath("test/platform"); //$NON-NLS-1$ 
    String path2 = PentahoSystem.getApplicationContext().getSolutionPath("test/dashboard"); //$NON-NLS-1$
    compare.compare(path1, path2);

    assertTrue(true);
    finishTest();
  }

  public void testSolutionCompare2() {
    startTest();

    SolutionCompare compare = new SolutionCompare();
    String path1 = PentahoSystem.getApplicationContext().getSolutionPath("test/charts/areachart_data.xaction"); //$NON-NLS-1$ 
    String path2 = PentahoSystem.getApplicationContext().getSolutionPath("test/dashboard"); //$NON-NLS-1$
    compare.compare(path1, path2);

    assertTrue(true);
    finishTest();
  }

  /*public void testSolutionHelper() {
    startTest();
    String outputPath = "c:/"; //$NON-NLS-1$
    try {
      File f = new File(outputPath + File.separator + "hello_world.txt"); //$NON-NLS-1$
      FileOutputStream outputStream = new FileOutputStream(f);
      HashMap parameters = new HashMap();
      ISolutionEngine solutionEngine = SolutionHelper.execute(
          "Simple Case Example", "Hello World", "samples/getting-started/HelloWorld.xaction", parameters, outputStream); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
      assertEquals(IRuntimeContext.RUNTIME_STATUS_SUCCESS, solutionEngine.getStatus());

    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      File f = new File(outputPath + File.separator + "report.html"); //$NON-NLS-1$
      FileOutputStream outputStream = new FileOutputStream(f);
      HashMap parameters = new HashMap();
      ISolutionEngine solutionEngine = SolutionHelper.execute(
          "Simple Case Example", "Simple Report", "samples/reporting/JFree_Quad.xaction", parameters, outputStream); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$

      SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
      parameterProvider.setParameter("type", "html"); //$NON-NLS-1$ //$NON-NLS-2$
      StandaloneSession session = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

      SolutionHelper.execute("Test", session, "samples/portal/departments.rule.xaction", parameters, outputStream); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals(IRuntimeContext.RUNTIME_STATUS_SUCCESS, solutionEngine.getStatus());
    } catch (Exception e) {
      e.printStackTrace();
    }

    assertTrue(true);
    finishTest();
  }

  public void testSolutionEngineAgent() {
    startTest();

    SolutionEngineAgent agent = new SolutionEngineAgent();

    agent.setDescription("description"); //$NON-NLS-1$  
    agent.setParamter("name1", "value1");//$NON-NLS-1$ //$NON-NLS-2$
    agent.setParamter("name2", "value2");//$NON-NLS-1$ //$NON-NLS-2$
    agent.setParamter("name3", "value3");//$NON-NLS-1$ //$NON-NLS-2$
    agent.setParamter("name4", "value4");//$NON-NLS-1$ //$NON-NLS-2$
    agent.setUserId("Joe"); //$NON-NLS-1$
    agent.setActionSequence("test/dashboard/departments.rule.xaction");//$NON-NLS-1$
    int result = agent.execute();
    assertEquals(agent.getDescription(), "description"); //$NON-NLS-1$
    assertEquals(agent.getUserId(), "Joe"); //$NON-NLS-1$
    assertEquals(agent.getActionSequence(), "test/dashboard/departments.rule.xaction"); //$NON-NLS-1$
    assertEquals(IRuntimeContext.RUNTIME_STATUS_SUCCESS, result);

    finishTest();
  }*/

  /*public void testSimpleParameterProvider() {
    startTest();
    Map parameters = new HashMap();
    Date date = new Date();
    Object decimalValue = Double.valueOf("2000000.00"); //$NON-NLS-1$
    String type = "html"; //$NON-NLS-1$
    long longValue = Long.parseLong("200000000");//$NON-NLS-1$
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider(parameters);

    parameterProvider.setParameter("type", type); //$NON-NLS-1$
    parameterProvider.setParameter("total", longValue); //$NON-NLS-1$ 
    parameterProvider.setParameter("decimalValue", decimalValue); //$NON-NLS-1$
    parameterProvider.setParameter("todaysDate", date); //$NON-NLS-1$

    parameterProvider.setParameters(parameters);

    assertEquals(parameterProvider.getDateParameter("todaysDate", null), date);//$NON-NLS-1$
    assertEquals(parameterProvider.getDecimalParameter("decimalValue", new BigDecimal(0.00)), decimalValue); //$NON-NLS-1$ //$NON-NLS-2$

    parameters.put("baseUrl", "http://localhost:8080"); //$NON-NLS-1$ //$NON-NLS-2$
    parameters.put("actionUrl", "http://localhost:8080/pentaho"); //$NON-NLS-1$ //$NON-NLS-2$

    assertTrue(true);
    finishTest();
  }*/

  public void testOutputDef() {
    startTest();
    try {
      FileOutputStream outputStream = new FileOutputStream("c:/test.txt"); //$NON-NLS-1$
      OutputDef outputDef1 = new OutputDef("FirstOutputName", outputStream); //$NON-NLS-1$
      outputDef1.setValue("MyOutputDefinition"); //$NON-NLS-1$
      assertEquals(outputDef1.getName(), "FirstOutputName"); //$NON-NLS-1$
      OutputDef outputDef2 = new OutputDef("SecondOutputName", new ArrayList()); //$NON-NLS-1$
      assertEquals(outputDef2.getName(), "SecondOutputName"); //$NON-NLS-1$      
      OutputDef outputDef3 = new OutputDef("ThirdOutputName", "MyOutputType"); //$NON-NLS-1$ //$NON-NLS-2$
      assertEquals(outputDef3.getName(), "ThirdOutputName"); //$NON-NLS-1$

    } catch (Exception e) {
      e.printStackTrace();
    }
    assertTrue(true);
    finishTest();
  }

  public static void main(String[] args) {
    SolutionTest test = new SolutionTest();
    test.setUp();
    test.testSolutionCompare();
    test.testSolutionCompare2();
//    test.testSolutionHelper();
//    test.testSolutionEngineAgent();
//    test.testSimpleParameterProvider();
    test.testOutputDef();
    try {

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
