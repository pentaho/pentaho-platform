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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class DefaultSpringWadlResourceDocumentTest {
  DefaultSpringWadlResourceDocument wadlResourceDocumentPlugin, wadlResourceDocumentPluginSpy,
      wadlResourceDocumentWebapp, wadlResourceDocumentWebappSpy;
  ClassPathResource classPathResourcePlugin, classPathResourceWebapp;
  PluginClassLoader pluginClassLoader;
  WebAppClassLoader webAppClassLoader;

  String pluginName = "reporting";
  private static String WADL_NAME = "META-INF/wadl/wadlExtension.xml";

  @Before
  public void setUp() throws Exception {
    File plugin = mock( File.class );
    doReturn( pluginName ).when( plugin ).getName();
    pluginClassLoader = mock( PluginClassLoader.class );
    doReturn( plugin ).when( pluginClassLoader ).getPluginDir();
    classPathResourcePlugin = new ClassPathResource( pluginName, pluginClassLoader );
    wadlResourceDocumentPlugin = new DefaultSpringWadlResourceDocument( classPathResourcePlugin );
    wadlResourceDocumentPluginSpy = spy( wadlResourceDocumentPlugin );

    webAppClassLoader = mock( WebAppClassLoader.class );
    classPathResourceWebapp = new ClassPathResource( pluginName, webAppClassLoader );
    wadlResourceDocumentWebapp = new DefaultSpringWadlResourceDocument( classPathResourceWebapp );
    wadlResourceDocumentWebappSpy = spy( wadlResourceDocumentWebapp );
  }

  @Test
  public void testConstructor() throws Exception {
    assertTrue( wadlResourceDocumentPlugin.isFromPlugin() );
    assertEquals( wadlResourceDocumentPlugin.getPluginId(), pluginName );

    assertFalse( wadlResourceDocumentWebapp.isFromPlugin() );
    assertEquals( wadlResourceDocumentWebapp.getPluginId(), "" );
  }

  @Test
  public void testGetResourceAsStream() throws Exception {
    URL urlReporting = new URL( "file:system/reporting/wadlExtension.xml" ),
        urlMyPlugin = new URL( "file:system/myplugin/wadlExtension.xml" );

    Set<URL> urls = new HashSet<URL>();
    urls.add( urlReporting );
    urls.add( urlMyPlugin );

    Enumeration<URL> urlsEnumeration = java.util.Collections.enumeration( urls );
    doReturn( urlsEnumeration ).when( pluginClassLoader ).getResources( WADL_NAME );
    doReturn( "system" ).when( wadlResourceDocumentPluginSpy ).getSystemPath();
    doReturn( mock( InputStream.class ) ).when( wadlResourceDocumentPluginSpy ).getInputStream( urlReporting );

    assertNotNull( wadlResourceDocumentPluginSpy.getResourceAsStream() );

    urlsEnumeration = java.util.Collections.enumeration( urls );
    doReturn( urlsEnumeration ).when( webAppClassLoader ).getResources( WADL_NAME );
    doReturn( "system" ).when( wadlResourceDocumentWebappSpy ).getSystemPath();
    doReturn( mock( InputStream.class ) ).when( wadlResourceDocumentWebappSpy ).getInputStream( urlReporting );
    doReturn( mock( InputStream.class ) ).when( wadlResourceDocumentWebappSpy ).getInputStream( urlMyPlugin );

    assertNotNull( wadlResourceDocumentWebappSpy.getResourceAsStream() );

    urls = new HashSet<URL>();
    urls.add( urlMyPlugin );
    urlsEnumeration = java.util.Collections.enumeration( urls );
    doReturn( urlsEnumeration ).when( pluginClassLoader ).getResources( WADL_NAME );
    assertNull( wadlResourceDocumentPluginSpy.getResourceAsStream() );
  }
}
