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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.authentication.AbstractLoginModule;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.messages.Messages;


/**
 * Adds support for "pre-authentication" scenarios. An example is a web application participating in SSO. In this
 * case, the web application does not handle user passwords. So it doesn't have the user's password to submit with the
 * repository.login() call. One implementation could simply ignore passwords. But that assumes that all incoming logins
 * have been pre-authenticated. This is a dangerous assumption. You cannot guarantee this. Instead, we must have 
 * something that proves that we can trust the caller has already authenticated the user. For this, we use a key that
 * is known to the caller and to this login module. If the key is present and correct, mark the user as authenticed.
 * 
 * <p>
 * See <a href="http://issues.apache.org/jira/browse/JCR-2355">JCR-2355</a>. It describes this improvement exactly 
 * except that we do more than simply check for the presence of the attribute. We make sure the value is among those
 * configured for this login module.
 * </p>
 * @author mlowery
 */
public abstract class AbstractPentahoLoginModule extends AbstractLoginModule {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(AbstractPentahoLoginModule.class);

  /**
   * javax.jcr.Credentials attribute name. If an attribute with this name is present, then the application logging in on
   * behalf of the user is claiming to have already authenticated the user. If this login module recognizes the value
   * of this attribute, then the user will be marked as authenticated (without checking a password).
   */
  private static final String ATTR_PRE_AUTHENTICATION_TOKEN = "pre_authentication_token"; //$NON-NLS-1$

  /**
   * Comma separated list of known tokens. If a Credentials instance has a preauthentication token, it must match one 
   * of the values in this list. Ideally, there is a token per application. In this way, other applications are 
   * unaffected should a token have to be blacklisted.
   */
  private static final String KEY_PRE_AUTHENTICATION_TOKENS = "preAuthenticationTokens"; //$NON-NLS-1$

  private static final String PRE_AUTHENTICATION_TOKEN_SEPARATOR = ","; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private static Set<String> preAuthenticationTokens = new HashSet<String>();

  // ~ Constructors ====================================================================================================

  public AbstractPentahoLoginModule() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Override
  protected void doInit(CallbackHandler callbackHandler, Session session, Map options) throws LoginException {

    if (options.containsKey(KEY_PRE_AUTHENTICATION_TOKENS)) {

      String preAuthenticationTokensString = (String) options.get(KEY_PRE_AUTHENTICATION_TOKENS);
      String[] tokens = preAuthenticationTokensString.split(PRE_AUTHENTICATION_TOKEN_SEPARATOR);

      if (tokens.length == 0) {
        throw new LoginException(Messages.getInstance().getString(
            "AbstractPentahoLoginModule.ERROR_0001_PRE_AUTH_TOKENS_MALFORMED", KEY_PRE_AUTHENTICATION_TOKENS)); //$NON-NLS-1$
      }

      for (String token : tokens) {
        preAuthenticationTokens.add(token.trim());
      }

      logger.debug("preAuthenticationTokens=" + preAuthenticationTokens); //$NON-NLS-1$
    }

  }

  @Override
  public boolean login() throws LoginException {
    if (!isInitialized()) {
      logger.warn("Unable to perform login: initialization not completed."); //$NON-NLS-1$
      return false;
    }

    // check for availablity of Credentials;
    Credentials creds = getCredentials();
    if (creds == null) {
      logger.warn("No credentials available; trying anonymous authentication"); //$NON-NLS-1$
    }
    try {
      Principal userPrincipal = getPrincipal(creds);
      if (userPrincipal == null) {
        // unknown principal or a Group-principal
        logger.debug("Unknown User -> ignore."); //$NON-NLS-1$
        return false;
      }
      boolean authenticated;
      // test for anonymous, pre-authentication, impersonation or common authentication.
      if (isAnonymous(creds) || isPreAuthenticated(creds)) {
        authenticated = true;
      } else if (isImpersonation(creds)) {
        authenticated = impersonate(userPrincipal, creds);
      } else {
        authenticated = authenticate(userPrincipal, creds);
      }

      // process authenticated user
      if (authenticated) {
        if (creds instanceof SimpleCredentials) {
          credentials = (SimpleCredentials) creds;
        } else {
          credentials = new SimpleCredentials(getUserID(creds), new char[0]);
        }
        principal = userPrincipal;
        return true;
      }
    } catch (RepositoryException e) {
      logger.error("Login failed:", e); //$NON-NLS-1$
    }
    return false;
  }

  protected boolean isPreAuthenticated(final Credentials creds) {
    if (!(creds instanceof SimpleCredentials)) {
      return false;
    }
    SimpleCredentials simpleCredentials = (SimpleCredentials) creds;
    Object preAuthenticationToken = simpleCredentials.getAttribute(ATTR_PRE_AUTHENTICATION_TOKEN);
    if (preAuthenticationToken == null) {
      return false;
    }
    boolean preAuthenticated = preAuthenticationTokens.contains(preAuthenticationToken);
    if (preAuthenticated) {
      logger.debug(simpleCredentials.getUserID() + " is pre-authenticated"); //$NON-NLS-1$
    } else {
      logger.debug("pre-authentication token rejected"); //$NON-NLS-1$
    }
    return preAuthenticated;
  }
}