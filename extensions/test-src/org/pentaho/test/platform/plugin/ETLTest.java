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

import java.util.Map;

import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class ETLTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put("kettle", "kettle"); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  /*public void testKettleTransform1() {

    startTest();
    IRuntimeContext context = run("test", "etl", "ETLTransform1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    assertEquals(context.getOutputNames().contains("rule-result"), true); //$NON-NLS-1$
    IActionParameter param = context.getOutputParameter("rule-result"); //$NON-NLS-1$

    OutputStream os = getOutputStream("DataTest.testKettleTransform1", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
    IPentahoResultSet resultSet = param.getValueAsResultSet();
    String soapString = SoapHelper.toSOAP("ETL Result", resultSet); //$NON-NLS-1$
    try {
      os.write(soapString.getBytes());
    } catch (Exception e) {

    }
    finishTest();

  }*/

 /* public void testKettleJob1() {

    startTest();
    // this writes a XML file directly into test/tmp/DataTest.testKettleJob1.xml
    IRuntimeContext context = run("test", "etl", "ETLJob1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$

    finishTest();

  }*/
  
  public void testDummyTest() {}

  public static void main(String[] args) {
    ETLTest test = new ETLTest();
    test.setUp();
    try {
//      test.testKettleTransform1();
//      test.testKettleJob1();
    } finally {
//      test.tearDown();
//      BaseTest.shutdown();
    }
  }

}
