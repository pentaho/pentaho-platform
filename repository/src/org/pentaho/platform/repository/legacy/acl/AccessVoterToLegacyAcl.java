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

package org.pentaho.platform.repository.legacy.acl;

import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class AccessVoterToLegacyAcl implements IRepositoryAccessVoter {

  private IAclVoter aclVoter;

  public AccessVoterToLegacyAcl( IAclVoter aclVoter ) {
    Assert.notNull( aclVoter );
    this.aclVoter = aclVoter;
  }

  @Override
  public boolean hasAccess( RepositoryFile file, RepositoryFilePermission operation, RepositoryFileAcl acl,
      IPentahoSession session ) {

    Assert.notNull( file );
    Assert.notNull( operation );
    Assert.notNull( acl );

    return aclVoter.hasAccess( session, convert( file, acl ), mask( operation ) );
  }

  private int mask( RepositoryFilePermission permission ) {

    Assert.notNull( permission );

    if ( RepositoryFilePermission.READ == permission ) {
      return IPentahoAclEntry.PERM_EXECUTE;
    } else if ( RepositoryFilePermission.WRITE == permission ) {
      return IPentahoAclEntry.PERM_CREATE | IPentahoAclEntry.PERM_UPDATE;
    } else if ( RepositoryFilePermission.DELETE == permission ) {
      return IPentahoAclEntry.PERM_DELETE;
    } else if ( RepositoryFilePermission.ACL_MANAGEMENT == permission ) {
      return IPentahoAclEntry.PERM_UPDATE_PERMS;
    } else if ( RepositoryFilePermission.ALL == permission ) {
      return IPentahoAclEntry.PERM_FULL_CONTROL;
    } else {
      return IPentahoAclEntry.PERM_NOTHING;
    }
  }

  private LegacyRepositoryFile convert( RepositoryFile file, RepositoryFileAcl acl ) {

    LegacyRepositoryFile legacy = new LegacyRepositoryFile( file.getName(), file.getPath(), file.isFolder() );

    legacy.setId( file.getId() );

    if ( file.getLastModifiedDate() != null ) {
      legacy.setLastModified( file.getLastModifiedDate().getTime() );
    }

    List<IPentahoAclEntry> legacyAcls = new ArrayList<IPentahoAclEntry>();
    for ( RepositoryFileAce fileAce : acl.getAces() ) {
      if ( fileAce != null && fileAce.getSid() != null && fileAce.getPermissions() != null ) {
        for ( RepositoryFilePermission filePermission : fileAce.getPermissions() ) {

          PentahoAclEntry fileAcl = new PentahoAclEntry();

          if ( RepositoryFileSid.Type.USER == fileAce.getSid().getType() ) {
            // user
            fileAcl.setRecipient( fileAce.getSid().getName() );
          } else {
            // role
            fileAcl.setRecipient( new GrantedAuthorityImpl( fileAce.getSid().getName() ) );
          }
          fileAcl.setMask( mask( filePermission ) );
          legacyAcls.add( fileAcl );
        }
      }
    }

    legacy.setAccessControls( legacyAcls );
    return legacy;
  }
}
