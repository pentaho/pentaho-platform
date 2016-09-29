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

package org.pentaho.platform.plugin.services.exporter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
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
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    exporterSpy.setScheduler( scheduler );
    exporterSpy.setExportManifest( exportManifest );

    doReturn( "session name" ).when( session ).getName();
    exporter = new PentahoPlatformExporter( repo );
  }

  @After
  public void tearDown() {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testExportSchedules() throws Exception {
    List<Job> jobs = new ArrayList<>();
    ComplexJobTrigger trigger = mock( ComplexJobTrigger.class );
    JobTrigger unknownTrigger = mock( JobTrigger.class );

    Job job1 = mock( Job.class );
    Job job2 = mock( Job.class );
    Job job3 = mock( Job.class );
    jobs.add( job1 );
    jobs.add( job2 );
    jobs.add( job3 );

    when( scheduler.getJobs( null ) ).thenReturn( jobs );
    when( job1.getJobName() ).thenReturn( EmbeddedVersionCheckSystemListener.VERSION_CHECK_JOBNAME );
    when( job2.getJobName() ).thenReturn( "job 2" );
    when( job2.getJobTrigger() ).thenReturn( trigger );
    when( job3.getJobName() ).thenReturn( "job 3" );
    when( job3.getJobTrigger() ).thenReturn( unknownTrigger );

    exporterSpy.exportSchedules();

    verify( scheduler ).getJobs( null );
    assertEquals( 1, exporterSpy.getExportManifest().getScheduleList().size() );
  }

  @Test
  public void testExportSchedules_SchedulereThrowsException() throws Exception {
    when( scheduler.getJobs( null ) ).thenThrow( new SchedulerException( "bad" ) );

    exporterSpy.exportSchedules();

    verify( scheduler ).getJobs( null );
    assertEquals( 0, exporterSpy.getExportManifest().getScheduleList().size() );
  }

  @Test
  public void testExportUsersAndRoles() {
    IUserRoleDao mockDao = mock( IUserRoleDao.class );
    IAnyUserSettingService userSettingService = mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( mockDao );
    PentahoSystem.registerObject( userSettingService );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    String tenantPath = "path";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );

    List<IPentahoUser> userList = new ArrayList<IPentahoUser>();
    IPentahoUser user = new PentahoUser( "testUser" );
    IPentahoRole role = new PentahoRole( "testRole" );
    userList.add( user );
    when( mockDao.getUsers( any( ITenant.class ) ) ).thenReturn( userList );

    List<IPentahoRole> roleList = new ArrayList<IPentahoRole>();
    roleList.add( role );
    when( mockDao.getRoles() ).thenReturn( roleList );

    Map<String, List<String>> map = new HashMap<String, List<String>>();
    List<String> permissions = new ArrayList<String>();
    permissions.add( "read" );
    map.put( "testRole", permissions );
    RoleBindingStruct struct = mock( RoleBindingStruct.class );
    struct.bindingMap = map;
    when( roleBindingDao.getRoleBindingStruct( anyString() ) ).thenReturn( struct );

    ArgumentCaptor<UserExport> userCaptor = ArgumentCaptor.forClass( UserExport.class );
    ArgumentCaptor<RoleExport> roleCaptor = ArgumentCaptor.forClass( RoleExport.class );
    ExportManifest manifest = mock( ExportManifest.class );
    exporter.setExportManifest( manifest );

    List<IUserSetting> settings = new ArrayList<>();
    IUserSetting setting = mock( IUserSetting.class );
    settings.add( setting );
    when( userSettingService.getUserSettings( user.getUsername() ) ).thenReturn( settings );
    when( userSettingService.getGlobalUserSettings() ).thenReturn( settings );
    exporter.exportUsersAndRoles();

    verify( manifest ).addUserExport( userCaptor.capture() );
    verify( manifest ).addRoleExport( roleCaptor.capture() );
    verify( userSettingService ).getGlobalUserSettings();
    verify( manifest ).addGlobalUserSetting( any( ExportManifestUserSetting.class ) );
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
    when( is.read( any( ( new byte[] {} ).getClass() ) ) ).thenReturn( -1 );
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
    IDatabaseConnection conn = mock( DatabaseConnection.class );
    IDatabaseConnection icon = mock( IDatabaseConnection.class );
    datasources.add( conn );
    datasources.add( icon );

    when( svc.getDatasources() ).thenReturn( datasources );

    exporterSpy.exportDatasources();

    assertEquals( 1, exporterSpy.getExportManifest().getDatasourceList().size() );
    assertEquals( conn, exporterSpy.getExportManifest().getDatasourceList().get( 0 ) );
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

    verify( exportManifest, never() ).addMondrian( any( ExportManifestMondrian.class ) );
    verify( mondrianCatalogRepositoryHelper, never() ).getModrianSchemaFiles( anyString() );
  }

  @Test
  public void testExportMondrianSchemas() throws Exception {
    PentahoSystem.registerObject( mondrianCatalogService );
    exporterSpy.setMondrianCatalogRepositoryHelper( mondrianCatalogRepositoryHelper );

    List<MondrianCatalog> catalogs = new ArrayList<>();
    MondrianCatalog catalog = new MondrianCatalog( "test", "EnableXmla=\"true\"", null, null );
    catalogs.add( catalog );
    when( mondrianCatalogService.listCatalogs( any( IPentahoSession.class ), anyBoolean() ) ).thenReturn( catalogs );
    Map<String, InputStream> inputMap = new HashMap<>();
    InputStream is = mock( InputStream.class );
    when( is.read( any( ( new byte[] {} ).getClass() ) ) ).thenReturn( -1 );
    inputMap.put( "test", is );

    when( mondrianCatalogRepositoryHelper.getModrianSchemaFiles( "test" ) ).thenReturn( inputMap );
    exporterSpy.zos = mock( ZipOutputStream.class );

    exporterSpy.exportMondrianSchemas();

    verify( exportManifest ).addMondrian( any( ExportManifestMondrian.class ) );
    verify( mondrianCatalogRepositoryHelper ).getModrianSchemaFiles( anyString() );
    assertEquals( "test", exportManifest.getMondrianList().get( 0 ).getCatalogName() );
    assertTrue( exportManifest.getMondrianList().get( 0 ).isXmlaEnabled() );
    verify( exporterSpy.zos ).putNextEntry( any( ZipEntry.class ) );
  }

  @Test
  public void testExportMetaStore() throws Exception {
    exporterSpy.zos = mock( ZipOutputStream.class );
    IMetaStore metastore = mock( IMetaStore.class );
    exporterSpy.setRepoMetaStore( metastore );
    ExportManifest manifest = mock( ExportManifest.class );
    exporterSpy.setExportManifest( manifest );

    exporterSpy.exportMetastore();
    verify( exporterSpy.zos ).putNextEntry( any( ZipEntry.class ) );
    verify( manifest ).setMetaStore( any( ExportManifestMetaStore.class ) );
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
