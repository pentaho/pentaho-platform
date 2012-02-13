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
package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.authentication.Authentication;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;

/**
 * A Jackrabbit {@code LoginModule} that delegates to a Spring Security {@link AuthenticationManager}.
 * 
 * @author mlowery
 */
public class SpringSecurityLoginModule extends AbstractPentahoLoginModule {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(SpringSecurityLoginModule.class);

  // ~ Instance fields =================================================================================================

  private AuthenticationManager authenticationManager;

  // ~ Constructors ====================================================================================================

  public SpringSecurityLoginModule() {
    super();
  }

  // ~ Methods =========================================================================================================

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Searches in classpath for resource named {@code beanRefFactory.xml} which contains a single bean which is itself
   * a bean factory. That bean factory (and all beans in it) are created once. Pattern copied from 
   * {@code JbossSpringSecurityLoginModule}.
   * </p>
   * 
   * @see SingletonBeanFactoryLocator
   */
  @Override
  protected void doInit(final CallbackHandler callbackHandler, final Session session, final Map options)
      throws LoginException {
    // call superclass to setup pre-authentication
    super.doInit(callbackHandler, session, options);

    authenticationManager = getAuthenticationManager(callbackHandler, session, options);
  }

  protected AuthenticationManager getAuthenticationManager(final CallbackHandler callbackHandler,
      final Session session, final Map options) {
    AuthenticationManager am = PentahoSystem.get(AuthenticationManager.class);
    Assert.state(am != null);
    return am;
  }

  /**
   * {@inheritDoc}
   * 
   * Creates a {@code UsernamePasswordAuthenticationToken} from the given {@code principal} and {@code credentials} and 
   * passes to Spring Security {@code AuthenticationManager}.
   */
  @Override
  protected Authentication getAuthentication(final Principal principal, final Credentials credentials)
      throws RepositoryException {

    // only handles SimpleCredential instances; DefaultLoginModule behaves the same way (albeit indirectly)
    if (!(credentials instanceof SimpleCredentials)) {
      logger.debug("credentials not instance of SimpleCredentials; returning null"); //$NON-NLS-1$
      return null;
    }

    SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;

    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(simpleCredentials.getUserID(),
        String.valueOf(simpleCredentials.getPassword()));

    boolean authenticated = false;

    try {
      // delegate to Spring Security
      authenticationManager.authenticate(token);
      authenticated = true;
    } catch (AuthenticationException e) {
      logger.debug("authentication exception", e); //$NON-NLS-1$
    }

    final boolean authenticateResult = authenticated;

    return new Authentication() {
      public boolean canHandle(Credentials credentials) {
        // this is decided earlier in getAuthentication
        return true;
      }

      public boolean authenticate(Credentials credentials) throws RepositoryException {
        return authenticateResult;
      }
    };

  }

  /**
   * {@inheritDoc}
   * 
   * <p>Implementation copied from {@link org.apache.jackrabbit.core.security.simple.SimpleLoginModule}. Delegates to a 
   * {@code PrincipalProvider}.</p>
   */
  @Override
  protected Principal getPrincipal(final Credentials credentials) {
    String userId = getUserID(credentials);
    Principal principal = principalProvider.getPrincipal(userId);
    if (principal == null || principal instanceof Group) {
      // no matching user principal
      return null;
    } else {
      return principal;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * <p>Not implemented.</p>
   */
  @Override
  protected boolean impersonate(final Principal principal, final Credentials credentials) throws RepositoryException,
      LoginException {
    throw new UnsupportedOperationException();
  }

}
