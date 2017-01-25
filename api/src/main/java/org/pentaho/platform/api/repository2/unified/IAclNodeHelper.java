package org.pentaho.platform.api.repository2.unified;

import java.util.EnumSet;

/**
 * <p>The interface for operations over ACL nodes.</p>
 *
 * <p>Certain entities, such as data sources, are stored in areas of the repository in which non-admin users have no
 * natural access. In order to provide ACLs on these entities surrogate nodes are created which store the ACLs instead.
 * Implementations of this class are responsible for storing and querying these surrogate ACL nodes.</p>
 *
 *
 * @author Andrey Khayrutdinov
 * @author Nick Baker
 */
public interface IAclNodeHelper {

  /**
   * Returns <code>true</code> if the current user has access to <code>repositoryFile</code> by way of ACL node.
   *
   * @param repositoryFile file for which to check access by ACL node
   * @param permissions EnumSet of permissions to check against the repositoryFile
   * @return <code>true</code> if the user can access the Repository File governed by this ACL node
   */
  boolean canAccess( RepositoryFile repositoryFile, EnumSet<RepositoryFilePermission> permissions );


  /**
   * Returns an ACL for <code>repositoryFile</code>. If none exists, <code>null</code> is returned. <b>Note:</b> this
   * method should be invoked with 'repository admin' privileges.
   *
   * @param repositoryFile file for which to retrieve ACLs for
   * @return ACL rules if exist or <code>null</code> otherwise
   */
  RepositoryFileAcl getAclFor( RepositoryFile repositoryFile );

  /**
   * Sets <code>acl</code> for <code>repositoryFile</code>. If a ACL node does not exist, it is created. If <code>acl</code> is
   * <code>null</code>, the ACL node is removed.
   *
   * @param repositoryFile data source
   * @param acl            an ACL rules for the data source
   */
  void setAclFor( RepositoryFile repositoryFile, RepositoryFileAcl acl );

  /**
   * Deletes the ACL node associated with the <code>repositoryFile</code> if it exists.
   *
   * @param repositoryFile data source
   */
  void removeAclFor( RepositoryFile repositoryFile );

}
