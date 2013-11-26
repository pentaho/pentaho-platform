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

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class LocaleHelperTest extends TestCase {

  public void testLocaleHelper() {

    Locale myLocale = Locale.US;
    Locale newLocale = Locale.FRANCE;

    LocaleHelper.setDefaultLocale( myLocale );
    Locale myDefaultLocale = LocaleHelper.getDefaultLocale();
    Assert.assertEquals( myDefaultLocale, myLocale );

    LocaleHelper.setLocale( newLocale );
    Locale myNewLocale = LocaleHelper.getLocale();
    Assert.assertEquals( myNewLocale, newLocale );

    LocaleHelper.setSystemEncoding( "UTF8" ); //$NON-NLS-1$
    String systemEncoding = LocaleHelper.getSystemEncoding();
    Assert.assertEquals( systemEncoding, "UTF8" ); //$NON-NLS-1$

    LocaleHelper.setTextDirection( "English" ); //$NON-NLS-1$
    String textDirection = LocaleHelper.getTextDirection();
    Assert.assertEquals( textDirection, "English" ); //$NON-NLS-1$

    DateFormat dateFormat = LocaleHelper.getDateFormat( LocaleHelper.FORMAT_MEDIUM, LocaleHelper.FORMAT_MEDIUM );
    String format = dateFormat.format( new Date() );
    Assert.assertNotNull( format );

    DateFormat fullDateFormat = LocaleHelper.getFullDateFormat( true, true );
    String format1 = fullDateFormat.format( new Date() );
    Assert.assertNotNull( format1 );

    DateFormat fullDateFormat1 = LocaleHelper.getFullDateFormat( true, false );
    String format2 = fullDateFormat1.format( new Date() );
    Assert.assertNotNull( format2 );

    DateFormat fullDateFormat2 = LocaleHelper.getFullDateFormat( false, true );
    String format3 = fullDateFormat2.format( new Date() );
    Assert.assertNotNull( format3 );

    DateFormat longDateFormat = LocaleHelper.getLongDateFormat( true, true );
    String format4 = longDateFormat.format( new Date() );
    Assert.assertNotNull( format4 );

    DateFormat longDateFormat1 = LocaleHelper.getLongDateFormat( true, false );
    String format5 = longDateFormat1.format( new Date() );
    Assert.assertNotNull( format5 );

    DateFormat longDateFormat2 = LocaleHelper.getLongDateFormat( false, true );
    String format6 = longDateFormat2.format( new Date() );
    Assert.assertNotNull( format6 );

    DateFormat mediumDateFormat = LocaleHelper.getMediumDateFormat( true, true );
    String format7 = mediumDateFormat.format( new Date() );
    Assert.assertNotNull( format7 );

    DateFormat mediumDateFormat1 = LocaleHelper.getMediumDateFormat( true, false );
    String format8 = mediumDateFormat1.format( new Date() );
    Assert.assertNotNull( format8 );

    DateFormat mediumDateFormat2 = LocaleHelper.getMediumDateFormat( false, true );
    String format9 = mediumDateFormat2.format( new Date() );
    Assert.assertNotNull( format9 );

    DateFormat shortDateFormat = LocaleHelper.getShortDateFormat( true, true );
    String format10 = shortDateFormat.format( new Date() );
    Assert.assertNotNull( format10 );

    DateFormat shortDateFormat1 = LocaleHelper.getMediumDateFormat( true, false );
    String format11 = shortDateFormat1.format( new Date() );
    Assert.assertNotNull( format11 );

    DateFormat shortDateFormat2 = LocaleHelper.getMediumDateFormat( false, true );
    String format12 = shortDateFormat2.format( new Date() );
    Assert.assertNotNull( format12 );

  }

  public void testClosestLocale() {

    // should return the locale passed in
    String locale = LocaleHelper.getClosestLocale( "en-US", null ); //$NON-NLS-1$
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

  public void testEncoding() {
    LocaleHelper.setSystemEncoding( "Shift_JIS" );
  }

  public void testParseAndSetLocaleOverride() {
    final String TEST_LOCALE_LANG = "en";
    final String TEST_LOCALE_COUNTRY = "US";
    LocaleHelper.parseAndSetLocaleOverride( TEST_LOCALE_LANG + "_" + TEST_LOCALE_COUNTRY );

    assertTrue( LocaleHelper.getLocale().equals( new Locale( TEST_LOCALE_LANG, TEST_LOCALE_COUNTRY ) ) );

    // reset override to not break other tests
    LocaleHelper.setLocaleOverride( null );
  }

}
