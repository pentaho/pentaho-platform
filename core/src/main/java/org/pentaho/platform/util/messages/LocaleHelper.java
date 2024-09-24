/*!
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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.util.messages;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
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

  /**
   * The default locale that is used when even the Java VM's {@link Locale#getDefault()} is {@code null}.
   */
  private static final Locale DEFAULT_LOCALE = Locale.US;

  private static final ThreadLocal<Locale> threadLocalesBase = new ThreadLocal<>();
  private static final ThreadLocal<Locale> threadLocalesOverride = new ThreadLocal<>();

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

  public static final String USER_LOCALE_SETTING = "user_selected_language";

  static {
    setDefaultLocale( null );
  }

  // region Locale

  /**
   * Parses a locale string, taking into account if it is composed of several parts, separated by {@code _} characters.
   * <p>
   * If the given locale string is {@code null} or empty, then {@code null} is returned.
   * <p>
   * If the locale string has at least two parts, language and country, it creates and returns a locale using the first
   * two parts as first and second arguments of the locale constructor {@link Locale(String, String)}, and ignoring the
   * remaining parts.
   * <p>
   * Otherwise, it creates and returns a locale in which the whole string is passed to {@link Locale(String)}.
   * <p>
   * See BISERVER-9863 for more information.
   *
   * @param locale The locale string.
   * @return The new locale or {@code null}.
   */
  public static Locale parseLocale( final String locale ) {
    if ( StringUtils.isEmpty( locale ) ) {
      return null;
    }

    if ( locale.contains( "_" ) ) {
      String[] parts = locale.split( "_" );
      return new Locale( parts[ 0 ], parts[ 1 ] );
    }

    return new Locale( locale );
  }

  // region 3 - Default Locale

  /**
   * Sets the default locale.
   * <p>
   * The default locale applies to any session or thread.
   * <p>
   * When {@code null}, the locale assumes the Java VM's current default locale,
   * as given by {@link Locale#getDefault()}.
   * In the rare cases that this may be {@code null}, it is initialized to {@link Locale#US}.
   * <p>
   * In the Pentaho server, the default locale is initialized by the
   * {@code org.pentaho.platform.web.http.context.SolutionContextListener}.
   * When the {@code web.xml} server parameters {@code locale-language} and {@code locale-country} are both specified
   * and match an available locale, as given by {@link Locale#getAvailableLocales()}, it sets the default locale to
   * that.
   *
   * @param newLocale The new default locale, possibly {@code null}.
   */
  public static void setDefaultLocale( Locale newLocale ) {
    if ( newLocale == null ) {
      newLocale = Locale.getDefault();
    }

    if ( newLocale == null ) {
      newLocale = DEFAULT_LOCALE;
    }

    LocaleHelper.defaultLocale = newLocale;
  }

  /**
   * Gets the default locale.
   * <p>
   * The default locale applies to any session or thread.
   * <p>
   * The default locale is always defined; it's never {@code null}.
   *
   * @return The default locale.
   */
  public static Locale getDefaultLocale() {
    return LocaleHelper.defaultLocale;
  }
  // endregion

  // region 1 - Thread Locale Override

  /**
   * BISERVER-9863 Check if override locale string contains language and country. If so, instantiate Locale with two
   * parameters for language and country, instead of just language.
   *
   * @param localeOverride The new locale override, or {@code null} or an empty string, if none.
   *
   * @deprecated Use a combination of {@link #parseLocale(String)} and {@link #setThreadLocaleOverride(Locale)} instead.
   */
  @Deprecated
  public static void parseAndSetLocaleOverride( final String localeOverride ) {
    setThreadLocaleOverride( parseLocale( localeOverride ) );
  }

  /**
   * Sets the locale <i>override</i> of the current thread.
   *
   * @param newLocale The new override locale. Can be {@code null}.
   * @deprecated Use {@link #setThreadLocaleOverride(Locale)} instead.
   */
  @Deprecated
  public static void setLocaleOverride( final Locale newLocale ) {
    setThreadLocaleOverride( newLocale );
  }

  /**
   * Gets the locale <i>override</i> of the current thread.
   *
   * @return The locale override, if set; {@code null}, otherwise.
   * @deprecated Use {@link #getThreadLocaleOverride()} instead.
   */
  @Deprecated
  public static Locale getLocaleOverride() {
    return getThreadLocaleOverride();
  }

  /**
   * Sets the locale <i>override</i> of the current thread.
   * <p>
   * A thread's locale <i>override</i> is the most specific locale, as returned by {@link #getLocale()}.
   * <p>
   * The Pentaho server sets the thread locale override, of threads handling HTTP requests,
   * to the locale override of the Pentaho session of the corresponding request,
   * as given by {@link IPentahoSession#getAttributeLocaleOverride()}.
   * This is done by the {@code org.pentaho.platform.web.http.filters.HttpSessionPentahoSessionIntegrationFilter}
   * filter.
   *
   * @param newLocale The new thread locale override. Can be {@code null}.
   */
  public static void setThreadLocaleOverride( final Locale newLocale ) {
    if ( newLocale == null ) {
      LocaleHelper.threadLocalesOverride.remove();
    } else {
      LocaleHelper.threadLocalesOverride.set( newLocale );
    }
  }

  /**
   * Gets the locale <i>override</i> of the current thread.
   *
   * @return The locale override, if set; {@code null}, otherwise.
   */
  public static Locale getThreadLocaleOverride() {
    return LocaleHelper.threadLocalesOverride.get();
  }
  // endregion

  // region 2 - Thread Locale Base

  /**
   * Sets the <i>base</i> locale of the current thread.
   *
   * @param newLocale The new base locale. Can be {@code null}.
   *
   * @deprecated Use {@link #setThreadLocaleBase(Locale)} instead.
   */
  @Deprecated
  public static void setLocale( final Locale newLocale ) {
    setThreadLocaleBase( newLocale );
  }

  /**
   * Sets the <i>base</i> locale of the current thread.
   * <p>
   * The Pentaho server sets the base locale of threads handling HTTP requests
   * to the corresponding request's locale, as given by {@code javax.servlet.http.HttpServletRequest#getLocale()}.
   * This is done by the
   * {@code org.pentaho.platform.web.http.filters.HttpSessionPentahoSessionIntegrationFilter}
   * filter.
   * <p>
   * In this manner, according to the rules of {@link #getLocale()},
   * and unless a web user has explicitly set their Pentaho session's locale override
   * (or the thread's override locale is set by some other means),
   * the effective locale will be the web browser's preferred language.
   *
   * @param newLocale The new base locale. Can be {@code null}.
   */
  public static void setThreadLocaleBase( final Locale newLocale ) {
    if ( newLocale == null ) {
      LocaleHelper.threadLocalesBase.remove();
    } else {
      LocaleHelper.threadLocalesBase.set( newLocale );
    }
  }

  /**
   * Gets the <i>base</i> locale of the current thread.
   *
   * @return The base locale, if any; {@code null}, otherwise.
   */
  public static Locale getThreadLocaleBase() {
    return LocaleHelper.threadLocalesBase.get();
  }
  // endregion

  // region Effective Locale

  /**
   * Gets the <i>effective</i> locale of the current thread.
   * <p>
   * The effective locale is the value of the first non-{@code null}, among the following:
   * <ol>
   *   <li>
   *     The current thread's locale override, {@link #getThreadLocaleOverride()}
   *   </li>
   *   <li>
   *     The current thread's base locale, {@link #getThreadLocaleBase()}
   *   </li>
   *   <li>
   *     The default locale, {@link #getDefaultLocale()}
   *   </li>
   * </ol>
   * <p>
   * The effective locale is never {@code null}.
   *
   * @return The effective locale.
   */
  public static Locale getLocale() {
    // 1
    Locale locale = getThreadLocaleOverride();
    if ( locale != null ) {
      return locale;
    }

    // 2
    locale = getThreadLocaleBase();
    if ( locale != null ) {
      return locale;
    }

    // 3
    return getDefaultLocale();
  }
  // endregion

  /**
   * Sets the locale override of the current session, if any.
   *
   * @see PentahoSessionHolder#getSession()
   * @see IPentahoSession#setAttributeLocaleOverride(String)
   */
  public static void setSessionLocaleOverride( Locale locale ) {
    IPentahoSession session = PentahoSessionHolder.getSession();
    if ( session != null ) {
      String localeCode = locale != null ? locale.toString() : null;
      session.setAttributeLocaleOverride( localeCode );
    }
  }
  // endregion

  // region Encoding, Conversion, TextDirection
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
   * This method is called to convert strings from ISO-8859-1 (post/get parameters for example) into the default system
   * locale.
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
  // endregion

  // region Date and Number Formatting
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
  // endregion

  public static String getClosestLocale( String locale, String[] locales ) {
    // see if this locale is supported
    if ( locales == null || locales.length == 0 ) {
      return locale;
    }
    if ( locale == null || locale.length() == 0 ) {
      return locales[ 0 ];
    }
    String localeLanguage = locale.substring( 0, 2 );
    String localeCountry = ( locale.length() > 4 ) ? locale.substring( 0, 5 ) : localeLanguage;
    int looseMatch = -1;
    int closeMatch = -1;
    int exactMatch = -1;
    for ( int idx = 0; idx < locales.length; idx++ ) {
      if ( locales[ idx ].equals( locale ) ) {
        exactMatch = idx;
        break;
      } else if ( locales[ idx ].length() > 1 && locales[ idx ].substring( 0, 2 ).equals( localeLanguage ) ) {
        looseMatch = idx;
      } else if ( locales[ idx ].length() > 4 && locales[ idx ].substring( 0, 5 ).equals( localeCountry ) ) {
        closeMatch = idx;
      }
    }
    //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
    if ( exactMatch != -1 ) {
      // do nothing we have an exact match
    } else if ( closeMatch != -1 ) {
      locale = locales[ closeMatch ];
    } else if ( looseMatch != -1 ) {
      locale = locales[ looseMatch ];
    } else {
      // no locale is close , just go with the first?
      locale = locales[ 0 ];
    }
    return locale;
  }
}
