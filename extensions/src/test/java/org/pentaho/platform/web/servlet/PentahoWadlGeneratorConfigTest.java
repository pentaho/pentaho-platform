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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.util.IWadlDocumentResource;

import org.glassfish.jersey.server.wadl.config.WadlGeneratorDescription;

public class PentahoWadlGeneratorConfigTest {

  PentahoWadlGeneratorConfig spyConfig;

  @Before
  public void setUp() {
    spyConfig = spy( new PentahoWadlGeneratorConfig() );
  }

  @Test
  public void testConfigure() throws Exception {
    List<WadlGeneratorDescription> result;

    doReturn( null ).when( spyConfig ).getBuilder( null );

    String originalRequest = "/api/application.wadl";
    doReturn( originalRequest ).when( spyConfig ).getOriginalRequest();
    result = spyConfig.configure();
    verify( spyConfig, times( 1 ) ).getBuilder( null );
    verify( spyConfig, times( 1 ) ).getOriginalRequest();
    assertTrue( result.size() == 0 );

    originalRequest = "/asdffsadasdfdsaf/adsafds.wadl";
    doReturn( originalRequest ).when( spyConfig ).getOriginalRequest();
    result = spyConfig.configure();
    verify( spyConfig, times( 2 ) ).getBuilder( null );
    verify( spyConfig, times( 2 ) ).getOriginalRequest();
    assertTrue( result.size() == 0 );

    originalRequest = "/plugin/reporting/api/application.wadl";
    doReturn( originalRequest ).when( spyConfig ).getOriginalRequest();
    result = spyConfig.configure();
    verify( spyConfig, times( 1 ) ).getBuilder( "reporting" );
    verify( spyConfig, times( 3 ) ).getOriginalRequest();
    assertTrue( result.size() == 0 );

    List<WadlGeneratorConfig> wadlGeneratorConfigList = new ArrayList<WadlGeneratorConfig>();
    wadlGeneratorConfigList.add( mock( WadlGeneratorConfig.class ) );

    WadlGeneratorConfig.WadlGeneratorConfigDescriptionBuilder builder = mock(
        WadlGeneratorConfig.WadlGeneratorConfigDescriptionBuilder.class );
    doReturn( builder ).when( spyConfig ).getBuilder( null );
    doReturn( builder ).when( spyConfig ).getBuilder( "reporting" );
    doReturn( wadlGeneratorConfigList ).when( builder ).descriptions();

    originalRequest = "/api/application.wadl";
    doReturn( originalRequest ).when( spyConfig ).getOriginalRequest();
    result = spyConfig.configure();
    verify( spyConfig, times( 3 ) ).getBuilder( null );
    verify( spyConfig, times( 4 ) ).getOriginalRequest();
    assertTrue( result.size() == 1 );

    originalRequest = "/asdffsadasdfdsaf/adsafds.wadl";
    doReturn( originalRequest ).when( spyConfig ).getOriginalRequest();
    result = spyConfig.configure();
    verify( spyConfig, times( 4 ) ).getBuilder( null );
    verify( spyConfig, times( 5 ) ).getOriginalRequest();
    assertTrue( result.size() == 1 );

    originalRequest = "/plugin/reporting/api/application.wadl";
    doReturn( originalRequest ).when( spyConfig ).getOriginalRequest();
    result = spyConfig.configure();
    verify( spyConfig, times( 2 ) ).getBuilder( "reporting" );
    verify( spyConfig, times( 6 ) ).getOriginalRequest();
    assertTrue( result.size() == 1 );
  }

  @Test
  public void testGetBuilder() throws Exception {
    List<IWadlDocumentResource> list = new ArrayList<IWadlDocumentResource>();
    IWadlDocumentResource documentResource1 = mock( IWadlDocumentResource.class ),
        documentResource2 = mock( IWadlDocumentResource.class ),
        documentResource3 = mock( IWadlDocumentResource.class );
    list.add( documentResource1 );
    list.add( documentResource2 );
    list.add( documentResource3 );

    doReturn( false ).when( documentResource1 ).isFromPlugin();
    doReturn( true ).when( documentResource2 ).isFromPlugin();
    doReturn( true ).when( documentResource3 ).isFromPlugin();
    doReturn( "" ).when( documentResource1 ).getPluginId();
    doReturn( "reporting" ).when( documentResource2 ).getPluginId();
    doReturn( "data-access" ).when( documentResource3 ).getPluginId();
    doReturn( mock( InputStream.class ) ).when( documentResource1 ).getResourceAsStream();
    doReturn( mock( InputStream.class ) ).when( documentResource2 ).getResourceAsStream();
    doThrow( new IOException() ).when( documentResource3 ).getResourceAsStream();
    doReturn( list ).when( spyConfig ).getWadlDocumentResources();

    assertNotNull( spyConfig.getBuilder( null ) );
    assertNotNull( spyConfig.getBuilder( "reporting" ) );
    assertNull( spyConfig.getBuilder( "data-access" ) );

    doThrow( new IOException() ).when( documentResource1 ).getResourceAsStream();
    assertNull( spyConfig.getBuilder( null ) );
  }
}
