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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

/**
 * Converts {@code RepositoryFileTree} into JAXB-safe object and vice-versa.
 *
 * @author mlowery
 */
public class RepositoryFileTreeAdapter extends XmlAdapter<RepositoryFileTreeDto, RepositoryFileTree> {
  private Set<String> membersSet;
  private boolean exclude;
  private boolean includeAcls;

  public RepositoryFileTreeAdapter() {

  }

  public RepositoryFileTreeAdapter( RepositoryRequest repositoryRequest ) {
    if ( repositoryRequest.getExcludeMemberSet() != null && !repositoryRequest.getExcludeMemberSet().isEmpty() ) {
      this.exclude = true;
      this.membersSet = repositoryRequest.getExcludeMemberSet();
    } else {
      this.exclude = false;
      this.membersSet = repositoryRequest.getIncludeMemberSet();
    }
    this.includeAcls = repositoryRequest.isIncludeAcls();

  }

  @Override
  public RepositoryFileTreeDto marshal( final RepositoryFileTree v ) {
    RepositoryFileTreeDto treeDto = new RepositoryFileTreeDto();

    RepositoryFileDto file = RepositoryFileAdapter.toFileDto( v, membersSet, exclude, includeAcls );
    if ( file != null ) {
      treeDto.setFile( RepositoryFileAdapter.toFileDto( v, membersSet, exclude, includeAcls ) );
      List<RepositoryFileTreeDto> children = null;
      if ( v.getChildren() != null ) {
        children = new ArrayList<RepositoryFileTreeDto>();
        for ( RepositoryFileTree child : v.getChildren() ) {
          RepositoryFileTreeDto childTreeDto = marshal( child );
          if ( childTreeDto != null ) {
            children.add( childTreeDto );
          }
        }
      }

      treeDto.setChildren( children );

      return treeDto;
    } else {
      return null;
    }
  }

  @Override
  public RepositoryFileTree unmarshal( final RepositoryFileTreeDto v ) {
    List<RepositoryFileTree> children = null;
    List<RepositoryFileTreeDto> childrenDTO = v.getChildren();
    if ( childrenDTO != null ) {
      children = new ArrayList<RepositoryFileTree>();
      for ( RepositoryFileTreeDto child : childrenDTO ) {
        children.add( unmarshal( child ) );
      }
    }

    RepositoryFileTree repositoryFileTree = new RepositoryFileTree( RepositoryFileAdapter.toFile( v.getFile() ), children );
    Boolean versioningEnable = v.getFile().getVersioningEnabled();
    Boolean versionCommentEnabled = v.getFile().getVersionCommentEnabled();
    if ( versioningEnable != null ) {
      repositoryFileTree.setVersioningEnabled( versioningEnable );
    }
    if ( versionCommentEnabled != null ) {
      repositoryFileTree.setVersionCommentEnabled( versionCommentEnabled );
    }
    return repositoryFileTree;
  }
}
