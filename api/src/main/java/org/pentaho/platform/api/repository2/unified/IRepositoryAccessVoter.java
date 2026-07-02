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


package org.pentaho.platform.api.repository2.unified;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Repository voter to add custom security to the repository.
 * You should implement this interface if you need to create your custom security model.
 * The voter is used as a part of the chain in which each voter checks if a file from repository can be accessed
 * in scope of current request, taking into account user's details, the file itself and desired type of access.
 * 
 * @author rmansoor
 */
public interface IRepositoryAccessVoter {

  /**
   * Determines whether the user has access to perform a specific operation on a particular file in the repository.
   * based on the list of effective authorities from the holder.
   * 
   * @param file
   *         Parent folder or file.
   * @param operation
   *         Operation the user is trying to perform.
   * @param acl
   *         ACL of the folder/file where this operation is about to be performed.
   * @param session
   *         Hitachi Vantara session of the user.
   * @return Returns true if the user has the requested access.
   */

  public boolean hasAccess( final RepositoryFile file, final RepositoryFilePermission operation,
      final RepositoryFileAcl acl, final IPentahoSession session );

}
