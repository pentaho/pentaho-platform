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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IServiceOperationAwareContentGenerator;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryResourceTest {
  private IUnifiedRepository repositoryMock;
  private IAuthorizationPolicy authorizationPolicyMock;
  private HttpServletRequest httpServletRequestMock;
  private HttpServletResponse httpServletResponseMock;
  private IPluginManager pluginManagerMock;
  private CsrfValidator csrfValidatorMock;
  private IContentGenerator contentGeneratorMock;
  private MockedStatic<PentahoSystem> pentahoSystemStaticMock;
  private RepositoryResource repositoryResource;

  @Before
  public void setup() throws ServletException, IOException {
    repositoryMock = mock( IUnifiedRepository.class );
    authorizationPolicyMock = mock( IAuthorizationPolicy.class );
    httpServletRequestMock = mock( HttpServletRequest.class );
    httpServletResponseMock = mock( HttpServletResponse.class );
    csrfValidatorMock = mock( CsrfValidator.class );
    pluginManagerMock = mock( IPluginManager.class );
    contentGeneratorMock = mock( IContentGenerator.class );

    repositoryResource = new RepositoryResource( repositoryMock, pluginManagerMock );
    repositoryResource.setHttpServletRequest( httpServletRequestMock );
    repositoryResource.setHttpServletResponse( httpServletResponseMock );
    repositoryResource.acceptableMediaTypes = Collections.emptyList();

    repositoryResource.setCsrfValidator( csrfValidatorMock );

    // Unfortunately, some of the tests use FileResource, which is very tied to PentahoSystem.
    pentahoSystemStaticMock = Mockito.mockStatic( PentahoSystem.class );
    pentahoSystemStaticMock.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( repositoryMock );
    pentahoSystemStaticMock.when( () -> PentahoSystem.get( IAuthorizationPolicy.class ) )
      .thenReturn( authorizationPolicyMock );
  }

  @After
  public void teardown() {
    if ( pentahoSystemStaticMock != null ) {
      pentahoSystemStaticMock.close();
      pentahoSystemStaticMock = null;
    }
  }

  @Test
  public void testDoExecuteDefaultNotFound() throws Exception {
    when( repositoryMock.getFile( "/home/admin/comments.wcdf" ) ).thenReturn( null );

    Response response = repositoryResource.doExecuteDefault( ":home:admin:comments.wcdf" );

    assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
  }

  // region get CsrfValidator
  @Test
  public void testSetCsrfValidatorRespectsGivenValue() {
    assertSame( csrfValidatorMock, repositoryResource.getCsrfValidator() );
  }
  // endregion

  // region doGet
  public abstract class AbstractCGExample {
    public final String contextId;
    public final String perspectiveId;
    public final String commandId;
    public final String resourceId;
    public final String pluginId;
    public final String operationName;
    public final String operationId;

    public AbstractCGExample( String contextId, String perspectiveId, String commandId, String operationName,
                              String pluginId ) {
      this.contextId = contextId;
      this.perspectiveId = perspectiveId;
      this.commandId = commandId;
      this.operationName = operationName;
      this.pluginId = pluginId;
      this.resourceId = this.perspectiveId + "/" + this.commandId;
      this.operationId = "SomeContentGenerator#" + this.operationName;
    }

    public void configureMocks() {
      when( pluginManagerMock.isPublic( pluginId, resourceId ) ).thenReturn( false );

      // Successful validation by default
      configureMocksWithCsrfValidationRequestResult( httpServletRequestMock );

      if ( !operationName.equals( commandId ) ) {
        contentGeneratorMock = mock( IServiceOperationAwareContentGenerator.class );
        when( ( (IServiceOperationAwareContentGenerator) contentGeneratorMock ).getServiceOperationName() )
          .thenReturn( operationName );

        when( httpServletRequestMock.getHeaderNames() ).thenReturn( Collections.emptyEnumeration() );
      }
    }

    public void configureMocksWithCsrfValidationRequestResult( HttpServletRequest httpServletRequest ) {
      // Successful validation by default
      try {
        when( csrfValidatorMock.validateRequestOfOperation(
          any( HttpServletRequest.class ),
          any( Method.class ),
          eq( operationName ) ) )
          .thenReturn( httpServletRequest );
      } catch ( IOException | ServletException e ) {
        // Never happens.
        throw new RuntimeException( e );
      }
    }

    public void configureMocksWithCsrfValidationError( @NonNull Throwable error ) {
      try {
        when( csrfValidatorMock.validateRequestOfOperation(
          any( HttpServletRequest.class ),
          any( Method.class ),
          eq( operationName ) ) )
          .thenThrow( error );
      } catch ( IOException | ServletException e ) {
        // Never happens.
        throw new RuntimeException( e );
      }
    }

    public void testDoGetWhenCSRFValidationDisabledThenRespondsWithOk()
      throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

      configureMocks();

      repositoryResource.setCsrfValidator( null );

      // ---

      Response response = repositoryResource.doGet( contextId, resourceId );

      // ---

      assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }

    public void testDoGetWhenCSRFValidationSucceedsThenRespondsWithOk()
      throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

      configureMocks();

      // ---

      Response response = repositoryResource.doGet( contextId, resourceId );

      // ---

      assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

      verify( csrfValidatorMock, times( 1 ) )
        .validateRequestOfOperation(
          any( HttpServletRequest.class ),
          any( Method.class ),
          eq( operationName ) );
    }

    public void testDoGetWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned()
      throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

      configureMocks();

      HttpServletRequest otherHttpServletRequestMock = mock( HttpServletRequest.class );
      configureMocksWithCsrfValidationRequestResult( otherHttpServletRequestMock );

      // ---

      repositoryResource.doGet( contextId, resourceId );

      // ---

      assertSame( otherHttpServletRequestMock, repositoryResource.httpServletRequest );

      verify( csrfValidatorMock, times( 1 ) )
        .validateRequestOfOperation(
          any( HttpServletRequest.class ),
          any( Method.class ),
          eq( operationName ) );
    }

    public void testDoGetWhenContentGeneratorIsServiceMappingAwareThenObtainsAndUsesTheCustomOperationName()
      throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

      configureMocks();

      String customOperationName = "customOperationName";
      contentGeneratorMock = mock( IServiceOperationAwareContentGenerator.class );
      when( ( (IServiceOperationAwareContentGenerator) contentGeneratorMock ).getServiceOperationName() )
        .thenReturn( customOperationName );

      when( httpServletRequestMock.getHeaderNames() ).thenReturn( Collections.emptyEnumeration() );

      HttpServletRequest otherHttpServletRequestMock = mock( HttpServletRequest.class );
      configureMocksWithCsrfValidationRequestResult( otherHttpServletRequestMock );

      // ---

      repositoryResource.doGet( contextId, resourceId );

      // ---

      assertSame( otherHttpServletRequestMock, repositoryResource.httpServletRequest );

      verify( csrfValidatorMock, times( 1 ) )
        .validateRequestOfOperation(
          any( HttpServletRequest.class ),
          any( Method.class ),
          eq( customOperationName ) );
    }

    public void testDoGetWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException()
      throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

      AccessDeniedException validationException = new AccessDeniedException( "Access Denied Test" );

      configureMocks();
      configureMocksWithCsrfValidationError( validationException );

      // ---

      // It is the JAX-RS ExceptionMapper which converts the WebApplicationException to
      // a Response with the corresponding status code.
      try {
        repositoryResource.doGet( contextId, resourceId );
        fail( "Should have thrown WebApplicationException" );
      } catch ( WebApplicationException ex ) {
        assertSame( validationException, ex.getCause() );
      }
    }

    public void testDoGetWhenCSRFValidationFailsWithServletExceptionThenThrowsBack()
      throws PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

      ServletException validationException = new ServletException( "ServletException Test" );

      configureMocks();
      configureMocksWithCsrfValidationError( validationException );

      // ---

      try {
        repositoryResource.doGet( contextId, resourceId );
        fail( "Should have thrown ServletException" );
      } catch ( ServletException ex ) {
        assertSame( validationException, ex );
      }
    }

    public void testDoGetWhenCSRFValidationFailsWithIOExceptionThenThrowsBack()
      throws ServletException, PluginBeanException, ObjectFactoryException, URISyntaxException {

      IOException validationException = new IOException( "IOException Test" );

      configureMocks();
      configureMocksWithCsrfValidationError( validationException );

      // ---

      try {
        repositoryResource.doGet( contextId, resourceId );
        fail( "Should have thrown IOException" );
      } catch ( IOException ex ) {
        assertSame( validationException, ex );
      }
    }
  }

  // region Content Type Content Generator
  public class ContentTypeCGExample extends AbstractCGExample {
    public ContentTypeCGExample() {
      this( "modeling/save" );
    }

    public ContentTypeCGExample( String customOperationName ) {
      super( "xanalyzer", "service", "modeling/save", customOperationName, "analyzer" );
    }

    @Override
    public void configureMocks() {
      super.configureMocks();

      when( pluginManagerMock.getPluginIdForType( contextId ) ).thenReturn( pluginId );
      when( pluginManagerMock.getContentGenerator( contextId, perspectiveId ) ).thenReturn( contentGeneratorMock );
    }
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenCSRFValidationDisabledThenRespondsWithOk()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new ContentTypeCGExample().testDoGetWhenCSRFValidationDisabledThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenCSRFValidationSucceedsThenRespondsWithOk()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new ContentTypeCGExample().testDoGetWhenCSRFValidationSucceedsThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenContentGeneratorIsServiceMappingAwareThenObtainsAndUsesTheCustomOperationName()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new ContentTypeCGExample( "customOperationName" )
      .testDoGetWhenCSRFValidationSucceedsThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new ContentTypeCGExample().testDoGetWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned();
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new ContentTypeCGExample().testDoGetWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException();
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenCSRFValidationFailsWithServletExceptionThenThrowsBack()
    throws PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new ContentTypeCGExample().testDoGetWhenCSRFValidationFailsWithServletExceptionThenThrowsBack();
  }

  @Test
  public void testDoGetWithContentTypeCGAndWhenCSRFValidationFailsWithIOExceptionThenThrowsBack()
    throws ServletException, PluginBeanException, ObjectFactoryException, URISyntaxException {

    new ContentTypeCGExample().testDoGetWhenCSRFValidationFailsWithIOExceptionThenThrowsBack();
  }
  // endregion

  // region Repository File Content Generator
  public class RepositoryFileCGExample extends AbstractCGExample {
    public final String fileName = "report.xanalyzer";
    public final String filePath = "/home/report.xanalyzer";
    public final String contentType = "xanalyzer";

    public RepositoryFileCGExample() {
      super( ":home:report.xanalyzer", "service", "modeling/save", "modeling/save", "analyzer" );
    }

    @Override
    public void configureMocks() {
      super.configureMocks();

      when( pluginManagerMock.getPluginIdForType( contentType ) ).thenReturn( pluginId );
      when( pluginManagerMock.getContentGenerator( contentType, perspectiveId ) ).thenReturn( contentGeneratorMock );

      RepositoryFile fileMock = mock( RepositoryFile.class );
      when( fileMock.getName() ).thenReturn( fileName );
      when( fileMock.getPath() ).thenReturn( filePath );

      when( repositoryMock.getFile( filePath ) ).thenReturn( fileMock );
    }
  }

  @Test
  public void testDoGetWithRepositoryFileCGAndWhenCSRFValidationDisabledThenRespondsWithOk()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new RepositoryFileCGExample().testDoGetWhenCSRFValidationDisabledThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithRepositoryFileCGAndWhenCSRFValidationSucceedsThenRespondsWithOk()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new RepositoryFileCGExample().testDoGetWhenCSRFValidationSucceedsThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithRepositoryFileCGAndWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new RepositoryFileCGExample().testDoGetWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned();
  }

  @Test
  public void testDoGetWithRepositoryFileCGAndWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new RepositoryFileCGExample().testDoGetWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException();
  }

  @Test
  public void testDoGetWithRepositoryFileCGAndWhenCSRFValidationFailsWithServletExceptionThenThrowsBack()
    throws PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new RepositoryFileCGExample().testDoGetWhenCSRFValidationFailsWithServletExceptionThenThrowsBack();
  }

  @Test
  public void testDoGetWithRepositoryFileCGAndWhenCSRFValidationFailsWithIOExceptionThenThrowsBack()
    throws PluginBeanException, ObjectFactoryException, ServletException, URISyntaxException {

    new RepositoryFileCGExample().testDoGetWhenCSRFValidationFailsWithIOExceptionThenThrowsBack();
  }
  // endregion

  // region Direct Content Generator
  public class DirectCGExample extends AbstractCGExample {
    public DirectCGExample() {
      super( "analyzer", "service", "modeling/save", "modeling/save", "analyzer" );
    }

    @Override
    public void configureMocks() {
      super.configureMocks();

      // contextId == pluginId

      when( pluginManagerMock.getPluginIdForType( contextId ) ).thenReturn( null );
      when( pluginManagerMock.getRegisteredPlugins() ).thenReturn( Collections.singletonList( contextId ) );
      when( pluginManagerMock.getContentGenerator( null, perspectiveId ) ).thenReturn( contentGeneratorMock );
    }
  }

  @Test
  public void testDoGetWithDirectCGAndWhenCSRFValidationDisabledThenRespondsWithOk()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new DirectCGExample().testDoGetWhenCSRFValidationDisabledThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithDirectCGAndWhenCSRFValidationSucceedsThenRespondsWithOk()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new DirectCGExample().testDoGetWhenCSRFValidationSucceedsThenRespondsWithOk();
  }

  @Test
  public void testDoGetWithDirectCGAndWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new DirectCGExample().testDoGetWhenCSRFValidationSucceedsThenReplacesServletRequestWithThatReturned();
  }

  @Test
  public void testDoGetWithDirectCGAndWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException()
    throws ServletException, PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new DirectCGExample().testDoGetWhenCSRFValidationFailsWithAccessDeniedThenThrowsWebApplicationException();
  }

  @Test
  public void testDoGetWithDirectCGAndWhenCSRFValidationFailsWithServletExceptionThenThrowsBack()
    throws PluginBeanException, ObjectFactoryException, IOException, URISyntaxException {

    new DirectCGExample().testDoGetWhenCSRFValidationFailsWithServletExceptionThenThrowsBack();
  }

  @Test
  public void testDoGetWithDirectCGAndWhenCSRFValidationFailsWithIOExceptionThenThrowsBack()
    throws PluginBeanException, ObjectFactoryException, ServletException, URISyntaxException {

    new DirectCGExample().testDoGetWhenCSRFValidationFailsWithIOExceptionThenThrowsBack();
  }
  // endregion
  // endregion
}
