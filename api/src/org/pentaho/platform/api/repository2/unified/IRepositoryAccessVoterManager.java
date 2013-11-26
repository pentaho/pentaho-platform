/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
