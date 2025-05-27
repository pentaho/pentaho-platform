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

package org.pentaho.platform.plugin.services.pluginmgr;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CachingResourceBundleProviderTest {
  @Test
  public void testCachingResourceBundleProvider() {
    CachingResourceBundleProvider crbProvider = new CachingResourceBundleProvider( locale -> {
      // Mock implementation of the delegate's getResourceBundle method
      assertEquals( Locale.FRANCE, locale );
      return Mockito.mock( ResourceBundle.class );
    } );

    ResourceBundle cachedResourceBundle = crbProvider.getResourceBundle( Locale.FRANCE );
    // consecutive calls with the same locale should return the same cached ResourceBundle
    assertEquals( cachedResourceBundle, crbProvider.getResourceBundle( Locale.FRANCE ) );
  }

  @Test
  public void testCachingResourceBundleNullLocale() {
    CachingResourceBundleProvider crbProvider = new CachingResourceBundleProvider( locale -> {
      // Mock implementation of the delegate's getResourceBundle method
      assertEquals( LocaleHelper.getLocale(), locale );
      return Mockito.mock( ResourceBundle.class );
    } );

    ResourceBundle cachedResourceBundle = crbProvider.getResourceBundle( null );
    // consecutive calls with the same locale should return the same cached ResourceBundle
    assertEquals( cachedResourceBundle, crbProvider.getResourceBundle( null ) );
  }

  @Test
  public void testCachingResourceBundleUncached() {
    CachingResourceBundleProvider crbProvider = new CachingResourceBundleProvider( locale -> {
      // Mock implementation of the delegate's getResourceBundle method
      return Mockito.mock( ResourceBundle.class );
    } );

    ResourceBundle cachedResourceBundle = crbProvider.getResourceBundle( Locale.FRANCE );
    // consecutive calls with different locales should not return the same cached ResourceBundle
    assertNotEquals( cachedResourceBundle, crbProvider.getResourceBundle( Locale.CANADA ) );
  }
}
