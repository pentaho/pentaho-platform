/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.webservices;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RepositoryFileAclAdapter extends XmlAdapter<RepositoryFileAclDto, RepositoryFileAcl> {

  @Override
  public RepositoryFileAclDto marshal( final RepositoryFileAcl v ) {
    RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    Serializable id = v.getId();
    aclDto.setId( id != null ? id.toString() : null );
    RepositoryFileSid owner = v.getOwner();
    if ( owner != null ) {
      aclDto.setOwner( owner.getName() );
      aclDto.setOwnerType( owner.getType() != null ? owner.getType().ordinal() : -1 );
    }
    aclDto.setAces( toAcesDto( v.getAces() ), v.isEntriesInheriting() );
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
    if ( v.getOwnerType() != -1 ) {
      if ( v.getId() != null ) {
        builder = new RepositoryFileAcl.Builder( v.getId(), v.getOwner(), RepositoryFileSid.Type.values()[v.getOwnerType()] );
      } else {
        builder = new RepositoryFileAcl.Builder( v.getTenantPath(), v.getOwner(), RepositoryFileSid.Type.values()[v.getOwnerType()] );
      }
    } else {
      builder = new RepositoryFileAcl.Builder( (Serializable) v.getId(), null );
    }
    builder.entriesInheriting( v.isEntriesInheriting() );
    for ( RepositoryFileAclAceDto fileAclAceDto : v.getAces() ) {
      builder.ace( RepositoryFileAclAceAdapter.toAce( fileAclAceDto ) );
    }
    return builder.build();
  }

}
