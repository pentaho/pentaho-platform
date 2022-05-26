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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.messages.LocaleHelper;

public class LocaleHelperTest {

  @Test
  public void testLocaleHelper() {
    LocaleHelper helper = new LocaleHelper();
    assertNotNull( helper );

    LocaleHelper.setSystemEncoding( "UTF8" ); //$NON-NLS-1$
    String systemEncoding = LocaleHelper.getSystemEncoding();
    Assert.assertEquals( systemEncoding, "UTF8" ); //$NON-NLS-1$

    LocaleHelper.setTextDirection( "English" ); //$NON-NLS-1$
    String textDirection = LocaleHelper.getTextDirection();
    Assert.assertEquals( textDirection, "English" ); //$NON-NLS-1$

    DateFormat dateFormat = LocaleHelper.getDateFormat( LocaleHelper.FORMAT_MEDIUM, LocaleHelper.FORMAT_MEDIUM );
    String format = dateFormat.format( new Date() );
    assertNotNull( format );

    DateFormat fullDateFormat0 = LocaleHelper.getFullDateFormat( false, false );
    assertNull( fullDateFormat0 );

    DateFormat fullDateFormat = LocaleHelper.getFullDateFormat( true, true );
    String format1 = fullDateFormat.format( new Date() );
    assertNotNull( format1 );

    DateFormat fullDateFormat1 = LocaleHelper.getFullDateFormat( true, false );
    String format2 = fullDateFormat1.format( new Date() );
    assertNotNull( format2 );

    DateFormat fullDateFormat2 = LocaleHelper.getFullDateFormat( false, true );
    String format3 = fullDateFormat2.format( new Date() );
    assertNotNull( format3 );

    DateFormat longDateFormat = LocaleHelper.getLongDateFormat( true, true );
    String format4 = longDateFormat.format( new Date() );
    assertNotNull( format4 );

    DateFormat longDateFormat1 = LocaleHelper.getLongDateFormat( true, false );
    String format5 = longDateFormat1.format( new Date() );
    assertNotNull( format5 );

    DateFormat longDateFormat2 = LocaleHelper.getLongDateFormat( false, true );
    String format6 = longDateFormat2.format( new Date() );
    assertNotNull( format6 );

    DateFormat mediumDateFormat = LocaleHelper.getMediumDateFormat( true, true );
    String format7 = mediumDateFormat.format( new Date() );
    assertNotNull( format7 );

    DateFormat mediumDateFormat1 = LocaleHelper.getMediumDateFormat( true, false );
    String format8 = mediumDateFormat1.format( new Date() );
    assertNotNull( format8 );

    DateFormat mediumDateFormat2 = LocaleHelper.getMediumDateFormat( false, true );
    String format9 = mediumDateFormat2.format( new Date() );
    assertNotNull( format9 );

    DateFormat shortDateFormat = LocaleHelper.getShortDateFormat( true, true );
    String format10 = shortDateFormat.format( new Date() );
    assertNotNull( format10 );

    DateFormat shortDateFormat1 = LocaleHelper.getMediumDateFormat( true, false );
    String format11 = shortDateFormat1.format( new Date() );
    assertNotNull( format11 );

    DateFormat shortDateFormat2 = LocaleHelper.getMediumDateFormat( false, true );
    String format12 = shortDateFormat2.format( new Date() );
    assertNotNull( format12 );
  }

