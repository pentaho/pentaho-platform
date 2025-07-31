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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class AccessVoterToLegacyAcl implements IRepositoryAccessVoter {

  private IAclVoter aclVoter;

  public AccessVoterToLegacyAcl( IAclVoter aclVoter ) {
    Assert.notNull( aclVoter, "AclVoter must not be null" );
    this.aclVoter = aclVoter;
  }

  @Override
  public boolean hasAccess( RepositoryFile file, RepositoryFilePermission operation, RepositoryFileAcl acl,
                            IPentahoSession session ) {

    Assert.notNull( file, "RepositoryFile must not be null" );
    Assert.notNull( operation, "RepositoryFilePermission must not be null" );
    Assert.notNull( acl, "RepositoryFileAcl must not be null" );

    return aclVoter.hasAccess( session, convert( file, acl ), mask( operation ) );
  }

  private int mask( RepositoryFilePermission permission ) {

    Assert.notNull( permission, "permission must not be null" );

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
            fileAcl.setRecipient( new SimpleGrantedAuthority( fileAce.getSid().getName() ) );
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
