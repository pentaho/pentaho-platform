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

package org.pentaho.platform.repository2.unified.webservices;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

import javax.xml.bind.annotation.adapters.XmlAdapter;
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
    aceDto.recipient = v.getSid().getName();
    aceDto.recipientType = v.getSid().getType().ordinal();
    aceDto.permissions = toIntPerms( v.getPermissions() );
    return aceDto;
  }

  @Override
  public RepositoryFileAce unmarshal( RepositoryFileAclAceDto v ) {
    return toAce( v );
  }

  public static RepositoryFileAce toAce( RepositoryFileAclAceDto v ) {
    return new RepositoryFileAce(
        new RepositoryFileSid( v.recipient, RepositoryFileSid.Type.values()[v.recipientType] ),
      toPerms( v.permissions ) );
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
