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

import javax.xml.bind.annotation.XmlRootElement;
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
