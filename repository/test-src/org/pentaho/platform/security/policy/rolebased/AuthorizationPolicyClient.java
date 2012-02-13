/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.security.policy.rolebased;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService;
import org.pentaho.platform.security.policy.rolebased.ws.IRoleAuthorizationPolicyRoleBindingDaoWebService;

import static org.junit.Assert.assertEquals;

/**
 * To run this, put Metro JARs in your classpath.
 */
public class AuthorizationPolicyClient {
  private IAuthorizationPolicy policy;

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  @Before
  public void setUp() throws Exception {
    Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/authorizationPolicy?wsdl"),
        new QName("http://www.pentaho.org/ws/1.0", "authorizationPolicy"));

    policy = service.getPort(IAuthorizationPolicyWebService.class);

    service = Service.create(new URL("http://localhost:8080/pentaho/webservices/roleBindingDao?wsdl"), new QName(
        "http://www.pentaho.org/ws/1.0", "roleBindingDao"));

    roleBindingDao = service.getPort(IRoleAuthorizationPolicyRoleBindingDaoWebService.class);

    // basic auth
    ((BindingProvider) policy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "suzy");
    ((BindingProvider) policy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) policy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
    ((BindingProvider) roleBindingDao).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "joe");
    ((BindingProvider) roleBindingDao).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    // accept cookies to maintain session on server
    ((BindingProvider) roleBindingDao).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
  }

  @Test
  public void testEverything() {
    final String RUNTIME_ROLE_AUTHENTICATED = "Authenticated";
    final String LOGICAL_ROLE_CREATOR = "org.pentaho.di.creator";
    final String LOGICAL_ROLE_READER = "org.pentaho.di.reader";
    roleBindingDao.setRoleBindings(RUNTIME_ROLE_AUTHENTICATED, Arrays.asList(new String[] { LOGICAL_ROLE_READER,
        LOGICAL_ROLE_CREATOR }));

    List<String> allowedActions = policy.getAllowedActions("org.pentaho");
    assertEquals(2, allowedActions.size());
  }
}
