package org.apache.jackrabbit.core.security.user; 

import java.util.Properties;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;

public class PentahoUserManagerImpl extends UserManagerImpl {

  public PentahoUserManagerImpl( SessionImpl session, String adminId, Properties config ) throws RepositoryException {
    super( session, adminId, config );
    // TODO Auto-generated constructor stub
  }

  /**
   * We are over riding this method to always set the forceHash value to to be value. It will then hash the password if it is a plain one
   * other wise it will store the encrypted password.
   * @param userNode
   * @param password
   * @param forceHash
   * @throws RepositoryException
   */
  void setPassword( NodeImpl userNode, String password, boolean forceHash ) throws RepositoryException {
    // TODO Auto-generated method stub
    super.setPassword( userNode, password, false );
  }

  
}
