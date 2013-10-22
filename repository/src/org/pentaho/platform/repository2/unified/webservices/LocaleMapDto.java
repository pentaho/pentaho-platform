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
import java.util.List;

/**
 * @author Rowell Belen
 */

@XmlRootElement
public class LocaleMapDto implements Serializable {
  private static final long serialVersionUID = -8846483185435026656L;
  private String locale;
  private List<StringKeyStringValueDto> properties;

  public LocaleMapDto() {
    super();
  }

  public LocaleMapDto( final String locale, final List<StringKeyStringValueDto> properties ) {
    this.locale = locale;
    this.properties = properties;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    this.locale = locale;
  }

  public List<StringKeyStringValueDto> getProperties() {
    return properties;
  }

  public void setProperties( List<StringKeyStringValueDto> properties ) {
    this.properties = properties;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "LocaleMapDto [locale=" + locale + ", properties=" + properties + "]";
  }

  @Override
  public boolean equals( final Object obj ) {
    return ( obj != null && obj instanceof StringKeyStringValueDto
        && strEquals( locale, ( (LocaleMapDto) obj ).getLocale() ) && strEquals( properties.toString(),
          ( (LocaleMapDto) obj ).getProperties().toString() ) );
  }

  @Override
  public int hashCode() {
    return ( locale == null ? 1 : locale.hashCode() ) * ( properties == null ? -1 : properties.hashCode() );
  }

  private boolean strEquals( final String s1, final String s2 ) {
    return ( s1 == s2 || ( s1 != null && s1.equals( s2 ) ) );
  }
}
