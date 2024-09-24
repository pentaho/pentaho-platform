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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.exporter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

public class PentahoPlatformExporterTest {

  PentahoPlatformExporter exporterSpy;
  PentahoPlatformExporter exporter;
  IUnifiedRepository repo;
  IScheduler scheduler;
  IPentahoSession session;
  IMondrianCatalogService mondrianCatalogService;
  MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper;

  ExportManifest exportManifest;

  @Before
  public void setUp() throws Exception {
    repo = mock( IUnifiedRepository.class );
    scheduler = mock( IScheduler.class );
    session = mock( IPentahoSession.class );
    mondrianCatalogService = mock( IMondrianCatalogService.class );
    mondrianCatalogRepositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    exportManifest = spy( new ExportManifest() );

    PentahoSessionHolder.setSession( session );
    exporterSpy = spy( new PentahoPlatformExporter( repo ) );
    exporterSpy.setExportManifest( exportManifest );

    doReturn( "session name" ).when( session ).getName();
    exporter = new PentahoPlatformExporter( repo );
  }

  @After
  public void tearDown() {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testExportUsersAndRoles() {
    IUserRoleListService mockDao = mock( IUserRoleListService.class );
    IAnyUserSettingService userSettingService = mock( IAnyUserSettingService.class );
    UserDetailsService userDetailsService = mock( UserDetailsService.class );
    PentahoSystem.registerObject( mockDao );
    PentahoSystem.registerObject( userSettingService );
    PentahoSystem.registerObject( userDetailsService );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    String tenantPath = "path";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );

    List<String> userList = new ArrayList<>();
    String user = "testUser";
    String role = "testRole";

    userList.add( user );
    when( mockDao.getAllUsers( ArgumentMatchers.any( ITenant.class ) ) ).thenReturn( userList );

    List<String> roleList = new ArrayList<>();
    roleList.add( role );
    when( mockDao.getAllRoles() ).thenReturn( roleList );

    Map<String, List<String>> map = new HashMap<>();
    List<String> permissions = new ArrayList<>();
    permissions.add( "read" );
    map.put( "testRole", permissions );
    RoleBindingStruct struct = mock( RoleBindingStruct.class );
    struct.bindingMap = map;
    when( roleBindingDao.getRoleBindingStruct( nullable( String.class ) ) ).thenReturn( struct );

    ArgumentCaptor<UserExport> userCaptor = ArgumentCaptor.forClass( UserExport.class );
    ArgumentCaptor<RoleExport> roleCaptor = ArgumentCaptor.forClass( RoleExport.class );
    ExportManifest manifest = mock( ExportManifest.class );
    exporter.setExportManifest( manifest );

    List<IUserSetting> settings = new ArrayList<>();
    IUserSetting setting = mock( IUserSetting.class );
    settings.add( setting );
    when( userSettingService.getUserSettings( user ) ).thenReturn( settings );
    when( userSettingService.getGlobalUserSettings() ).thenReturn( settings );

    List<GrantedAuthority> authList = new ArrayList<>();
    UserDetails userDetails = new User( "testUser", "testPassword", true, true, true, true, authList );
    when( userDetailsService.loadUserByUsername( nullable( String.class ) ) ).thenReturn( userDetails );

    exporter.exportUsersAndRoles();

    verify( manifest ).addUserExport( userCaptor.capture() );
    verify( manifest ).addRoleExport( roleCaptor.capture() );
    verify( userSettingService ).getGlobalUserSettings();
    verify( manifest ).addGlobalUserSetting( ArgumentMatchers.any( ExportManifestUserSetting.class ) );
    assertEquals( settings.size(), userCaptor.getValue().getUserSettings().size() );

    UserExport userExport = userCaptor.getValue();
    assertEquals( "testUser", userExport.getUsername() );
    RoleExport roleExport = roleCaptor.getValue();
    assertEquals( "testRole", roleExport.getRolename() );
  }

  @Test
  public void testExportMetadata_noModels() throws Exception {
    IMetadataDomainRepository mdr = mock( IMetadataDomainRepository.class );
    exporter.setMetadataDomainRepository( mdr );

    exporter.exportMetadataModels();
    assertEquals( 0, exporter.getExportManifest().getMetadataList().size() );
  }

