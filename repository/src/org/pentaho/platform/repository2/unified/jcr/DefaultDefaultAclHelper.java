package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;

/**
 * Creates an ACL with current user as the owner and entries inheriting.
 * 
 * @author mlowery
 */
public class DefaultDefaultAclHelper implements IDefaultAclHelper {

  private String getUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null, "this method cannot be called with a null IPentahoSession");
    return pentahoSession.getName();
  }

  @Override
  public RepositoryFileAcl createDefaultAcl() {
    return new RepositoryFileAcl.Builder(getUsername()).entriesInheriting(true).build();
  }

}
