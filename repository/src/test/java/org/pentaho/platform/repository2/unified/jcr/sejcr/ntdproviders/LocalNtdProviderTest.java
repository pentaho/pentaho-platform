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

package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.GERMAN_GREECE;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.NORWEGIAN;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.NORWEGIAN_BOKMAL;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.NORWEGIAN_NYNORSK;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.SIMPLIFIED_CHINESE_CHINA;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.SIMPLIFIED_CHINESE_SINGAPORE;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.TRADITIONAL_CHINESE_HONG_KONG;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.TRADITIONAL_CHINESE_TAIWAN;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.INDONESIAN_11;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.INDONESIAN_17;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.INDONESIAN_INDONESIA_11;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.INDONESIAN_INDONESIA_17;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.HEBREW_11;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.HEBREW_17;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.HEBREW_ISRAEL_11;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.LocaleNtdProvider.HEBREW_ISRAEL_17;

@RunWith( MockitoJUnitRunner.class )
public class LocalNtdProviderTest {

  /**
   * note we require GERMAN (GREECE) for jackrabbit repository backwards compatibility, if the repository is implemented
   * with a different solution, this requirement and test will no longer be required.
   */
  @Test
  public void localeTest() {
    final List<String> localeNames = LocaleNtdProvider.getLocaleNames();
    assert( localeNames.contains( GERMAN_GREECE ) );
    assert( localeNames.contains( INDONESIAN_11 ) );
    assert( localeNames.contains( INDONESIAN_17 ) );
    assert( localeNames.contains( INDONESIAN_INDONESIA_11 ) );
    assert( localeNames.contains( INDONESIAN_INDONESIA_17 ) );
    assert( localeNames.contains( HEBREW_11 ) );
    assert( localeNames.contains( HEBREW_17 ) );
    assert( localeNames.contains( HEBREW_ISRAEL_11 ) );
    assert( localeNames.contains( HEBREW_ISRAEL_17 ) );

     /* All the below locales were added in Java 11. Locales are used as NodeType children in Jackrabbit. If a user
        upgrades to Java 11, then reverts back to Java 8, the jackrabbit repository will have non-trivial remove
        operations for each of these locales. To prevent this don't add these new locales at this time.
        Note - When Java 8 is no longer supported the below assertions can be removed */
    assertFalse( localeNames.contains( SIMPLIFIED_CHINESE_SINGAPORE ) );
    assertFalse( localeNames.contains( TRADITIONAL_CHINESE_TAIWAN ) );
    assertFalse( localeNames.contains( TRADITIONAL_CHINESE_HONG_KONG ) );
    assertFalse( localeNames.contains( SIMPLIFIED_CHINESE_CHINA ) );
    assertFalse( localeNames.contains( NORWEGIAN_BOKMAL ) );
    assertFalse( localeNames.contains( NORWEGIAN ) );
    assertFalse( localeNames.contains( NORWEGIAN_NYNORSK ) );
    assert( localeNames.size() > 100 );
  }
}