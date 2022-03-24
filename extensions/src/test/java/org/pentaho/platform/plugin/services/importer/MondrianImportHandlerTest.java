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

package org.pentaho.platform.plugin.services.importer;

import org.junit.After;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MondrianImportHandlerTest {

  private static final String OTHER_PARAMETR = "parametr=\"value\"";

  private String parameters;

  private IPlatformImportBundle bundle;

  private IMondrianCatalogService mondrianImporter;

  private List<IMimeType> mimeTypes;

  private InputStream inputStream;

  @Before
  public void setUp() throws IOException {
    mondrianImporter = mock( IMondrianCatalogService.class );
    bundle = mock( IPlatformImportBundle.class );
    parameters = MondrianImportHandler.PROVIDER + "=provider;" + MondrianImportHandler.DATA_SOURCE + "=dataSource;" + OTHER_PARAMETR;
    mimeTypes = Arrays.asList( mock( IMimeType.class ) );
    inputStream = new ByteArrayInputStream( "<text>hello world</text>".getBytes() );

    when( bundle.getProperty( eq( MondrianImportHandler.ENABLE_XMLA ) ) ).thenReturn( "true" );
    when( bundle.getProperty( eq( MondrianImportHandler.PARAMETERS ) ) ).thenReturn( parameters );
    when( bundle.getInputStream() ).thenReturn( inputStream );
  }

  @After
  public void cleanUp() throws IOException {
    if ( inputStream != null ) {
      inputStream.close();
    }
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
    verify( aclImporter ).addCatalog( or( any( InputStream.class ), eq( null ) ), any( MondrianCatalog.class ), anyBoolean(), captor.capture(),
        or( any( IPentahoSession.class ), eq( null ) ) );
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
    verify( aclImporter ).addCatalog( or( any( InputStream.class ), eq( null ) ), any( MondrianCatalog.class ), anyBoolean(), captor.capture(),
      or( any( IPentahoSession.class ), eq( null ) ) );
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

  @Test
  public void testImportFileInvalidXML() throws IOException {
    InputStream invalidXmlInputStream = new ByteArrayInputStream( "<invalid-xml>".getBytes() );
    when( bundle.getProperty( MondrianImportHandler.DOMAIN_ID ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    when( bundle.getInputStream() ).thenReturn( invalidXmlInputStream );
    try {
      testImportFileBase();
    } catch ( PlatformImportException e ) {
      assertEquals( PlatformImportException.PUBLISH_GENERAL_ERROR, e.getErrorStatus() );
    } catch ( Exception e ) {
      fail( "According current implementation, should not happen." );
    }
  }

  @Test
  public void testImportFileEmpty() throws IOException {
    InputStream invalidXmlInputStream = new ByteArrayInputStream( "".getBytes() );
    when( bundle.getProperty( MondrianImportHandler.DOMAIN_ID ) ).thenReturn( MondrianImportHandler.DOMAIN_ID );
    when( bundle.getInputStream() ).thenReturn( invalidXmlInputStream );
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
    verify( mondrianImporter ).addCatalog( or( any( InputStream.class ), eq( null ) ), mondrianCatalog.capture(), anyBoolean(), or( any( IPentahoSession.class ), eq( null ) ) );
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
