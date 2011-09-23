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

//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import org.pentaho.platform.api.engine.IParameterProvider;
//import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
//import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
//import org.pentaho.platform.engine.core.system.StandaloneSession;
//import org.pentaho.platform.engine.services.BaseRequestHandler;
//import org.pentaho.platform.plugin.services.messages.Messages;
//import org.pentaho.platform.uifoundation.component.xml.FilterPanelComponent;
//import org.pentaho.platform.util.FileHelper;
//import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class FilterPanelTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

 /* public void testFilterDefinition1() {
    startTest();

    String definitionPath = "test/dashboard/panel1.filterpanel.xml"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    FilterPanelComponent filterPanel = new FilterPanelComponent(definitionPath, null, urlFactory, messages);
    filterPanel.setLoggingLevel(getLoggingLevel());
    String test1Name = "FilterPanelTest.testFilterDefinition1_html_" + System.currentTimeMillis(); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream(test1Name, ".html"); //$NON-NLS-1$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
    BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

    try {
      filterPanel.validate(session, requestHandler);
      boolean initOk = filterPanel.init();
      assertTrue(initOk);
      filterPanel.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
      String test2Name = "FilterPanelTest.testFilterDefinition1_xml_" + System.currentTimeMillis(); //$NON-NLS-1$
      outputStream = getOutputStream(test2Name, ".xml"); //$NON-NLS-1$
      outputStream.write(filterPanel.getXmlContent().asXML().getBytes());
      outputStream.close();
      InputStream is = this.getInputStreamFromOutput(test1Name, ".html"); //$NON-NLS-1$
      assertNotNull(is);
      String test1Data = FileHelper.getStringFromInputStream(is);
      assertNotNull(test1Data);
      assertTrue(test1Data.indexOf("Select filters to apply to other controls on this page") > 0); //$NON-NLS-1$
      is = this.getInputStreamFromOutput(test2Name, ".xml"); //$NON-NLS-1$
      assertNotNull(is);
      String test2Data = FileHelper.getStringFromInputStream(is);
      assertNotNull(test2Data);
      assertTrue(test2Data.indexOf("<xf:label>Central</xf:label>") > 0); //$NON-NLS-1$
    } catch (IOException e) {
      e.printStackTrace();
    }

    finishTest();
  }*/

  /*public void testFilterDefinition3() {
    startTest();

    String definitionPath = "test/dashboard/panel3.filterpanel.xml"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    FilterPanelComponent filterPanel = new FilterPanelComponent(definitionPath, null, urlFactory, messages);
    filterPanel.setLoggingLevel(getLoggingLevel());
    String test1Name = "FilterPanelTest.testFilterDefinition3_html_" + System.currentTimeMillis(); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream(test1Name, ".html"); //$NON-NLS-1$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
    BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

    try {
      filterPanel.validate(session, requestHandler);
      boolean initOk = filterPanel.init();
      assertTrue(initOk);
      filterPanel.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
      String test3Name = "FilterPanelTest.testFilterDefinition3_xml_" + System.currentTimeMillis(); //$NON-NLS-1$
      outputStream = getOutputStream(test3Name, ".xml"); //$NON-NLS-1$
      outputStream.write(filterPanel.getXmlContent().asXML().getBytes());
      outputStream.close();
      InputStream is = this.getInputStreamFromOutput(test1Name, ".html"); //$NON-NLS-1$
      assertNotNull(is);
      is = this.getInputStreamFromOutput(test3Name, ".xml"); //$NON-NLS-1$
      assertNotNull(is);
      String test3Data = FileHelper.getStringFromInputStream(is);
      assertNotNull(test3Data);
      assertTrue(test3Data.indexOf("Select one or more Position Titles") > 0); //$NON-NLS-1$
      is = this.getInputStreamFromOutput(test3Name, ".xml"); //$NON-NLS-1$
      assertNotNull(is);
      assertTrue(test3Data.indexOf("Year") > 0); //$NON-NLS-1$
      is = this.getInputStreamFromOutput(test3Name, ".xml"); //$NON-NLS-1$
      assertNotNull(is);

    } catch (IOException e) {
      e.printStackTrace();
    }

    finishTest();
  }*/

  /*public void testFilterDefinition4() {
    startTest();

    String definitionPath = "test/dashboard/panel4.filterpanel.xml"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    FilterPanelComponent filterPanel = new FilterPanelComponent(definitionPath, null, urlFactory, messages);
    filterPanel.setLoggingLevel(getLoggingLevel());
    String test1Name = "FilterPanelTest.testFilterDefinition4_html_" + System.currentTimeMillis(); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream(test1Name, ".html"); //$NON-NLS-1$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
    BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

    try {
      filterPanel.validate(session, requestHandler);
      boolean initOk = filterPanel.init();
      assertTrue(initOk);
      filterPanel.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue("Expected to throw exception since the static list of values were empty", true);
    }

    finishTest();
  }*/

  /*public void testFilterDefinition5() {
    startTest();

    String definitionPath = "test/dashboard/panel5.filterpanel.xml"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory("/testurl?"); //$NON-NLS-1$
    ArrayList messages = new ArrayList();

    FilterPanelComponent filterPanel = new FilterPanelComponent(definitionPath, null, urlFactory, messages);
    filterPanel.setLoggingLevel(getLoggingLevel());
    String test1Name = "FilterPanelTest.testFilterDefinition5_html_" + System.currentTimeMillis(); //$NON-NLS-1$
    OutputStream outputStream = getOutputStream(test1Name, ".html"); //$NON-NLS-1$
    String contentType = "text/html"; //$NON-NLS-1$

    SimpleParameterProvider requestParameters = new SimpleParameterProvider();
    SimpleParameterProvider sessionParameters = new SimpleParameterProvider();

    HashMap parameterProviders = new HashMap();
    parameterProviders.put(IParameterProvider.SCOPE_REQUEST, requestParameters);
    parameterProviders.put(IParameterProvider.SCOPE_SESSION, sessionParameters);
    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$

    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, false);
    BaseRequestHandler requestHandler = new BaseRequestHandler(session, null, outputHandler, null, urlFactory);

    try {
      filterPanel.validate(session, requestHandler);
      boolean initOk = filterPanel.init();
      assertTrue(initOk);
      filterPanel.handleRequest(outputStream, requestHandler, contentType, parameterProviders);
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Expected to throw exception since the output result type was misspelled", true);
    }

    finishTest();
  }*/
  
  public void testDummyTest() {}

  public static void main(String[] args) {
    FilterPanelTest test = new FilterPanelTest();
    test.setUp();
    try {
//      test.testFilterDefinition1();
//      test.testFilterDefinition3();
//      test.testFilterDefinition4();
//      test.testFilterDefinition5();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
