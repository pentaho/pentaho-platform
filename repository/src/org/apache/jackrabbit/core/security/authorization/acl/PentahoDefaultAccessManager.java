package org.apache.jackrabbit.core.security.authorization.acl;

import javax.jcr.AccessDeniedException;

import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.DefaultAccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;

/**
 * Extended version of <code>DefaultAccessManager</code>.
 * The only difference it is storing link on <code>AccessControlProvider</code> and has getter for it.
 */
public class PentahoDefaultAccessManager extends DefaultAccessManager {

  private AccessControlProvider accessControlProvider;

  @Override
  public void init( AMContext amContext, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager )
    throws AccessDeniedException, Exception {
    super.init( amContext, acProvider, wspAccessManager );
    accessControlProvider = acProvider;
  }

  public AccessControlProvider getAccessControlProvider() {
    return accessControlProvider;
  }
  
}
