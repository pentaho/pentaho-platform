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

package org.pentaho.platform.core.mimetype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;

/**
 * Hold mime type name and extensions associated with it.
 *
 * @author tkafalas
 */
@XmlRootElement
public class MimeType implements Comparable<IMimeType>, IMimeType {
  private String name;
  private List<String> extensions = new ArrayList<String>();
  private boolean hidden;
  private boolean locale;
  private boolean versionEnabled;
  private boolean versionCommentEnabled;

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

  @Override
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public List<String> getExtensions() {
    return extensions;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#isHidden()
   */
  @Override
  public boolean isHidden() {
    return hidden;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#setHidden(boolean)
   */
  @Override
  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#isLocale()
   */
  @Override
  public boolean isLocale() {
    return locale;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#setLocale(boolean)
   */
  @Override
  public void setLocale( boolean locale ) {
    this.locale = locale;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#isVersionEnabled()
   */
  @Override
  public boolean isVersionEnabled() {
    return versionEnabled;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#setVersionEnabled(boolean)
   */
  @Override
  public void setVersionEnabled( boolean versionEnabled ) {
    this.versionEnabled = versionEnabled;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#isVersionCommentEnabled()
   */
  @Override
  public boolean isVersionCommentEnabled() {
    return versionCommentEnabled;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#setVersionCommentEnabled(boolean)
   */
  @Override
  public void setVersionCommentEnabled( boolean versionCommentEnabled ) {
    this.versionCommentEnabled = versionCommentEnabled;
  }

  @XmlTransient
  @Override
  public Converter getConverter() {
    return converter;
  }

  @Override
  public void setConverter( Converter converter ) {
    this.converter = converter;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#setExtensions(java.util.List)
   */
  @Override
  public void setExtensions( List<String> extensions ) {
    for ( String extension : extensions ) {
      this.extensions.add( extension );
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.core.mimetype.IMimeType#toString()
   */
  @Override
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
    sb.append( ",versionEnabled:" ).append( versionEnabled );
    sb.append( ",versionCommentEnabled:" ).append( versionCommentEnabled );
    return sb.toString();
  }

  @Override
  public int compareTo( IMimeType o ) {
    return name.compareTo( o.getName() );
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
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
    IMimeType other = (IMimeType) obj;
    if ( name == null ) {
      if ( other.getName() != null ) {
        return false;
      }
    } else if ( !name.equals( other.getName() ) ) {
      return false;
    }
    return true;
  }


}
