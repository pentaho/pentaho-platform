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

import java.io.OutputStream;

import org.junit.Ignore;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import com.dumbster.smtp.SimpleSmtpServer;

@SuppressWarnings("nls")
@Ignore
public class EmailTest extends BaseTestCase {

  private SimpleSmtpServer smtpServer;
  public EmailTest() {
    super(SOLUTION_PATH);
  }

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() throws Exception{
    super.setUp();
    smtpServer = SimpleSmtpServer.start();
  }

  public void tearDown() throws Exception {
    try {
      smtpServer.stop();
    } catch (NullPointerException ex) {
      // ignored.
    }
    super.tearDown();
  }
  
  public void testEmailLoop() {
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_only_email-loop", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run(
        SOLUTION_PATH + "/test/email/", "text_only_email-loop.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
  }

  public void testEmailOnly() {
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_only_email", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run(
        SOLUTION_PATH + "/test/email/", "text_only_email.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
  }

  /*public void testEmailLoopProp() {
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_only_email-loop_property", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run(
        SOLUTION_PATH + "/test/email/", "text_only_email-loop_property.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
  }*/

  public void testEmailHtml() {
    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
    OutputStream outputStream = getOutputStream(SOLUTION_PATH, "text_html_attach_email", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    SimpleOutputHandler outputHandler = new SimpleOutputHandler(outputStream, true);
    IRuntimeContext context = run(
        SOLUTION_PATH + "/test/email/", "text_html_attach_email.xaction", parameterProvider, outputHandler); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$        
    assertEquals(
        Messages.getInstance().getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
  }

  public static void main(String[] args) {
    EmailTest test = new EmailTest();
    try {
      test.testEmailLoop();
      test.testEmailOnly();
//      test.testEmailLoopProp();
      test.testEmailHtml();
    } finally {
    }
  }
}
