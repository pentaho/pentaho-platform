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
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.webservices.ExecutableFileTypeDto;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.web.http.api.resources.FileResourceContentGenerator;
import org.pentaho.platform.web.http.api.resources.RepositoryResource;
import org.pentaho.platform.web.http.filters.PentahoRequestContextFilter;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.test.platform.web.http.api.JerseyTestUtil.assertResponse;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
@SuppressWarnings( "nls" )
public class RepositoryResourceIT extends JerseyTest implements ApplicationContextAware {

  public static final String MAIN_TENANT_1 = "maintenant1";
  public static final String SYSTEM_PROPERTY = "spring.security.strategy";

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder(
      "org.pentaho.platform.web.http.api.resources" ).contextPath( "api" ).addFilter(
      PentahoRequestContextFilter.class, "pentahoRequestContextFilter" ).build();

  private DefaultUnifiedRepositoryBase repositoryBase;
  private ITenant mainTenant_1;
  private String publicFolderPath;

  public RepositoryResourceIT() throws Exception {
    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );

    repositoryBase = new DefaultUnifiedRepositoryBase() {
      @Override
      protected String getSolutionPath() {
        return TestResourceLocation.TEST_RESOURCES + "/PluginResourceTest";
      }
    };
  }

  @Override
  protected AppDescriptor configure() {
    return webAppDescriptor;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );
    Logger.getLogger( "org" ).setLevel( Level.WARN );
    Logger.getLogger( "org.pentaho" ).setLevel( Level.WARN );
    Logger.getLogger( RepositoryResource.class ).setLevel( Level.DEBUG );
    // Logger.getLogger(RequestProxy.class).setLevel(Level.DEBUG);
    Logger.getLogger( "MIME_TYPE" ).setLevel( Level.TRACE );

    DefaultUnifiedRepositoryBase.setUpClass();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    DefaultUnifiedRepositoryBase.tearDownClass();
  }

  @Before
  public void beforeTest() throws Exception {
    repositoryBase.setUp();
    repositoryBase.getMp().define( IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
    repositoryBase.getMp().defineInstance( IPluginResourceLoader.class, new PluginResourceLoader() {
      protected PluginClassLoader getOverrideClassloader() {
        return new PluginClassLoader( new File( TestResourceLocation.TEST_RESOURCES + "/PluginResourceTest/system/test-plugin" ), this );
      }
    } );
    repositoryBase.getMp().define( IPluginProvider.class, TestPlugin.class, Scope.GLOBAL );
    PentahoSystem.get( IPluginManager.class ).reload();
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );

    repositoryBase.loginAsRepositoryAdmin();

    mainTenant_1 = repositoryBase.createTenant( repositoryBase.getSystemTenant(), MAIN_TENANT_1 );
    repositoryBase.createUser( mainTenant_1, "admin", "password", new String[] { repositoryBase.getTenantAdminRoleName() } );

    repositoryBase.login( "admin", mainTenant_1, new String[] { repositoryBase.getTenantAdminRoleName() } );

    publicFolderPath = ClientRepositoryPaths.getPublicFolderPath().replaceAll( "/", ":" );
  }

  @After
  public void afterTest() throws Exception {
    repositoryBase.cleanupUserAndRoles( mainTenant_1 );
    repositoryBase.tearDown();
  }

  @Test
  public void testDummy() {

  }

  @Test
  public void testGetFileText() throws Exception {
    createTestFile( publicFolderPath + ":" + "file.txt", "abcdefg" );

    WebResource webResource = resource();

    ClientResponse r1 =
        webResource.path( "repos/:public:file.txt/content" ).accept( TEXT_PLAIN ).get( ClientResponse.class );
    assertResponse( r1, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r1.getEntity( String.class ) );

    // check again but with no Accept header
    ClientResponse r2 = webResource.path( "repos/:public:file.txt/content" ).get( ClientResponse.class );
    assertResponse( r2, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r2.getEntity( String.class ) );

    // check again but with */*
    ClientResponse r3 =
        webResource.path( "repos/:public:file.txt/content" ).accept( TEXT_PLAIN ).accept( MediaType.WILDCARD ).get(
            ClientResponse.class );
    assertResponse( r3, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r3.getEntity( String.class ) );
  }

  @Test
  public void a1_HappyPath() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    ClientResponse response =
        webResource.path( "repos/:public:test.xjunit/public/file.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.getEntity( String.class ) );
  }

  @Test
  public void a2_HappyPath() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );
    createTestFile( publicFolderPath + ":" + "js/test.js", "js content" );
    createTestFile( publicFolderPath + ":" + "test.css", "css content" );

    WebResource webResource = resource();

    // load the css
    ClientResponse response = webResource.path( "repos/:public:test.xjunit/test.css" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, "text/css" );
    assertEquals( "contents of file incorrect/missing", "css content", response.getEntity( String.class ) );

    // load the js
    response = webResource.path( "repos/:public:test.xjunit/js/test.js" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, "text/javascript" );
    assertEquals( "contents of file incorrect/missing", "js content", response.getEntity( String.class ) );
  }

  @Test
  public void a3_dotUrl() {
    final String text = "URL=http://google.com";
    createTestFile( publicFolderPath + ":" + "test.url", text );

    WebResource webResource = resource();

    String response = webResource.path( "repos/:public:test.url/content" ).get( String.class );
    assertEquals( "contents of file incorrect/missing", text, response );

    ClientResponse getResponse =
        webResource.path( "repos/:public:test.url/generatedContent" ).get( ClientResponse.class );
    assertEquals( ClientResponse.Status.OK, getResponse.getClientResponseStatus() );
  }

  @Test
  public void a3_HappyPath_GET() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    // get the output of the .xjunit file (should invoke the content generator)
    String textResponse = webResource.path( "repos/:public:test.xjunit/viewer" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );
  }

  @Test
  public void a3_HappyPath_POST() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    // get the output of the .junit file (should invoke the content generator)
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add( "testParam", "testParamValue" );

    ClientResponse response =
        webResource.path( "repos/:public:test.xjunit/viewer" ).type( APPLICATION_FORM_URLENCODED ).post(
            ClientResponse.class, formData );
    assertResponse( response, Status.OK );
    assertEquals( "Content generator failed to provide correct output", "hello viewer content generator", response
        .getEntity( String.class ) );
  }

  @Test
  public void a3_HappyPath_GET_withCommand() throws PlatformInitializationException {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    String expectedResponse = "hello this is service content generator servicing command dosomething";
    String textResponse =
        webResource.path( "repos/:public:test.xjunit/testservice/dosomething" ).queryParam( "testParam",
            "testParamValue" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output", expectedResponse, textResponse );
  }

  @Test
  public void a3_HappyPath_POST_withCommand() throws PlatformInitializationException {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add( "testParam", "testParamValue" );

    ClientResponse response =
        webResource.path( "repos/:public:test.xjunit/testservice/dosomething" ).type( APPLICATION_FORM_URLENCODED )
            .post( ClientResponse.class, formData );
    assertResponse( response, Status.OK );
    String expectedResponse = "hello this is service content generator servicing command dosomething";
    assertEquals( "Content generator failed to provide correct output", expectedResponse, response
        .getEntity( String.class ) );
  }

  @Test
  public void b1_HappyPath() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/xjunit/public/file.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.getEntity( String.class ) );
  }

  @Test
  public void b1_PluginDNE() {
    WebResource webResource = resource();
    ClientResponse response =
        webResource.path( "repos/non-existent-plugin/private/private.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.NOT_FOUND );
  }

  @Test
  public void b1_FileDNE() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/xjunit/public/doesnotexist.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.NOT_FOUND );
  }

  @Test
  public void b3_HappyPath_GET() throws PlatformInitializationException {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    // get the output of the .xjunit file (should invoke the content generator)
    String textResponse = webResource.path( "repos/xjunit/viewer" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );
  }

  @Test
  public void b3_HappyPath_GET_withMimeType() throws PlatformInitializationException {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebResource webResource = resource();

    // get the output of the .xjunit file (should invoke the content generator)
    ClientResponse response =
        webResource.path( "repos/xjunit/report" ).accept( "application/pdf" ).get( ClientResponse.class );

    assertResponse( response, ClientResponse.Status.OK, "application/pdf;charset=UTF-8" );
  }

  @Test
  public void c1_HappyPath() {
    WebResource webResource = resource();
    ClientResponse response = webResource.path( "repos/test-plugin/public/file.txt" ).get( ClientResponse.class );
    assertResponse( response, ClientResponse.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.getEntity( String.class ) );
  }

  @Test
  public void c3_HappyPath_GET_withCommand() throws PlatformInitializationException {
    WebResource webResource = resource();

    String expectedResponse = "hello this is service content generator servicing command dosomething";
    String textResponse =
        webResource.path( "repos/test-plugin/testservice/dosomething" ).queryParam( "testParam", "testParamValue" )
            .get( String.class );
    assertEquals( "Content generator failed to provide correct output", expectedResponse, textResponse );
  }

  @Test
  public void c3_HappyPath_POST_withCommand() {
    WebResource webResource = resource();

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.add( "testParam", "testParamValue" );

    ClientResponse response =
        webResource.path( "repos/test-plugin/testservice/dosomething" ).type( APPLICATION_FORM_URLENCODED ).post(
            ClientResponse.class, formData );
    assertResponse( response, Status.OK );
    String expectedResponse = "hello this is service content generator servicing command dosomething";
    assertEquals( "Content generator failed to provide correct output", expectedResponse, response
        .getEntity( String.class ) );
  }

  @Test
  public void testExecutableTypes() {
    WebResource webResource = resource();
    System.out.println( webResource.getURI().getPath() );
    List<ExecutableFileTypeDto> executableTypes =
        webResource.path( "repos/executableTypes" ).get( new GenericType<List<ExecutableFileTypeDto>>() {
        } );
    assertEquals( 1, executableTypes.size() );
    assertEquals( "xjunit", executableTypes.get( 0 ).getExtension() );
  }

  public static class TestPlugin implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

      PlatformPlugin p = new PlatformPlugin( new DefaultListableBeanFactory() );
      p.setId( "test-plugin" );

      p.addStaticResourcePath( "notused", "public" );

      BeanDefinition def =
          BeanDefinitionBuilder.rootBeanDefinition( JUnitContentGenerator.class.getName() ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "xjunit.viewer", def );

      def =
          BeanDefinitionBuilder.rootBeanDefinition( JUnitServiceContentGenerator.class.getName() ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "testservice", def );

      def = BeanDefinitionBuilder.rootBeanDefinition( ReportContentGenerator.class.getName() ).getBeanDefinition();
      p.getBeanFactory().registerBeanDefinition( "xjunit.report", def );

      ContentInfo contentInfo = new ContentInfo();
      contentInfo.setDescription( "JUnit file type" );
      contentInfo.setExtension( "xjunit" );
      contentInfo.setMimeType( "application/zip" );
      contentInfo.setTitle( "JUNIT" );
      p.addContentInfo( contentInfo );

      plugins.add( p );

      return plugins;
    }
  }

  @SuppressWarnings( "serial" )
  public static class JUnitContentGenerator extends BaseContentGenerator {
    @Override
    public void createContent() throws Exception {
      try {
        IContentItem responseContentItem =
            outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );
        // mime type setting will blow up since servlet api used by grizzly is too old
        try {
          responseContentItem.setMimeType( "text/plain" );
        } catch ( Throwable t ) {
          //ignored
        }
        OutputStream outputStream = responseContentItem.getOutputStream( null );
        IOUtils.write( "hello viewer content generator", outputStream );
        outputStream.close();
      } catch ( Throwable t ) {
        t.printStackTrace();
      }
    }

    @Override
    public Log getLogger() {
      return null;
    }
  }

  @SuppressWarnings( "serial" )
  public static class JUnitServiceContentGenerator extends BaseContentGenerator {
    @Override
    public void createContent() throws Exception {
      try {
        IContentItem responseContentItem =
            outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );
        // mime type setting will blow up since servlet api used by grizzly is too old
        try {
          responseContentItem.setMimeType( "text/plain" );
        } catch ( Throwable t ) {
          //ignored
        }
        OutputStream outputStream = responseContentItem.getOutputStream( null );
        IParameterProvider pathParams = parameterProviders.get( "path" );
        String command = pathParams.getStringParameter( "cmd", "" );

        Object testParamValue = parameterProviders.get( IParameterProvider.SCOPE_REQUEST ).getParameter( "testParam" );
        assertEquals( "testParam is missing from request", "testParamValue", testParamValue );

        IOUtils.write( "hello this is service content generator servicing command " + command, outputStream );
        outputStream.close();
      } catch ( Throwable t ) {
        t.printStackTrace();
      }
    }

    @Override
    public Log getLogger() {
      return null;
    }
  }

  @SuppressWarnings( "serial" )
  public static class ReportContentGenerator extends FileResourceContentGenerator {
    static Log logger = LogFactory.getLog( ReportContentGenerator.class );
    OutputStream out = null;

    @Override
    public Log getLogger() {
      return logger;
    }

    @Override
    public void setRepositoryFile( RepositoryFile repositoryFile ) {
    }

    @Override
    public String getMimeType( String streamPropertyName ) {
      return "application/pdf;";
    }

    @Override
    public void setOutputStream( OutputStream outputStream ) {
      out = outputStream;
    }

    @Override
    public void execute() throws Exception {
      IParameterProvider pathParams = parameterProviders.get( "path" );
      String command = pathParams.getStringParameter( "cmd", "" );

      IOUtils.write( "hello this is service content generator servicing command " + command, out );
      out.close();
    }
  }

  @Override
  public void setApplicationContext( final ApplicationContext applicationContext ) throws BeansException {
    repositoryBase.setApplicationContext( applicationContext );
  }

  protected void createTestFile( String pathId, String text ) {
    WebResource webResource = resource();
    ClientResponse response =
        webResource.path( "repo/files/" + pathId ).type( TEXT_PLAIN ).put( ClientResponse.class, text );
    assertResponse( response, Status.OK );
  }

}
