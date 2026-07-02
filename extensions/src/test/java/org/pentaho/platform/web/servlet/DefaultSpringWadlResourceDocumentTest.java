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


package org.pentaho.platform.web.servlet;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.ee10.webapp.WebAppClassLoader;
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