  @Test
  public void testExportMetadata() throws Exception {
    IMetadataDomainRepository mdr = mock( IMetadataDomainRepository.class );

    Set<String> domainIds = new HashSet<>();
    domainIds.add( "test1" );

    when( mdr.getDomainIds() ).thenReturn( domainIds );
    exporterSpy.setMetadataDomainRepository( mdr );
    exporterSpy.zos = mock( ZipOutputStream.class );

    Map<String, InputStream> inputMap = new HashMap<>();
    InputStream is = mock( InputStream.class );
    when( is.read( ArgumentMatchers.any( byte[].class ) ) ).thenReturn( -1 );
    inputMap.put( "test1", is );

    doReturn( inputMap ).when( exporterSpy ).getDomainFilesData( "test1" );

    exporterSpy.exportMetadataModels();
    assertEquals( 1, exporterSpy.getExportManifest().getMetadataList().size() );

    assertEquals( "test1", exporterSpy.getExportManifest().getMetadataList().get( 0 ).getDomainId() );
    assertEquals( PentahoPlatformExporter.METADATA_PATH_IN_ZIP + "test1.xmi",
      exporterSpy.getExportManifest().getMetadataList().get( 0 ).getFile() );
  }

  @Test
  public void testExportDatasources() throws Exception {

    IDatasourceMgmtService svc = mock( IDatasourceMgmtService.class );
    exporterSpy.setDatasourceMgmtService( svc );

    List<IDatabaseConnection> datasources = new ArrayList<>();
    IDatabaseConnection conn = mock( org.pentaho.database.model.DatabaseConnection.class );
    when( conn.getName() ).thenReturn( "aDatasourceName" );
    IDatabaseConnection icon = mock( IDatabaseConnection.class );
    datasources.add( conn );
    datasources.add( icon );

    when( svc.getDatasources() ).thenReturn( datasources );

    exporterSpy.exportDatasources();

    assertEquals( 1, exporterSpy.getExportManifest().getDatasourceList().size() );
    DatabaseConnection exportedDatabaseConnection = exporterSpy.getExportManifest().getDatasourceList().get( 0 );
    assertEquals( "aDatasourceName", exportedDatabaseConnection.getName() );
  }

  @Test
  public void testParseXmlaEnabled() throws Exception {
    String dsInfo = "DataSource=SampleData;Provider=mondrian;EnableXmla=\"false\"";
    assertFalse( exporterSpy.parseXmlaEnabled( dsInfo ) );

    dsInfo = "DataSource=SampleData;Provider=mondrian;EnableXmla=\"true\"";
    assertTrue( exporterSpy.parseXmlaEnabled( dsInfo ) );

    dsInfo = "DataSource=SampleData;Provider=mondrian";
    assertFalse( exporterSpy.parseXmlaEnabled( dsInfo ) );

    dsInfo = "DataSource=SampleData;EnableXmla=\"true\";Provider=mondrian";
    assertTrue( exporterSpy.parseXmlaEnabled( dsInfo ) );
  }

  @Test
  public void testExportMondrianSchemas_noCatalogs() throws Exception {
    PentahoSystem.registerObject( mondrianCatalogService );
    exporterSpy.setMondrianCatalogRepositoryHelper( mondrianCatalogRepositoryHelper );

    exporterSpy.exportMondrianSchemas();

    verify( exportManifest, never() ).addMondrian( ArgumentMatchers.any( ExportManifestMondrian.class ) );
    verify( mondrianCatalogRepositoryHelper, never() ).getModrianSchemaFiles( nullable( String.class ) );
  }

  @Test
  public void testExportMondrianSchemas() throws Exception {
    final String catalogName = "test";
    String dataSourceInfo = "EnableXmla=\"true\"";

    executeExportMondrianSchemasForDataSourceInfo( catalogName, dataSourceInfo );

    verify( exportManifest ).addMondrian( ArgumentMatchers.any( ExportManifestMondrian.class ) );
    verify( mondrianCatalogRepositoryHelper ).getModrianSchemaFiles( nullable( String.class ) );
    assertEquals( catalogName, exportManifest.getMondrianList().get( 0 ).getCatalogName() );
    assertTrue( exportManifest.getMondrianList().get( 0 ).isXmlaEnabled() );
    verify( exporterSpy.zos ).putNextEntry( ArgumentMatchers.any( ZipEntry.class ) );
  }

  @Test
  public void testExportMondrianSchemas_AdditionalParametersSaved() throws Exception {
    final String parameterName = "AdditionalParameter";
    final String parameterValue = "TestValue";
    final String expectedParameterValue = "TestValue";
    final String dataSourceInfo = "EnableXmla=\"true\";" + parameterName + "=" + parameterValue;
    String catalogName = "test";

    executeExportMondrianSchemasForDataSourceInfo( catalogName, dataSourceInfo );

    String returnedParameterValue = exportManifest.getMondrianList().get( 0 ).getParameters().get( parameterName );
    assertNotNull( returnedParameterValue );
    assertEquals( returnedParameterValue, expectedParameterValue );
  }

