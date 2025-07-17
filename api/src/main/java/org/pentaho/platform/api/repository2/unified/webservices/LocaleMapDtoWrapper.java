/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.api.repository2.unified.webservices;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement( name = "localePropertiesMapEntries" )
@XmlAccessorType( XmlAccessType.FIELD )
public class LocaleMapDtoWrapper {

  @XmlElement( name = "localeMapDto" )
  private List<LocaleMapDto> localePropertiesMapEntries;

  public LocaleMapDtoWrapper() {

  }

  public LocaleMapDtoWrapper( List<LocaleMapDto> localePropertiesMapEntries ) {
    this.localePropertiesMapEntries = localePropertiesMapEntries;
  }

  public List<LocaleMapDto> getLocalePropertiesMapEntries() {
    return localePropertiesMapEntries;
  }

  public void setLocalePropertiesMapEntries( List<LocaleMapDto> localePropertiesMapEntries ) {
    this.localePropertiesMapEntries = localePropertiesMapEntries;
  }
}
