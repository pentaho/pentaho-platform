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

import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class PrintTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

 /* public void testPrinting1() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest1.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }
*/
 public void testPrinting_NoReportOutput() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_NoReportOutput.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  /*public void testPrinting_NoPrinterName() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_NoPrinterName.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }
*/
  public void testPrinting_NoPrintFile() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_NoPrintFile.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public void testPrinting_PrintFileAsInput() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_PrintFileAsInput.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public void testPrinting_FakeFileName() {
    startTest();
    IRuntimeContext context = run("test", "printing", "PrintTest_FakeFileName.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE, context.getStatus()); //$NON-NLS-1$
    finishTest();
  }

  public static void main(String[] args) {
    PrintTest test = new PrintTest();
    test.setUp();
    try {
     /* test.testPrinting1();
      test.testPrinting_NoPrinterName();*/
      test.testPrinting_NoPrintFile();
      test.testPrinting_NoReportOutput();
      test.testPrinting_PrintFileAsInput();
      test.testPrinting_FakeFileName();

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }

  }
}
