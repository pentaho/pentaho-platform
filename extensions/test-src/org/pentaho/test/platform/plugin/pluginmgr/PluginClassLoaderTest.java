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

package org.pentaho.test.platform.plugin.pluginmgr;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class PluginClassLoaderTest {

  private PluginClassLoader pluginLoader;

  @Before
  public void init() {
    // now load a class
    pluginLoader = new PluginClassLoader( new File( "./test-res/PluginClassLoaderTest/" ),
      getClass().getClassLoader() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testOverrideLoad() throws ClassNotFoundException, SecurityException, InstantiationException,
    IllegalAccessException {
    pluginLoader.setOverrideLoad( true );

    String className = "org.pentaho.test.platform.plugin.pluginmgr.ClassToOverride";

    Class clazz = Class.forName( className, true, getClass().getClassLoader() );
    Object o = clazz.newInstance();
    assertEquals( "I am the original class from the parent class loader", o.toString() );

    Class overridenClazz = Class.forName( className, true, pluginLoader );
    Object o2 = overridenClazz.newInstance();
    assertEquals( "I am the overridden class from the plugin class loader", o2.toString() );
  }

  private Object getContainerFromPrivateClassLoader() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException {
    String className = "org.pentaho.test.platform.plugin.pluginmgr.ClassToOverrideContainer";
    Class<?> t = Class.forName( className, true, pluginLoader );
    return t.newInstance();
  }

  @Test
  public void testImplicitLoad() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    pluginLoader.setOverrideLoad( true );

    //
    // 1. Implicitly create an instance of ClassToOverride and check that it was loaded by the default loader.
    //
    ClassToOverride cto = new ClassToOverride();
    assertNotSame( PluginClassLoader.class.getName(), cto.getClass().getClassLoader().getClass().getName() );
    assertEquals( "failed to load original class", "I am the original class from the parent class loader", cto
        .toString() );

    // NOTE: implicit loading of a class (i.e. "new" or static method access) is always performed by the
    // classloader of the enclosing class (the "current" classloader), so if we set
    // Thread.currentThread().setContextClassLoader(pluginLoader) and then did a new ClassToOverride(),
    // the pluginLoader would *not* be asked to load ClassToOverride, rather the current classloader
    // will be asked to load it.

    //
    // 2. Ask a class that we are certain was loaded by the private loader to implicitly create an instance
    // of ClassToOverride and check that the class was loaded by the private loader
    //
    Object explicitlyLoadedContainer = getContainerFromPrivateClassLoader();
    assertEquals( PluginClassLoader.class.getName(), explicitlyLoadedContainer.getClass().getClassLoader().getClass()
        .getName() );
    assertEquals( "failed to load override class", "I am the overridden class from the plugin class loader",
        explicitlyLoadedContainer.toString() );
  }

  @Test
  public void testLoadClass() throws IOException, ClassNotFoundException {
    // now try getting it as a class
    Class<?> testClass = pluginLoader.loadClass( "org.pentaho.test.platform.engine.services.TestClassForClassloader" );
    assertNotNull( "class is null", testClass );
    assertEquals( "wrong class", "org.pentaho.test.platform.engine.services.TestClassForClassloader", testClass
        .getName() );
  }

  @Test
  public void testLoadClassAsResource() throws IOException, ClassNotFoundException {
    // test the byte array first
    InputStream in =
        pluginLoader.getResourceAsStream( "org/pentaho/test/platform/engine/services/TestClassForClassloader.class" );
    assertNotNull( "Could not find class TestClassForClassloader in jar file", in );

    byte[] b = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "method is missing", classBytes
        .contains( "org/pentaho/test/platform/engine/services/TestClassForClassloader" ) );
  }

  private byte[] toBytes( InputStream in ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] b = new byte[2048];
    int n = in.read( b );
    while ( n != -1 ) {
      out.write( b, 0, n );
      n = in.read( b );
    }
    return out.toByteArray();
  }

  @Test
  public void testLoadXml() throws IOException {
    InputStream in = pluginLoader.getResourceAsStream( "test1.xml" );
    assertNotNull( "input stream is null", in );

    byte[] b = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" ) );
  }

  @Test
  public void testLoadBadResource() throws IOException {
    InputStream in = pluginLoader.getResourceAsStream( "bogus.xml" );
    assertNull( "input stream should be null", in );
  }

  @Test
  public void testLoadBadClass() throws IOException {
    // now try getting it as a class
    try {
      pluginLoader.loadClass( "bogus" );
      assertFalse( "Exception expected", true );
    } catch ( ClassNotFoundException e ) {
      assertTrue( "Exception expected", true );
    }
  }

  @Test
  public void testLoadProperties_fromJar() throws IOException {
    InputStream in = pluginLoader.getResourceAsStream( "org/pentaho/test/platform/engine/services/test.properties" );
    assertNotNull( "input stream is null", in );

    byte[] b = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "test_setting=test" ) );

  }

  @Test
  public void testLoadProperties_fromDir() throws IOException {
    InputStream in = pluginLoader.getResourceAsStream( "resources/plugin-classloader-test-inresourcesdir.properties" );
    assertNotNull( "input stream is null", in );

    byte[] b = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "name=" ) );

  }

  @Test
  public void testFindXmlResource() throws IOException {
    URL url = pluginLoader.getResource( "test1.xml" );

    assertNotNull( "URL is null", url );

    InputStream in = url.openStream();
    assertNotNull( "input stream is null", in );

    byte[] b = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" ) );

  }

  @Test
  public void testFindClassResource() throws IOException {
    InputStream in =
        pluginLoader.getResourceAsStream( "org/pentaho/test/platform/engine/services/TestClassForClassloader.class" );

    assertNotNull( "input stream is null", in );

    byte[] b = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "TestClassForClassloader" ) );

  }

  @Test
  public void testFindBadResource() throws IOException {
    URL url = pluginLoader.getResource( "bogus.xml" );

    assertNull( "URL should be null", url );

  }

  @Test
  public void testFindResources() throws IOException {
    Enumeration<URL> urls = pluginLoader.getResources( "test1.xml" );

    assertNotNull( "URLS is null", urls );

    int count = 0;
    while ( urls.hasMoreElements() ) {
      URL url = urls.nextElement();
      InputStream in = url.openStream();
      assertNotNull( "input stream is null", in );

      byte[] b = toBytes( in );
      String xml = new String( b );
      assertTrue( "xml is wrong", xml.contains( "<test1>" ) );
      count++;
    }

    assertEquals( "Wrong number of URLS", 1, count );
  }

  @Test
  public void testFindBadResources() throws IOException {
    Enumeration<URL> urls = pluginLoader.getResources( "bogus.xml" );

    assertNotNull( "URLS is null", urls );

    int count = 0;
    while ( urls.hasMoreElements() ) {
      count++;
    }

    assertEquals( "Wrong number of URLS", 0, count );
  }

  @Test
  public void testJarListedInClassLoader() throws ClassNotFoundException {

    boolean jarFound = false;
    for ( URL url : pluginLoader.getURLs() ) {
      if ( url.toString().contains( "test-jar.jar" ) ) {
        jarFound = true;
      }
    }

    assertTrue( "test-jar.jar not found in classloader", jarFound );

    // now load a class from the jar
    pluginLoader.loadClass( "org.pentaho.test.platform.engine.services.TestClassForClassloader" );
  }

  // @Test
  // public void testIsPluginClass() throws ClassNotFoundException {
  // Class testClass = loader.loadClass("org/pentaho/test/platform/engine/services/TestClassForClassloader");
  // assertTrue("Class should have been identified as a plugin class", loader.isPluginClass(testClass));
  //
  // assertFalse("Class should NOT have been identified as a plugin class", loader.isPluginClass(String.class));
  // }
}
