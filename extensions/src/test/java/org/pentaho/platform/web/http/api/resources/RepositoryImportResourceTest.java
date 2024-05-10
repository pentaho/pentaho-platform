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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.multipart.FormDataBodyPart;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importer.PentahoPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;

import com.sun.jersey.core.header.FormDataContentDisposition;

public class RepositoryImportResourceTest {
  private static final String REAL_USER = "testUser";
  private static final String IMPORT_DIR = "/home/" + REAL_USER;
  private IAuthorizationPolicy policy;
  private ITenantedPrincipleNameResolver resolver;
  private IPentahoObjectFactory pentahoObjectFactory;
  private NameBaseMimeResolver iPlatformMimeResolver;
  private IRepositoryImportLogger iRepositoryImportLogger;
  private IMondrianCatalogService catalogService;
  private SolutionImportHandler handler;
  private PentahoPlatformImporter importer;
  @Before
  public void setUp() throws ObjectFactoryException, PlatformImportException, DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {
    PentahoSystem.init();
    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( "sampleSession" ).when( session ).getName();
    PentahoSessionHolder.setSession( session );
    handler = mock( SolutionImportHandler.class );
    importer = mock( PentahoPlatformImporter.class );
    policy = mock( IAuthorizationPolicy.class );
    ITenant tenat = mock( ITenant.class );
    resolver = mock( ITenantedPrincipleNameResolver.class );
    doReturn( tenat ).when( resolver ).getTenant( nullable( String.class ) );
    doReturn( REAL_USER ).when( resolver ).getPrincipleName( nullable( String.class ) );
    policy = mock( IAuthorizationPolicy.class );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    iPlatformMimeResolver = mock( NameBaseMimeResolver.class );
    iRepositoryImportLogger = mock( IRepositoryImportLogger.class );
    catalogService = mock( MondrianCatalogHelper.class );
    doReturn( "xml" ).when( iPlatformMimeResolver ).resolveMimeForFileName( "" );
    doReturn( iRepositoryImportLogger ).when( importer ).getRepositoryImportLogger();
    //for calling importFile in RepositoryImportResource
    doAnswer(
      (Answer<Void>) invocation -> {
        handler.importFile( any( IPlatformImportBundle.class ) );
        return null;
      } ).when( importer ).importFile( any( IPlatformImportBundle.class ) );
    //for calling importFile in PentahoPlatformImporter
    doAnswer(
      (Answer<Void>) invocation -> {
        handler.getImportSession();
        return null;
      } ).when( handler ).importFile( any( IPlatformImportBundle.class ) );
    //for calling getImportSession in SolutionImportHandler
    doAnswer(
      (Answer<Void>) invocation -> {
        ImportSession importsession = ImportSession.getSession();
        importsession.setManifest( mock( ExportManifest.class ) );
        return null;
      } ).when( handler ).getImportSession();

    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), nullable( String.class ), any( IPentahoSession.class ) ) ).thenAnswer(
      invocation -> {
        if ( invocation.getArguments()[0].equals( IAuthorizationPolicy.class ) ) {
          return policy;
        }
        if ( invocation.getArguments()[0].equals( ITenantedPrincipleNameResolver.class ) ) {
          return resolver;
        }
        if ( invocation.getArguments()[0].equals( IMondrianCatalogService.class ) ) {
          return catalogService;
        }
        return null;
      } );

    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
    PentahoSystem.registerObject( iPlatformMimeResolver );
    PentahoSystem.registerObject( iRepositoryImportLogger );
    PentahoSystem.registerObject( catalogService );
    PentahoSystem.registerObject( handler );
    PentahoSystem.registerObject( importer );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.shutdown();
  }

  @Test
  public void doPostImport() {
    RepositoryImportResource importResource = new RepositoryImportResource();
    InputStream mockInputStream = mock( InputStream.class );
    FormDataContentDisposition formDataContentDisposition =  mock( FormDataContentDisposition.class );
    when( policy.isAllowed( nullable( String.class ) ) ).thenAnswer( (Answer<Boolean>) invocation -> true );
    importResource.doPostImport( IMPORT_DIR, mockInputStream, "true", "true", "true", "true", "UTF-8", "WARN", formDataContentDisposition, "" );
    Assert.assertNull( ImportSession.getSession().getManifest()  );
  }

  @Test
  public void doPostImportMultipleFiles() {
    RepositoryImportResource importResource = new RepositoryImportResource();
    List<FormDataBodyPart> fileParts = new ArrayList<>();
    FormDataBodyPart mockInputStream1 = mock( FormDataBodyPart.class );
    FormDataBodyPart mockInputStream2 = mock( FormDataBodyPart.class );
    fileParts.add( mockInputStream1 );
    fileParts.add( mockInputStream2 );
    FormDataContentDisposition formDataContentDisposition =  mock( FormDataContentDisposition.class );
    when( policy.isAllowed( nullable( String.class ) ) ).thenAnswer( (Answer<Boolean>) invocation -> true );
    importResource.doPostImport( IMPORT_DIR, fileParts, "true", "true", "true", "true", "UTF-8", "WARN", formDataContentDisposition, "" );
    Assert.assertNull( ImportSession.getSession().getManifest()  );
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }
  private static class AnyClassMatcher implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Class<?> arg ) {
      return true;
    }
  }
}
