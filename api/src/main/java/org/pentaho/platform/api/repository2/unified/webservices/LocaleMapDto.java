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
