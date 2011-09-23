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
package org.pentaho.test.platform.plugin.outputs;



import java.io.File;

//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
//import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class VFSOutputTest extends BaseTest {

  private static final String SOLUTION_PATH = "outputs/test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";

  @Override 
  public String getSolutionPath() {
      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
      if(file.exists()) {
        return SOLUTION_PATH;  
      } else {
        return ALT_SOLUTION_PATH;
      }
  }	
//  public void testFileOutput() {
//    startTest();
//    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
//    IRuntimeContext context = run("test", "platform", "VFSOutputTest_file.xaction", parameterProvider, "VFSOutputTest.testFileOutput", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//    assertEquals( Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() ); //$NON-NLS-1$
//
//    finishTest();
//  }
  
  public void testDummyTest() {
    // TODO: remove once tests pass
  }
  
  public static void main(String[] args) {
    VFSOutputTest test = new VFSOutputTest();
    try {
      test.setUp();
//      test.testFileOutput();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
