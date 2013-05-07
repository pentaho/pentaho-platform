package org.pentaho.platform.repository2.unified;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.springframework.util.Assert;

public class RepositoryAccessVoterManager implements IRepositoryAccessVoterManager{

  private IAuthorizationPolicy authorizationPolicy;
  private String repositoryAdminUsername;
  private List<IRepositoryAccessVoter> voters;


  public RepositoryAccessVoterManager(final IAuthorizationPolicy authorizationPolicy, final String repositoryAdminUsername) {
    super();
    this.authorizationPolicy = authorizationPolicy;
    this.repositoryAdminUsername = repositoryAdminUsername;
  }

  public RepositoryAccessVoterManager(final List<IRepositoryAccessVoter> voters, final IAuthorizationPolicy authorizationPolicy, final String repositoryAdminUsername) {
    this(authorizationPolicy, repositoryAdminUsername);
    Assert.notNull(voters);
    this.voters = new ArrayList<IRepositoryAccessVoter>();
    this.voters.addAll(voters);
  }

  public void registerVoter(IRepositoryAccessVoter voter) {
    voters.add(voter);
  }
  

  @Override
  public boolean hasAccess(RepositoryFile file, RepositoryFilePermission operation,
      RepositoryFileAcl repositoryFileAcl, IPentahoSession session) {
    if(voters != null && !authorizationPolicy.isAllowed(AdministerSecurityAction.NAME) 
        && (session.getName() != null && !session.getName().equals(repositoryAdminUsername))) {
      for(IRepositoryAccessVoter voter:voters) {
        if(!voter.hasAccess(file, operation, repositoryFileAcl, session)) {
          return false;
        }
      }
    }
    return true;
  }
  
  
}
