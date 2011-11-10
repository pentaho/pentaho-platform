package org.pentaho.platform.engine.security.userroledao.ws;

import javax.jws.WebService;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.userroledao.messages.Messages;

/**
 * Same as {@link UserRoleWebService} except that it uses task permissions to determine administrator status instead of 
 * {@code ISecurityHelper.isPentahoAdministrator}.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
@WebService(endpointInterface = "org.pentaho.platform.engine.security.userroledao.ws.IUserRoleWebService", serviceName = "userRoleService", portName = "userRoleServicePort", targetNamespace = "http://www.pentaho.org/ws/1.0")
public class AuthorizationPolicyBasedUserRoleWebService extends UserRoleWebService {

  @Override
  protected boolean isAdmin() {
    IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);
    if (policy == null) {
      throw new IllegalStateException(Messages.getInstance().getString(
          "AuthorizationPolicyBasedUserRoleWebService.ERROR_0001_MISSING_AUTHZ_POLICY")); //$NON-NLS-1$
    }
    return policy.isAllowed("org.pentaho.security.administerSecurity"); //$NON-NLS-1$
  }
}
