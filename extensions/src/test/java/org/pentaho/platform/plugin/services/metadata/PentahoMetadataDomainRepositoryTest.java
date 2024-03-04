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

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.READ;
import static org.pentaho.platform.api.repository2.unified.RepositoryFilePermission.WRITE;
import static org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository.PROPERTY_NAME_DATASOURCE_TYPE;
import static org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository.PROPERTY_NAME_DOMAIN_ID;
import static org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository.PROPERTY_NAME_LOCALE;
import static org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository.PROPERTY_NAME_TYPE;
import static org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository.TYPE_DOMAIN;
import static org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository.TYPE_LOCALE;
import static org.pentaho.test.platform.repository2.unified.MockUnifiedRepository.everyone;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class PentahoMetadataDomainRepositoryTest {

  private static IUnifiedRepository repos = new MockUnifiedRepository( new UserProvider() );

  @ClassRule
  public static TemporaryFolder tempFolder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static PentahoMetadataDomainRepository domainRepos;

  private static InputStream xmi1, xmi2, xmi3;

  private static final HashSet<Serializable> originalFileIds = new HashSet();

  @BeforeClass
  public static void setup() throws Exception {
    repos.createFolder( repos.getFile( "/etc" ).getId(),
      new RepositoryFile.Builder( "metadata" ).folder( true ).build(),
      new RepositoryFileAcl.Builder( MockUnifiedRepository.root() ).ace( everyone(), READ, WRITE ).build(), null );
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<>( String.class ).object( "__root__" )
        .attributes( Collections.singletonMap( "id", "singleTenantAdminUserName" ) ).build() );
    PentahoSystem.registerReference(
      new SingletonPentahoObjectReference.Builder<>( IUnifiedRepository.class ).object( repos ).build() );
    SecurityHelper.setMockInstance( new MockedSecurityHelper() );

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( new Domain() );

    //capture original files to reset to later

    RepositoryFile root = repos.getFile( "/" );
    Stack<RepositoryFile> unvisited = new Stack<>();
    unvisited.add( root );

    // Keep track of original Files, will delete them between tests
    walkRepository( repositoryFile -> originalFileIds.add( repositoryFile.getId() ) );

    domainRepos = new PentahoMetadataDomainRepository( repos, null, xmiParser, null );
  }

  @AfterClass
  public static void tearDown() throws Exception {
    close( xmi1 );
    close( xmi2 );
    close( xmi3 );
  }

  public static void close( InputStream is ) throws IOException {
    if ( is != null ) {
      is.close();
    }
  }

  @Before
  public void before() {
    // DELETE any recently added files
    walkRepository( repositoryFile -> {
      if ( !originalFileIds.contains( repositoryFile.getId() ) ) {
        repos.deleteFile( repositoryFile.getId(), true, null );
      }
    } );
  }

  /**
   * Walk repository and apply logic to each file.
   * @param fn
   */
  public static void walkRepository( Consumer<RepositoryFile> fn ) {
    // Simple DFS logic for tree
    RepositoryFile root = repos.getFile( "/" );
    Stack<RepositoryFile> unvisited = new Stack<>();
    unvisited.add( root );

    while ( !unvisited.isEmpty() ) {
      RepositoryFile repositoryFile = unvisited.pop();
      unvisited.addAll( repos.getChildren( repositoryFile.getId() ) );
      fn.accept( repositoryFile );
    }
  }

  private static InputStream getXmiInputStream() throws Exception {
    return getInputSteam( new File( TestResourceLocation.TEST_RESOURCES + "/ImportTest/steel-wheels.xmi" ) );
  }

  private static InputStream getInputSteam( File file ) throws Exception {
    return new ByteArrayInputStream( IOUtils.toByteArray( file.toURI() ) );
  }

  @Test
  public void testGetDomainFilesNoSuchDomain() {
    assertTrue( domainRepos.getDomainFilesData( "NOSUCH_DOMAIN" ).isEmpty() );
  }

  @Test
  public void testGetDomainFiles() throws Exception {
    // SETUP
    File jaFile = tempFolder.newFile( "messages_ja.properties" );
    File frFile = tempFolder.newFile( "messages_fr_FR.properties" );

    xmi1 = getXmiInputStream();
    xmi2 = getXmiInputStream();
    xmi3 = getXmiInputStream();

    domainRepos.addLocalizationFile( "testDomain1.xmi", "ja", getInputSteam( jaFile ), false );
    domainRepos.addLocalizationFile( "testDomain1.xmi", "fr_FR", getInputSteam( frFile ), false );
    domainRepos.storeDomain( xmi1, "testDomain1.xmi", false );
    domainRepos.storeDomain( xmi2, "testDomain_noLocaleFiles.xmi", false );
    domainRepos.storeDomain( xmi3, "testDomain_doesntEndIn_dotXMI", false );

    // TEST 1
    Map<String, InputStream> domainFiles = domainRepos.getDomainFilesData( "testDomain1.xmi" );
    assertThat( domainFiles.size(), equalTo( 3 ) );
    assertThat( domainFiles.keySet().stream().sorted().collect( Collectors.joining( "," ) ),
      equalTo( "messages_fr_FR.properties,messages_ja.properties,testDomain1.xmi" ) );

    // TEST 2
    domainFiles = domainRepos.getDomainFilesData( "testDomain_noLocaleFiles.xmi" );
    assertThat( domainFiles.size(), equalTo( 1 ) );
    assertThat( domainFiles.keySet().stream().sorted().collect( Collectors.joining( "," ) ),
      equalTo( "testDomain_noLocaleFiles.xmi" ) );

    // TEST 3
    domainFiles = domainRepos.getDomainFilesData( "testDomain_doesntEndIn_dotXMI" );
    assertThat( domainFiles.size(), equalTo( 1 ) );
    assertThat( domainFiles.keySet().stream().sorted().collect( Collectors.joining( "," ) ),
      equalTo( "testDomain_doesntEndIn_dotXMI.xmi" ) );
  }

  @Test
  public void testAddDataSourceType_DSW() throws Exception {
    // SETUP
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );
    when( domainRepos.getXmiParser().parseXmi( inputStream ) ).thenReturn( createDSWTestObject() );

    Map<String, Serializable> fileMetadata = new HashMap<>();

    // EXECUTE
    domainRepos.addDataSourceType( fileMetadata, srfd );

    // VERIFY
    assertTrue( fileMetadata.entrySet().stream().anyMatch( e ->
            e.getKey().equals( PROPERTY_NAME_DATASOURCE_TYPE )
            && e.getValue().equals( PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() ) ) );
  }

  @Test
  public void testAddDataSourceType_Metadata() throws Exception {
    // SETUP
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );
    when( domainRepos.getXmiParser().parseXmi( inputStream ) ).thenReturn( createMetadataTestObject() );

    Map<String, Serializable> fileMetadata = new HashMap<>();

    // EXECUTE
    domainRepos.addDataSourceType( fileMetadata, srfd );

    // VERIFY
    assertTrue( fileMetadata.entrySet().stream().anyMatch( e ->
            e.getKey().equals( PROPERTY_NAME_DATASOURCE_TYPE )
                    && e.getValue().equals( PentahoDataSourceType.METADATA.toString() ) ) );
  }

  @Test
  public void testAddDataSourceType_NullDomain() throws Exception {
    // SETUP
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );
    when( domainRepos.getXmiParser().parseXmi( inputStream ) ).thenReturn( null );

    Map<String, Serializable> fileMetadata = new HashMap<>();

    // EXECUTE
    domainRepos.addDataSourceType( fileMetadata, srfd );

    // VERIFY
    assertFalse( fileMetadata.entrySet().stream().anyMatch( e ->
            e.getKey().equals( PROPERTY_NAME_DATASOURCE_TYPE )
                    && e.getValue().equals( PentahoDataSourceType.METADATA.toString() ) ) );
    assertFalse( fileMetadata.entrySet().stream().anyMatch( e ->
            e.getKey().equals( PROPERTY_NAME_DATASOURCE_TYPE )
                    && e.getValue().equals( PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() ) ) );
  }

  @Test
  public void testCreateUniqueFile_nonLocale() throws Exception {
    // SETUP
    String filename = "TEST_FILE_NAME";
    String domainId = "TEST_DOMAIN_ID";
    String locale = null;
    Serializable serializableMetadataDir = "test-metadata-dir-id";
    String createdFileId = "TEST_CREATED_FILE_ID";

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    RepositoryFile rfCreated = Mockito.mock( RepositoryFile.class );
    when( rfCreated.getId() ).thenReturn( createdFileId );
    when( rfCreated.getName() ).thenReturn( filename );

    RepositoryFile rfMetaDataDir = Mockito.mock( RepositoryFile.class );
    when( rfMetaDataDir.getId() ).thenReturn( serializableMetadataDir );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    when( repository.createFile( any(), any(), any(), any() ) ).thenReturn( rfCreated );

    // assume only #getMetadataDir() calls this function
    when( repository.getFile( any() ) ).thenReturn( rfMetaDataDir );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    when( srfd.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[0] ) );

    Map<String, Serializable> expectedMetadataMap = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    // EXECUTE
    RepositoryFile actual = pmdr.createUniqueFile( filename, domainId, locale, srfd );

    // VERIFY
    assertEquals( filename, actual.getName() );

    verify( repository, times( 1 ) ).createFile(
            eq( serializableMetadataDir ),
            argThat( f -> filename.equals( f.getName() ) ),
            eq( srfd ),
            isNull() );

    verify( repository, times( 0 ) ).updateFile( any(), any(), any() );

    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> createdFileId.equals( id ) ),
            argThat( map -> equalMaps( expectedMetadataMap, map ) ) );
  }

  @Test
  public void testCreateUniqueFile_locale() throws Exception {
    // SETUP
    String filename = "TEST_FILE_NAME";
    String domainId = "TEST_DOMAIN_ID";
    String locale = "jp";
    Serializable serializableMetadataDir = "test-metadata-dir-id";
    String createdFileId = "TEST_CREATED_FILE_ID";

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    RepositoryFile rfCreated = Mockito.mock( RepositoryFile.class );
    when( rfCreated.getId() ).thenReturn( createdFileId );
    when( rfCreated.getName() ).thenReturn( filename );

    RepositoryFile rfMetaDataDir = Mockito.mock( RepositoryFile.class );
    when( rfMetaDataDir.getId() ).thenReturn( serializableMetadataDir );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    when( repository.createFile( any(), any(), any(), any() ) ).thenReturn( rfCreated );

    // assume only #getMetadataDir() calls this function
    when( repository.getFile( any() ) ).thenReturn( rfMetaDataDir );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    when( srfd.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[0] ) );

    Map<String, Serializable> expectedMetadataMap = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      put( PROPERTY_NAME_LOCALE, locale );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    // EXECUTE
    RepositoryFile actual = pmdr.createUniqueFile( filename, domainId, locale, srfd );

    // VERIFY
    assertEquals( filename, actual.getName() );

    verify( repository, times( 1 ) ).createFile(
            eq( serializableMetadataDir ),
            argThat( f -> filename.equals( f.getName() ) ),
            eq( srfd ),
            isNull() );

    verify( repository, times( 0 ) ).updateFile( any(), any(), any() );

    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> createdFileId.equals( id ) ),
            argThat( map -> equalMaps( expectedMetadataMap, map ) ) );
  }

  @Test
  public void testUpdateFile_nonLocale() throws Exception {
    // SETUP
    String domainId = "TEST_DOMAIN_ID";

    Map<String, Serializable> fileMetadata = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    Map<String, Serializable> expectedMetadataMap = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    when( srfd.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[0] ) );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    when( domainFile.getId() ).thenReturn( domainId );

    when( repository.getFileMetadata( eq( domainId ) ) ).thenReturn( fileMetadata );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    pmdr.updateFile( domainFile, srfd );

    // VERIFY
    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> domainId.equals( id ) ),
            argThat( map -> equalMaps( expectedMetadataMap, map ) ) );

    verify( repository, times( 0 ) ).createFile( any(), any(), any(), any() );

    verify( repository, times( 1 ) ).updateFile(
            eq( domainFile ),
            eq( srfd ),
            isNull() );
  }

  @Test
  public void testUpdateFile_nonLocale_DatasourceTypeChange() throws Exception {
    // SETUP
    String domainId = "TEST_DOMAIN_ID";

    Map<String, Serializable> fileMetadata = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() );
    } };

    Map<String, Serializable> expectedMetadataMap = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    when( srfd.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[0] ) );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    when( domainFile.getId() ).thenReturn( domainId );

    when( repository.getFileMetadata( eq( domainId ) ) ).thenReturn( fileMetadata );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    pmdr.updateFile( domainFile, srfd );

    // VERIFY
    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> domainId.equals( id ) ),
            argThat( map -> equalMaps( expectedMetadataMap, map ) ) );

    verify( repository, times( 0 ) ).createFile( any(), any(), any(), any() );

    verify( repository, times( 1 ) ).updateFile(
            eq( domainFile ),
            eq( srfd ),
            isNull() );
  }

  @Test
  public void testUpdateFile_locale() throws Exception {
    // SETUP
    String domainId = "TEST_DOMAIN_ID";

    Map<String, Serializable> fileMetadata = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      put( PROPERTY_NAME_LOCALE, "jp" );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    Map<String, Serializable> expectedMetadataMap = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      put( PROPERTY_NAME_LOCALE, "jp" );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    when( srfd.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[0] ) );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    when( domainFile.getId() ).thenReturn( domainId );

    when( repository.getFileMetadata( eq( domainId ) ) ).thenReturn( fileMetadata );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    pmdr.updateFile( domainFile, srfd );

    // VERIFY
    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> domainId.equals( id ) ),
            argThat( map -> equalMaps( expectedMetadataMap, map ) ) );

    verify( repository, times( 0 ) ).createFile( any(), any(), any(), any() );

    verify( repository, times( 1 ) ).updateFile(
            eq( domainFile ),
            eq( srfd ),
            isNull() );
  }

  @Test
  public void testUpdateFile_locale_DatasourceTypeChange() throws Exception {
    // SETUP
    String domainId = "TEST_DOMAIN_ID";

    Map<String, Serializable> fileMetadata = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      put( PROPERTY_NAME_LOCALE, "jp" );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() );
    } };

    Map<String, Serializable> expectedMetadataMap = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
      put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      put( PROPERTY_NAME_LOCALE, "jp" );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    when( srfd.getInputStream() ).thenReturn( new ByteArrayInputStream( new byte[0] ) );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );

    RepositoryFile domainFile = Mockito.mock( RepositoryFile.class );
    when( domainFile.getId() ).thenReturn( domainId );

    when( repository.getFileMetadata( eq( domainId ) ) ).thenReturn( fileMetadata );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    pmdr.updateFile( domainFile, srfd );

    // VERIFY
    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> domainId.equals( id ) ),
            argThat( map -> equalMaps( expectedMetadataMap, map ) ) );

    verify( repository, times( 0 ) ).createFile( any(), any(), any(), any() );

    verify( repository, times( 1 ) ).updateFile(
            eq( domainFile ),
            eq( srfd ),
            isNull() );
  }

  @Test
  public void testIsMetadataDataSource() {
    Domain domainMD = createMetadataTestObject();
    assertTrue( domainRepos.isMetadataDataSource( domainMD ) );

    Domain domainMD2 = createDSWTestObject();
    domainMD2.setLogicalModels( null );
    assertTrue( domainRepos.isMetadataDataSource( domainMD2 ) );

    Domain domainDSW = createDSWTestObject();
    assertFalse( domainRepos.isMetadataDataSource( domainDSW ) );
  }

  @Test
  public void testIsDSWDatasource() {
    Domain domainMD = createMetadataTestObject();
    assertFalse( domainRepos.isDSWDatasource( domainMD ) );

    Domain domainMD2 = createDSWTestObject();
    domainMD2.setLogicalModels( null );
    assertFalse( domainRepos.isDSWDatasource( domainMD2 ) );

    Domain domainDSW = createDSWTestObject();
    assertTrue( domainRepos.isDSWDatasource( domainDSW ) );
  }

  @Test
  public void testGetDomain_MapData() {
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );

    Domain domain = domainRepos.getDomain( new HashMap<>(), srfd );
    assertNotNull( domain );
  }

  @Test
  public void testGetDomain_MapData_UnifiedRepositoryException_Unknown() throws Exception {
    thrown.expect( UnifiedRepositoryException.class );
    thrown.expectMessage( "__UNKNOWN_ID__" );

    // SETUP
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );

    when( domainRepos.getXmiParser().parseXmi( inputStream ) ).thenThrow( new IOException() );

    // EXECUTE
    domainRepos.getDomain( new HashMap<>(), srfd );
  }

  @Test
  public void testGetDomain_MapData_UnifiedRepositoryException_Id() throws Exception {
    String domainId = "TESTING-124";
    thrown.expect( UnifiedRepositoryException.class );
    thrown.expectMessage( domainId );

    // SETUP
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );

    when( domainRepos.getXmiParser().parseXmi( inputStream ) ).thenThrow( new IOException() );

    // EXECUTE
    domainRepos.getDomain( new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, domainId );
    }}, srfd );
  }

  @Test
  public void testIsDomain() {
    Map<String, Serializable> fileMetadataDomain = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() );
    } };

    assertTrue( domainRepos.isDomain( fileMetadataDomain ) );

    Map<String, Serializable> fileMetadataLocale = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_LOCALE );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() );
    } };

    assertFalse( domainRepos.isDomain( fileMetadataLocale ) );

    Map<String, Serializable> fileMetadataNonDomain = new HashMap<String, Serializable>() {{
      put( "randomKey", "randomValue" );
    } };

    assertFalse( domainRepos.isDomain( fileMetadataNonDomain ) );
  }

  @Test
  public void testHasDatasourceType() {
    Map<String, Serializable> fileMetadataDomain = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() );
    } };

    assertTrue( domainRepos.hasDatasourceType( fileMetadataDomain ) );

    Map<String, Serializable> fileMetadataNonDomain = new HashMap<String, Serializable>() {{
      put( "randomKey", "randomValue" );
    } };

    assertFalse( domainRepos.hasDatasourceType( fileMetadataNonDomain ) );
  }

  @Test
  public void testMigrateDomain() throws Exception {
    // SETUP
    Serializable serializableMetadataDir = "test-metadata-dir-id";
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    when( repository.getDataForRead( any(), any() ) ).thenReturn( srfd );

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    Map<String, Serializable> fileMetadataLegacy = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
    } };

    Map<String, Serializable> fileMetadataMigrated = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    // EXECUTE
    Map<String, Serializable> fileMetadataActual = pmdr.migrateDomain( serializableMetadataDir, fileMetadataLegacy );

    // VERIFY
    assertTrue( equalMaps( fileMetadataMigrated, fileMetadataActual ) );

    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> serializableMetadataDir.equals( id ) ),
            argThat( map -> equalMaps( fileMetadataMigrated, map ) ) );
  }

  @Test
  public void testGetFileMetadataHelper_NonDomain() throws Exception {
    // testing migration doesn't happen because this isn't a domain
    // SETUP
    Serializable serializableMetadataDir = "test-metadata-dir-id";

    Map<String, Serializable> fileMetadataLegacy = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
    } };

    Map<String, Serializable> fileMetadataExpected = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
    } };

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    when( repository.getFileMetadata( any() ) ).thenReturn( fileMetadataLegacy );

    XmiParser xmiParser = Mockito.mock( XmiParser.class );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    Map<String, Serializable> fileMetadataActual = pmdr.getFileMetadataHelper( serializableMetadataDir );

    // VERIFY
    assertTrue( equalMaps( fileMetadataExpected, fileMetadataActual ) );
  }

  @Test
  public void testGetFileMetadataHelper_hasDatasourceType() throws Exception {
    // testing migration does not happen because migration already happened
    // SETUP
    Serializable serializableMetadataDir = "test-metadata-dir-id";

    Map<String, Serializable> fileMetadataLegacy = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    Map<String, Serializable> fileMetadataExpected = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    when( repository.getFileMetadata( any() ) ).thenReturn( fileMetadataLegacy );

    XmiParser xmiParser = Mockito.mock( XmiParser.class );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    Map<String, Serializable> fileMetadataActual = pmdr.getFileMetadataHelper( serializableMetadataDir );

    // VERIFY
    assertTrue( equalMaps( fileMetadataExpected, fileMetadataActual ) );
  }

  @Test
  public void testGetFileMetadataHelper_migrateDomain() throws Exception {
    // testing migration happens
    // SETUP
    Serializable serializableMetadataDir = "test-metadata-dir-id";
    SimpleRepositoryFileData srfd = Mockito.mock( SimpleRepositoryFileData.class );
    InputStream inputStream = Mockito.mock( InputStream.class );
    when( srfd.getInputStream() ).thenReturn( inputStream );

    Map<String, Serializable> fileMetadataLegacy = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
    } };

    Map<String, Serializable> fileMetadataExpected = new HashMap<String, Serializable>() {{
      put( PROPERTY_NAME_DOMAIN_ID, "someId" );
      put( PROPERTY_NAME_TYPE, TYPE_DOMAIN );
      // testing this property is added
      put( PROPERTY_NAME_DATASOURCE_TYPE, PentahoDataSourceType.METADATA.toString() );
    } };

    IUnifiedRepository repository = Mockito.mock( IUnifiedRepository.class );
    when( repository.getDataForRead( any(), any() ) ).thenReturn( srfd );
    when( repository.getFileMetadata( any() ) ).thenReturn( fileMetadataLegacy );

    XmiParser xmiParser = Mockito.mock( XmiParser.class );
    when( xmiParser.parseXmi( any( InputStream.class ) ) ).thenReturn( createMetadataTestObject() );

    PentahoMetadataDomainRepository pmdr = new PentahoMetadataDomainRepository( repository, null, xmiParser, null );

    // EXECUTE
    Map<String, Serializable> fileMetadataActual = pmdr.getFileMetadataHelper( serializableMetadataDir );

    // VERIFY
    assertTrue( equalMaps( fileMetadataExpected, fileMetadataActual ) );

    verify( repository, times( 1 ) ).setFileMetadata(
            argThat( id -> serializableMetadataDir.equals( id ) ),
            argThat( map -> equalMaps( fileMetadataExpected, map ) ) );
  }

  Domain createDSWTestObject() {
    Domain domain = new Domain();

    List<LogicalModel> models = new ArrayList<>();

    LogicalModel lm1 = new LogicalModel();
    lm1.setProperty( "AGILE_BI_GENERATED_SCHEMA", "TRUE" );
    models.add( lm1 );

    LogicalModel lm2 = new LogicalModel();
    lm2.setProperty( "WIZARD_GENERATED_SCHEMA", "TRUE" );
    models.add( lm2 );

    domain.setLogicalModels( models );

    return domain;
  }

  Domain createMetadataTestObject() {
    Domain domain = createDSWTestObject();
    domain.getLogicalModels().clear();

    return domain;
  }

  /**
   * Tests if two maps are equal in size and values.
   * @param m1
   * @param m2
   * @param <K>
   * @param <V>
   * @return true is equal in size and values, false otherwise.
   */
  public static <K, V> boolean equalMaps( Map<K, V> m1, Map<K, V> m2 ) {
    if ( m1 == null && m2 == null ) return true;
    if ( m1 == null || m2 == null ) return false;
    if ( m1.size() != m2.size() ) return false;

    for ( K key: m1.keySet() )
      if ( !m1.get( key ).equals( m2.get( key ) ) )
        return false;
    return true;
  }

  public static class UserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    public String getUser() {
      return "__root__";
    }

    public List<String> getRoles() {
      return Arrays.asList( "__everyone__", "role2" );
    }
  }

  private static class MockedSecurityHelper extends SecurityHelper {
    @Override public IUserRoleListService getUserRoleListService() {
      return Mockito.mock( IUserRoleListService.class );
    }
  }
}
