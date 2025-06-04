/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.test.platform.plugin.webservice;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.plugin.action.xml.webservice.WebServiceLookupRule;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.repository.messages.Messages;

@SuppressWarnings( "nls" )
public class WebServiceLookupRuleIT extends BaseTest {

  protected IPentahoConnection connection = null;

  protected WebServiceLookupRule wslr = null;

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/solution";

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
