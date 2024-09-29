/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.plugin.pluginmgr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings( "nls" )
@RunWith( MockitoJUnitRunner.class )
public class PluginResourceLoaderTest {

  public static final String TEST_RES = "src/test/resources/";

  private PluginResourceLoader resLoader;

  private Class<?> pluginClass;

  private PluginClassLoader classLoader;

  @Before
  public void init() throws ClassNotFoundException {
    resLoader = new PluginResourceLoader();
    classLoader =
      new PluginClassLoader( new File( "./" + TEST_RES + "PluginResourceLoaderTest" ), getClass().getClassLoader() );
    pluginClass = classLoader.loadClass( "PluginResLoaderDummyClass" );
  }

  @Test
  public void testGetResource_fromFileSystem() throws UnsupportedEncodingException {
    InputStream in = resLoader.getResourceAsStream( pluginClass, "pluginResourceTest.properties" );
    assertNotNull( "Failed to get resource as stream", in );

    byte[] bytes = resLoader.getResourceAsBytes( pluginClass, "pluginResourceTest.properties" );
    assertNotNull( "Failed to get resource as bytes", bytes );

    String s = resLoader.getResourceAsString( pluginClass, "pluginResourceTest.properties" );
    assertNotNull( "Failed to get resource as string", s );

    s = resLoader.getResourceAsString( pluginClass, "pluginResourceTest.properties", "UTF-8" );
    assertNotNull( "Failed to get resource as string", s );

    // load a resource from a subdirectory
    in = resLoader.getResourceAsStream( pluginClass, "resources/pluginResourceTest-inresources.properties" );
    assertNotNull( "Failed to get resource as stream", in );
  }

  @Test
  public void testGetResource_FileDNE() throws UnsupportedEncodingException {
    InputStream in = resLoader.getResourceAsStream( pluginClass, "non-existent-file" );
    assertNull( "InputStream should have been null indicating resource not found", in );

    byte[] bytes = resLoader.getResourceAsBytes( pluginClass, "non-existent-file" );
    assertNull( "byte array should have been null indicating resource not found", bytes );

    String s = resLoader.getResourceAsString( pluginClass, "non-existent-file" );
    assertNull( "InputStream should have been null indicating resource not found", s );
  }

  @Test( expected = UnsupportedEncodingException.class )
  public void testBadStringEncoding() throws UnsupportedEncodingException {
    @SuppressWarnings( "unused" )
    String s = resLoader.getResourceAsString( pluginClass, "pluginResourceTest.properties", "bogus encoding" );
  }

  @Test
  public void testGetResourceBundleFromInsideJar() {
    ResourceBundle.getBundle( "pluginResourceTest-injar", LocaleHelper.getLocale(), classLoader );
  }

  @Test
  public void testGetResourceBundleFromResourcesDir() {
    // this properties file lives in the "resources" directory under the plugin root dir

    // test that retrieving a resource bundle works the same by in the resource loader and the java ResourceBundle api
    ResourceBundle.getBundle( "resources/pluginResourceTest-inresources", LocaleHelper.getLocale(), classLoader );
    ResourceBundle.getBundle( "resources.pluginResourceTest-inresources", LocaleHelper.getLocale(), classLoader );

    resLoader.getResourceBundle( pluginClass, "resources/pluginResourceTest-inresources" );
    resLoader.getResourceBundle( pluginClass, "resources.pluginResourceTest-inresources" );
  }

  @Test
  public void ItestGetResource_fromClassLoader() {
    // find a properties file included in a jar
    assertNotNull( "Could not find the properties file embededd in the jar", resLoader.getResourceAsStream(
        pluginClass, "pluginResourceTest-injar.properties" ) );
    // find a properties file at the classloader root directory
    assertNotNull( "Could not find the properties file on the classloader root dir", resLoader.getResourceAsStream(
        pluginClass, "pluginResourceTest.properties" ) );
    assertNotNull( "Could not find the properties file embededd in the jar", resLoader.getResourceAsStream(
        pluginClass, "org/pentaho/test/pluginResourceTest-deepinjar.properties" ) );
    assertNotNull( "Could not find the properties file embededd in the jar", resLoader.getResourceAsStream(
        pluginClass, "org/pentaho/test/file.with.dots.in.name.properties" ) );
  }

  @Test
  public void testPluginPath() {
    String path = resLoader.getSystemRelativePluginPath( pluginClass.getClassLoader() );
    assertTrue( "Plugin path is not correct", path.endsWith( TestResourceLocation.TEST_RESOURCES + "/PluginResourceLoaderTest" ) ); //$NON-NLS-2$
  }

  @Test
  public void testGetPluginSettings() {

    final ISystemSettings mockSettings = mock( ISystemSettings.class );

    final String fullPathToSettingsFile =
        resLoader.getSystemRelativePluginPath( pluginClass.getClassLoader() ) + "/settings.xml";

    when( mockSettings.getSystemSetting( fullPathToSettingsFile, "testsetting", null ) ).thenReturn( "false" );
    when( mockSettings.getSystemSetting( fullPathToSettingsFile, "bogussetting", null ) ).thenReturn( null );
    when( mockSettings.getSystemSetting( fullPathToSettingsFile, "bogussetting", "true" ) ).thenReturn( "true" );

    PentahoSystem.setSystemSettingsService( mockSettings );

    assertEquals( "Cache value incorrect", "false", resLoader.getPluginSetting( pluginClass, "testsetting" ) );

    assertNull( "Bogus value should not have been found", resLoader.getPluginSetting( pluginClass, "bogussetting" ) );

    assertEquals( "Bogus value should have a default of true", "true", resLoader.getPluginSetting( pluginClass,
        "bogussetting", "true" ) );
  }

