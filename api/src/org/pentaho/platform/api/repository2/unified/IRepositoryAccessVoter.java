package org.pentaho.platform.api.repository2.unified;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Repository voter to add custom security to the repository
 * @author rmansoor
 */
public interface IRepositoryAccessVoter {

  /**
   * Determines whether the user has access to perform a specific operation on a particular file in the repository. 
   * based on the list of effective authorities from the holder.
   * 
   * @param file - parent folder or file
   * @param operation - operation user is trying to perform
   * @param acl - acl of the folder/file where this operation is about to be performed
   * @param session - Pentaho Session of the user
   * @return true if the user has the requested access.
   */

  public boolean hasAccess(final RepositoryFile file, final RepositoryFilePermission operation, final RepositoryFileAcl acl, final IPentahoSession session);

}
