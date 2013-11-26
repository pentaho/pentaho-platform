/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.Serializable;
import java.util.List;

/**
 * Handles delete, undelete, and permanent delete. Handles listing deleted files and purging some or all deleted
 * files.
 * 
 * @author mlowery
 */
public interface IDeleteHelper {

  /**
   * Deletes a file in a way that it can be recovered.
   * 
   * @param fileId
   */
  void deleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId )
    throws RepositoryException;

  /**
   * Recovers a deleted file to its original location.
   * 
   * @param fileId
   */
  void undeleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Serializable fileId )
    throws RepositoryException;

  /**
   * Deletes a file in a way that it cannot be recovered. (Note that "cannot be recovered" doesn't mean "shred"--it
   * means that the file cannot be recovered using this API.)
   * 
   * @param fileId
   */
  void permanentlyDeleteFile( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId ) throws RepositoryException;

  /**
   * Lists deleted files for this folder and user.
   * 
   * @param origParentFolderPath
   *          path to original parent folder
   * @param filter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*")
   * @return list of deleted files IDs for this folder and user
   */
  List<RepositoryFile> getDeletedFiles( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final String origParentFolderPath, final String filter ) throws RepositoryException;

  /**
   * Lists deleted files for this user. In this case, the path field of each file is the original path where it was
   * located prior to deletion. This is the "recycle bin" view.
   * 
   * @return list of deleted files for this user
   */
  List<RepositoryFile> getDeletedFiles( final Session session, final PentahoJcrConstants pentahoJcrConstants )
    throws RepositoryException;

  /**
   * Returns the absolute path of the original parent folder. Can be used by caller to checkout parent folder
   * before calling {@link #undeleteFile(Session, PentahoJcrConstants, Serializable)}.
   * 
   * @param fileId
   *          file id of deleted file
   */
  String getOriginalParentFolderPath( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final Serializable fileId ) throws RepositoryException;

}
