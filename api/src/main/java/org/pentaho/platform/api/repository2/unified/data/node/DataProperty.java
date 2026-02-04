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
