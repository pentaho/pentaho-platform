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


package org.pentaho.platform.repository2.unified.webservices;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RepositoryFileAclAceAdapter extends XmlAdapter<RepositoryFileAclAceDto, RepositoryFileAce> {

  @Override
  public RepositoryFileAclAceDto marshal( RepositoryFileAce v ) {
    return toAceDto( v );
  }

  public static RepositoryFileAclAceDto toAceDto( RepositoryFileAce v ) {
    RepositoryFileAclAceDto aceDto = new RepositoryFileAclAceDto();
    RepositoryFileSid sid = v.getSid();
    aceDto.setRecipient( sid.getName() );
    aceDto.setRecipientType( sid.getType().ordinal() );
    aceDto.setPermissions( toIntPerms( v.getPermissions() ) );
    return aceDto;
  }

  @Override
  public RepositoryFileAce unmarshal( RepositoryFileAclAceDto v ) {
    return toAce( v );
  }

  public static RepositoryFileAce toAce( RepositoryFileAclAceDto v ) {
    return new RepositoryFileAce(
        new RepositoryFileSid( v.getRecipient(), RepositoryFileSid.Type.values()[v.getRecipientType()] ),
      toPerms( v.getPermissions() ) );
  }

  public static List<Integer> toIntPerms( EnumSet<RepositoryFilePermission> perms ) {
    List<Integer> intPerms = new ArrayList<Integer>();
    // guard against NPE (PDI-4926)
    if ( perms != null ) {
      for ( RepositoryFilePermission perm : perms ) {
        intPerms.add( perm.ordinal() );
      }
    }
    return intPerms;
  }

  public static EnumSet<RepositoryFilePermission> toPerms( List<Integer> intPerms ) {
    List<RepositoryFilePermission> perms = new ArrayList<RepositoryFilePermission>();
    // guard against NPE (PDI-4926)
    if ( intPerms != null ) {
      for ( int intPerm : intPerms ) {
        perms.add( RepositoryFilePermission.values()[intPerm] );
      }
    }
    return EnumSet.copyOf( perms );
  }
}
