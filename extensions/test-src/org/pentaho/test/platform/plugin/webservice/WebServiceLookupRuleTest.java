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

package org.pentaho.test.platform.plugin.webservice;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.plugin.action.xml.webservice.WebServiceLookupRule;
import org.pentaho.test.platform.engine.core.BaseTest;

//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.repository.messages.Messages;

@SuppressWarnings( "nls" )
public class WebServiceLookupRuleTest extends BaseTest {

  protected IPentahoConnection connection = null;

  protected WebServiceLookupRule wslr = null;

  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void testDummyTest() {
  }

  /**
   * Tests the success path of the xmlEncodeUrl method in the WebServiceLookupRule class
   */
  /*
   * public void testSuccess() { startTest(); IRuntimeContext context = run("test", "webservice",
   * "webservice-test-success.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */
  /**
   * Tests the failure path of the xmlEncodeUrl method in the WebServiceLookupRule class. This failure path is caused by
   * an invalid URL
   */
  /*
   * public void testBadUrl() { startTest(); IRuntimeContext context = run("test", "webservice",
   * "webservice-test-badurl.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */

  /**
   * Tests the failure path of the xmlEncodeUrl method in the WebServiceLookupRule class. This failure path is caused by
   * an no URL
   */
  /*
   * public void testBadInput() { startTest(); IRuntimeContext context = run("test", "webservice",
   * "webservice-test-nourl.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_FAILURE,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */

  /**
   * Tests the failure path of the xmlEncodeUrl method in the WebServiceLookupRule class. This failure path is caused by
   * an no URL
   */
  /*
   * public void testNoOutput() { startTest(); IRuntimeContext context = run("test", "webservice",
   * "webservice-test-no-output.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ assertEquals(
   * Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS,
   * context.getStatus()); //$NON-NLS-1$ finishTest(); }
   */
}
