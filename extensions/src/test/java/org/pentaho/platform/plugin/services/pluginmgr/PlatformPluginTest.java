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

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlatformPluginTest {
  @Test
  public void testAddLifecycleListenerClassname() {
    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.addLifecycleListenerClassname( "bogus1" );
    platformPlugin.addLifecycleListenerClassname( "bogus2" );
    List<String> classnames = platformPlugin.getLifecycleListenerClassnames();
    assertEquals( 2, classnames.size() );
    assertTrue( classnames.contains( "bogus1" ) );
    assertTrue( classnames.contains( "bogus2" ) );
  }

  @Test
  public void testSetLifecycleListenerClassname() {
    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setLifecycleListenerClassname( "bogus1" );
    platformPlugin.setLifecycleListenerClassname( "bogus2" );
    List<String> classnames = platformPlugin.getLifecycleListenerClassnames();
    assertEquals( 2, classnames.size() );
    assertTrue( classnames.contains( "bogus1" ) );
    assertTrue( classnames.contains( "bogus2" ) );
  }

  @Test
  public void testGetTitle() {
    PlatformPlugin spyPlatformPlugin = spy( PlatformPlugin.class );
    spyPlatformPlugin.setTitle( "pluginTitle" );
    when( spyPlatformPlugin.localizeInterpolatedString( "pluginTitle", null ) )
      .thenReturn( "Test Plugin" );
    when( spyPlatformPlugin.getTitle() ).thenCallRealMethod();

    assertEquals( "Test Plugin", spyPlatformPlugin.getTitle() );
  }

  @Test
  public void testGetTitleLocale() {
    PlatformPlugin spyPlatformPlugin = spy( PlatformPlugin.class );
    spyPlatformPlugin.setTitle( "pluginTitle" );
    when( spyPlatformPlugin.localizeInterpolatedString( "pluginTitle", Locale.FRANCE ) )
      .thenReturn( "Test Plugin" );
    when( spyPlatformPlugin.getTitle( any() ) ).thenCallRealMethod();

    assertEquals( "Test Plugin", spyPlatformPlugin.getTitle( Locale.FRANCE ) );
  }

  @Test
  public void testGetDescription() {
    PlatformPlugin spyPlatformPlugin = spy( PlatformPlugin.class );
    spyPlatformPlugin.setDescription( "pluginDescription" );
    when( spyPlatformPlugin.localizeInterpolatedString( "pluginDescription", null ) )
      .thenReturn( "Test Plugin" );
    when( spyPlatformPlugin.getDescription() ).thenCallRealMethod();

    assertEquals( "Test Plugin", spyPlatformPlugin.getDescription() );
  }

  @Test
  public void testGetDescriptionLocale() {
    PlatformPlugin spyPlatformPlugin = spy( PlatformPlugin.class );
    spyPlatformPlugin.setDescription( "pluginDescription" );
    when( spyPlatformPlugin.localizeInterpolatedString( "pluginDescription", Locale.FRANCE ) )
      .thenReturn( "Test Plugin" );
    when( spyPlatformPlugin.getDescription( any() ) ).thenCallRealMethod();

    assertEquals( "Test Plugin", spyPlatformPlugin.getDescription( Locale.FRANCE ) );
  }

  @Test
  public void testLocalizeInterpolatedString() {
    ResourceBundle mockResourceBundle = mock( ResourceBundle.class );
    when( mockResourceBundle.getString( "key1" ) ).thenReturn( "key_1" );
    when( mockResourceBundle.getString( "key2" ) ).thenReturn( "key_2" );
    CachingResourceBundleProvider mockCRBProvider = mock( CachingResourceBundleProvider.class );
    when( mockCRBProvider.getResourceBundle( Locale.FRANCE ) ).thenReturn( mockResourceBundle );

    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleProvider( mockCRBProvider );

    assertEquals(
      "key_1 - key_1 key_2",
      platformPlugin.localizeInterpolatedString( "${key1} - ${key1} ${key2}", Locale.FRANCE )
    );
  }

  @Test
  public void testLocalizeInterpolatedStringPartial() {
    ResourceBundle mockResourceBundle = mock( ResourceBundle.class );
    when( mockResourceBundle.getString( "key1" ) ).thenReturn( "key_1" );
    CachingResourceBundleProvider mockCRBProvider = mock( CachingResourceBundleProvider.class );
    when( mockCRBProvider.getResourceBundle( Locale.FRANCE ) ).thenReturn( mockResourceBundle );

    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleProvider( mockCRBProvider );

    assertEquals(
      "key_1 - key_1 ${key2}",
      platformPlugin.localizeInterpolatedString( "${key1} - ${key1} ${key2}", Locale.FRANCE )
    );
  }

  @Test
  public void testLocalizeInterpolatedStringNoInterpolation() {
    ResourceBundle mockResourceBundle = mock( ResourceBundle.class );
    CachingResourceBundleProvider mockCRBProvider = mock( CachingResourceBundleProvider.class );
    when( mockCRBProvider.getResourceBundle( Locale.FRANCE ) ).thenReturn( mockResourceBundle );

    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleProvider( mockCRBProvider );

    assertEquals(
      "value with no keys",
      platformPlugin.localizeInterpolatedString( "value with no keys", Locale.FRANCE )
    );

    verify( mockResourceBundle, never() ).getString( any() );
  }

  @Test
  public void testLocalizeInterpolatedStringMissingResourceException() {
    CachingResourceBundleProvider mockCRBProvider = mock( CachingResourceBundleProvider.class );
    doThrow( MissingResourceException.class ).when( mockCRBProvider ).getResourceBundle( any() );

    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleProvider( mockCRBProvider );

    assertEquals(
      "${key1} - ${key1} ${key2}",
      platformPlugin.localizeInterpolatedString( "${key1} - ${key1} ${key2}", Locale.FRANCE )
    );

    verify( mockCRBProvider ).getResourceBundle( any() );
  }

  @Test
  public void testLocalizeInterpolatedStringEmpty() {
    CachingResourceBundleProvider mockCRBProvider = mock( CachingResourceBundleProvider.class );

    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleProvider( mockCRBProvider );

    assertEquals( "", platformPlugin.localizeInterpolatedString( "", Locale.FRANCE ) );
  }

  @Test
  public void testLocalizeInterpolatedStringNull() {
    CachingResourceBundleProvider mockCRBProvider = mock( CachingResourceBundleProvider.class );

    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleProvider( mockCRBProvider );

    assertNull( platformPlugin.localizeInterpolatedString( null, Locale.FRANCE ) );
  }

  @Test
  public void testGetResourceBundleClassName() {
    PlatformPlugin platformPlugin = new PlatformPlugin();
    platformPlugin.setResourceBundleClassName( "messages" );

    assertEquals( "messages", platformPlugin.getResourceBundleClassName() );
  }
}
