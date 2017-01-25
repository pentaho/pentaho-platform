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

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rowell Belen
 */
public class LocalizationUtil {

  private Map<String, Properties> localePropertiesMap;
  private Locale locale;

  public static final String DEFAULT = "default";

  public LocalizationUtil( Map<String, Properties> localePropertiesMap, Locale locale ) {
    Assert.notNull( localePropertiesMap );
    this.localePropertiesMap = localePropertiesMap;
    this.locale = locale;
  }

  public String resolveLocalizedString( final String propertyName, final String defaultValue ) {

    String localizedString;

    if ( this.locale != null ) {
      // find locale (language & country) - example: en_US
      localizedString = findLocalizedString( this.locale.toString(), propertyName );
      if ( StringUtils.isNotBlank( localizedString ) ) {
        return localizedString;
      }

      // find locale (language only) - example: en
      localizedString = findLocalizedString( this.locale.getLanguage(), propertyName );
      if ( StringUtils.isNotBlank( localizedString ) ) {
        return localizedString;
      }
    }

    // find default locale if provided locale is not found
    localizedString = findLocalizedString( DEFAULT, propertyName );
    if ( StringUtils.isNotBlank( localizedString ) ) {
      return localizedString;
    }

    // return default value if provided/default locales are not found
    return defaultValue;
  }

  private String findLocalizedString( final String locale, final String propertyName ) {

    String localeString = locale;
    if ( StringUtils.isBlank( localeString ) ) {
      localeString = DEFAULT;
    }

    if ( this.localePropertiesMap != null ) {
      Properties localeProperties = this.localePropertiesMap.get( localeString );
      if ( localeProperties != null && !localeProperties.isEmpty() ) {
        return localeProperties.getProperty( propertyName ); // found localized string in map
      }
    }

    return null;
  }
}
