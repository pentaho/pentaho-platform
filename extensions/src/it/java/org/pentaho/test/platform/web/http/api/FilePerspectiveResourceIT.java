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

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.util.LogUtil;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.plugin.services.pluginmgr.DefaultPluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.mt.RepositoryTenantManager;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleAuthorizationPolicy;
import org.pentaho.platform.web.http.filters.PentahoRequestContextFilter;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.utils.TestResourceLocation;
import org.tuckey.web.filters.urlrewrite.RequestProxy;

import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.test.JerseyTest;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@SuppressWarnings ( "nls" )
public class FilePerspectiveResourceIT extends JerseyTest {

  private static MicroPlatform mp = new MicroPlatform( TestResourceLocation.TEST_RESOURCES + "/FileOutputResourceTest/" );

  private static ResourceConfig config = new ResourceConfig().packages( "org.pentaho.platform.web.http.api.resources" );
  private static ServletDeploymentContext servletDeploymentContext = ServletDeploymentContext.forServlet( new ServletContainer( config ) )
     .addFilter( PentahoRequestContextFilter.class, "pentahoRequestContextFilter" )
     .contextPath( "api" )
     .build();

  public FilePerspectiveResourceIT() throws Exception {
    mp.setFullyQualifiedServerUrl( getBaseUri() + servletDeploymentContext.getContextPath() + "/" );
    mp.define( IPluginManager.class, DefaultPluginManager.class, Scope.GLOBAL );
    mp.define( IPluginResourceLoader.class, PluginResourceLoader.class, Scope.GLOBAL );
    mp.define( IRoleAuthorizationPolicyRoleBindingDao.class, RoleAuthorizationPolicy.class, Scope.GLOBAL );
    mp.define( ITenantManager.class, RepositoryTenantManager.class, Scope.GLOBAL );
    mp.defineInstance( "singleTenantAdminAuthorityName", new String( "Administrator" ) );

  }

  @Override
  protected DeploymentContext configureDeployment() {
    return servletDeploymentContext;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    Configurator.initialize(new DefaultConfiguration());
    Configurator.setRootLevel(Level.INFO);
    LogUtil.setLevel(LogManager.getLogger(), Level.DEBUG);
  }

  @AfterClass
  public static void afterClass() {
  }

  @Before
  public void beforeTest() throws PlatformInitializationException {
  }

  @After
  public void afterTest() {
  }

  protected void createTestFile( String path, String text ) {
    WebTarget webTarget = target();
    Response postResponse =
        webTarget.path( path ).request( MediaType.TEXT_PLAIN ).put( Entity.entity( text, MediaType.TEXT_PLAIN ) );
    assertEquals( Response.Status.OK, postResponse.getStatus() );
  }

  @Test
  public void testDummy() {

  }


  //This is testing Rest calls and not the underlying functionality of the classes
  /*@Test
  public void testRenderThroughContentGenerator() throws PlatformInitializationException {
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    final String publicFolderId = "123";
    final String fileName = "test.junit";
    doReturn( new RepositoryFile.Builder( publicFolderId, "public" ).folder( true ).build() ).when( repo ).getFile(
        ClientRepositoryPaths.getPublicFolderPath() );
    RepositoryFile f = new RepositoryFile.Builder( fileName ).build();
    final String path = ClientRepositoryPaths.getPublicFolderPath() + RepositoryFile.SEPARATOR + fileName;
    RepositoryFile fWithId = new RepositoryFile.Builder( f ).id( "456" ).path( path ).build();
    // return null when the file does not exist and then non-null after file creation
    when( repo.getFile( path ) ).thenReturn( null ).thenReturn( fWithId );
    mp.defineInstance( IUnifiedRepository.class, repo );

    mp.define( IPluginProvider.class, JUnitContentGeneratorPluginProvider.class );
    PentahoSystem.get( IPluginManager.class ).reload();

    WebResource webResource = resource();

    // write a .junit file
    final String text = "sometext";
    createTestFile( "repo/files/public:test.junit", text );

    // get the output of the .junit file (should invoke the content generator)
    String textResponse = webResource.path( "repos/:public:test.junit/myperspective" ).get( String.class );
    assertEquals( "Content generator failed to provide correct output",
      "hello viewer content generator", textResponse );

    verify( repo ).createFile( eq( publicFolderId ),
        argThat( isLikeFile( new RepositoryFile.Builder( fileName ).build() ) ),
        argThat( hasData( text.getBytes(), "application/octet-stream" ) ), anyString() );
  }*/

  public static class JUnitContentGeneratorPluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setId( "JUnitContentGeneratorPluginProvider" );

      ContentGeneratorInfo cg = new ContentGeneratorInfo();
      cg.setDescription( "test plugin description" );
      cg.setId( "junit.myperspective" );
      cg.setType( "junit" );
      cg.setTitle( "JUnit CG" );
      cg.setUrl( "/bogus" );
      cg.setClassname( JUnitContentGenerator.class.getName() );
      p.addContentGenerator( cg );

      return Arrays.asList( (IPlatformPlugin) p );
    }
  }

  @SuppressWarnings ( "serial" )
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
          //ignore
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
}