  @Test
  public void testPerformExportMondrianSchemas_XmlUnsafeDataSourceInfoSaved() throws IOException {

    final String dataSourceInfo = "DataSource=\"&quot;DS &quot;Test&apos;s&quot; &amp; &lt;Fun&gt;&quot;\";"
      + "DynamicSchemaProcessor=\"&quot;DSP&apos;s &amp; &quot;Other&quot; &lt;stuff&gt;&quot;\";";

    final String dataSourceExpectedValue = "\"DS \"Test's\" & <Fun>\"";
    final String dynamicSchemaProcessorExpectedValue = "\"DSP's & \"Other\" <stuff>\"";

    executeExportMondrianSchemasForDataSourceInfo( "", dataSourceInfo );

    String returnedParameterValue = exportManifest.getMondrianList().get( 0 ).getParameters().get( "DataSource" );
    assertNotNull( returnedParameterValue );
    assertEquals( returnedParameterValue, dataSourceExpectedValue );

    returnedParameterValue = exportManifest.getMondrianList().get( 0 ).getParameters().get( "DynamicSchemaProcessor" );
    assertNotNull( returnedParameterValue );
    assertEquals( returnedParameterValue, dynamicSchemaProcessorExpectedValue );

  }

  private void executeExportMondrianSchemasForDataSourceInfo( String catalogName, String dataSourceInfo ) throws IOException {
    PentahoSystem.registerObject( mondrianCatalogService );
    exporterSpy.setMondrianCatalogRepositoryHelper( mondrianCatalogRepositoryHelper );

    List<MondrianCatalog> catalogs = new ArrayList<>();
    MondrianCatalog catalog = new MondrianCatalog( catalogName, dataSourceInfo, null, null );
    catalogs.add( catalog );
    when( mondrianCatalogService.listCatalogs( ArgumentMatchers.any( IPentahoSession.class ), ArgumentMatchers.anyBoolean() ) ).thenReturn( catalogs );

    Map<String, InputStream> inputMap = new HashMap<>();
    InputStream is = mock( InputStream.class );
    when( is.read( ArgumentMatchers.any( byte[].class ) ) ).thenReturn( -1 );
    inputMap.put( catalogName, is );
    when( mondrianCatalogRepositoryHelper.getModrianSchemaFiles( catalogName ) ).thenReturn( inputMap );
    exporterSpy.zos = mock( ZipOutputStream.class );

    exporterSpy.exportMondrianSchemas();
  }

  @Test
  public void testExportMetaStore() throws Exception {
    exporterSpy.zos = mock( ZipOutputStream.class );
    IMetaStore metastore = mock( IMetaStore.class );
    exporterSpy.setRepoMetaStore( metastore );
    ExportManifest manifest = mock( ExportManifest.class );
    exporterSpy.setExportManifest( manifest );

    exporterSpy.exportMetastore();
    verify( exporterSpy.zos ).putNextEntry( ArgumentMatchers.any( ZipEntry.class ) );
    verify( manifest ).setMetaStore( ArgumentMatchers.any( ExportManifestMetaStore.class ) );
  }

  @Test
  public void testIsExportCandidate() throws Exception {
    assertTrue( exporter.isExportCandidate( "/etc" ) );
    assertTrue( exporter.isExportCandidate( "/etc/operations_mart" ) );
    assertTrue( exporter.isExportCandidate( "/etc/operations_mart/someSubFolder" ) );

    assertTrue( exporter.isExportCandidate( "/public" ) );
    assertTrue( exporter.isExportCandidate( "/home" ) );

    assertFalse( exporter.isExportCandidate( "/etc/someRandomFolder" ) );
    assertFalse( exporter.isExportCandidate( "/etc/someRandomFolder/someSubFOlder" ) );
    assertFalse( exporter.isExportCandidate( "/etc/mondrian" ) );
    assertFalse( exporter.isExportCandidate( "/etc/metadata" ) );
    assertFalse( exporter.isExportCandidate( "/etc/metastore" ) );
    assertFalse( exporter.isExportCandidate( "/etc/olap-servers" ) );
    assertFalse( exporter.isExportCandidate( "/etc/models" ) );
    assertFalse( exporter.isExportCandidate( "/etc/pdi" ) );
  }
}