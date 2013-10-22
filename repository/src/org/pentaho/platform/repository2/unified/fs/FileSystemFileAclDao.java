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

package org.pentaho.platform.repository2.unified.fs;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

public class FileSystemFileAclDao implements IRepositoryFileAclDao {

  public void addAce( Serializable fileId, RepositoryFileSid recipient, EnumSet<RepositoryFilePermission> permission ) {
    // TODO Auto-generated method stub

  }

  public RepositoryFileAcl createAcl( Serializable fileId, RepositoryFileAcl acl ) {
    // TODO Auto-generated method stub
    return null;
  }

  public RepositoryFileAcl getAcl( Serializable fileId ) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<RepositoryFileAce> getEffectiveAces( Serializable fileId, boolean forceEntriesInheriting ) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasAccess( String relPath, EnumSet<RepositoryFilePermission> permissions ) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setFullControl( Serializable fileId, RepositoryFileSid sid, RepositoryFilePermission permission ) {
    // TODO Auto-generated method stub

  }

  public RepositoryFileAcl updateAcl( RepositoryFileAcl acl ) {
    // TODO Auto-generated method stub
    return null;
  }

}
