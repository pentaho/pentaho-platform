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


package org.pentaho.test.platform.web;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.web.servlet.GetResource;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.matches;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.endsWith;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.GetResource</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class GetResourceIT {
  private static final String TEST_MIME_TYPE = "test-mime-type";
  private static final String RESOURCE_PARAM = "resource";
  private static final String CONTENT_DISPOSITION_HEADER = "content-disposition";

  private MicroPlatform mp = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution" );
  private HttpServletRequest request;
  private HttpServletResponse response;
  private GetResource servlet;

  @Before
  public void setUp() throws PlatformInitializationException, ServletException {
    request = mock( HttpServletRequest.class );
    when( request.getMethod() ).thenReturn( "GET" );

    response = mock( HttpServletResponse.class );

    servlet = spy( new GetResource() );
    final ServletConfig servletConfig = mock( ServletConfig.class );
    final ServletContext servletContext = mock( ServletContext.class );
    when( servletContext.getMimeType( nullable( String.class ) ) ).thenReturn( TEST_MIME_TYPE );
    when( servletConfig.getServletContext() ).thenReturn( servletContext );
    servlet.init( servletConfig );

    mp.start();
  }

  @After
  public void tearDown() throws Exception {
    mp.stop();

    request = null;
    response = null;
    servlet = null;
    mp = null;
  }

  @Test
  public void testGetResource() throws ServletException, IOException {
    String resource = "adhoc/picklist.xsl";

    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( resource );

    final ServletOutputStream outputStream = mock( ServletOutputStream.class );
    final MutableInt fileLength = new MutableInt( 0 );
    doAnswer( (Answer<Void>) invocation -> {
      fileLength.add( (Integer) invocation.getArguments()[2] );
      return null;
    } ).when( outputStream ).write( any( byte[].class ), anyInt(), anyInt() );
    when( response.getOutputStream() ).thenReturn( outputStream );

    servlet.service( request, response );

    verify( response ).setContentType( eq( TEST_MIME_TYPE ) );
    verify( response ).setHeader( eq( CONTENT_DISPOSITION_HEADER ),
      endsWith( resource.substring( resource.lastIndexOf( "/" ) + 1 ) ) );

    final int expected = new Long( new File( PentahoSystem.getApplicationContext()
      .getSolutionPath( "system/custom/xsl/" + resource ) ).length() ).intValue();
    assertEquals( expected, fileLength.intValue() );
    verify( response ).setContentLength( eq( expected ) );
  }

  @Test
  public void testNotSupportedExt() throws ServletException, IOException {
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( "not_supported.nsp" );

    servlet.service( request, response );

    verify( response ).sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
    verify( servlet ).error( matches( ".*ERROR_0002.*" ) );
  }

  @Test
  public void testParentPath() throws ServletException, IOException {
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( "../some_file.jar" );

    servlet.service( request, response );

    verify( response ).sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
    verify( servlet ).error( matches( ".*ERROR_0001.*" ) );
  }

  @Test
  public void testFileNotExist() throws ServletException, IOException {
    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( "not_exist.xsl" );

    servlet.service( request, response );

    verify( response ).sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
    verify( servlet ).error( matches( ".*ERROR_0003.*" ) );
  }

  @Test
  public void testRepositoryFile() throws ServletException, IOException {
    final String repoFileName = "repo_file.jpg";
    final int repoFileLength = 100;

    final RepositoryFile repositoryFile = mock( RepositoryFile.class );

    final InputStream inputStream = mock( InputStream.class );
    when( inputStream.read( any( byte[].class ) ) ).thenReturn( repoFileLength, -1 );

    final SimpleRepositoryFileData repositoryFileData = mock( SimpleRepositoryFileData.class );
    when( repositoryFileData.getStream() ).thenReturn( inputStream );

    final IUnifiedRepository repo = mock( IUnifiedRepository.class );
    when( repo.getFile( eq( repoFileName ) ) ).thenReturn( repositoryFile );
    when( repo.getDataForRead( any( Serializable.class ), eq( SimpleRepositoryFileData.class ) ) )
      .thenReturn( repositoryFileData );
    mp.defineInstance( IUnifiedRepository.class, repo );

    when( request.getParameter( RESOURCE_PARAM ) ).thenReturn( repoFileName );

    final ServletOutputStream outputStream = mock( ServletOutputStream.class );
    when( response.getOutputStream() ).thenReturn( outputStream );

    servlet.service( request, response );

    verify( response ).setContentType( eq( TEST_MIME_TYPE ) );
    verify( response ).setHeader( eq( CONTENT_DISPOSITION_HEADER ), endsWith( repoFileName ) );
    verify( response ).setContentLength( repoFileLength );
  }
}
