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


package org.pentaho.platform.config.i18n;

import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "org.pentaho.platform.config.i18n.messages"; //$NON-NLS-1$

  private static final Map<Locale, ResourceBundle> locales = Collections
      .synchronizedMap( new HashMap<Locale, ResourceBundle>() );

  protected static Map<Locale, ResourceBundle> getLocales() {
    return locales;
  }

  private static ResourceBundle getBundle() {
    Locale locale = LocaleHelper.getLocale();
    ResourceBundle bundle = locales.get( locale );
    if ( bundle == null ) {
      bundle = ResourceBundle.getBundle( BUNDLE_NAME, locale );
      locales.put( locale, bundle );
    }
    return bundle;
  }

  public static String getString( String key ) {
    try {
      return getBundle().getString( key );
    } catch ( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( String key, String param1 ) {
    return MessageUtil.getString( getBundle(), key, param1 );
  }

  public static String getString( String key, String param1, String param2 ) {
    return MessageUtil.getString( getBundle(), key, param1, param2 );
  }

  public static String getString( String key, String param1, String param2, String param3 ) {
    return MessageUtil.getString( getBundle(), key, param1, param2, param3 );
  }

  public static String getString( String key, String param1, String param2, String param3, String param4 ) {
    return MessageUtil.getString( getBundle(), key, param1, param2, param3, param4 );
  }

  public static String getErrorString( String key ) {
    return MessageUtil.formatErrorMessage( key, getString( key ) );
  }

  public static String getErrorString( String key, String param1 ) {
    return MessageUtil.getErrorString( getBundle(), key, param1 );
  }

  public static String getErrorString( String key, String param1, String param2 ) {
    return MessageUtil.getErrorString( getBundle(), key, param1, param2 );
  }

  public static String getErrorString( String key, String param1, String param2, String param3 ) {
    return MessageUtil.getErrorString( getBundle(), key, param1, param2, param3 );
  }

  public static void main( String[] args ) {
    ResourceBundle bundle = Messages.getBundle();
  }
}
