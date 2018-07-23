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

import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.webservices.DataNodeDto;
import org.pentaho.platform.api.repository2.unified.webservices.DataPropertyDto;
import org.pentaho.platform.api.repository2.unified.webservices.NodeRepositoryFileDataDto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NodeRepositoryFileDataAdapter extends XmlAdapter<NodeRepositoryFileDataDto, NodeRepositoryFileData> {

  @Override
  public NodeRepositoryFileDataDto marshal( final NodeRepositoryFileData v ) {
    NodeRepositoryFileDataDto d = new NodeRepositoryFileDataDto();
    DataNodeDto node = new DataNodeDto();
    d.setNode( node );
    toDataNodeDto( node, v.getNode() );
    return d;
  }

  protected void toDataNodeDto( final DataNodeDto nodeDto, final DataNode node ) {
    nodeDto.setName( node.getName() );
    if ( node.getId() != null ) {
      nodeDto.setId( node.getId().toString() );
    }
    List<DataPropertyDto> dtoProps = new ArrayList<DataPropertyDto>();
    for ( DataProperty prop : node.getProperties() ) {
      DataPropertyDto dtoProp = new DataPropertyDto();
      dtoProp.setName( prop.getName() );
      if ( ( prop.getType() == DataPropertyType.BOOLEAN ) || ( prop.getType() == DataPropertyType.DOUBLE )
          || ( prop.getType() == DataPropertyType.LONG ) || ( prop.getType() == DataPropertyType.STRING )
          || ( prop.getType() == DataPropertyType.REF ) ) {
        dtoProp.setValue( prop.getString() );
      } else if ( prop.getType() == DataPropertyType.DATE ) {
        Date dateProp = prop.getDate();
        dtoProp.setValue( dateProp != null ? String.valueOf( dateProp.getTime() ) : null );
      } else {
        throw new IllegalArgumentException();
      }
      dtoProp.setType( prop.getType() != null ? prop.getType().ordinal() : -1 );
      dtoProps.add( dtoProp );
    }
    nodeDto.setChildProperties( dtoProps );
    List<DataNodeDto> nodeDtos = new ArrayList<DataNodeDto>();
    for ( DataNode childNode : node.getNodes() ) {
      DataNodeDto child = new DataNodeDto();
      nodeDtos.add( child );
      toDataNodeDto( child, childNode );
    }
    nodeDto.setChildNodes( nodeDtos );
  }

  @Override
  public NodeRepositoryFileData unmarshal( final NodeRepositoryFileDataDto v ) {
    DataNode node = toDataNode( v.getNode() );
    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    return data;
  }

  protected DataNode toDataNode( final DataNodeDto nodeDto ) {
    DataNode node = new DataNode( nodeDto.getName() );
    node.setId( nodeDto.getId() );

    for ( DataPropertyDto dtoProp : nodeDto.getChildProperties() ) {
      int type = dtoProp.getType();
      String name = dtoProp.getName();
      String value = dtoProp.getValue();
      if ( type == DataPropertyType.BOOLEAN.ordinal() ) {
        node.setProperty( name, Boolean.parseBoolean( value ) );
      } else if ( type == DataPropertyType.DATE.ordinal() ) {
        node.setProperty( name, new Date( Long.parseLong( value ) ) );
      } else if ( type == DataPropertyType.DOUBLE.ordinal() ) {
        node.setProperty( name, Double.parseDouble( value ) );
      } else if ( type == DataPropertyType.LONG.ordinal() ) {
        node.setProperty( name, Long.parseLong( value ) );
      } else if ( type == DataPropertyType.STRING.ordinal() ) {
        node.setProperty( name, value );
      } else if ( type == DataPropertyType.REF.ordinal() ) {
        node.setProperty( name, new DataNodeRef( value ) );
      } else {
        throw new IllegalArgumentException();
      }
    }

    for ( DataNodeDto childNodeDto : nodeDto.getChildNodes() ) {
      node.addNode( toDataNode( childNodeDto ) );
    }

    return node;
  }

}
