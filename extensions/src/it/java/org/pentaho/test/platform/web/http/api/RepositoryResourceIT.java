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
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
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
import org.pentaho.platform.api.repository2.unified.webservices.ExecutableFileTypeDtoWrapper;
import org.pentaho.platform.api.util.LogUtil;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
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

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
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

  private final DefaultUnifiedRepositoryBase repositoryBase;
  private IPluginManager pluginManager;
  private ITenant mainTenant_1;
  private String publicFolderPath;

  public RepositoryResourceIT() {
    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( nullable( String.class ) ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( nullable( String.class ) ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );

    repositoryBase = new DefaultUnifiedRepositoryBase() {
      @Override
      protected String getSolutionPath() {
        return TestResourceLocation.TEST_RESOURCES + "/PluginResourceTest";
      }
    };
  }

  @Override
  protected DeploymentContext configureDeployment() {
    ResourceConfig config = new ResourceConfig()
      .packages( "org.pentaho.platform.web.http.api.resources" )
      .register( PentahoRequestContextFilter.class );

    return ServletDeploymentContext.forServlet( new ServletContainer( config ) ).build();
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    System.setProperty( SYSTEM_PROPERTY, "MODE_GLOBAL" );

    LogUtil.setLevel( LogManager.getLogger( "org" ), Level.WARN );
    LogUtil.setLevel( LogManager.getLogger( "org.pentaho" ), Level.WARN );
    LogUtil.setLevel( LogManager.getLogger( RepositoryResource.class ), Level.DEBUG );
    // LogUtil.setLevel( LogManager.getLogger( RequestProxy.class ), Level.DEBUG );
    LogUtil.setLevel( LogManager.getLogger( "MIME_TYPE" ), Level.TRACE );

    DefaultUnifiedRepositoryBase.setUpClass();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    DefaultUnifiedRepositoryBase.tearDownClass();
  }

  @Before
  public void beforeTest() throws Exception {
    repositoryBase.setUp();
    repositoryBase.getMp().defineInstance( IPluginManager.class, pluginManager );
    repositoryBase.getMp().defineInstance( IPluginResourceLoader.class, new PluginResourceLoader() {
      protected PluginClassLoader getOverrideClassloader() {
        return new PluginClassLoader( new File( TestResourceLocation.TEST_RESOURCES + "/PluginResourceTest/system/test-plugin" ), this );
      }
    } );
    repositoryBase.getMp().define( IPluginProvider.class, TestPlugin.class, Scope.GLOBAL );
    pluginManager.reload();
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
    pluginManager = null;
  }

  @Test
  public void testDummy() {

  }

  @Test
  public void testGetFileText() {
    createTestFile( publicFolderPath + ":" + "file.txt", "abcdefg" );

    WebTarget webTarget = target();

    Response r1 =
        webTarget.path( "repos/:public:file.txt/content" ).request( TEXT_PLAIN ).get( Response.class );
    assertResponse( r1, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r1.readEntity( String.class ) );

    // check again but with no Accept header
    Response r2 = webTarget.path( "repos/:public:file.txt/content" ).request( TEXT_PLAIN ).get( Response.class );
    assertResponse( r2, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r2.readEntity( String.class ) );

    // check again but with */*
    Response r3 =
        webTarget.path( "repos/:public:file.txt/content" ).request( TEXT_PLAIN ).accept( MediaType.WILDCARD ).get(
            Response.class );
    assertResponse( r3, Status.OK, MediaType.TEXT_PLAIN );
    assertEquals( "abcdefg", r3.readEntity( String.class ) );
  }

  @Test
  public void a1_HappyPath() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget = target();

    Response response =
        webTarget.path( "repos/:public:test.xjunit/public/file.txt" ).request().get( Response.class );
    assertResponse( response, Response.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.readEntity( String.class ) );
  }

  @Test
  public void a2_HappyPath() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );
    createTestFile( publicFolderPath + ":" + "js/test.js", "js content" );
    createTestFile( publicFolderPath + ":" + "test.css", "css content" );

    WebTarget webTarget = target();

    // load the css
    Response response = webTarget.path( "repos/:public:test.xjunit/test.css" ).request().get( Response.class );
    assertResponse( response, Response.Status.OK, "text/css" );
    assertEquals( "contents of file incorrect/missing", "css content", response.readEntity( String.class ) );

    // load the js
    response = webTarget.path( "repos/:public:test.xjunit/js/test.js" ).request().get( Response.class );
    assertResponse( response, Response.Status.OK, "text/javascript" );
    assertEquals( "contents of file incorrect/missing", "js content", response.readEntity( String.class ) );
  }

  @Test
  public void a3_dotUrl() {
    final String text = "URL=http://google.com";
    createTestFile( publicFolderPath + ":" + "test.url", text );

    WebTarget webTarget= target();

    String response = webTarget.path( "repos/:public:test.url/content" ).request().get( String.class );
    assertEquals( "contents of file incorrect/missing", text, response );

    Response getResponse =
        webTarget.path( "repos/:public:test.url/generatedContent" ).request().get( Response.class );
    assertEquals( Response.Status.OK.getStatusCode(), getResponse.getStatus() );
  }

  @Test
  public void a3_HappyPath_GET() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget = target();

    // get the output of the .xjunit file (should invoke the content generator)
    String textResponse = webTarget.path( "repos/:public:test.xjunit/viewer" ).request().get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );
  }

  @Test
  public void a3_HappyPath_POST() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget = target();

    // get the output of the .junit file (should invoke the content generator)
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add( "testParam", "testParamValue" );

    Response response =
        webTarget.path( "repos/:public:test.xjunit/viewer" ).request( APPLICATION_FORM_URLENCODED ).post(
            Entity.entity( formData, APPLICATION_FORM_URLENCODED ) );
    assertResponse( response, Status.OK );
    assertEquals( "Content generator failed to provide correct output", "hello viewer content generator", response
        .readEntity( String.class ) );
  }

  @Test
  public void a3_HappyPath_GET_withCommand() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget = target();

    String expectedResponse = "hello this is service content generator servicing command dosomething";
    String textResponse =
        webTarget.path( "repos/:public:test.xjunit/testservice/dosomething" ).queryParam( "testParam",
            "testParamValue" ).request( TEXT_PLAIN ).get( String.class );
    assertEquals( "Content generator failed to provide correct output", expectedResponse, textResponse );
  }

  @Test
  public void a3_HappyPath_POST_withCommand() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget= target();

    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add( "testParam", "testParamValue" );

    Response response =
        webTarget.path( "repos/:public:test.xjunit/testservice/dosomething" ).request( APPLICATION_FORM_URLENCODED )
            .post( Entity.entity( formData, APPLICATION_FORM_URLENCODED ) );
    assertResponse( response, Status.OK );
    String expectedResponse = "hello this is service content generator servicing command dosomething";
    assertEquals( "Content generator failed to provide correct output", expectedResponse, response
        .readEntity( String.class ) );
  }

  @Test
  public void b1_HappyPath() {
    WebTarget webTarget = target();
    Response response = webTarget.path( "repos/xjunit/public/file.txt" ).request().get( Response.class );
    assertResponse( response, Response.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.readEntity( String.class ) );
  }

  @Test
  public void b1_PluginDNE() {
    WebTarget webTarget = target();
    Response response =
        webTarget.path( "repos/non-existent-plugin/private/private.txt" ).request().get( Response.class );
    assertResponse( response, Response.Status.NOT_FOUND );
  }

  @Test
  public void b1_FileDNE() {
    WebTarget webTarget = target();
    Response response = webTarget.path( "repos/xjunit/public/doesnotexist.txt" ).request().get( Response.class );
    assertResponse( response, Response.Status.NOT_FOUND );
  }

  @Test
  public void b3_HappyPath_GET() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget= target();

    // get the output of the .xjunit file (should invoke the content generator)
    String textResponse = webTarget.path( "repos/xjunit/viewer" ).request().get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );
  }

  @Test
  public void b3_HappyPath_GET_withMimeType() {
    createTestFile( publicFolderPath + ":" + "test.xjunit", "abcdefg" );

    WebTarget webTarget= target();

    // get the output of the .xjunit file (should invoke the content generator)
    Response response =
        webTarget.path( "repos/xjunit/report" ).request( "application/pdf" ).get( Response.class );

    assertResponse( response, Response.Status.OK, "application/pdf;charset=UTF-8" );
  }

  @Test
  public void c1_HappyPath() {
    WebTarget webTarget= target();
    Response response = webTarget.path( "repos/test-plugin/public/file.txt" ).request( TEXT_PLAIN ).get( Response.class );
    assertResponse( response, Response.Status.OK, TEXT_PLAIN );
    assertEquals( "contents of file incorrect/missing", "test text", response.readEntity( String.class ) );
  }

  @Test
  public void c3_HappyPath_GET_withCommand() {
    WebTarget webTarget = target();

    String expectedResponse = "hello this is service content generator servicing command dosomething";
    String textResponse =
        webTarget.path( "repos/test-plugin/testservice/dosomething" ).queryParam( "testParam", "testParamValue" ).request( TEXT_PLAIN)
            .get( String.class );
    assertEquals( "Content generator failed to provide correct output", expectedResponse, textResponse );
  }

  @Test
  public void c3_HappyPath_POST_withCommand() {
    WebTarget webTarget = target();

    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add( "testParam", "testParamValue" );

    Response response =
        webTarget.path( "repos/test-plugin/testservice/dosomething" ).request( APPLICATION_FORM_URLENCODED ).post(
            Entity.entity( formData, APPLICATION_FORM_URLENCODED ) );
    assertResponse( response, Status.OK );
    String expectedResponse = "hello this is service content generator servicing command dosomething";
    assertEquals( "Content generator failed to provide correct output", expectedResponse, response
        .readEntity( String.class ) );
  }

  @Test
  public void testExecutableTypes() {
    WebTarget webTarget= target();
    System.out.println( webTarget.getUri().getPath() );
    ExecutableFileTypeDtoWrapper executableTypes =
      webTarget.path( "repos/executableTypes" ).request( MediaType.APPLICATION_JSON ).get( ExecutableFileTypeDtoWrapper.class );
    assertEquals( 1, executableTypes.getExecutableFileTypeDtoes().size() );
    assertEquals( "xjunit", executableTypes.getExecutableFileTypeDtoes().get( 0 ).getExtension() );
  }

  public static class TestPlugin implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      List<IPlatformPlugin> plugins = new ArrayList<>();

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
    pluginManager = (IPluginManager) applicationContext.getBean( "IPluginManager" );
  }

  protected void createTestFile( String pathId, String text ) {
    WebTarget webTarget= target();
    Response response =
        webTarget.path( "repo/files/" + pathId ).request( TEXT_PLAIN ).put( Entity.text( text ) );
    assertResponse( response, Status.OK );
  }

}
