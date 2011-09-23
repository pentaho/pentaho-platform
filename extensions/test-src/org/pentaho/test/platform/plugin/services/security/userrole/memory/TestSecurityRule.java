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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 * 
 * Created Apr 17, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.plugin.services.security.userrole.memory;

//import org.pentaho.platform.api.engine.IActionParameter;
//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.engine.core.messages.Messages;
//import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
//import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.engine.core.BaseTest;

public class TestSecurityRule extends BaseTest {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestSecurityRule.class);
    System.exit(0);
  }

  //  public TestSecurityRule(String arg0) {
  //    super(arg0);
  //  }

  public TestSecurityRule() {
    super();
  }

  public void setUp() {
    // TODO: Uncomment once tests are fixed
//    super.setUp();
//    MockSecurityUtility.setupApplicationContext();
  }

//  public void testExecuteSecurityRule() {
//    StandaloneSession session = new StandaloneSession("JUnit TestSecurityRule"); //$NON-NLS-1$
//    // Mock up credentials for ACL Testing
//    MockSecurityUtility.createPat(session);
//
//    SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
//
//    // At this point, the security provider should have already been set up.
//    // Run the action sequence - We're specifically calling this version because
//    // we need to get our session in there.
//    IRuntimeContext context = run(
//        "test", "rules", "securitytest.xaction", null, false, parameterProvider, null, session); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//    assertNotNull(context);
//    assertEquals(
//        Messages.getString("BaseTest.USER_RUNNING_ACTION_SEQUENCE"), IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus()); //$NON-NLS-1$
//    IActionParameter output = context.getOutputParameter("rule-result"); //$NON-NLS-1$
//    assertNotNull(output);
//    String outputValue = output.getStringValue();
//    assertNotNull(outputValue);
//
//    String expected = "Userid: pat, Authenticated(true), Administrator(false)\n" + //$NON-NLS-1$
//        " User Roles: ROLE_DEV, ROLE_AUTHENTICATED\n" + //$NON-NLS-1$
//        " System Roles: ROLE_DEV, ROLE_AUTHENTICATED, ROLE_IS, ROLE_ADMIN, ROLE_DEVMGR, ROLE_CEO, ROLE_CTO\n" + //$NON-NLS-1$
//        " System Users: tiffany, joe, suzy, pat, admin"; //$NON-NLS-1$
//    assertEquals(outputValue, expected);
//  }
  
  
  public void testDummyTest() {
    // TODO: remove once tests pass
  }

}
