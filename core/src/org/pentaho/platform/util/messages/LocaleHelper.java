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

package org.pentaho.platform.util.messages;

import org.pentaho.platform.util.logging.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class LocaleHelper {

  private static final ThreadLocal<Locale> threadLocales = new ThreadLocal<Locale>();
  private static final ThreadLocal<Locale> threadLocaleOverride = new ThreadLocal<Locale>();

  public static final int FORMAT_SHORT = DateFormat.SHORT;

  public static final int FORMAT_MEDIUM = DateFormat.MEDIUM;

  public static final int FORMAT_LONG = DateFormat.LONG;

  public static final int FORMAT_FULL = DateFormat.FULL;

  public static final int FORMAT_IGNORE = -1;

  private static Locale defaultLocale;

  public static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

  private static String encoding = LocaleHelper.UTF_8;

  public static final String LEFT_TO_RIGHT = "LTR"; //$NON-NLS-1$

  private static String textDirection = LocaleHelper.LEFT_TO_RIGHT;

  public static final String USER_LOCALE_PARAM = "user_locale";

  public static void setDefaultLocale( final Locale newLocale ) {

    LocaleHelper.defaultLocale = newLocale;
  }

  public static Locale getDefaultLocale() {
    return LocaleHelper.defaultLocale;
  }

  /**
   * BISERVER-9863 Check if override locale string contains language and country. If so, instantiate Locale with
   * two parameters for language and country, instead of just language
   * 
   * @param localeOverride
   */
  public static void parseAndSetLocaleOverride( final String localeOverride ) {
    if ( localeOverride.contains( "_" ) ) {
      String[] parts = localeOverride.split( "_" );
      if ( parts.length >= 2 ) {
        setLocaleOverride( new Locale( parts[0], parts[1] ) );
      }
    } else {
      setLocaleOverride( new Locale( localeOverride ) );
    }
  }

  public static void setLocaleOverride( final Locale localeOverride ) {
    LocaleHelper.threadLocaleOverride.set( localeOverride );
  }

  public static Locale getLocaleOverride() {
    return LocaleHelper.threadLocaleOverride.get();
  }

  public static void setLocale( final Locale newLocale ) {
    LocaleHelper.threadLocales.set( newLocale );
  }

  public static Locale getLocale() {
    Locale override = LocaleHelper.threadLocaleOverride.get();
    if ( override != null ) {
      return override;
    }
    Locale rtn = LocaleHelper.threadLocales.get();
    if ( rtn != null ) {
      return rtn;
    }
    LocaleHelper.defaultLocale = Locale.getDefault();
    LocaleHelper.setLocale( LocaleHelper.defaultLocale );
    return LocaleHelper.defaultLocale;
  }

  public static void setSystemEncoding( final String encoding ) {

    Charset platformCharset = Charset.forName( encoding );
    Charset defaultCharset = Charset.defaultCharset();

    if ( platformCharset.compareTo( defaultCharset ) != 0 ) {
      Logger.warn( LocaleHelper.class.getName(), Messages.getInstance().getString(
          "LocaleHelper.WARN_CHARSETS_DONT_MATCH", platformCharset.name(), defaultCharset.name() ) );
    }

    LocaleHelper.encoding = encoding;
  }

  public static void setTextDirection( final String textDirection ) {
    // TODO make this ThreadLocal
    LocaleHelper.textDirection = textDirection;
  }

  public static String getSystemEncoding() {
    return LocaleHelper.encoding;
  }

  public static String getTextDirection() {
    // TODO make this ThreadLocal
    return LocaleHelper.textDirection;
  }

  /**
   * This method is called to convert strings from ISO-8859-1 (post/get parameters for example) into the default
   * system locale.
   * 
   * @param isoString
   * @return Re-encoded string
   */
  public static String convertISOStringToSystemDefaultEncoding( String isoString ) {
    return convertEncodedStringToSystemDefaultEncoding( "ISO-8859-1", isoString ); //$NON-NLS-1$
  }

  /**
   * This method converts strings from a known encoding into a string encoded by the system default encoding.
   * 
   * @param fromEncoding
   * @param encodedStr
   * @return Re-encoded string
   */
  public static String convertEncodedStringToSystemDefaultEncoding( String fromEncoding, String encodedStr ) {
    return convertStringEncoding( encodedStr, fromEncoding, LocaleHelper.getSystemEncoding() );
  }

  /**
   * This method converts an ISO-8859-1 encoded string to a UTF-8 encoded string.
   * 
   * @param isoString
   * @return Re-encoded string
   */
  public static String isoToUtf8( String isoString ) {
    return convertStringEncoding( isoString, "ISO-8859-1", "UTF-8" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This method converts a UTF8-encoded string to ISO-8859-1
   * 
   * @param utf8String
   * @return Re-encoded string
   */
  public static String utf8ToIso( String utf8String ) {
    return convertStringEncoding( utf8String, "UTF-8", "ISO-8859-1" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This method converts strings between various encodings.
   * 
   * @param sourceString
   * @param sourceEncoding
   * @param targetEncoding
   * @return Re-encoded string.
   */
  public static String convertStringEncoding( String sourceString, String sourceEncoding, String targetEncoding ) {
    String targetString = null;
    if ( null != sourceString && !sourceString.equals( "" ) ) { //$NON-NLS-1$
      try {
        byte[] stringBytesSource = sourceString.getBytes( sourceEncoding );
        targetString = new String( stringBytesSource, targetEncoding );
      } catch ( UnsupportedEncodingException e ) {
        throw new RuntimeException( e );
      }
    } else {
      targetString = sourceString;
    }
    return targetString;
  }

  /**
   * @param aString
   * @return true if the provided string is completely within the US-ASCII character set.
   */
  public static boolean isAscii( String aString ) {
    return isWithinCharset( aString, "US-ASCII" ); //$NON-NLS-1$
  }

  /**
   * @param aString
   * @return true if the provided string is completely within the Latin-1 character set (ISO-8859-1).
   */
  public static boolean isLatin1( String aString ) {
    return isWithinCharset( aString, "ISO-8859-1" ); //$NON-NLS-1$
  }

  /**
   * @param aString
   * @param charsetTarget
   * @return true if the provided string is completely within the target character set.
   */
  public static boolean isWithinCharset( String aString, String charsetTarget ) {
    byte[] stringBytes = aString.getBytes();
    CharsetDecoder decoder = Charset.forName( charsetTarget ).newDecoder();
    try {
      decoder.decode( ByteBuffer.wrap( stringBytes ) );
      return true;
    } catch ( CharacterCodingException ignored ) {
      //ignored
    }
    return false;
  }

  public static DateFormat getDateFormat( final int dateFormat, final int timeFormat ) {

    if ( ( dateFormat != LocaleHelper.FORMAT_IGNORE ) && ( timeFormat != LocaleHelper.FORMAT_IGNORE ) ) {
      return DateFormat.getDateTimeInstance( dateFormat, timeFormat, LocaleHelper.getLocale() );
    } else if ( dateFormat != LocaleHelper.FORMAT_IGNORE ) {
      return DateFormat.getDateInstance( dateFormat, LocaleHelper.getLocale() );
    } else if ( timeFormat != LocaleHelper.FORMAT_IGNORE ) {
      return DateFormat.getTimeInstance( timeFormat, LocaleHelper.getLocale() );
    } else {
      return null;
    }

  }

  public static DateFormat getShortDateFormat( final boolean date, final boolean time ) {
    if ( date && time ) {
      return DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, LocaleHelper.getLocale() );
    } else if ( date ) {
      return DateFormat.getDateInstance( DateFormat.SHORT, LocaleHelper.getLocale() );
    } else if ( time ) {
      return DateFormat.getTimeInstance( DateFormat.SHORT, LocaleHelper.getLocale() );
    } else {
      return null;
    }
  }

  public static DateFormat getMediumDateFormat( final boolean date, final boolean time ) {
    if ( date && time ) {
      return DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, LocaleHelper.getLocale() );
    } else if ( date ) {
      return DateFormat.getDateInstance( DateFormat.MEDIUM, LocaleHelper.getLocale() );
    } else if ( time ) {
      return DateFormat.getTimeInstance( DateFormat.MEDIUM, LocaleHelper.getLocale() );
    } else {
      return null;
    }
  }

  public static DateFormat getLongDateFormat( final boolean date, final boolean time ) {
    if ( date && time ) {
      return DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG, LocaleHelper.getLocale() );
    } else if ( date ) {
      return DateFormat.getDateInstance( DateFormat.LONG, LocaleHelper.getLocale() );
    } else if ( time ) {
      return DateFormat.getTimeInstance( DateFormat.LONG, LocaleHelper.getLocale() );
    } else {
      return null;
    }
  }

  public static DateFormat getFullDateFormat( final boolean date, final boolean time ) {
    if ( date && time ) {
      return DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL, LocaleHelper.getLocale() );
    } else if ( date ) {
      return DateFormat.getDateInstance( DateFormat.FULL, LocaleHelper.getLocale() );
    } else if ( time ) {
      return DateFormat.getTimeInstance( DateFormat.FULL, LocaleHelper.getLocale() );
    } else {
      return null;
    }
  }

  public static NumberFormat getNumberFormat() {
    return NumberFormat.getNumberInstance( LocaleHelper.getLocale() );
  }

  public static NumberFormat getCurrencyFormat() {
    return NumberFormat.getCurrencyInstance( LocaleHelper.getLocale() );
  }

  public static String getClosestLocale( String locale, String[] locales ) {
    // see if this locale is supported
    if ( locales == null || locales.length == 0 ) {
      return locale;
    }
    if ( locale == null || locale.length() == 0 ) {
      return locales[0];
    }
    String localeLanguage = locale.substring( 0, 2 );
    String localeCountry = ( locale.length() > 4 ) ? locale.substring( 0, 5 ) : localeLanguage;
    int looseMatch = -1;
    int closeMatch = -1;
    int exactMatch = -1;
    for ( int idx = 0; idx < locales.length; idx++ ) {
      if ( locales[idx].equals( locale ) ) {
        exactMatch = idx;
        break;
      } else if ( locales[idx].length() > 1 && locales[idx].substring( 0, 2 ).equals( localeLanguage ) ) {
        looseMatch = idx;
      } else if ( locales[idx].length() > 4 && locales[idx].substring( 0, 5 ).equals( localeCountry ) ) {
        closeMatch = idx;
      }
    }
    //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
    if ( exactMatch != -1 ) {
      // do nothing we have an exact match
    } else if ( closeMatch != -1 ) {
      locale = locales[closeMatch];
    } else if ( looseMatch != -1 ) {
      locale = locales[looseMatch];
    } else {
      // no locale is close , just go with the first?
      locale = locales[0];
    }
    return locale;
  }
}
