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

import org.pentaho.platform.api.repository2.unified.data.node.DataNode.DataPropertyType;

import java.util.Date;

public class DataProperty {

  private DataPropertyType type;

  private Object value;

  private String name;

  public DataProperty( final String name, final Object value, final DataPropertyType type ) {
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public String getString() {
    return value != null ? String.valueOf( value ) : null;
  }

  public boolean getBoolean() {
    return value != null ? Boolean.valueOf( getString() ) : null;
  }

  public long getLong() {
    return value != null ? Long.valueOf( getString() ) : null;
  }

  public double getDouble() {
    return value != null ? Double.valueOf( getString() ) : null;
  }

  public DataNodeRef getRef() {
    return value != null ? new DataNodeRef( value.toString() ) : null;
  }

  public Date getDate() {
    if ( value == null ) {
      return null;
    }
    if ( !( value instanceof Date ) ) {
      throw new IllegalArgumentException();
    }
    return new Date( ( (Date) value ).getTime() );
  }

  public DataPropertyType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
    result = prime * result + ( ( value == null ) ? 0 : value.hashCode() );
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
    DataProperty other = (DataProperty) obj;
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    if ( type == null ) {
      if ( other.type != null ) {
        return false;
      }
    } else if ( !type.equals( other.type ) ) {
      return false;
    }
    if ( value == null ) {
      if ( other.value != null ) {
        return false;
      }
    } else if ( !value.equals( other.value ) ) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DataProperty [name=" + name + ", type=" + type + ", value=" + value + "]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
  }

}
