/*!
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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.plugin.services.importer;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException.Reason;

public class MondrianImportHandlerTest {

  private static final String OTHER_PARAMETR = "parametr=\"value\"";

  private String parameters;

  private IPlatformImportBundle bundle;

  private IMondrianCatalogService mondrianImporter;

  private List<IMimeType> mimeTypes;

  @Before
  public void setUp() {
    mondrianImporter = mock( IMondrianCatalogService.class );
    bundle = mock( IPlatformImportBundle.class );
    parameters = MondrianImportHandler.PROVIDER + "=provider;" + MondrianImportHandler.DATA_SOURCE + "=dataSource;" + OTHER_PARAMETR;
    mimeTypes = Arrays.asList( mock( IMimeType.class ) );

    when( bundle.getProperty( eq( MondrianImportHandler.ENABLE_XMLA ) ) ).thenReturn( "true" );
    when( bundle.getProperty( eq( MondrianImportHandler.PARAMETERS ) ) ).thenReturn( parameters );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_fail() {
    List<IMimeType> mimeTypes = Arrays.asList( mock( IMimeType.class ) );
    new MondrianImportHandler( mimeTypes, null );
  }

  @Test
  public void testGetMymeTypes() {
    List<IMimeType> mimeTypes = Arrays.asList( mock( IMimeType.class ) );
    MondrianImportHandler handler = new MondrianImportHandler( mimeTypes, mondrianImporter );
    assertEquals( mimeTypes, handler.getMimeTypes() );
  }

  @Test( expected = PlatformImportException.class )
  public void testImportFile_nullDomain() throws Exception {
    when( bundle.getProperty( eq( MondrianImportHandler.DOMAIN_ID ) ) ).thenReturn( null );
    testImportFileBase();
  }

  @Test
  public void testImportFile() throws Exception {
    when( bundle.getProperty( eq( MondrianImportHandler.DOMAIN_ID ) ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    testImportFileBase();
  }

  @Test
  public void testImportFile_DoNotApplyAclSettings() throws Exception {
    when( bundle.getProperty( eq( MondrianImportHandler.DOMAIN_ID ) ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    when( bundle.isApplyAclSettings() ).thenReturn( false );
    IAclAwareMondrianCatalogService aclImporter = mock( IAclAwareMondrianCatalogService.class );
    MondrianImportHandler handler = new MondrianImportHandler( mimeTypes, aclImporter );
    handler.importFile( bundle );
    ArgumentCaptor<RepositoryFileAcl> captor = ArgumentCaptor.forClass( RepositoryFileAcl.class );
    verify( aclImporter ).addCatalog( any( InputStream.class ), any( MondrianCatalog.class ), anyBoolean(), captor.capture(),
        any( IPentahoSession.class ) );
    assertNull( captor.getValue() );
  }

  @Test
  public void testImportFile_applyAclSettings() throws Exception {
    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );
    when( bundle.getProperty( eq( MondrianImportHandler.DOMAIN_ID ) ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    when( bundle.isApplyAclSettings() ).thenReturn( true );
    when( bundle.getAcl() ).thenReturn( acl );
    IAclAwareMondrianCatalogService aclImporter = mock( IAclAwareMondrianCatalogService.class );
    MondrianImportHandler handler = new MondrianImportHandler( mimeTypes, aclImporter );
    handler.importFile( bundle );
    ArgumentCaptor<RepositoryFileAcl> captor = ArgumentCaptor.forClass( RepositoryFileAcl.class );
    verify( aclImporter ).addCatalog( any( InputStream.class ), any( MondrianCatalog.class ), anyBoolean(), captor.capture(),
        any( IPentahoSession.class ) );
    assertEquals( acl, captor.getValue() );
  }

  @Test
  public void testConvertExceptionToStatus_ACCESS_DENIED() {
    testConvertExceptionToStatus( PlatformImportException.PUBLISH_TO_SERVER_FAILED, Reason.ACCESS_DENIED   );
  }

  @Test
  public void testConvertExceptionToStatus_GENERAL() {
    testConvertExceptionToStatus( PlatformImportException.PUBLISH_GENERAL_ERROR, Reason.GENERAL   );
  }

  @Test
  public void testConvertExceptionToStatus_ALREADY_EXISTS() {
    testConvertExceptionToStatus( PlatformImportException.PUBLISH_SCHEMA_EXISTS_ERROR, Reason.ALREADY_EXISTS   );
  }

  @Test
  public void testConvertExceptionToStatus_XMLA_SCHEMA_NAME_EXISTS() {
    testConvertExceptionToStatus( PlatformImportException.PUBLISH_XMLA_CATALOG_EXISTS, Reason.XMLA_SCHEMA_NAME_EXISTS   );
  }

  @Test
  public void testImportFileNonMondrianException() throws IOException {
    IOException exception = new IOException();
    when( bundle.getProperty( eq( MondrianImportHandler.DOMAIN_ID ) ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    doThrow( exception ).when( bundle ).getInputStream();
    try {
      testImportFileBase();
    } catch ( PlatformImportException e ) {
      assertEquals( PlatformImportException.PUBLISH_GENERAL_ERROR, e.getErrorStatus() );
    } catch ( Exception e ) {
      fail( "According current implementation, should not happen." );
    }
  }

  public void testConvertExceptionToStatus( int importStatus, Reason reason ) {
    MondrianCatalogServiceException exception = new MondrianCatalogServiceException( "msg", reason );
    when( bundle.getProperty( eq( MondrianImportHandler.DOMAIN_ID ) ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    doThrow( exception ).when( mondrianImporter ).addCatalog( any( InputStream.class ), any( MondrianCatalog.class ),
        anyBoolean(), any( IPentahoSession.class ) );
    try {
      testImportFileBase();
    } catch ( PlatformImportException e ) {
      assertEquals( importStatus, e.getErrorStatus() );
    } catch ( Exception e ) {
      fail( "According current implementation, should not happen." );
    }
  }

  public void testImportFileBase() throws PlatformImportException, DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException, IOException  {
    MondrianImportHandler handler = new MondrianImportHandler( mimeTypes, mondrianImporter );
    handler.importFile( bundle );
    ArgumentCaptor<MondrianCatalog> mondrianCatalog = ArgumentCaptor.forClass( MondrianCatalog.class );
    verify( mondrianImporter ).addCatalog( any( InputStream.class ), mondrianCatalog.capture(), anyBoolean(), any( IPentahoSession.class ) );
    assertTrue( mondrianCatalog.getValue().getDataSourceInfo().contains( OTHER_PARAMETR ) );
  }

  @Test
  public void testCreateCatalogObject_SpecificSymbolsInBundle() throws Exception {
    //createCatalogObject method has input parameters with custom-escaped only quotes
    //should unescape it and escape all symbols by standard escapeXml

    IPlatformImportBundle customBundle = mock( IPlatformImportBundle.class );
    parameters = MondrianImportHandler.PROVIDER + "=provider;"
      + MondrianImportHandler.DATA_SOURCE + "=\"DS &quot;Test's&quot; & <Fun>\";"
      + "DynamicSchemaProcessor=\"DSP's & &quot;Other&quot; <stuff>\"";

    String expectedValue = new StringBuilder()
      .append( "DataSource=\"DS &quot;Test&apos;s&quot; &amp; &lt;Fun&gt;\";" )
      .append( "EnableXmla=true;" )
      .append( "Provider=\"provider\";" )
      .append( "DynamicSchemaProcessor=\"DSP&apos;s &amp; &quot;Other&quot; &lt;stuff&gt;\"" )
      .toString();

    when( customBundle.getProperty( eq( MondrianImportHandler.ENABLE_XMLA ) ) ).thenReturn( "true" );
    when( customBundle.getProperty( eq( MondrianImportHandler.DATA_SOURCE ) ) ).thenReturn( "DS &quot;Test's&quot; & <Fun>" );
    when( customBundle.getProperty( eq( MondrianImportHandler.PARAMETERS ) ) ).thenReturn( parameters );
    MondrianImportHandler mondrianImportHandler = new MondrianImportHandler( mimeTypes, mondrianImporter );
    MondrianCatalog catalog = mondrianImportHandler.createCatalogObject( "catalog", true, customBundle );

    assertEquals( catalog.getDataSourceInfo(), expectedValue );
  }
}
