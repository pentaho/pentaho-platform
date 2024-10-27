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

import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.webservices.DataNodeDto;
import org.pentaho.platform.api.repository2.unified.webservices.DataPropertyDto;
import org.pentaho.platform.api.repository2.unified.webservices.NodeRepositoryFileDataDto;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NodeRepositoryFileDataAdapter extends XmlAdapter<NodeRepositoryFileDataDto, NodeRepositoryFileData> {

  @Override
  public NodeRepositoryFileDataDto marshal( final NodeRepositoryFileData v ) {
    NodeRepositoryFileDataDto d = new NodeRepositoryFileDataDto();
    DataNodeDto node = new DataNodeDto();
    d.setNode( node );
    d.setDataSize( v.getDataSize() );
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
    return new NodeRepositoryFileData( node, v.getDataSize() );
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
