/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

/**
 * A data access object for reading and writing {@code RepositoryFileAcl} instances. The methods in this interface
 * might closely resemble those in {@link IUnifiedRepository} but this interface is not part of the public Pentaho
 * API and can evolve independently.
 * 
 * @author mlowery
 */
public interface IRepositoryFileAclDao {

  /**
   * Returns the list of access control entries that will be used to make an access control decision.
   * 
   * @param fileId
   *          file id
   * @param forceEntriesInheriting
   *          {@code true} to treat ACL as if {@code isEntriesInheriting} was true; this avoids having the caller
   *          fetch the parent of ACL belonging to file with {@code fileId}; no change is persisted to the ACL
   * @return list of ACEs
   */
  List<RepositoryFileAce> getEffectiveAces( final Serializable fileId, final boolean forceEntriesInheriting );

  /**
   * Returns {@code true} if the user has all of the permissions. The implementation should return {@code false} if
   * either the user does not have access or the file does not exist.
   * 
   * @param relPath
   *          path to file
   * @param permissions
   *          permissions to check
   * @return {@code true} if user has access
   */
  boolean hasAccess( final String relPath, final EnumSet<RepositoryFilePermission> permissions );

  /**
   * Returns ACL for file.
   * 
   * @param fileId
   *          file id
   * @return access control list
   */
  RepositoryFileAcl getAcl( final Serializable fileId );

  /**
   * Updates an ACL.
   * 
   * @param acl
   *          ACL to set; must have non-null id
   * @return updated ACL
   */
  RepositoryFileAcl updateAcl( final RepositoryFileAcl acl );

  /**
   * Creates an ACL.
   * 
   * @param fileId
   *          file id
   * @param acl
   *          file acl
   * @return acl with id populated
   */
  RepositoryFileAcl createAcl( final Serializable fileId, final RepositoryFileAcl acl );

  /**
   * Adds ACE to end of ACL. ACL should already have been created. {@link #updateAcl(RepositoryFileAcl)} should not
   * need to be called after this method returns.
   * 
   * @param fileId
   *          file id
   * @param recipient
   *          recipient of permission
   * @param permission
   *          permission to set
   */
  void addAce( final Serializable fileId, final RepositoryFileSid recipient,
      final EnumSet<RepositoryFilePermission> permission );

  /**
   * Gives full control (all permissions) to given sid. {@link #updateAcl(RepositoryFileAcl)} should not need to be
   * called after this method returns.
   * 
   * @param fileId
   *          file id
   * @param sid
   *          sid that should own the domain object associated with this ACL
   * @param permision
   *          permission representing full control
   */
  void setFullControl( final Serializable fileId, RepositoryFileSid sid, final RepositoryFilePermission permission );

}