  @Test
  public void testFindResources_propertyFiles() throws URISyntaxException {
    List<URL> urls = resLoader.findResources( pluginClass, "*properties*" );
    boolean propFile1Found = false, propFile2Found = false;
    for ( URL url : urls ) {
      File f = new File( url.toURI() );
      String fileName = f.getName();
      if ( "pluginResourceTest-inresources.properties".equals( fileName ) ) {
        propFile1Found = true;
      }
      if ( "pluginResourceTest.properties".equals( fileName ) ) {
        propFile2Found = true;
      }
    }
    assertTrue( "pluginResourceTest-inresources.properties was not found", propFile1Found );
    assertTrue( "pluginResourceTest.properties was not found", propFile2Found );
  }

  @Test
  public void testFindResources_allRecursive() {
    List<URL> urls = resLoader.findResources( pluginClass, "templates/*" );
    for ( URL url : urls ) {
      System.err.println( url.getPath() );
      assertTrue( "Url does not contain templates dir in path: " + url.getPath(),
        url.getPath().contains( "templates" ) );
    }
    boolean found = false;
    for ( URL url : urls ) {
      found = url.getPath().endsWith( "test.html" );
      if ( found ) {
        break;
      }
    }
    assertTrue( "Template not found", found );
  }

  /**
   * <p>
   *   Path Traversal Mitigation.
   * </p>
   * Given path of an existing resource file, containing upper directories references.
   * When the file is being requested on this path, an exception should be thrown.
   */
  @Test
  public void shouldFailWhenExistingResourcePathIsReferringUpperDirectories() throws UnsupportedEncodingException {
    try {
      resLoader.getResourceAsStream( pluginClass, "resources/../settings.xml" );
      fail();
    } catch ( IllegalArgumentException e ) {
      //
    }
    try {
      resLoader.getResourceAsBytes( pluginClass, "resources/../settings.xml" );
      fail();
    } catch ( IllegalArgumentException e ) {
      //
    }
    try {
      resLoader.getResourceAsString( pluginClass, "resources/../settings.xml" );
      fail();
    } catch ( IllegalArgumentException e ) {
      //
    }
  }

  /**
   * <p>
   *   Path Traversal Mitigation.
   * </p>
   * Given path of an NON-existing resource file, containing upper directories references.
   * When the file is being requested on this path, an exception should be thrown.
   */
  @Test
  public void shouldFailWhenNonExistingResourcePathIsReferringUpperDirectories() throws UnsupportedEncodingException {
    try {
      resLoader.getResourceAsStream( pluginClass, "resources/../settings.xml-non-existing" );
      fail();
    } catch ( IllegalArgumentException e ) {
      //
    }
    try {
      resLoader.getResourceAsBytes( pluginClass, "resources/../settings.xml-non-existing" );
      fail();
    } catch ( IllegalArgumentException e ) {
      //
    }
    try {
      resLoader.getResourceAsString( pluginClass, "resources/../settings.xml-non-existing" );
      fail();
    } catch ( IllegalArgumentException e ) {
      //
    }
  }

  /**
   * <p>
   *   Path Traversal Mitigation.
   * </p>
   * <p>
   *   Doing path traversal check, we do not authorize the request to the file,
   *   we just avoid directory traversal.
   *   Thus, settings.xml is still available by legal direct link.
   * </p>
   * Given path of an existing resource file, NOT containing upper directories references.
   * When the file is being requested on this path, the requested resource should be returned.
   */
  @Test
  public void shouldNotFailWhenExistingResourcePathIsNotReferringUpperDirectories() throws UnsupportedEncodingException {
    assertNotNull( resLoader.getResourceAsStream( pluginClass, "settings.xml" ) );
    assertNotNull( resLoader.getResourceAsBytes( pluginClass, "settings.xml" ) );
    assertNotNull( resLoader.getResourceAsString( pluginClass, "settings.xml" ) );
  }

  /**
   * <p>
   *   Path Traversal Mitigation.
   * </p>
   * <p>
   *   Path values are often got from concatenated strings, and might therefore contain double slashes.
   *   That slashes should be considered as one, and we should not fall here.
   * </p>
   * Given path of an existing resource file, NOT containing upper directories references,
   * but containing double slashes.
   * When the file is being requested on this path, the requested resource should be returned.
   */
  @Test
  public void shouldNotFailWhenExistingResourcePathIsContainingDoubleSlashes() throws UnsupportedEncodingException {
    assertNotNull( resLoader.getResourceAsStream( pluginClass, "resources//pluginResourceTest-inresources.properties" ) );
    assertNotNull( resLoader.getResourceAsBytes( pluginClass, "resources//pluginResourceTest-inresources.properties" ) );
    assertNotNull( resLoader.getResourceAsString( pluginClass, "resources//pluginResourceTest-inresources.properties" ) );
  }
}
