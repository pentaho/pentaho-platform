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
package org.pentaho.platform.plugin.services.importer.mimeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.platform.api.repository2.unified.Converter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Hold mime type name and extensions associated with it.
 *
 * @author tkafalas
 */
@XmlRootElement
public class MimeType implements Comparable<MimeType> {
  private String name;
  private List<String> extensions = new ArrayList<String>();
  private boolean hidden;
  private boolean locale;

  @XmlTransient
  private Converter converter;

  public MimeType() {

  }

  public MimeType( String name, List<String> extensions ) {
    this.name = name;
    setExtensions( extensions );
  }

  /**
   * Convenience method to allow comma delimited list of extensions
   *
   * @param name
   * @param extensions
   */
  public MimeType( String name, String extensions ) {
    this( name, Arrays.asList( extensions.split( "," ) ) );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public boolean isLocale() {
    return locale;
  }

  public void setLocale( boolean locale ) {
    this.locale = locale;
  }

  @XmlTransient
  public Converter getConverter() {
    return converter;
  }

  public void setConverter( Converter converter ) {
    this.converter = converter;
  }

  public void setExtensions( List<String> extensions ) {
    for ( String extension : extensions ) {
      this.extensions.add( extension );
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append( "name:" ).append( name );
    sb.append( ",extensions:[" );
    for ( String extension : extensions ) {
      if ( !sb.substring( sb.length() - 1 ).equals( "[" ) ) {
        sb.append( "," );
      }
      sb.append( extension );
    }
    sb.append( "]" );
    sb.append( ",hidden:" ).append( hidden );
    sb.append( ",locale:" ).append( locale );
    sb.append( ",converter:" ).append( converter );
    return sb.toString();
  }

  public int compareTo( MimeType o ) {
    return name.compareTo( o.name );
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    return result;
  }

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
    MimeType other = (MimeType) obj;
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    return true;
  }


}
