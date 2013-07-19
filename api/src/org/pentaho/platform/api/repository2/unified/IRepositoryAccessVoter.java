/*
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
 * Copyright 2012-2013 Pentaho Corporation.  All rights reserved.
 *
 */
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
