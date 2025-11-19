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
