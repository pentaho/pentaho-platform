package org.pentaho.platform.datasource;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ActionBasedSecurityService {
  private IAuthorizationPolicy policy;

  private static final String ACTION_READ = "org.pentaho.repository.read"; //$NON-NLS-1$

  private static final String ACTION_CREATE = "org.pentaho.repository.create"; //$NON-NLS-1$

  private static final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity"; //$NON-NLS-1$

  public ActionBasedSecurityService() {
    policy = PentahoSystem.get(IAuthorizationPolicy.class);
  }
  public ActionBasedSecurityService(IAuthorizationPolicy policy) {
    this.policy = policy;
  }
  
  public void checkAdministratorAccess() throws PentahoAccessControlException{
    boolean access = policy.isAllowed(ACTION_READ) && policy.isAllowed(ACTION_CREATE)
    && policy.isAllowed(ACTION_ADMINISTER_SECURITY);
    if(!access) {
      throw new 
      PentahoAccessControlException("You have to be admin to perform this operation");
    }
  }
  
}
