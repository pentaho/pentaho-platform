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
package org.pentaho.test.platform.web;


import org.pentaho.actionsequence.dom.ActionSequenceDocument;
import org.pentaho.actionsequence.dom.actions.PivotViewAction;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoSystemException;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.web.servlet.AnalysisViewService;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;


/**
 * This group of unit tests exercise AnalysisViewService, the service which
 * allows creation of new analysis views. 
 * 
 * @author Will Gorman (wgorman@pentaho.org)
 */
@SuppressWarnings("nls")
public class AnalysisViewServiceTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/web-servlet-solution";


  public String getSolutionPath() {
      return SOLUTION_PATH;  
  }
  public void testLoadAnalysisViewTemplate() {
    AnalysisViewService avs = new AnalysisViewService();
    StandaloneSession initialSession = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    IPentahoSession session = sessionStartup(initialSession);
    try {
      ActionSequenceDocument doc = avs.loadAnalysisViewTemplate(session);
      
      // verify the doc contains key elements
      assertTrue(doc.getElement("/" + ActionSequenceDocument.ACTION_SEQUENCE + "/" + ActionSequenceDocument.ACTIONS_NAME + "/" + ActionSequenceDocument.ACTION_DEFINITION_NAME + "[component-name='PivotViewComponent']") instanceof PivotViewAction);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$)
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testLoadAnalysisViewTemplateNotFound() {
    String origTemplate = AnalysisViewService.ANALYSIS_VIEW_TEMPLATE;
    AnalysisViewService.ANALYSIS_VIEW_TEMPLATE = "dummy.xaction"; //$NON-NLS-1$
    try {
      AnalysisViewService avs = new AnalysisViewService();
      StandaloneSession initialSession = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
      IPentahoSession session = sessionStartup(initialSession);
      try {
        avs.loadAnalysisViewTemplate(session);
        fail();
      } catch (PentahoSystemException e) {
        assertTrue(e.getMessage(), e.getMessage().indexOf("dummy.xaction") >= 0); //$NON-NLS-1$
      }
    } finally {
      AnalysisViewService.ANALYSIS_VIEW_TEMPLATE = origTemplate;
    }
  }
  
  public void testLoadAnalysisViewTemplateInvalidXML() {
    String origTemplate = AnalysisViewService.ANALYSIS_VIEW_TEMPLATE;
    AnalysisViewService.ANALYSIS_VIEW_TEMPLATE = "mondrian.properties"; //$NON-NLS-1$
    try {
      AnalysisViewService avs = new AnalysisViewService();
      StandaloneSession initialSession = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
      IPentahoSession session = sessionStartup(initialSession);
      try {
        avs.loadAnalysisViewTemplate(session);
        fail();
      } catch (PentahoSystemException e) {
        assertTrue(e.getMessage(), e.getMessage().indexOf("mondrian.properties") >= 0); //$NON-NLS-1$
        assertTrue(e.getMessage(), e.getMessage().indexOf("parse") >= 0); //$NON-NLS-1$
      }
    } finally {
      AnalysisViewService.ANALYSIS_VIEW_TEMPLATE = origTemplate;
    }
  }
  
  /*
   * this is commented out until fixed in action sequence dom.  see jira case:
   * http://jira.pentaho.org/browse/BISERVER-1092 
   * 
  public void testLoadAnalysisViewTemplateInvalidFormat() {
    String origTemplate = AnalysisViewService.ANALYSIS_VIEW_TEMPLATE;
    AnalysisViewService.ANALYSIS_VIEW_TEMPLATE = "../olap/datasources.xml"; //$NON-NLS-1$
    try {
      AnalysisViewService avs = new AnalysisViewService();
      StandaloneSession initialSession = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
      IPentahoSession session = sessionStartup(initialSession);
      try {
        avs.loadAnalysisViewTemplate(session);
        fail();
      } catch (PentahoSystemException e) {
        e.printStackTrace();
        assertTrue(e.getMessage(), e.getMessage().indexOf("mondrian.properties") >= 0); //$NON-NLS-1$
        assertTrue(e.getMessage(), e.getMessage().indexOf("parse") >= 0); //$NON-NLS-1$
      }
    } finally {
      AnalysisViewService.ANALYSIS_VIEW_TEMPLATE = origTemplate;
    }
  }
  */
  
  public void testGenerateXAction() {
    AnalysisViewService avs = new AnalysisViewService();
    StandaloneSession initialSession = new StandaloneSession(Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    IPentahoSession session = sessionStartup(initialSession);
    try {
      String xml = avs.generateXAction(session, "TestTitle", "TestDescription", "TestModel", "TestJNDI", null /* JDBC */, "TestCube"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      
      assertTrue(xml.indexOf("TestTitle") >= 0); //$NON-NLS-1$
      assertTrue(xml.indexOf("TestDescription") >= 0); //$NON-NLS-1$
      assertTrue(xml.indexOf("TestModel") >= 0); //$NON-NLS-1$
      assertTrue(xml.indexOf("TestJNDI") >= 0); //$NON-NLS-1$
      assertTrue(xml.indexOf("TestCube") >= 0); //$NON-NLS-1$

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
  
  // TODO: Test Save XACTION (will do this once we have the full plumbing)
  
  
}
