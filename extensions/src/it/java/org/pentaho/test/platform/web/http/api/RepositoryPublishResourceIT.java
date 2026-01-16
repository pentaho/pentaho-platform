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


package org.pentaho.test.platform.web.http.api;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importexport.TestAuthorizationPolicy;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.web.http.filters.PentahoRequestContextFilter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import jakarta.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
public class RepositoryPublishResourceIT extends JerseyTest implements ApplicationContextAware {

  private final DefaultUnifiedRepositoryBase testBase;

  public RepositoryPublishResourceIT() throws TestContainerException {
    testBase = new DefaultUnifiedRepositoryBase();
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    testBase.setApplicationContext( applicationContext );
  }

  @BeforeClass
  public static void init() throws Exception {
    DefaultUnifiedRepositoryBase.setUpClass();
  }

  @AfterClass
  public static void dispose() throws Exception {
    DefaultUnifiedRepositoryBase.tearDownClass();
  }

  @Override
  protected DeploymentContext configureDeployment() {
    ClientConfig config = new ClientConfig();
    config.register( MultiPartWriter.class );
    config.register( MultiPartFeature.class );

    return ServletDeploymentContext.forServlet( new ServletContainer( new ResourceConfig().packages( "org.pentaho.platform.web.http.api.resources" ) ) )
      .addFilter( PentahoRequestContextFilter.class, "pentahoRequestContextFilter" )
      .contextPath( "api" )
      .build();
  }

  private IPlatformImporter importer;

  @Override
  @Before
  public void setUp() throws Exception {
    importer = mock( IPlatformImporter.class );

    testBase.setUp();
    // allow all actions
    testBase.getMp().defineInstance( IAuthorizationPolicy.class, new TestAuthorizationPolicy() );
    // let's replace real Importer with a mock here
    testBase.getMp().defineInstance( IPlatformImporter.class, importer );

    super.setUp();

    // will be a repository admin not to bother setting sufficient rights
    testBase.loginAsRepositoryAdmin();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    testBase.tearDown();
  }

  @Test
  public void importsPath_Simple() throws Exception {
    testImportsSuccessfully( "/public", "my.txt" );
  }

  @Test
  public void importsPath_WithQuotes() throws Exception {
    testImportsSuccessfully( "/public", "my-\"quoted\".txt" );
  }

  private void testImportsSuccessfully( String path, String filename ) throws Exception {
    String full = path + '/' + filename;
    FormDataMultiPart part = new FormDataMultiPart();
    part.field( "importPath", URLEncoder.encode( full, "UTF-8"  ), MULTIPART_FORM_DATA_TYPE );
    part.field( "fileUpload", new ByteArrayInputStream( new byte[ 0 ] ), MULTIPART_FORM_DATA_TYPE );
    part.field( "overwriteFile", "true", MULTIPART_FORM_DATA_TYPE );
    part.getField( "fileUpload" )
      .setContentDisposition( FormDataContentDisposition.name( "fileUpload" )
        .fileName( URLEncoder.encode( filename, "UTF-8" ) )
        .build() );

    Response response = target()
      .path( "repo/publish/file" )
      .request( MediaType.MULTIPART_FORM_DATA )
      .accept( TEXT_PLAIN )
      .post( Entity.entity( part, MediaType.MULTIPART_FORM_DATA ) );
    assertResponse( response, Response.Status.OK, MediaType.TEXT_PLAIN );

    ArgumentCaptor<IPlatformImportBundle> captor = ArgumentCaptor.forClass( IPlatformImportBundle.class );
    verify( importer ).importFile( captor.capture() );
    IPlatformImportBundle bundle = captor.getValue();

    assertEquals( path, bundle.getPath() );
    assertEquals( filename, bundle.getName() );
  }
}
