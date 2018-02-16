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

package org.pentaho.test.platform.web.http.api;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
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

import javax.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
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
  protected AppDescriptor configure() {
    ClientConfig config = new DefaultClientConfig();
    config.getClasses().add( MultiPartWriter.class );
    config.getFeatures().put( JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE );

    return new WebAppDescriptor.Builder( "org.pentaho.platform.web.http.api.resources" )
      .contextPath( "api" )
      .addFilter( PentahoRequestContextFilter.class, "pentahoRequestContextFilter" )
      .clientConfig( config )
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

    ClientResponse response = resource()
      .path( "repo/publish/file" )
      .type( MediaType.MULTIPART_FORM_DATA )
      .accept( TEXT_PLAIN )
      .post( ClientResponse.class, part );
    assertResponse( response, ClientResponse.Status.OK, MediaType.TEXT_PLAIN );

    ArgumentCaptor<IPlatformImportBundle> captor = ArgumentCaptor.forClass( IPlatformImportBundle.class );
    verify( importer ).importFile( captor.capture() );
    IPlatformImportBundle bundle = captor.getValue();

    assertEquals( path, bundle.getPath() );
    assertEquals( filename, bundle.getName() );
  }
}
