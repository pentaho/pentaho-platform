/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified.data.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataNode {

  public enum DataPropertyType {
    BOOLEAN, DATE, DOUBLE, LONG, STRING, REF
  }

  private String name;

  private Serializable id;

  private Map<String, DataNode> nodeNameToNodeMap = new HashMap<String, DataNode>();

  private List<DataNode> childNodes = new ArrayList<DataNode>();

  private Map<String, DataProperty> propNameToPropMap = new HashMap<String, DataProperty>();

  public DataNode( final String name ) {
    super();
    this.name = name;
  }

  public DataNode addNode( final String name ) {
    DataNode child = new DataNode( name );
    internalAddNode( child );
    return child;
  }

  public void addNode( final DataNode child ) {
    internalAddNode( child );
  }

  protected void internalAddNode( final DataNode child ) {
    childNodes.add( child );
    nodeNameToNodeMap.put( child.getName(), child );
  }

  public Iterable<DataNode> getNodes() {
    return childNodes; // maintain order of child nodes
  }

  public DataNode getNode( final String name ) {
    return nodeNameToNodeMap.get( name );
  }

  public String getName() {
    return name;
  }

  public void setProperty( final String name, String value ) {
    propNameToPropMap.put( name, new DataProperty( name, value, DataPropertyType.STRING ) );
  }

  public void setProperty( final String name, boolean value ) {
    propNameToPropMap.put( name, new DataProperty( name, value, DataPropertyType.BOOLEAN ) );
  }

  public void setProperty( final String name, double value ) {
    propNameToPropMap.put( name, new DataProperty( name, value, DataPropertyType.DOUBLE ) );
  }

  public void setProperty( final String name, long value ) {
    propNameToPropMap.put( name, new DataProperty( name, value, DataPropertyType.LONG ) );
  }

  public void setProperty( final String name, Date value ) {
    propNameToPropMap.put( name, new DataProperty( name, value, DataPropertyType.DATE ) );
  }

  public void setProperty( final String name, DataNodeRef value ) {
    propNameToPropMap.put( name, new DataProperty( name, value, DataPropertyType.REF ) );
  }

  public boolean hasProperty( final String name ) {
    return propNameToPropMap.containsKey( name );
  }

  public boolean hasNode( final String name ) {
    return nodeNameToNodeMap.containsKey( name );
  }

  public DataProperty getProperty( final String name ) {
    return propNameToPropMap.get( name );
  }

  public Iterable<DataProperty> getProperties() {
    return propNameToPropMap.values();
  }

  public void setId( Serializable id ) {
    this.id = id;
  }

  public Serializable getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( childNodes == null ) ? 0 : childNodes.hashCode() );
    result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    result = prime * result + ( ( nodeNameToNodeMap == null ) ? 0 : nodeNameToNodeMap.hashCode() );
    result = prime * result + ( ( propNameToPropMap == null ) ? 0 : propNameToPropMap.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    DataNode other = (DataNode) obj;
    if ( childNodes == null ) {
      if ( other.childNodes != null ) {
        return false;
      }
    } else if ( !childNodes.equals( other.childNodes ) ) {
      return false;
    }
    if ( id == null ) {
      if ( other.id != null ) {
        return false;
      }
    } else if ( !id.equals( other.id ) ) {
      return false;
    }
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    if ( nodeNameToNodeMap == null ) {
      if ( other.nodeNameToNodeMap != null ) {
        return false;
      }
    } else if ( !nodeNameToNodeMap.equals( other.nodeNameToNodeMap ) ) {
      return false;
    }
    if ( propNameToPropMap == null ) {
      if ( other.propNameToPropMap != null ) {
        return false;
      }
    } else if ( !propNameToPropMap.equals( other.propNameToPropMap ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return toString( 0 );
  }

  protected String toString( final int depth ) {
    final String SPACER = "  "; //$NON-NLS-1$
    final String NL = "\n"; //$NON-NLS-1$
    final String SLASH = "/"; //$NON-NLS-1$
    StringBuilder buf = new StringBuilder();
    for ( int i = 0; i < depth; i++ ) {
      buf.append( SPACER );
    }
    buf.append( getName() );
    // slash to denote node as opposed to property
    buf.append( SLASH + " " ); //$NON-NLS-1$
    buf.append( propNameToPropMap );
    buf.append( NL );
    for ( DataNode child : childNodes ) {
      buf.append( child.toString( depth + 1 ) );
    }
    return buf.toString();
  }

}
