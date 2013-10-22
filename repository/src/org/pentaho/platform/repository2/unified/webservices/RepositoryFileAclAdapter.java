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
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RepositoryFileAclAdapter extends XmlAdapter<RepositoryFileAclDto, RepositoryFileAcl> {

  @Override
  public RepositoryFileAclDto marshal( final RepositoryFileAcl v ) {
    RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.id = v.getId() != null ? v.getId().toString() : null;
    if ( v.getOwner() != null ) {
      aclDto.owner = v.getOwner().getName();
      aclDto.ownerType = v.getOwner().getType() != null ? v.getOwner().getType().ordinal() : -1;
    }
    aclDto.entriesInheriting = v.isEntriesInheriting();
    aclDto.aces = toAcesDto( v.getAces() );
    return aclDto;
  }

  protected List<RepositoryFileAclAceDto> toAcesDto( final List<RepositoryFileAce> aces ) {
    List<RepositoryFileAclAceDto> aceDtos = new ArrayList<RepositoryFileAclAceDto>();
    for ( RepositoryFileAce ace : aces ) {
      aceDtos.add( RepositoryFileAclAceAdapter.toAceDto( ace ) );
    }
    return aceDtos;
  }

  @Override
  public RepositoryFileAcl unmarshal( final RepositoryFileAclDto v ) {
    RepositoryFileAcl.Builder builder = null;
    if ( v.ownerType != -1 ) {
      if ( v.id != null ) {
        builder = new RepositoryFileAcl.Builder( v.id, v.owner, RepositoryFileSid.Type.values()[v.ownerType] );
      } else {
        builder = new RepositoryFileAcl.Builder( v.tenantPath, v.owner, RepositoryFileSid.Type.values()[v.ownerType] );
      }
    } else {
      builder = new RepositoryFileAcl.Builder( (Serializable) v.id, null );
    }
    builder.entriesInheriting( v.entriesInheriting );
    for ( RepositoryFileAclAceDto fileAclAceDto : v.aces ) {
      builder.ace( RepositoryFileAclAceAdapter.toAce( fileAclAceDto ) );
    }
    return builder.build();
  }

}
