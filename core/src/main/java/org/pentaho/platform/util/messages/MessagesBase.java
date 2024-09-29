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

package org.pentaho.platform.util.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessagesBase {

  private final Map<Locale, ResourceBundle> locales = Collections
      .synchronizedMap( new HashMap<Locale, ResourceBundle>() );
  private String bundleName;
  
  public MessagesBase( ) {
    this.bundleName = getClass().getPackage().getName() + ".messages"; //$NON-NLS-1$;
  }

  public MessagesBase( String bundleName ) {
    this.bundleName = bundleName;
  }

  public ResourceBundle getBundle() {
    return getBundle( LocaleHelper.getLocale() );
  }

  public ResourceBundle getBundle( Locale locale ) {
    ResourceBundle bundle = locales.get( locale );
    if ( bundle == null ) {
      bundle = ResourceBundle.getBundle( bundleName, locale );
      locales.put( locale, bundle );
    }
    return bundle;
  }

  public String getEncodedString( final String rawValue ) {
    if ( rawValue == null ) {
      return ( "" ); //$NON-NLS-1$
    }

    StringBuffer value = new StringBuffer();
    for ( int n = 0; n < rawValue.length(); n++ ) {
      int charValue = rawValue.charAt( n );
      if ( charValue >= 0x80 ) {
        value.append( "&#x" ); //$NON-NLS-1$
        value.append( Integer.toString( charValue, 0x10 ) );
        value.append( ";" ); //$NON-NLS-1$
      } else {
        value.append( (char) charValue );
      }
    }
    return value.toString();

  }

  public String getXslString( final String key ) {
    String rawValue = getString( key );
    return getEncodedString( rawValue );
  }

  public String getString( final String key ) {
    try {
      return getBundle().getString( key );
    } catch ( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }

  public String getString( final String key, final Object... params ) {
    return MessageUtil.getString( getBundle(), key, params );
  }

  public String getErrorString( final String key ) {
    return MessageUtil.formatErrorMessage( key, getString( key ) );
  }

  public String getErrorString( final String key, final Object... params ) {
    return MessageUtil.getErrorString( getBundle(), key, params );
  }

}
