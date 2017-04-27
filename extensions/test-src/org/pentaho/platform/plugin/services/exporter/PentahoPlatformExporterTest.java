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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.exporter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
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
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.scheduler2.versionchecker.EmbeddedVersionCheckSystemListener;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

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
    repo = Mockito.mock( IUnifiedRepository.class );
    scheduler = Mockito.mock( IScheduler.class );
    session = Mockito.mock( IPentahoSession.class );
    mondrianCatalogService = Mockito.mock( IMondrianCatalogService.class );
    mondrianCatalogRepositoryHelper = Mockito.mock( MondrianCatalogRepositoryHelper.class );
    exportManifest = Mockito.spy( new ExportManifest() );

    PentahoSessionHolder.setSession( session );
    exporterSpy = Mockito.spy( new PentahoPlatformExporter( repo ) );
    exporterSpy.setScheduler( scheduler );
    exporterSpy.setExportManifest( exportManifest );

    Mockito.doReturn( "session name" ).when( session ).getName();
    exporter = new PentahoPlatformExporter( repo );
  }

  @After
  public void tearDown() {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testExportSchedules() throws Exception {
    List<Job> jobs = new ArrayList<Job>();
    ComplexJobTrigger trigger = Mockito.mock( ComplexJobTrigger.class );
    JobTrigger unknownTrigger = Mockito.mock( JobTrigger.class );

    Job job1 = Mockito.mock( Job.class );
    Job job2 = Mockito.mock( Job.class );
    Job job3 = Mockito.mock( Job.class );
    jobs.add( job1 );
    jobs.add( job2 );
    jobs.add( job3 );

    Mockito.when( scheduler.getJobs( null ) ).thenReturn( jobs );
    Mockito.when( job1.getJobName() ).thenReturn( EmbeddedVersionCheckSystemListener.VERSION_CHECK_JOBNAME );
    Mockito.when( job2.getJobName() ).thenReturn( "job 2" );
    Mockito.when( job2.getJobTrigger() ).thenReturn( trigger );
    Mockito.when( job3.getJobName() ).thenReturn( "job 3" );
    Mockito.when( job3.getJobTrigger() ).thenReturn( unknownTrigger );

    exporterSpy.exportSchedules();

    Mockito.verify( scheduler ).getJobs( null );
    Assert.assertEquals( 1, exporterSpy.getExportManifest().getScheduleList().size() );
  }

  @Test
  public void testExportSchedules_SchedulereThrowsException() throws Exception {
    Mockito.when( scheduler.getJobs( null ) ).thenThrow( new SchedulerException( "bad" ) );

    exporterSpy.exportSchedules();

    Mockito.verify( scheduler ).getJobs( null );
    Assert.assertEquals( 0, exporterSpy.getExportManifest().getScheduleList().size() );
  }

  @Test
  public void testExportUsersAndRoles() {
    IUserRoleListService mockDao = Mockito.mock( IUserRoleListService.class );
    IAnyUserSettingService userSettingService = Mockito.mock( IAnyUserSettingService.class );
    UserDetailsService userDetailsService = Mockito.mock( UserDetailsService.class );
    PentahoSystem.registerObject( mockDao );
    PentahoSystem.registerObject( userSettingService );
    PentahoSystem.registerObject( userDetailsService );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = Mockito.mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    String tenantPath = "path";
    Mockito.when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );

    List<String> userList = new ArrayList<String>();
    String user = "testUser";
    String role = "testRole";

    userList.add( user );
    Mockito.when( mockDao.getAllUsers( Mockito.any( ITenant.class ) ) ).thenReturn( userList );

    List<String> roleList = new ArrayList<String>();
    roleList.add( role );
    Mockito.when( mockDao.getAllRoles() ).thenReturn( roleList );

    Map<String, List<String>> map = new HashMap<String, List<String>>();
    List<String> permissions = new ArrayList<String>();
    permissions.add( "read" );
    map.put( "testRole", permissions );
    RoleBindingStruct struct = Mockito.mock( RoleBindingStruct.class );
    struct.bindingMap = map;
    Mockito.when( roleBindingDao.getRoleBindingStruct( Mockito.anyString() ) ).thenReturn( struct );

    ArgumentCaptor<UserExport> userCaptor = ArgumentCaptor.forClass( UserExport.class );
    ArgumentCaptor<RoleExport> roleCaptor = ArgumentCaptor.forClass( RoleExport.class );
    ExportManifest manifest = Mockito.mock( ExportManifest.class );
    exporter.setExportManifest( manifest );

    List<IUserSetting> settings = new ArrayList<IUserSetting>();
    IUserSetting setting = Mockito.mock( IUserSetting.class );
    settings.add( setting );
    Mockito.when( userSettingService.getUserSettings( user ) ).thenReturn( settings );
    Mockito.when( userSettingService.getGlobalUserSettings() ).thenReturn( settings );

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();

    for ( String roleName : roleList ) {
      authList.add( new GrantedAuthorityImpl( roleName ) );
    }
    GrantedAuthority[] authorities = authList.toArray( new GrantedAuthority[ 0 ] );
    UserDetails userDetails = new User( "testUser", "testPassword", true, true, true, true, authorities );
    Mockito.when( userDetailsService.loadUserByUsername( Mockito.anyString() ) ).thenReturn( userDetails );

    exporter.exportUsersAndRoles();

    Mockito.verify( manifest ).addUserExport( userCaptor.capture() );
    Mockito.verify( manifest ).addRoleExport( roleCaptor.capture() );
    Mockito.verify( userSettingService ).getGlobalUserSettings();
    Mockito.verify( manifest ).addGlobalUserSetting( Mockito.any( ExportManifestUserSetting.class ) );
    Assert.assertEquals( settings.size(), userCaptor.getValue().getUserSettings().size() );

    UserExport userExport = userCaptor.getValue();
    Assert.assertEquals( "testUser", userExport.getUsername() );
    RoleExport roleExport = roleCaptor.getValue();
    Assert.assertEquals( "testRole", roleExport.getRolename() );
  }

  @Test
  public void testExportMetadata_noModels() throws Exception {
    IMetadataDomainRepository mdr = Mockito.mock( IMetadataDomainRepository.class );
    exporter.setMetadataDomainRepository( mdr );

    exporter.exportMetadataModels();
    Assert.assertEquals( 0, exporter.getExportManifest().getMetadataList().size() );
  }

  @Test
  public void testExportMetadata() throws Exception {
    IMetadataDomainRepository mdr = Mockito.mock( IMetadataDomainRepository.class );

    Set<String> domainIds = new HashSet<String>();
    domainIds.add( "test1" );

    Mockito.when( mdr.getDomainIds() ).thenReturn( domainIds );
    exporterSpy.setMetadataDomainRepository( mdr );
    exporterSpy.zos = Mockito.mock( ZipOutputStream.class );

    Map<String, InputStream> inputMap = new HashMap<String, InputStream>();
    InputStream is = Mockito.mock( InputStream.class );
    Mockito.when( is.read( Mockito.any( ( new byte[] {} ).getClass() ) ) ).thenReturn( -1 );
    inputMap.put( "test1", is );

    Mockito.doReturn( inputMap ).when( exporterSpy ).getDomainFilesData( "test1" );

    exporterSpy.exportMetadataModels();
    Assert.assertEquals( 1, exporterSpy.getExportManifest().getMetadataList().size() );

    Assert.assertEquals( "test1", exporterSpy.getExportManifest().getMetadataList().get( 0 ).getDomainId() );
    Assert.assertEquals( PentahoPlatformExporter.METADATA_PATH_IN_ZIP + "test1.xmi",
      exporterSpy.getExportManifest().getMetadataList().get( 0 ).getFile() );
  }

  @Test
  public void testExportDatasources() throws Exception {

    IDatasourceMgmtService svc = Mockito.mock( IDatasourceMgmtService.class );
    exporterSpy.setDatasourceMgmtService( svc );

    List<IDatabaseConnection> datasources = new ArrayList<IDatabaseConnection>();
    IDatabaseConnection conn = Mockito.mock( DatabaseConnection.class );
    IDatabaseConnection icon = Mockito.mock( IDatabaseConnection.class );
    datasources.add( conn );
    datasources.add( icon );

    Mockito.when( svc.getDatasources() ).thenReturn( datasources );

    exporterSpy.exportDatasources();

    Assert.assertEquals( 1, exporterSpy.getExportManifest().getDatasourceList().size() );
    Assert.assertEquals( conn, exporterSpy.getExportManifest().getDatasourceList().get( 0 ) );
  }

  @Test
  public void testParseXmlaEnabled() throws Exception {
    String dsInfo = "DataSource=SampleData;Provider=mondrian;EnableXmla=\"false\"";
    Assert.assertFalse( exporterSpy.parseXmlaEnabled( dsInfo ) );

    dsInfo = "DataSource=SampleData;Provider=mondrian;EnableXmla=\"true\"";
    Assert.assertTrue( exporterSpy.parseXmlaEnabled( dsInfo ) );

    dsInfo = "DataSource=SampleData;Provider=mondrian";
    Assert.assertFalse( exporterSpy.parseXmlaEnabled( dsInfo ) );

    dsInfo = "DataSource=SampleData;EnableXmla=\"true\";Provider=mondrian";
    Assert.assertTrue( exporterSpy.parseXmlaEnabled( dsInfo ) );
  }

  @Test
  public void testExportMondrianSchemas_noCatalogs() throws Exception {
    PentahoSystem.registerObject( mondrianCatalogService );
    exporterSpy.setMondrianCatalogRepositoryHelper( mondrianCatalogRepositoryHelper );

    exporterSpy.exportMondrianSchemas();

    Mockito.verify( exportManifest, Mockito.never() ).addMondrian( Mockito.any( ExportManifestMondrian.class ) );
    Mockito.verify( mondrianCatalogRepositoryHelper, Mockito.never() ).getModrianSchemaFiles( Mockito.anyString() );
  }

  @Test
  public void testExportMondrianSchemas() throws Exception {
    final String catalogName = "test";
    String dataSourceInfo = "EnableXmla=\"true\"";

    executeExportMondrianSchemasForDataSourceInfo( catalogName, dataSourceInfo, true );

    Mockito.verify( exportManifest ).addMondrian( Mockito.any( ExportManifestMondrian.class ) );
    Mockito.verify( mondrianCatalogRepositoryHelper ).getModrianSchemaFiles( Mockito.anyString() );
    Assert.assertEquals( catalogName, exportManifest.getMondrianList().get( 0 ).getCatalogName() );
    Assert.assertTrue( exportManifest.getMondrianList().get( 0 ).isXmlaEnabled() );
    Mockito.verify( exporterSpy.zos ).putNextEntry( Mockito.any( ZipEntry.class ) );
  }

  @Test
  public void testExportMondrianSchemas_AdditionalParametersSaved() throws Exception {
    final String parameterName = "AdditionalParameter";
    final String parameterValue = "TestValue";
    final String expectedParameterValue = "TestValue";
    final String dataSourceInfo = "EnableXmla=\"true\";" + parameterName + "=" + parameterValue;
    String catalogName = "test";

    executeExportMondrianSchemasForDataSourceInfo( catalogName, dataSourceInfo, true );

    String returnedParameterValue = exportManifest.getMondrianList().get( 0 ).getParameters().get( parameterName );
    Assert.assertNotNull( returnedParameterValue );
    Assert.assertEquals( returnedParameterValue, expectedParameterValue );
  }

  @Test
  public void testPerformExportMondrianSchemas_XmlUnsafeDataSourceInfoSaved() throws IOException {

    final String dataSourceInfo = "DataSource=\"&quot;DS &quot;Test&apos;s&quot; &amp; &lt;Fun&gt;&quot;\";"
      + "DynamicSchemaProcessor=\"&quot;DSP&apos;s &amp; &quot;Other&quot; &lt;stuff&gt;&quot;\";";

    final String dataSourceExpectedValue = "\"DS \"Test's\" & <Fun>\"";
    final String dynamicSchemaProcessorExpectedValue = "\"DSP's & \"Other\" <stuff>\"";

    executeExportMondrianSchemasForDataSourceInfo( "test", dataSourceInfo, false );

    String returnedParameterValue = exportManifest.getMondrianList().get( 0 ).getParameters().get( "DataSource" );
    Assert.assertNotNull( returnedParameterValue );
    Assert.assertEquals( returnedParameterValue, dataSourceExpectedValue );

    returnedParameterValue = exportManifest.getMondrianList().get( 0 ).getParameters().get( "DynamicSchemaProcessor" );
    Assert.assertNotNull( returnedParameterValue );
    Assert.assertEquals( returnedParameterValue, dynamicSchemaProcessorExpectedValue );

  }

  private void executeExportMondrianSchemasForDataSourceInfo( String catalogName, String dataSourceInfo, boolean isXmlaEnabled ) throws IOException {
    PentahoSystem.registerObject( mondrianCatalogService );
    exporterSpy.setMondrianCatalogRepositoryHelper( mondrianCatalogRepositoryHelper );

    List<MondrianCatalog> catalogs = new ArrayList<MondrianCatalog>();
    MondrianCatalog catalog = new MondrianCatalog( catalogName, dataSourceInfo, null, null );
    catalogs.add( catalog );
    Mockito.when( mondrianCatalogService.listCatalogs( Mockito.any( IPentahoSession.class ), Mockito.anyBoolean() ) ).thenReturn( catalogs );
    Map<String, InputStream> inputMap = new HashMap<String, InputStream>();
    InputStream is = Mockito.mock( InputStream.class );
    Mockito.when( is.read( Mockito.any( ( new byte[] {} ).getClass() ) ) ).thenReturn( -1 );
    inputMap.put( "test", is );

    Mockito.when( mondrianCatalogRepositoryHelper.getModrianSchemaFiles( "test" ) ).thenReturn( inputMap );
    exporterSpy.zos = Mockito.mock( ZipOutputStream.class );

    exporterSpy.exportMondrianSchemas();

    Mockito.verify( exportManifest ).addMondrian( Mockito.any( ExportManifestMondrian.class ) );
    Mockito.verify( mondrianCatalogRepositoryHelper ).getModrianSchemaFiles( Mockito.anyString() );
    Assert.assertEquals( "test", exportManifest.getMondrianList().get( 0 ).getCatalogName() );
    if ( isXmlaEnabled ) {
      Assert.assertTrue( exportManifest.getMondrianList().get( 0 ).isXmlaEnabled() );
    }
    Mockito.verify( exporterSpy.zos ).putNextEntry( Mockito.any( ZipEntry.class ) );
  }

  @Test
  public void testExportMetaStore() throws Exception {
    exporterSpy.zos = Mockito.mock( ZipOutputStream.class );
    IMetaStore metastore = Mockito.mock( IMetaStore.class );
    exporterSpy.setRepoMetaStore( metastore );
    ExportManifest manifest = Mockito.mock( ExportManifest.class );
    exporterSpy.setExportManifest( manifest );

    exporterSpy.exportMetastore();
    Mockito.verify( exporterSpy.zos ).putNextEntry( Mockito.any( ZipEntry.class ) );
    Mockito.verify( manifest ).setMetaStore( Mockito.any( ExportManifestMetaStore.class ) );
  }

  @Test
  public void testIsExportCandidate() throws Exception {
    Assert.assertTrue( exporter.isExportCandidate( "/etc" ) );
    Assert.assertTrue( exporter.isExportCandidate( "/etc/operations_mart" ) );
    Assert.assertTrue( exporter.isExportCandidate( "/etc/operations_mart/someSubFolder" ) );

    Assert.assertTrue( exporter.isExportCandidate( "/public" ) );
    Assert.assertTrue( exporter.isExportCandidate( "/home" ) );

    Assert.assertFalse( exporter.isExportCandidate( "/etc/someRandomFolder" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/someRandomFolder/someSubFOlder" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/mondrian" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/metadata" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/metastore" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/olap-servers" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/models" ) );
    Assert.assertFalse( exporter.isExportCandidate( "/etc/pdi" ) );
  }
}
