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


package org.pentaho.platform.api.repository2.unified.webservices;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class StringKeyStringValueDto implements Serializable {
  private static final long serialVersionUID = -4522687034876346385L;
  private String key;
  private String value;

  public StringKeyStringValueDto() {
    super();
  }

  public StringKeyStringValueDto( final String key, final String value ) {
    this.key = key;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "StringKeyStringValueDto [key=" + key + ", value=" + value + "]";
  }

  @Override
  public boolean equals( final Object obj ) {
    return ( obj != null && obj instanceof StringKeyStringValueDto
        && strEquals( key, ( (StringKeyStringValueDto) obj ).getKey() ) && strEquals( value,
          ( (StringKeyStringValueDto) obj ).getValue() ) );
  }

  @Override
  public int hashCode() {
    return ( key == null ? 1 : key.hashCode() ) * ( value == null ? -1 : value.hashCode() );
  }

  private boolean strEquals( final String s1, final String s2 ) {
    return ( s1 == s2 || ( s1 != null && s1.equals( s2 ) ) );
  }
}
