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
 * Repository voter manager to store list of repository access voters
 * 
 * @author rmansoor
 * 
 */
public interface IRepositoryAccessVoterManager {

  /**
   * Register a new repository access voter to the platform
   * 
   * @param voter
   */
  public void registerVoter( IRepositoryAccessVoter voter );

  /**
   * Evaluate if the current caller has access to the perform given operation on the current repository item
   * 
   * @param file
   * @param operation
   * @param repositoryFileAcl
   * @param session
   * @return
   */
  public boolean hasAccess( final RepositoryFile file, final RepositoryFilePermission operation,
      final RepositoryFileAcl repositoryFileAcl, final IPentahoSession session );
}