  @Test
  public void testClosestLocale() {

    // should return the locale passed in
    String locale = LocaleHelper.getClosestLocale( "en-US", null ); //$NON-NLS-1$
    assertEquals( "Locale is wrong", "en-US", locale ); //$NON-NLS-1$ //$NON-NLS-2$

    locale = LocaleHelper.getClosestLocale( null, new String[] { "en-US" } ); //$NON-NLS-1$
    assertEquals( "Locale is wrong", "en-US", locale ); //$NON-NLS-1$ //$NON-NLS-2$

    // should return the locale passed in
    locale = LocaleHelper.getClosestLocale( "en-US", new String[] {} ); //$NON-NLS-1$
    assertEquals( "Locale is wrong", "en-US", locale ); //$NON-NLS-1$ //$NON-NLS-2$

    // should return the first locale in the list
    locale = LocaleHelper.getClosestLocale( "en-US", new String[] { "fr-FR", "es-ES" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    assertEquals( "Locale is wrong", "fr-FR", locale ); //$NON-NLS-1$ //$NON-NLS-2$

    // should return the only English variant
    locale = LocaleHelper.getClosestLocale( "en-US", new String[] { "fr-FR", "es-ES", "en" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    assertEquals( "Locale is wrong", "en", locale ); //$NON-NLS-1$ //$NON-NLS-2$

    locale = LocaleHelper.getClosestLocale( "en-US", new String[] { "fr-FR", "es-ES", "en-UK" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    assertEquals( "Locale is wrong", "en-UK", locale ); //$NON-NLS-1$ //$NON-NLS-2$

    locale = LocaleHelper.getClosestLocale( "en", new String[] { "fr-FR", "es-ES", "en-UK" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    assertEquals( "Locale is wrong", "en-UK", locale ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Test
  public void testEncoding() {
    LocaleHelper.setSystemEncoding( "Shift_JIS" );
  }

  // region parseLocale
  @Test
  public void testParseAndSetLocaleOverride() {
    Locale initiaLocaleOverride = LocaleHelper.getThreadLocaleOverride();
    final String TEST_LOCALE_LANG = "en";
    final String TEST_LOCALE_COUNTRY = "US";
    try {
      LocaleHelper.parseAndSetLocaleOverride( TEST_LOCALE_LANG + "_" + TEST_LOCALE_COUNTRY );

      assertEquals( LocaleHelper.getLocale(), new Locale( TEST_LOCALE_LANG, TEST_LOCALE_COUNTRY ) );
    } finally {
      LocaleHelper.setThreadLocaleOverride( initiaLocaleOverride );
    }
  }

  @Test
  public void testParseLocaleReturnsNullWhenGivenNull() {

    Locale locale = LocaleHelper.parseLocale( null );

    assertNull( locale );
  }

  @Test
  public void testParseLocaleReturnsNullWhenGivenEmpty() {

    Locale locale = LocaleHelper.parseLocale( "" );

    assertNull( locale );
  }

  @Test
  public void testParseLocaleHandlesLocalesWithOneSection() {
    final String TEST_LOCALE_LANG = "en";

    Locale locale = LocaleHelper.parseLocale( TEST_LOCALE_LANG );

    assertEquals( locale, new Locale( TEST_LOCALE_LANG ) );
  }

  @Test
  public void testParseLocaleHandlesLocalesWithTwoSections() {
    final String TEST_LOCALE_LANG = "en";
    final String TEST_LOCALE_COUNTRY = "US";

    Locale locale = LocaleHelper.parseLocale( TEST_LOCALE_LANG + "_" + TEST_LOCALE_COUNTRY );

    assertEquals( locale, new Locale( TEST_LOCALE_LANG, TEST_LOCALE_COUNTRY ) );
  }

  // NOTE: this test was written to match the existing implementation,
  // but it is not clear if this behavior was intentional or a bug.
  @Test
  public void testParseLocaleSimplifiesLocalesWithMoreThanTwoSections() {
    final String TEST_LOCALE_LANG = "en";
    final String TEST_LOCALE_COUNTRY = "US";
    final String TEST_LOCALE_VARIANT = "Foo";

    Locale locale = LocaleHelper.parseLocale(
      TEST_LOCALE_LANG +
        "_" +
        TEST_LOCALE_COUNTRY +
        "_" +
        TEST_LOCALE_VARIANT);

    assertEquals( locale, new Locale( TEST_LOCALE_LANG, TEST_LOCALE_COUNTRY ) );
    assertNotEquals( locale, new Locale( TEST_LOCALE_LANG, TEST_LOCALE_COUNTRY, TEST_LOCALE_VARIANT ) );
  }

  @Test
  public void testParseLocaleHandlesAndNormalizesLocalesWithMixedCasingSections() {

    Locale locale = LocaleHelper.parseLocale( "eN_Us" );

    assertEquals( locale, new Locale( "en", "US" ) );

    locale = LocaleHelper.parseLocale( "En_us" );

    assertEquals( locale, new Locale( "en", "US" ) );
  }

  // NOTE: again, not a requirement per se, afaict, but it's how it's working.
  @Test
  public void testParseLocaleAcceptsSectionsOfAnyLength() {

    Locale locale = LocaleHelper.parseLocale( "foo_guru" );

    assertEquals( locale, new Locale( "foo", "GURU" ) );
  }

  // NOTE: again, not a requirement per se, afaict, but it's how it's working.
  // Notably, see SystemResource#setLocaleOverride( . ) which also supports sections separated
  // by "-" or "|". Additionally, it then uses org.apache.commons.lang3.LocaleUtils.toLocale
  // to parse the locale, instead of LocaleHelper.parseLocale.
  @Test
  public void testParseLocaleDoesNotSeparateSectionsByHyphen() {

    Locale locale = LocaleHelper.parseLocale( "foo-guru" );

    assertEquals( locale, new Locale( "foo-guru" ) );
  }
  // endregion

  // region Default Locale

  // Cannot test the case where Locale.getDefault() would be null with this kind of test,
  // because that would possibly only happen if `java` were passed a non-available locale parameter...

  @Test
  public void testDefaultLocaleIsJVMLocale() {
    assertEquals( Locale.getDefault(), LocaleHelper.getDefaultLocale() );
  }

  @Test
  public void testDefaultLocaleFallsbackToJVMLocaleWhenSetToNull() {
    Locale initialLocale = Locale.getDefault();
    Locale customLocale = Locale.forLanguageTag( "de-POSIX-x-URP-lvariant-Abc-Def" );
    Locale.setDefault( customLocale );
    try {
      LocaleHelper.setDefaultLocale( null );
      assertEquals( customLocale, LocaleHelper.getDefaultLocale() );
    } finally {
      Locale.setDefault( initialLocale );
    }
  }

  @Test
  public void testDefaultLocaleIsRespectedWhenSetToNonNull() {
    Locale initialDefaultLocale = LocaleHelper.getDefaultLocale();
    Locale customLocale = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
    try {
      LocaleHelper.setDefaultLocale( customLocale );
      assertEquals( customLocale, LocaleHelper.getDefaultLocale() );
    } finally {
      LocaleHelper.setDefaultLocale( initialDefaultLocale );
    }
  }
  // endregion

  // region Thread Locale Base
  @Test
  public void testThreadLocaleBaseDefaultsToNull() {
    assertNull( LocaleHelper.getThreadLocaleBase() );
  }

  @Test
  public void testThreadLocaleBaseRemembersBeingSetToNonNullValue() {
    Locale initialLocale = LocaleHelper.getThreadLocaleBase();
    Locale customLocale = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");

    try {
      LocaleHelper.setThreadLocaleBase( customLocale );

      assertEquals( customLocale, LocaleHelper.getThreadLocaleBase() );
    } finally {
      LocaleHelper.setThreadLocaleBase( initialLocale );
    }
  }

  @Test
  public void testThreadLocaleBaseCanBeSetToNullValue() {
    Locale initialLocale = LocaleHelper.getThreadLocaleBase();

    try {
      LocaleHelper.setThreadLocaleBase( Locale.ENGLISH );
      assertNotNull( LocaleHelper.getThreadLocaleBase() );

      LocaleHelper.setThreadLocaleBase( null );
      assertNull( LocaleHelper.getThreadLocaleBase() );
    } finally {
      LocaleHelper.setThreadLocaleBase( initialLocale );
    }
  }
  // endregion

  // region Thread Locale Override
  @Test
  public void testThreadLocaleOverrideDefaultsToNull() {
    assertNull( LocaleHelper.getThreadLocaleOverride() );
  }

  @Test
  public void testThreadLocaleOverrideRemembersBeingSetToNonNullValue() {
    Locale initialLocale = LocaleHelper.getThreadLocaleOverride();
    Locale customLocale = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");

    try {
      LocaleHelper.setThreadLocaleOverride( customLocale );

      assertEquals( customLocale, LocaleHelper.getThreadLocaleOverride() );
    } finally {
      LocaleHelper.setThreadLocaleOverride( initialLocale );
    }
  }

  @Test
  public void testThreadLocaleOverrideCanBeSetToNullValue() {
    Locale initialLocale = LocaleHelper.getThreadLocaleOverride();

    try {
      LocaleHelper.setThreadLocaleOverride( Locale.ENGLISH );
      assertNotNull( LocaleHelper.getThreadLocaleOverride() );

      LocaleHelper.setThreadLocaleOverride( null );
      assertNull( LocaleHelper.getThreadLocaleOverride() );
    } finally {
      LocaleHelper.setThreadLocaleOverride( initialLocale );
    }
  }
  // endregion

  // region (Effective) Locale
  @Test
  public void testEffectiveLocaleIsTheDefaultLocaleIfNoThreadBaseOrOverride() {
    Locale initialDefaultLocale = LocaleHelper.getDefaultLocale();
    Locale initialThreadLocaleBase = LocaleHelper.getThreadLocaleBase();
    Locale initialThreadLocaleOverride = LocaleHelper.getThreadLocaleOverride();

    Locale customLocale = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
    try {
      LocaleHelper.setDefaultLocale( customLocale );
      LocaleHelper.setThreadLocaleBase( null );
      LocaleHelper.setThreadLocaleOverride( null );

      assertEquals( customLocale, LocaleHelper.getLocale() );
    } finally {
      LocaleHelper.setDefaultLocale( initialDefaultLocale );
      LocaleHelper.setThreadLocaleBase( initialThreadLocaleBase );
      LocaleHelper.setThreadLocaleOverride( initialThreadLocaleOverride );
    }
  }

  @Test
  public void testEffectiveLocaleIsTheThreadLocaleBaseIfSetAndNoThreadOverride() {
    Locale initialDefaultLocale = LocaleHelper.getDefaultLocale();
    Locale initialThreadLocaleBase = LocaleHelper.getThreadLocaleBase();
    Locale initialThreadLocaleOverride = LocaleHelper.getThreadLocaleOverride();

    Locale customLocale1 = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
    Locale customLocale2 = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Ghi");
    try {
      LocaleHelper.setDefaultLocale( customLocale1 );
      LocaleHelper.setThreadLocaleBase( customLocale2 );
      LocaleHelper.setThreadLocaleOverride( null );

      assertEquals( customLocale2, LocaleHelper.getLocale() );
    } finally {
      LocaleHelper.setDefaultLocale( initialDefaultLocale );
      LocaleHelper.setThreadLocaleBase( initialThreadLocaleBase );
      LocaleHelper.setThreadLocaleOverride( initialThreadLocaleOverride );
    }
  }

  @Test
  public void testEffectiveLocaleIsTheThreadLocaleOverrideIfSet() {
    Locale initialDefaultLocale = LocaleHelper.getDefaultLocale();
    Locale initialThreadLocaleBase = LocaleHelper.getThreadLocaleBase();
    Locale initialThreadLocaleOverride = LocaleHelper.getThreadLocaleOverride();

    Locale customLocale1 = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
    Locale customLocale2 = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Ghi");
    Locale customLocale3 = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Jkl");
    try {
      LocaleHelper.setDefaultLocale( customLocale1 );
      LocaleHelper.setThreadLocaleBase( customLocale2 );
      LocaleHelper.setThreadLocaleOverride( customLocale3 );

      assertEquals( customLocale3, LocaleHelper.getLocale() );
    } finally {
      LocaleHelper.setDefaultLocale( initialDefaultLocale );
      LocaleHelper.setThreadLocaleBase( initialThreadLocaleBase );
      LocaleHelper.setThreadLocaleOverride( initialThreadLocaleOverride );
    }
  }
  // endregion

  // region Session Locale Override

  // The Test.None.class ensures that SonarQube does not complain that there are no assertions in the test case.
  @SuppressWarnings( "DefaultAnnotationParam" )
  @Test( expected = Test.None.class )
  public void testSessionLocaleOverrideIsNoOpIfNoSession() {
    IPentahoSession initialSession = PentahoSessionHolder.getSession();

    Locale customLocale = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
    try {
      PentahoSessionHolder.setSession( null );
      LocaleHelper.setSessionLocaleOverride( customLocale );
    } finally {
      PentahoSessionHolder.setSession( initialSession );
    }
  }

  @Test
  public void testSessionLocaleOverrideSetsCurrentSessionLocaleOverrideWithLocaleToString() {
    IPentahoSession initialSession = PentahoSessionHolder.getSession();
    IPentahoSession customSession = mock( IPentahoSession.class );
    Locale customLocale = Locale.forLanguageTag("de-POSIX-x-URP-lvariant-Abc-Def");
    try {
      PentahoSessionHolder.setSession( customSession );

      LocaleHelper.setSessionLocaleOverride( customLocale );

      verify( customSession, times( 1 )).setAttributeLocaleOverride( customLocale.toString() );
    } finally {
      PentahoSessionHolder.setSession( initialSession );
    }
  }

  @Test
  public void testSessionLocaleOverrideSetsCurrentSessionLocaleOverrideWithNullLocale() {
    IPentahoSession initialSession = PentahoSessionHolder.getSession();
    IPentahoSession customSession = mock( IPentahoSession.class );
    try {
      PentahoSessionHolder.setSession( customSession );

      LocaleHelper.setSessionLocaleOverride( null );

      verify( customSession, times( 1 )).setAttributeLocaleOverride( null );
    } finally {
      PentahoSessionHolder.setSession( initialSession );
    }
  }
  // endregion

  @Test
  public void testGetNumberFormat() {
    NumberFormat nfmt = LocaleHelper.getNumberFormat();
    assertNotNull( nfmt );
  }

  @Test
  public void testGetCurrencyFormat() {
    NumberFormat cfmt = LocaleHelper.getCurrencyFormat();
    assertNotNull( cfmt );
  }

  @Test
  public void testIsAsciiIsLatin1() {
    String symbol = "A";
    Assert.assertTrue( LocaleHelper.isAscii( symbol ) );
    Assert.assertTrue( LocaleHelper.isLatin1( symbol ) );
  }

  @Test
  public void testConvertISOStringToSystemDefaultEncoding() {
    String test = "someString";
    String result = LocaleHelper.convertISOStringToSystemDefaultEncoding( test );
    assertEquals( test, result );
  }
}
