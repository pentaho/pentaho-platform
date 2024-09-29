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

package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.channels.IllegalSelectorException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.PropertiesWrapper;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.api.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.SessionResource;
import org.pentaho.platform.web.http.api.resources.Setting;
import org.pentaho.platform.web.http.api.resources.StringListWrapper;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( FileUtils.class )
public class FileServiceIT {

  private static FileService fileService;
  private static final String COMMA = ",";
  private static final String FILE_1 = "file1";
  private static final String FILE_2 = "file2";
  private static final String FILE_3 = "file3";
  private static final String PARAMS = FILE_1 + COMMA + FILE_2;
  private static String USER_NAME = "user";
  private static String FOLDER_HOME = "home";
  private static String SEPARATOR = "/";
  private static String PATH_USER_HOME_FOLDER = SEPARATOR + FOLDER_HOME + SEPARATOR + USER_NAME;
  private static final String REAL_USER = "testUser";

  private static final String IMPORT_DIR = "/home/" + REAL_USER;

  private IPentahoObjectFactory pentahoObjectFactory;

  private IAuthorizationPolicy policy;

  private ITenantedPrincipleNameResolver resolver;

  private IPlatformImporter platformImporter;
  @Before
  public void setUp() throws ObjectFactoryException {
    fileService = spy( new FileService() );
    fileService.defaultUnifiedRepositoryWebService = mock( DefaultUnifiedRepositoryWebService.class );
    fileService.repository = mock( IUnifiedRepository.class );
    fileService.policy = mock( IAuthorizationPolicy.class );

    PentahoSystem.init();
    ITenant tenat = mock( ITenant.class );

    resolver = mock( ITenantedPrincipleNameResolver.class );
    doReturn( tenat ).when( resolver ).getTenant( nullable( String.class ) );
    doReturn( REAL_USER ).when( resolver ).getPrincipleName( nullable( String.class ) );
    policy = mock( IAuthorizationPolicy.class );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    platformImporter = mock( IPlatformImporter.class );
    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), nullable( String.class ), any( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            if ( invocation.getArguments()[0].equals( IAuthorizationPolicy.class ) ) {
              return policy;
            }
            if ( invocation.getArguments()[0].equals( ITenantedPrincipleNameResolver.class ) ) {
              return resolver;
            }
            if ( invocation.getArguments()[0].equals( IPlatformImporter.class ) ) {
              return platformImporter;
            }
            return null;
        } } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( userRoleListService );

    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( "sampleSession" ).when( session ).getName();
    PentahoSessionHolder.setSession( session );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.shutdown();
  }


  @After
  public void cleanup() {
    fileService = null;
  }

  @Test
  public void testDoDeleteFiles() throws Exception {

    fileService.doDeleteFiles( PARAMS );

    verify( fileService.getRepoWs(), times( 1 ) ).deleteFile( "file1", null );
    verify( fileService.getRepoWs(), times( 1 ) ).deleteFile( "file2", null );
  }

  @Test
  public void restoredFilesInTrashDeletedAfterRestoringInHomeDir_renameMode() throws Exception {
    FileService fileService = mock( FileService.class );
    mockSession( fileService, USER_NAME );
    when( fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_RENAME ) ).thenCallRealMethod();

    boolean restored = fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_RENAME );

    verify( fileService ).doDeleteFilesPermanent( PARAMS );
    assertEquals( restored, true );
  }

  @Test
  public void onlyNonConflictFilesDeletedAfterRestoringInHomeDir_noOverwriteMode() throws Exception {
    FileService fileService = mock( FileService.class );
    mockSession( fileService, USER_NAME );
    mockDoRestoreInHomeDir( fileService );

    final String filesToRestore = PARAMS;

    when( fileService.getSourceFileIdsThatNotConflictWithFolderFiles( PATH_USER_HOME_FOLDER, PARAMS ) )
      .thenCallRealMethod();
    when( fileService.getCommaSeparatedFileIds( anyListOf( String.class ) ) ).thenCallRealMethod();

    boolean result = fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_NO_OVERWRITE );

    verify( fileService ).doDeleteFilesPermanent( FILE_2 );
    assertEquals( result, true );
  }

  @Test
  public void filesOverwrittenWhenConflict_overwriteMode() throws Exception {
    FileService fileService = mock( FileService.class );
    mockSession( fileService, USER_NAME );
    mockDoRestoreInHomeDir( fileService );
    final String filesToRestore = PARAMS;

    when( fileService.getFolderFileIdsThatConflictWithSource( PATH_USER_HOME_FOLDER, filesToRestore ) )
      .thenCallRealMethod();
    when( fileService.getCommaSeparatedFileIds( anyListOf( String.class ) ) ).thenCallRealMethod();

    boolean result = fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_OVERWRITE );

    verify( fileService ).doMoveFiles( PATH_USER_HOME_FOLDER, filesToRestore );
    verify( fileService ).doDeleteFilesPermanent( FILE_1 );

    assertEquals( result, true );
  }


  @Test
  public void filesOverwrittenWhenNoConflict_overwriteMode() throws Exception {
    FileService fileService = mock( FileService.class );
    mockSession( fileService, USER_NAME );
    final String filesToRestore = PARAMS;

    when( fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_OVERWRITE ) ).thenCallRealMethod();
    when( fileService.getFolderFileIdsThatConflictWithSource( PATH_USER_HOME_FOLDER, filesToRestore ) )
      .thenReturn( "" );


    boolean result = fileService.doRestoreFilesInHomeDir( filesToRestore, FileService.MODE_OVERWRITE );

    verify( fileService ).doMoveFiles( PATH_USER_HOME_FOLDER, filesToRestore );

    assertEquals( result, true );
  }


  @Test
  public void noFilesOverwrittenWhenDeletingOfConflictFilesFailed_overwriteMode() throws Exception {
    FileService fileService = mock( FileService.class );
    mockSession( fileService, USER_NAME );
    when( fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_OVERWRITE ) ).thenCallRealMethod();
    when( fileService.getFolderFileIdsThatConflictWithSource( PATH_USER_HOME_FOLDER, PARAMS ) ).thenReturn( PARAMS );
    doThrow( new Exception() ).when( fileService ).doDeleteFilesPermanent( nullable( String.class ) );

    boolean result = fileService.doRestoreFilesInHomeDir( PARAMS, FileService.MODE_OVERWRITE );

    verify( fileService, never() ).doMoveFiles( PATH_USER_HOME_FOLDER, PARAMS );
    assertEquals( result, false );
  }

  @Test
  public void conflictWhenRestoreFileNameEqFolderFileName() throws Exception {
    FileService fileService = mock( FileService.class );
    mockDoRestoreInHomeDir( fileService );
    when( fileService.canRestoreToFolderWithNoConflicts( PATH_USER_HOME_FOLDER, PARAMS ) ).thenCallRealMethod();

    boolean result = fileService.canRestoreToFolderWithNoConflicts( PATH_USER_HOME_FOLDER, PARAMS );
    assertEquals( result, false );
  }

  public List<RepositoryFileDto> getMockedRepositoryFileDtoList( String[] fileNames ) {
    List<RepositoryFileDto> repoFileDtoList = new ArrayList<>();

    for ( String fileName : fileNames ) {
      RepositoryFileDto repoFileDto = mock( RepositoryFileDto.class );
      when( repoFileDto.getName() ).thenReturn( fileName );
      when( repoFileDto.getId() ).thenReturn( fileName );
      repoFileDtoList.add( repoFileDto );
    }

    return repoFileDtoList;
  }

  public RepositoryFile getMockedRepoFile( String fileName ) {
    RepositoryFile repoFileDto = mock( RepositoryFile.class );
    when( repoFileDto.getName() ).thenReturn( fileName );

    return repoFileDto;
  }

  public void mockDoRestoreInHomeDir( FileService fileService ) {
    IUnifiedRepository iUnifiedRepository = mock( IUnifiedRepository.class );

    List<RepositoryFileDto> homeFolderFiles = getMockedRepositoryFileDtoList( new String[] { FILE_1, FILE_3 } );
    when( fileService.doGetChildren( eq( PATH_USER_HOME_FOLDER ), nullable( String.class ), anyBoolean(), anyBoolean() ) )
      .thenReturn( homeFolderFiles );

    when( fileService.getRepository() ).thenReturn( iUnifiedRepository );
    when( fileService.doRestoreFilesInHomeDir( eq( PARAMS ), anyInt() ) ).thenCallRealMethod();
    RepositoryFile mockRepoFile1 = getMockedRepoFile( FILE_1 );
    RepositoryFile mockRepoFile2 = getMockedRepoFile( FILE_2 );
    when( iUnifiedRepository.getFileById( eq( FILE_1 ) ) ).thenReturn( mockRepoFile1 );
    when( iUnifiedRepository.getFileById( eq( FILE_2 ) ) ).thenReturn( mockRepoFile2 );
  }


  public void mockSession( FileService fileService, String userName ) {
    IPentahoSession mockSession = mock( IPentahoSession.class );
    when( mockSession.getName() ).thenReturn( userName );
    when( fileService.getSession() ).thenReturn( mockSession );
  }

  @Test
  public void testDoDeleteFilesException() {

    doThrow( new IllegalArgumentException() ).when(
      fileService.defaultUnifiedRepositoryWebService ).deleteFile( nullable( String.class ), nullable( String.class ) );

    try {
      fileService.doDeleteFiles( PARAMS );
      fail(); //This line should never be reached
    } catch ( IllegalArgumentException e ) {
      //Expected exception
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void doCopyFilesException() throws Exception {

    String destinationPathColon = ":path:to:destination";

    String fileId1 = "file1";

    String fileId2 = "file2";

    when( fileService.policy.isAllowed( nullable( String.class ) ) ).thenReturn( false );

    try {
      fileService.doCopyFiles( destinationPathColon, 1, fileId1 + "," + fileId2 );
      fail();
    } catch ( IllegalArgumentException e ) {
      //Should catch the exception
    }
  }

  @Test
  public void testDoGetFileOrDir() throws Exception {
    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( "file.txt" ).when( file ).getName();

    when( fileService.repository.getFile( nullable( String.class ) ) ).thenReturn( file );

    RepositoryFileInputStream mockInputStream = mock( RepositoryFileInputStream.class );

    doReturn( 1 ).when( fileService ).copy( any( java.io.InputStream.class ), any( java.io.OutputStream.class ) );
    doReturn( mockInputStream ).when( fileService ).getRepositoryFileInputStream( any( RepositoryFile.class ) );

    String pathId = "/usr/folder/file.txt";
    FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileOrDir( pathId );

    assertEquals( "file.txt", wrapper.getRepositoryFile().getName() );
  }

  @Test
  public void testDoGetFileOrDirException() throws Exception {
    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( "file.txt" ).when( file ).getName();

    RepositoryFileInputStream mockInputStream = mock( RepositoryFileInputStream.class );

    doReturn( 1 ).when( fileService ).copy( any( java.io.InputStream.class ), any( java.io.OutputStream.class ) );
    doReturn( mockInputStream ).when( fileService ).getRepositoryFileInputStream( any( RepositoryFile.class ) );

    String pathId = "/usr/folder/file.txt";
    try {
      fileService.doGetFileOrDir( pathId );
      fail(); //This line should never be reached
    } catch ( FileNotFoundException fileNotFound ) {
      //Expected exception
    }
  }

  @Test
  public void testSetFileAcls() throws Exception {
    RepositoryFileDto file = mock( RepositoryFileDto.class );
    doReturn( "file.txt" ).when( file ).getName();
    when( fileService.defaultUnifiedRepositoryWebService.getFile( nullable( String.class ) ) ).thenReturn( file );

    String pathId = "/usr/folder/file.txt";
    RepositoryFileAclDto acl = mock( RepositoryFileAclDto.class );
    fileService.setFileAcls( pathId, acl );

    verify( acl, times( 1 ) ).setId( nullable( String.class ) );
    verify( file, times( 1 ) ).getId();
    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).updateAcl( acl );
  }

  @Test
  public void testSetFileAclsException() throws Exception {
    String pathId = "/usr/folder/file.txt";
    RepositoryFileAclDto acl = mock( RepositoryFileAclDto.class );
    try {
      fileService.setFileAcls( pathId, acl );
      fail();
    } catch ( FileNotFoundException e ) {
      //expected exception
    }
  }

  @Test
  public void testDoGetRootProperties() {
    fileService.doGetRootProperties();

    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).
      getFile( FileUtils.PATH_SEPARATOR );
  }

  @Test
  public void testDoGetProperties() throws Exception {
    RepositoryFileDto file = mock( RepositoryFileDto.class );
    when( fileService.defaultUnifiedRepositoryWebService.getFile( nullable( String.class ) ) ).thenReturn( file );

    String pathId = "/usr/folder/file.txt";
    fileService.doGetProperties( pathId );

    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).getFile( nullable( String.class ) );
  }

  @Test
  public void testDoGetPropertiesException() throws Exception {
    String pathId = "/usr/folder/file.txt";
    try {
      fileService.doGetProperties( pathId );
      fail();
    } catch ( FileNotFoundException fileNotFound ) {
      //expected exception
    }
  }

  @Test
  public void testDoCreateFile() throws Exception {
    RepositoryFileOutputStream mockOutputStream = mock( RepositoryFileOutputStream.class );
    doReturn( mockOutputStream ).when( fileService ).getRepositoryFileOutputStream( nullable( String.class ) );

    InputStream mockInputStream = mock( InputStream.class );

    doReturn( 1 ).when( fileService ).copy( mockInputStream, mockOutputStream );

    String charsetName = "test";
    fileService.createFile( charsetName, "testString", mockInputStream );

    verify( mockOutputStream, times( 1 ) ).setCharsetName( eq( charsetName ) );
    verify( mockOutputStream, times( 1 ) ).close();
    verify( mockInputStream, times( 1 ) ).close();
  }

  @Test
  public void testDoCreateFileException() {
    doThrow( new IllegalArgumentException() ).when( fileService ).idToPath( nullable( String.class ) );

    try {
      fileService.createFile( null, null, null );
      fail();
    } catch ( IllegalArgumentException e ) {
      // expected
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testDoDeleteFilesPermanent() throws Exception {

    fileService.doDeleteFilesPermanent( PARAMS );

    verify( fileService.getRepoWs(), times( 1 ) ).deleteFileWithPermanentFlag( "file1", true, null );
    verify( fileService.getRepoWs(), times( 1 ) ).deleteFileWithPermanentFlag( "file2", true, null );
  }

  @Test
  public void testDoDeleteLocale() throws Exception {
    RepositoryFileDto file = mock( RepositoryFileDto.class );
    doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );
    doReturn( "file.txt" ).when( file ).getId();
    fileService.doDeleteLocale( file.getId(), "en_US" );
    verify( fileService.getRepoWs(), times( 1 ) ).deleteLocalePropertiesForFile( "file.txt", "en_US" );
  }

  @Test
  public void testDoGetCanAccessList() {
    String permissions = RepositoryFilePermission.READ.ordinal() + "|" + RepositoryFilePermission.WRITE.ordinal() + "|"
      + RepositoryFilePermission.DELETE.ordinal();
    doReturn( true ).when( fileService.repository ).hasAccess( nullable( String.class ), any( EnumSet.class ) );
    List<Setting> settings = fileService.doGetCanAccessList( "pathId", permissions );
    assertTrue( settings.size() > 0 );
  }

  @Test
  public void testDoGetPathsAccessList() {
    List<String> paths = new ArrayList<String>();
    paths.add( "path1" );
    paths.add( "path2" );
    paths.add( "path3" );

    doReturn( true ).when( fileService.repository ).hasAccess( nullable( String.class ), any( EnumSet.class ) );
    List<Setting> settings = fileService.doGetPathsAccessList( new StringListWrapper( paths ) );
    assertTrue( settings.size() > 0 );

    doReturn( false ).when( fileService.repository ).hasAccess( nullable( String.class ), any( EnumSet.class ) );
    settings = fileService.doGetPathsAccessList( new StringListWrapper( paths ) );
    assertEquals( 0, settings.size() );
  }

  @Test
  public void testDoDeleteFilesPermanentException() {

    doThrow( new IllegalArgumentException() ).when(
      fileService.defaultUnifiedRepositoryWebService ).deleteFileWithPermanentFlag( nullable( String.class ), eq( true ),
      nullable( String.class ) );

    try {
      fileService.doDeleteFilesPermanent( PARAMS );
      fail(); //This line should never be reached
    } catch ( Exception e ) {
      //Expected exception
    }
  }

  @Test
  public void testDoMoveFiles() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );

    fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );

    verify( fileService.getRepoWs(), times( 1 ) ).moveFile( params[ 0 ], destPathId, null );
    verify( fileService.getRepoWs(), times( 1 ) ).moveFile( params[ 1 ], destPathId, null );
  }

  @Test
  public void testDoMoveFilesForUnknownDestPath() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );

    try {
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      Assert.assertTrue( false );
    } catch ( FileNotFoundException e ) {
      verify( fileService.getRepoWs(), times( 0 ) ).moveFile( params[ 0 ], destPathId, null );
      verify( fileService.getRepoWs(), times( 0 ) ).moveFile( params[ 1 ], destPathId, null );
    }
  }

  @Test
  public void testDoMoveFilesException() throws Exception {
    String destPathId = "/test";
    String[] params = { "file1", "file2" };

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( destPathId ).when( repositoryFileDto ).getPath();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( destPathId );
    doThrow( new InternalError() ).when( fileService.defaultUnifiedRepositoryWebService ).moveFile(
      params[ 0 ], destPathId, null );

    try {
      fileService.doMoveFiles( destPathId, StringUtils.join( params, "," ) );
      fail(); //This line should never be reached
    } catch ( Throwable e ) {
      verify( fileService.getRepoWs(), times( 1 ) ).moveFile( params[ 0 ], destPathId, null );
      verify( fileService.getRepoWs(), times( 0 ) ).moveFile( params[ 1 ], destPathId, null );
    }
  }

  @Test
  public void testSystemRestore() throws Exception {
    InputStream inputStreamMock = mock( InputStream.class );
    IAuthorizationPolicy authorizationPolicy = mock( IAuthorizationPolicy.class );
    IRepositoryImportLogger iRepositoryImportLogger = mock( IRepositoryImportLogger.class );
    doReturn( authorizationPolicy ).when( fileService ).getPolicy();

    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    doReturn( iRepositoryImportLogger ).when( platformImporter ).getRepositoryImportLogger();

    fileService.systemRestore( inputStreamMock, "true", "false", "true" );

    verify( fileService ).doCanAdminister();
    verify( iRepositoryImportLogger ).startJob( any(), nullable( String.class ), any() );
    verify( iRepositoryImportLogger ).endJob();

    ArgumentCaptor<RepositoryFileImportBundle> argumentCaptor = ArgumentCaptor.forClass( RepositoryFileImportBundle.class );
    verify( platformImporter ).importFile( argumentCaptor.capture() );

    RepositoryFileImportBundle bundle = argumentCaptor.getValue();

    assertTrue( bundle.getInputStream() == inputStreamMock );
    assertEquals( "UTF-8", bundle.getCharSet() );
    assertEquals( RepositoryFile.HIDDEN_BY_DEFAULT, bundle.isHidden() );
    assertEquals( RepositoryFile.SCHEDULABLE_BY_DEFAULT, bundle.isSchedulable() );
    assertEquals( "/", bundle.getPath() );
    assertEquals( true, bundle.overwriteInRepository() );
    assertEquals( "SystemBackup.zip", bundle.getName() );
    assertFalse( bundle.isApplyAclSettings() );
    assertTrue( bundle.isRetainOwnership() );
    assertTrue( bundle.isOverwriteAclSettings() );
    assertTrue( bundle.isPreserveDsw() );

    ImportSession session = ImportSession.getSession();

    assertFalse( session.isApplyAclSettings() );
    assertTrue( session.isRetainOwnership() );
    assertTrue( session.isOverwriteAclSettings() );

  }

  @Test
  public void testDoRestoreFiles() throws Exception {
    String[] params = { "file1", "file2" };

    fileService.doRestoreFiles( StringUtils.join( params, "," ) );

    verify( fileService.getRepoWs(), times( 1 ) ).undeleteFile( params[ 0 ], null );
    verify( fileService.getRepoWs(), times( 1 ) ).undeleteFile( params[ 1 ], null );
  }

  @Test
  public void testDoRestoreFilesException() throws Exception {
    String[] params = { "file1", "file2" };

    doThrow( new InternalError() ).when( fileService.defaultUnifiedRepositoryWebService ).undeleteFile(
      params[ 0 ], null );

    try {
      fileService.doRestoreFiles( StringUtils.join( params, "," ) );
      fail(); //This line should never be reached
    } catch ( InternalError e ) {
      verify( fileService.getRepoWs(), times( 1 ) ).undeleteFile( params[ 0 ], null );
      verify( fileService.getRepoWs(), times( 0 ) ).undeleteFile( params[ 1 ], null );
    }
  }

  @Test
  public void testDoGetFileAsInline() throws FileNotFoundException {
    /*
     * TEST 1
     */
    doReturn( true ).when( fileService ).isPath( nullable( String.class ) );
    doReturn( true ).when( fileService ).isPathValid( nullable( String.class ) );

    RepositoryDownloadWhitelist mockWhiteList = mock( RepositoryDownloadWhitelist.class );
    doReturn( mockWhiteList ).when( fileService ).getWhitelist();
    doReturn( true ).when( mockWhiteList ).accept( nullable( String.class ) );

    RepositoryFile mockRepoFile = mock( RepositoryFile.class );
    doReturn( mockRepoFile ).when( fileService.repository ).getFile( nullable( String.class ) );

    SimpleRepositoryFileData mockData = mock( SimpleRepositoryFileData.class );
    doReturn( mockData ).when( fileService.repository ).getDataForRead( any( Serializable.class ), any( Class.class ) );

    InputStream mockInputStream = mock( InputStream.class );
    doReturn( mockInputStream ).when( mockData ).getInputStream();

    StreamingOutput mockStreamingOutput = mock( StreamingOutput.class );
    doReturn( mockStreamingOutput ).when( fileService ).getStreamingOutput( mockInputStream );

    FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileAsInline( "test" );

    verify( fileService.repository, times( 1 ) ).getFile( nullable( String.class ) );
    verify( mockWhiteList, times( 1 ) ).accept( nullable( String.class ) );
    verify( fileService, times( 2 ) ).getRepository();
    verify( fileService.repository, times( 1 ) ).getDataForRead( any( Serializable.class ), any( Class.class ) );
    verify( mockData, times( 1 ) ).getInputStream();

    assertEquals( mockRepoFile, wrapper.getRepositoryFile() );
    assertEquals( mockStreamingOutput, wrapper.getOutputStream() );

    /*
     * TEST 2
     */
    doReturn( false ).when( fileService ).isPath( nullable( String.class ) );
    doReturn( mockRepoFile ).when( fileService.repository ).getFileById( nullable( String.class ) );

    wrapper = fileService.doGetFileAsInline( "test" );

    verify( fileService.repository, times( 1 ) ).getFileById( nullable( String.class ) );
    verify( fileService, times( 4 ) ).getRepository();

    assertEquals( mockRepoFile, wrapper.getRepositoryFile() );
    assertEquals( mockStreamingOutput, wrapper.getOutputStream() );
  }

  @Test
  public void testDoGetFileAsInlineException() {

    /*
     * TEST 1
     */
    doReturn( true ).when( fileService ).isPath( nullable( String.class ) );
    doReturn( false ).when( fileService ).isPathValid( nullable( String.class ) );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( IllegalArgumentException e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      fail();
    }

    /*
     * TEST 2
     */
    doReturn( true ).when( fileService ).isPathValid( nullable( String.class ) );
    doReturn( null ).when( fileService.repository ).getFile( nullable( String.class ) );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( FileNotFoundException e ) {
      // Expected
    }

    /*
     * TEST 3
     */
    RepositoryFile mockFile = mock( RepositoryFile.class );
    doReturn( mockFile ).when( fileService.repository ).getFile( nullable( String.class ) );

    RepositoryDownloadWhitelist mockWhiteList = mock( RepositoryDownloadWhitelist.class );
    doReturn( mockWhiteList ).when( fileService ).getWhitelist();
    doReturn( false ).when( mockWhiteList ).accept( nullable( String.class ) );

    IAuthorizationPolicy mockPolicy = mock( IAuthorizationPolicy.class );
    doReturn( mockPolicy ).when( fileService ).getPolicy();
    doReturn( false ).when( mockPolicy ).isAllowed( nullable( String.class ) );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( IllegalArgumentException e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      fail();
    }

    /*
     * TEST 4
     */
    doReturn( true ).when( mockWhiteList ).accept( nullable( String.class ) );
    doThrow( new InternalError() ).when( fileService.repository )
      .getDataForRead( any( Serializable.class ), any( Class.class ) );

    try {
      fileService.doGetFileAsInline( "test" );
      fail();
    } catch ( InternalError e ) {
      // Excpected
    } catch ( FileNotFoundException e ) {
      fail();
    }
  }

  @Test
  public void testDoGetLocalProperties() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String locale = "";

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    Set<String> propertiesList = new HashSet<String>();
    propertiesList.add( "prop1" );
    propertiesList.add( "prop2" );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( fileId ).when( repositoryFileDto ).getId();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    Properties properties = mock( Properties.class );
    doReturn( "value1" ).when( properties ).getProperty( "prop1" );
    doReturn( "value2" ).when( properties ).getProperty( "prop2" );
    doReturn( false ).when( properties ).isEmpty();
    doReturn( propertiesList ).when( properties ).stringPropertyNames();

    PropertiesWrapper propertiesWrapper = mock( PropertiesWrapper.class );
    when( propertiesWrapper.getProperties() ).thenReturn( properties );

    doReturn( propertiesWrapper ).when( fileService.defaultUnifiedRepositoryWebService )
      .getLocalePropertiesForFileById( nullable( String.class ), nullable( String.class ) );

    List<StringKeyStringValueDto> keyValueList = fileService.doGetLocaleProperties( pathId, locale );

    verify( fileService.defaultUnifiedRepositoryWebService ).getFile( "/path/to/file/file1.ext" );
    verify( properties ).getProperty( "prop1" );
    verify( properties ).getProperty( "prop2" );
    verify( properties ).isEmpty();
    verify( properties ).stringPropertyNames();
    verify( fileService.defaultUnifiedRepositoryWebService ).getLocalePropertiesForFileById( nullable( String.class ), nullable( String.class ) );

    assertEquals( 2, keyValueList.size() );
    assertEquals( "prop1", keyValueList.get( 1 ).getKey() );
    assertEquals( "prop2", keyValueList.get( 0 ).getKey() );
    assertEquals( "value1", keyValueList.get( 1 ).getValue() );
    assertEquals( "value2", keyValueList.get( 0 ).getValue() );
  }

  @Test
  public void testDoSetLocalProperties() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String locale = "";

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( fileId ).when( repositoryFileDto ).getId();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    Properties fileProperties = mock( Properties.class );
    doReturn( false ).when( fileProperties ).isEmpty();

    List<StringKeyStringValueDto> properties = new ArrayList<StringKeyStringValueDto>();
    properties.add( new StringKeyStringValueDto( "key1", "value1" ) );
    properties.add( new StringKeyStringValueDto( "key2", "value2" ) );

    fileService.doSetLocaleProperties( pathId, locale, properties );

    verify( fileService.defaultUnifiedRepositoryWebService ).getFile( "/path/to/file/file1.ext" );
    verify( fileService.defaultUnifiedRepositoryWebService )
      .setLocalePropertiesForFileByFileId( nullable( String.class ), nullable( String.class ), any( Properties.class ) );
  }

  @Test
  public void testDoGetCanAccess() throws Exception {
    String pathId = "path:to:file:file1.ext";
    String permissions = "0|1|2|3|4";

    List<Integer> permissionsList = new ArrayList<Integer>();
    permissionsList.add( 0 );
    permissionsList.add( 1 );
    permissionsList.add( 2 );
    permissionsList.add( 3 );
    permissionsList.add( 4 );

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );
    doReturn( true ).when( fileService.defaultUnifiedRepositoryWebService ).hasAccess( nullable( String.class ), anyList() );

    String hasAccess = fileService.doGetCanAccess( pathId, permissions );

    verify( fileService.defaultUnifiedRepositoryWebService ).hasAccess( "/path/to/file/file1.ext", permissionsList );

    assertEquals( "true", hasAccess );
  }

  @Test
  public void testDoSetContentCreator() throws Exception {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = mock( Map.class );

    String idToPathResult = "/file1";
    doReturn( param ).when( repositoryFileDto ).getId();
    when( fileService.idToPath( param ) ).thenReturn( idToPathResult );
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFile( eq( idToPathResult ) );
    when( fileService.getRepository().getFileMetadata( repositoryFileDto.getId() ) ).thenReturn( fileMetadata );

    try {
      fileService.doSetContentCreator( param, repositoryFileDto );
      verify( fileService.getRepository(), times( 1 ) ).getFileMetadata( repositoryFileDto.getId() );
      verify( fileService.getRepository(), times( 1 ) ).setFileMetadata( param, fileMetadata );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      fail();
    }
  }

  @Test
  public void testDoSetContentCreatorFileNotFoundException() throws Exception {
    String param = "file1";
    RepositoryFileDto mockFileDto = mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = mock( Map.class );

    when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/file1" );

    try {
      fileService.doSetContentCreator( param, mockFileDto );
      fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testDoSetContentCreatorFileException() throws Exception {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    Map<String, Serializable> fileMetadata = mock( Map.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    when( fileService.idToPath( param ) ).thenReturn( "/file1" );
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/file1" );
    when( fileService.getRepository().getFileMetadata( repositoryFileDto.getId() ) ).thenThrow( new InternalError() );

    //failing in get
    try {
      fileService.doSetContentCreator( param, repositoryFileDto );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      verify( fileMetadata, times( 0 ) ).put( PentahoJcrConstants.PHO_CONTENTCREATOR, param );
      verify( fileService.repository, times( 0 ) ).setFileMetadata( param, fileMetadata );
    }
  }

  @Test
  public void testDoGetFileLocales() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    List<PentahoLocale> locales = new ArrayList<PentahoLocale>();
    PentahoLocale mockedLocale = mock( PentahoLocale.class );
    locales.add( mockedLocale );

    doReturn( param ).when( repositoryFileDto ).getId();
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );
    when( fileService.defaultUnifiedRepositoryWebService.getAvailableLocalesForFileById( repositoryFileDto.getId() ) )
      .thenReturn( locales );

    try {
      fileService.doGetFileLocales( param );
      verify( fileService.getRepository(), times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      fail();
    }
  }

  @Test
  public void testDoGetFileLocalesFileNotFoundException() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );

    try {
      fileService.doGetFileLocales( param );
      fail();
    } catch ( FileNotFoundException e ) {
      verify( fileService.getRepository(), times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    }
  }

  @Test
  public void testDoGetFileLocalesException() {
    String param = "file1";
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );

    doReturn( param ).when( repositoryFileDto ).getId();
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( "/" + param );
    when( fileService.defaultUnifiedRepositoryWebService.getAvailableLocalesForFileById( repositoryFileDto.getId() ) )
      .thenThrow( new InternalError() );

    try {
      fileService.doGetFileLocales( param );
    } catch ( FileNotFoundException e ) {
      fail();
    } catch ( InternalError e ) {
      verify( fileService.getRepository(), times( 0 ) ).getAvailableLocalesForFileById( repositoryFileDto.getId() );
    }
  }

  @Test
  public void testDoGetReservedChars() throws Exception {

    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append( '/' );
    stringBuffer.append( '\\' );
    stringBuffer.append( '\t' );
    stringBuffer.append( '\r' );
    stringBuffer.append( '\n' );

    List<Character> characters = new ArrayList<Character>();
    characters.add( '/' );
    characters.add( '\\' );
    characters.add( '\t' );
    characters.add( '\r' );
    characters.add( '\n' );

    doReturn( characters ).when( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();

    StringBuffer buffer = fileService.doGetReservedChars();

    verify( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();

    assertEquals( stringBuffer.toString(), buffer.toString() );
  }

  @Test
  public void testDoGetReservedCharactersDisplay() throws Exception {
    String stringBuffer = "/,\\,\\t,\\r,\\n";

    List<Character> characters = new ArrayList<Character>();
    characters.add( '/' );
    characters.add( '\\' );
    characters.add( '\t' );
    characters.add( '\r' );
    characters.add( '\n' );

    doReturn( "\\t" ).when( fileService ).escapeJava( "" + characters.get( 2 ) );
    doReturn( "\\r" ).when( fileService ).escapeJava( "" + characters.get( 3 ) );
    doReturn( "\\n" ).when( fileService ).escapeJava( "" + characters.get( 4 ) );

    doReturn( characters ).when( fileService.defaultUnifiedRepositoryWebService ).getReservedChars();
    StringBuffer buffer = fileService.doGetReservedCharactersDisplay();
    assertEquals( buffer.toString(), stringBuffer.toString() );

    verify( fileService, times( 3 ) ).escapeJava( nullable( String.class ) );
  }

  @Test
  public void testDoGetFileOrDirAsDownload0() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mockFileName.prpt", "true", "mockFileName.prpt.zip", "mockFileName.prpt.zip" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload1() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mockFileName.prpt", "false", "mockFileName.prpt", "mockFileName.prpt" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload2() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mock File+Name(%25).prpt", "true", "mock%20File%2BName%28%2525%29.prpt.zip",
      "mock File+Name(%25).prpt.zip" );
  }

  @Test
  public void testDoGetFileOrDirAsDownload3() throws Throwable {
    assertDoGetFileOrDirAsDownload( "mock File+Name(%25).prpt", "false", "mock%20File%2BName%28%2525%29.prpt",
      "mock File+Name(%25).prpt" );
  }

  /**
   * @param fileName
   * @param withManifest
   * @param expectedEncodedFileName
   * @param expectedFileName
   * @throws Throwable
   */
  public void assertDoGetFileOrDirAsDownload( final String fileName, final String withManifest,
                                              final String expectedEncodedFileName, final String expectedFileName )
    throws Throwable {

    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );

    /* register  mockAuthPolicy with PentahoSystem so SystemUtils can use it */
    PentahoSystem.registerObject( mockAuthPolicy );

    when( mockAuthPolicy.isAllowed( nullable( String.class ) ) ).thenReturn( true );

    BaseExportProcessor mockExportProcessor = mock( BaseExportProcessor.class );
    File mockExportFile = mock( File.class );
    ExportHandler mockExportHandler = mock( ExportHandler.class );
    StreamingOutput mockStream = mock( StreamingOutput.class );

    RepositoryFile mockRepoFile = mock( RepositoryFile.class );
    doReturn( fileName ).when( mockRepoFile ).getName();
    doReturn( mockExportFile ).when( mockExportProcessor ).performExport( mockRepoFile );

    doReturn( mockRepoFile ).when( fileService.repository ).getFile( nullable( String.class ) );
    doReturn( mockAuthPolicy ).when( fileService ).getPolicy();
    doReturn( mockExportProcessor ).when( fileService ).getDownloadExportProcessor( nullable( String.class ), anyBoolean(),
      anyBoolean() );
    doReturn( mockExportHandler ).when( fileService ).getDownloadExportHandler();
    doReturn( mockStream ).when( fileService ).getDownloadStream( mockRepoFile, mockExportProcessor );

    FileService.DownloadFileWrapper wrapper =
      fileService.doGetFileOrDirAsDownload( "", "mock:path:" + fileName, withManifest );

    verify( fileService.repository, times( 1 ) ).getFile( nullable( String.class ) );
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass( String.class );
    verify( mockAuthPolicy, times( 3 ) ).isAllowed( captor.capture() );
    assertTrue( captor.getAllValues().contains( RepositoryReadAction.NAME ) );
    assertTrue( captor.getAllValues().contains( RepositoryCreateAction.NAME ) );

    assertEquals( mockStream, wrapper.getOutputStream() );
    assertEquals( expectedEncodedFileName, wrapper.getEncodedFileName() );
    assertEquals( "attachment; filename=\"" + expectedFileName + "\"", wrapper.getAttachment() );
  }

  @Test
  public void testDoGetFileOrDirAsDownloadNonAdminUserHomeFolder() throws Throwable {

    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME ); /* user has 'Read Content' */
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME ); /* user has 'Create Content' */

    /* non-admin user */
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );

    doReturn( mockAuthPolicy ).when( fileService ).getPolicy();

    /* register  mockAuthPolicy with PentahoSystem so SystemUtils can use it */
    PentahoSystem.registerObject( mockAuthPolicy );

    // Test 1: in the home-folder
    try {
      fileService.doGetFileOrDirAsDownload( "", "home:testUser:test_file", "true" );
      fail();
    } catch ( FileNotFoundException ex ) {
      /* expected; this is a mock test, we don't actually have a 'test_file' to download :) */
    } catch ( Throwable t ) {
      fail();
    }

    // Test 2: in some home-folder sub-folders
    try {
      fileService.doGetFileOrDirAsDownload( "", "home:testUser:subFolder1:subFolder2:test_file", "true" );
      fail();
    } catch ( FileNotFoundException ex ) {
      /* expected; this is a mock test, we don't actually have a 'test_file' to download :) */
    } catch ( Throwable t ) {
      fail();
    }

    // Test 3: while still being on the user's home folder, user loses 'Read Content' permission
    try {
      doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
      fileService.doGetFileOrDirAsDownload( "", "home:testUser:test_file", "true" );
      fail();
    } catch ( PentahoAccessControlException e ) {
      /* expected */
    } catch ( Throwable t ) {
      fail();
    } finally {
      doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    }

    // Test 4: while still being on the user's home folder, user loses 'Create Content' permission
    try {
      doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
      fileService.doGetFileOrDirAsDownload( "", "home:testUser:test_file", "true" );
      fail();
    } catch ( PentahoAccessControlException e ) {
      /* expected */
    } catch ( Throwable t ) {
      fail();
    } finally {
      doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    }
  }

  @Test
  public void testDoGetFileOrDirAsDownloadException() {
    // Test 1
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( nullable( String.class ) );
    doReturn( mockAuthPolicy ).when( fileService ).getPolicy();

    /* register  mockAuthPolicy with PentahoSystem so SystemUtils can use it */
    PentahoSystem.registerObject( mockAuthPolicy );

    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      fail();
    } catch ( PentahoAccessControlException e ) {
      // Expected
    } catch ( Throwable t ) {
      fail();
    }

    // Test 2
    doReturn( true ).when( mockAuthPolicy ).isAllowed( nullable( String.class ) );
    try {
      fileService.doGetFileOrDirAsDownload( "", "", "true" );
      fail();
    } catch ( InvalidParameterException e ) {
      // Expected
    } catch ( Throwable e ) {
      fail();
    }

    // Test 3
    doReturn( false ).when( fileService ).isPathValid( nullable( String.class ) );
    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      fail();
    } catch ( IllegalSelectorException e ) {
      // Expected
    } catch ( Throwable t ) {
      fail();
    }

    /*
     * Test 4
     */
    doReturn( true ).when( fileService ).isPathValid( nullable( String.class ) );
    doReturn( null ).when( fileService.repository ).getFile( nullable( String.class ) );
    try {
      fileService.doGetFileOrDirAsDownload( "", "mock:path:fileName", "true" );
      fail();
    } catch ( FileNotFoundException e ) {
      // Expected
    } catch ( Throwable t ) {
    }
  }

  public void testDoCanAdminister() throws Exception {
    IAuthorizationPolicy authorizationPolicy = mock( IAuthorizationPolicy.class );
    doReturn( authorizationPolicy ).when( fileService ).getPolicy();

    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertTrue( fileService.doCanAdminister() );

    doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertFalse( fileService.doCanAdminister() );

    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertFalse( fileService.doCanAdminister() );

    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertFalse( fileService.doCanAdminister() );

    doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );
    assertFalse( fileService.doCanAdminister() );
  }

  @Test
  public void testDoCanAdministerException() throws Exception {
    IAuthorizationPolicy authorizationPolicy = mock( IAuthorizationPolicy.class );
    doReturn( authorizationPolicy ).when( fileService ).getPolicy();

    doThrow( new InternalError() ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    try {
      assertFalse( fileService.doCanAdminister() );
      fail();
    } catch ( InternalError e ) {
    }

    doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doThrow( new InternalError() ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    try {
      assertFalse( fileService.doCanAdminister() );
    } catch ( InternalError e ) { //the first comparison fail and the result should be false and no exception returned
      fail();
    }

    doReturn( true ).when( authorizationPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( authorizationPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doThrow( new InternalError() ).when( authorizationPolicy ).isAllowed( AdministerSecurityAction.NAME );

    try {
      assertFalse( fileService.doCanAdminister() );
    } catch ( InternalError e ) { //the second comparison fail and the result should be false and no exception returned
      fail();
    }
  }

  @Test
  public void testDoGetCanCreate() {
    doReturn( true ).when( fileService.policy ).isAllowed( nullable( String.class ) );

    String canCreate = fileService.doGetCanCreate();

    verify( fileService.policy ).isAllowed( nullable( String.class ) );

    assertEquals( "true", canCreate );
  }

  @Test
  public void doGetContentCreator() {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String creatorId = "creatorId";

    Map<String, Serializable> fileMetadata = mock( HashMap.class );
    doReturn( creatorId ).when( fileMetadata ).get( "contentCreator" );

    doReturn( fileMetadata ).when( fileService.repository ).getFileMetadata( fileId );

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( fileId ).when( repositoryFileDto ).getId();

    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    RepositoryFileDto repositoryFileDto1 = mock( RepositoryFileDto.class );

    doReturn( repositoryFileDto1 ).when( fileService.defaultUnifiedRepositoryWebService ).getFileById( creatorId );


    // Test 1
    RepositoryFileDto repositoryFileDto2 = null;
    try {
      repositoryFileDto2 = fileService.doGetContentCreator( pathId );
    } catch ( Exception e ) {
      fail();
    }

    assertEquals( repositoryFileDto1, repositoryFileDto2 );

    // Test 2
    doReturn( null ).when( fileMetadata ).get( "contentCreator" );

    try {
      repositoryFileDto2 = fileService.doGetContentCreator( pathId );
      assertEquals( null, repositoryFileDto2 );
    } catch ( Exception e ) {
      fail();
    }

    // Test 3
    doReturn( "" ).when( fileMetadata ).get( "contentCreator" );

    try {
      repositoryFileDto2 = fileService.doGetContentCreator( pathId );
      assertEquals( null, repositoryFileDto2 );
    } catch ( Exception e ) {
      fail();
    }

    verify( fileService, times( 3 ) ).idToPath( pathId );
    verify( fileService.repository, times( 3 ) ).getFileMetadata( fileId );
    verify( fileService.defaultUnifiedRepositoryWebService, times( 3 ) ).getFile( nullable( String.class ) );
    verify( fileService.defaultUnifiedRepositoryWebService ).getFileById( nullable( String.class ) );
  }

  @Test
  public void doGetContentCreatorException() {
    String pathId = "path:to:file:file1.ext";
    String fileId = "file1";
    String creatorId = "creatorId";

    Map<String, Serializable> fileMetadata = mock( HashMap.class );
    doReturn( creatorId ).when( fileMetadata ).get( "contentCreator" );

    doReturn( fileMetadata ).when( fileService.repository ).getFileMetadata( fileId );

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( fileId ).when( repositoryFileDto ).getId();

    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    RepositoryFileDto repositoryFileDto1 = mock( RepositoryFileDto.class );

    doReturn( repositoryFileDto1 ).when( fileService.defaultUnifiedRepositoryWebService ).getFileById( creatorId );

    try {
      fileService.doGetContentCreator( pathId );
      fail();
    } catch ( FileNotFoundException e ) {
      //Should catch the exception
    }
  }

  @Test
  public void testDoGetGeneratedContent() {
    String pathId = "test.prpt",
      userFolder = "public/admin";

    RepositoryFileDto fileDetailsMock = mock( RepositoryFileDto.class );
    RepositoryFile workspaceFolder = mock( RepositoryFile.class );
    doReturn( userFolder ).when( workspaceFolder ).getId();
    SessionResource sessionResource = mock( SessionResource.class );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    RepositoryFile mockedChild = mock( RepositoryFile.class );
    doReturn( false ).when( mockedChild ).isFolder();
    children.add( mockedChild );

    Map<String, Serializable> mockedFileMetadata = mock( Map.class );
    doReturn( pathId ).when( mockedFileMetadata ).get( PentahoJcrConstants.PHO_CONTENTCREATOR );
    when( fileService.repository.getFileMetadata( mockedChild.getId() ) ).thenReturn( mockedFileMetadata );

    doReturn( pathId ).when( fileDetailsMock ).getId();
    doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    doReturn( workspaceFolder ).when( fileService.repository ).getFile( userFolder );
    doReturn( sessionResource ).when( fileService ).getSessionResource();
    doReturn( children ).when( fileService.repository ).getChildren( userFolder );

    RepositoryFileDto mockedRepositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( mockedRepositoryFileDto ).when( fileService ).toFileDto( mockedChild, null, false );

    try {
      doReturn( fileDetailsMock ).when( fileService ).doGetProperties( pathId );
      List<RepositoryFileDto> list = fileService.doGetGeneratedContent( pathId );
      assertEquals( list.size(), 1 );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      fail();
    } catch ( Throwable t ) {
      fail();
    }
  }

  @Test
  public void testDoGetGeneratedContentFileNotFound() {
    String pathId = "test.prpt",
      userFolder = "public/admin";

    SessionResource sessionResource = mock( SessionResource.class );

    doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    doReturn( sessionResource ).when( fileService ).getSessionResource();

    try {
      doReturn( null ).when( fileService ).doGetProperties( pathId );
      fileService.doGetGeneratedContent( pathId );
      fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testDoGetGeneratedContentForUser() {
    String pathId = "test.prpt",
      user = "admin",
      userFolder = "public/admin";

    RepositoryFileDto fileDetailsMock = mock( RepositoryFileDto.class );
    RepositoryFile workspaceFolder = mock( RepositoryFile.class );
    doReturn( userFolder ).when( workspaceFolder ).getId();
    SessionResource sessionResource = mock( SessionResource.class );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    RepositoryFile mockedChild = mock( RepositoryFile.class );
    doReturn( false ).when( mockedChild ).isFolder();
    children.add( mockedChild );

    Map<String, Serializable> mockedFileMetadata = mock( Map.class );
    doReturn( pathId ).when( mockedFileMetadata ).get( PentahoJcrConstants.PHO_CONTENTCREATOR );
    when( fileService.repository.getFileMetadata( mockedChild.getId() ) ).thenReturn( mockedFileMetadata );

    doReturn( pathId ).when( fileDetailsMock ).getId();
    doReturn( userFolder ).when( sessionResource ).doGetUserDir( user );
    doReturn( workspaceFolder ).when( fileService.repository ).getFile( userFolder );
    doReturn( sessionResource ).when( fileService ).getSessionResource();
    doReturn( children ).when( fileService.repository ).getChildren( userFolder );

    RepositoryFileDto mockedRepositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( mockedRepositoryFileDto ).when( fileService ).toFileDto( mockedChild, null, false );

    try {
      doReturn( fileDetailsMock ).when( fileService ).doGetProperties( pathId );
      List<RepositoryFileDto> list = fileService.doGetGeneratedContent( pathId, user );
      assertEquals( list.size(), 1 );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      fail();
    } catch ( Throwable t ) {
      fail();
    }
  }

  @Test
  public void testDoGetGeneratedContentForUserFileNotFound() {
    String pathId = "test.prpt",
      user = "admin",
      userFolder = "public/admin";

    SessionResource sessionResource = mock( SessionResource.class );

    doReturn( userFolder ).when( sessionResource ).doGetUserDir( user );
    doReturn( sessionResource ).when( fileService ).getSessionResource();

    try {
      doReturn( null ).when( fileService ).doGetProperties( pathId );
      fileService.doGetGeneratedContent( pathId, user );
      fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void testSearchGeneratedContent() {
    String lineageId = "test.prpt",
      pathId = "test.prpt",
      userFolder = "public/admin";

    RepositoryFileDto fileDetailsMock = mock( RepositoryFileDto.class );
    RepositoryFile workspaceFolder = mock( RepositoryFile.class );
    doReturn( userFolder ).when( workspaceFolder ).getId();
    SessionResource sessionResource = mock( SessionResource.class );

    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    RepositoryFile mockedChild = mock( RepositoryFile.class );
    doReturn( false ).when( mockedChild ).isFolder();
    children.add( mockedChild );

    Map<String, Serializable> mockedFileMetadata = mock( Map.class );
    doReturn( lineageId ).when( mockedFileMetadata ).get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
    when( fileService.repository.getFileMetadata( mockedChild.getId() ) ).thenReturn( mockedFileMetadata );

    doReturn( pathId ).when( fileDetailsMock ).getId();
    doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    doReturn( workspaceFolder ).when( fileService.repository ).getFile( userFolder );
    doReturn( sessionResource ).when( fileService ).getSessionResource();
    doReturn( children ).when( fileService.repository ).getChildren( userFolder );

    RepositoryFileDto mockedRepositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( mockedRepositoryFileDto ).when( fileService ).toFileDto( mockedChild, null, false );

    try {
      doReturn( fileDetailsMock ).when( fileService ).doGetProperties( pathId );
      List<RepositoryFileDto> list =
        fileService.searchGeneratedContent( userFolder, lineageId, QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
      assertEquals( list.size(), 1 );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      fail();
    } catch ( Throwable t ) {
      fail();
    }
  }

  @Test
  public void testSearchGeneratedContentFileNotFound() {
    String lineageId = "test.prpt",
      pathId = "test.prpt",
      userFolder = "public/admin";

    SessionResource sessionResource = mock( SessionResource.class );

    doReturn( userFolder ).when( sessionResource ).doGetCurrentUserDir();
    doReturn( sessionResource ).when( fileService ).getSessionResource();

    try {
      doReturn( null ).when( fileService ).doGetProperties( pathId );
      fileService.searchGeneratedContent( userFolder, lineageId, QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
      fail();
    } catch ( FileNotFoundException e ) {
    }
  }

  @Test
  public void doGetDeletedFiles() {
    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    RepositoryFileDto repositoryFileDto1 = mock( RepositoryFileDto.class );

    List<RepositoryFileDto> fileDtos = new ArrayList<RepositoryFileDto>();
    fileDtos.add( repositoryFileDto );
    fileDtos.add( repositoryFileDto1 );

    //Test 1
    doReturn( fileDtos ).when( fileService.defaultUnifiedRepositoryWebService ).getDeletedFiles();

    List<RepositoryFileDto> repositoryFiles = fileService.doGetDeletedFiles();
    assertEquals( 2, repositoryFiles.size() );

    //Test 2
    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getDeletedFiles();

    repositoryFiles = fileService.doGetDeletedFiles();
    assertEquals( null, repositoryFiles );

    verify( fileService.defaultUnifiedRepositoryWebService, times( 2 ) ).getDeletedFiles();
  }

  @Test
  public void doGetMetadata() {
    String pathId = "path:to:file:file1.ext";

    List<StringKeyStringValueDto> stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();
    StringKeyStringValueDto stringKeyStringValueDto1 = mock( StringKeyStringValueDto.class );
    doReturn( "key1" ).when( stringKeyStringValueDto1 ).getKey();
    doReturn( "value1" ).when( stringKeyStringValueDto1 ).getValue();

    StringKeyStringValueDto stringKeyStringValueDto2 = mock( StringKeyStringValueDto.class );
    doReturn( "key2" ).when( stringKeyStringValueDto2 ).getKey();
    doReturn( "value2" ).when( stringKeyStringValueDto2 ).getValue();

    stringKeyStringValueDtos.add( stringKeyStringValueDto1 );
    stringKeyStringValueDtos.add( stringKeyStringValueDto2 );

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto repositoryFileDto = mock( RepositoryFileDto.class );
    doReturn( repositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );
    doReturn( true ).when( repositoryFileDto ).isHidden();

    doReturn( stringKeyStringValueDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFileMetadata( nullable( String.class ) );

    // Test 1
    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( pathId );
      assertEquals( 4, list.size() );
      Boolean hasIsHidden = false;
      Boolean hasScheduable = false;
      for ( StringKeyStringValueDto item : list ) {
        if ( item.getKey().equals( "_PERM_HIDDEN" ) ) {
          hasIsHidden = true;
        }
        if ( item.getKey().equals( RepositoryFile.SCHEDULABLE_KEY ) ) {
          hasScheduable = true;
        }
      }
      assertTrue( hasIsHidden );
      assertTrue( hasScheduable );
    } catch ( FileNotFoundException e ) {
      fail();
    }

    stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();
    stringKeyStringValueDtos.add( stringKeyStringValueDto1 );
    stringKeyStringValueDtos.add( stringKeyStringValueDto2 );

    StringKeyStringValueDto stringKeyStringValueDto3 = mock( StringKeyStringValueDto.class );
    doReturn( RepositoryFile.SCHEDULABLE_KEY ).when( stringKeyStringValueDto3 ).getKey();
    doReturn( "value3" ).when( stringKeyStringValueDto3 ).getValue();

    stringKeyStringValueDtos.add( stringKeyStringValueDto3 );

    doReturn( stringKeyStringValueDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getFileMetadata( nullable( String.class ) );

    // Test 2
    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( pathId );
      assertEquals( 4, list.size() );
      Boolean hasIsHidden = false;
      Boolean hasScheduable = false;
      for ( StringKeyStringValueDto item : list ) {
        if ( item.getKey().equals( "_PERM_HIDDEN" ) ) {
          hasIsHidden = true;
        }
        if ( item.getKey().equals( RepositoryFile.SCHEDULABLE_KEY ) ) {
          hasScheduable = true;
        }
      }
      assertTrue( hasIsHidden );
      assertTrue( hasScheduable );
    } catch ( FileNotFoundException e ) {
      fail();
    }

    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFileMetadata( nullable( String.class ) );

    // Test 3
    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( null );
      assertEquals( null, list );
    } catch ( FileNotFoundException e ) {
      fail();
    }

    verify( fileService, times( 2 ) ).idToPath( pathId );
    verify( fileService.defaultUnifiedRepositoryWebService, times( 3 ) ).getFile( nullable( String.class ) );
    verify( fileService.defaultUnifiedRepositoryWebService, times( 3 ) ).getFileMetadata( nullable( String.class ) );
  }

  @Test
  public void doGetMetadataException() {
    String pathId = "path:to:file:file1.ext";

    doReturn( null ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    try {
      List<StringKeyStringValueDto> list = fileService.doGetMetadata( pathId );
      fail();
    } catch ( FileNotFoundException e ) {
      // Should catch exception
    }
    verify( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );
  }

  @Test
  public void testDoGetChildren() {
    RepositoryFileDto mockRepositoryFileDto = mock( RepositoryFileDto.class );
    Collator mockCollator = mock( Collator.class );
    List<RepositoryFileDto> mockRepositoryFileDtos = new ArrayList<RepositoryFileDto>();
    mockRepositoryFileDtos.add( mockRepositoryFileDto );
    RepositoryRequest mockRepositoryRequest = mock( RepositoryRequest.class );

    doReturn( true ).when( fileService ).isPathValid( nullable( String.class ) );
    doReturn( mockRepositoryFileDto ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );
    doReturn( mockCollator ).when( fileService ).getCollator( anyInt() );
    doReturn( mockRepositoryRequest ).when( fileService )
      .getRepositoryRequest( (RepositoryFileDto) any(), anyBoolean(), nullable( String.class ), anyBoolean() );
    doReturn( mockRepositoryFileDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getChildrenFromRequest( mockRepositoryRequest );
    doReturn( true ).when( fileService ).isShowingTitle( mockRepositoryRequest );

    List<RepositoryFileDto> repositoryFileDtos = fileService.doGetChildren( "mock:path:fileName", null, true, true );

    verify( fileService, times( 1 ) ).isPathValid( nullable( String.class ) );
    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) ).getFile( nullable( String.class ) );
    verify( fileService, times( 1 ) ).getCollator( anyInt() );
    verify( fileService, times( 1 ) )
      .getRepositoryRequest( (RepositoryFileDto) any(), anyBoolean(), nullable( String.class ), anyBoolean() );
    verify( fileService.defaultUnifiedRepositoryWebService, times( 1 ) )
      .getChildrenFromRequest( mockRepositoryRequest );
    verify( fileService, times( 1 ) ).isShowingTitle( mockRepositoryRequest );

    assertEquals( mockRepositoryFileDtos, repositoryFileDtos );
    assertEquals( 1, repositoryFileDtos.size() );
    assertEquals( mockRepositoryFileDto, repositoryFileDtos.get( 0 ) );
  }

  @Test
  public void doSetMetadata() {
    String pathId = "path:to:file:file1.ext";

    List<StringKeyStringValueDto> stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();
    StringKeyStringValueDto stringKeyStringValueDto1 = mock( StringKeyStringValueDto.class );
    doReturn( "key1" ).when( stringKeyStringValueDto1 ).getKey();
    doReturn( "value1" ).when( stringKeyStringValueDto1 ).getValue();

    StringKeyStringValueDto stringKeyStringValueDto2 = mock( StringKeyStringValueDto.class );
    doReturn( "key2" ).when( stringKeyStringValueDto2 ).getKey();
    doReturn( "value2" ).when( stringKeyStringValueDto2 ).getValue();

    stringKeyStringValueDtos.add( stringKeyStringValueDto1 );
    stringKeyStringValueDtos.add( stringKeyStringValueDto2 );

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );

    RepositoryFileDto file = mock( RepositoryFileDto.class );
    doReturn( false ).when( file ).isFolder();
    doReturn( true ).when( file ).isHidden();

    doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    List<RepositoryFileAclAceDto> repositoryFileAclAceDtos = new ArrayList<RepositoryFileAclAceDto>();

    RepositoryFileAclDto repositoryFileAclDto = mock( RepositoryFileAclDto.class );
    doReturn( "sessionName" ).when( repositoryFileAclDto ).getOwner();
    doReturn( true ).when( repositoryFileAclDto ).isEntriesInheriting();
    doReturn( repositoryFileAclAceDtos ).when( repositoryFileAclDto ).getAces();

    doReturn( repositoryFileAclDto ).when( fileService.defaultUnifiedRepositoryWebService ).getAcl( nullable( String.class ) );

    IPentahoSession pentahoSession = mock( IPentahoSession.class );
    doReturn( pentahoSession ).when( fileService ).getSession();
    doReturn( "sessionName" ).when( pentahoSession ).getName();

    RepositoryFileAclAceDto repositoryFileAclAceDto = mock( RepositoryFileAclAceDto.class );
    List<Integer> permissions = new ArrayList<Integer>();
    permissions.add( RepositoryFilePermission.ACL_MANAGEMENT.ordinal() );
    doReturn( permissions ).when( repositoryFileAclAceDto ).getPermissions();
    doReturn( "sessionName" ).when( repositoryFileAclAceDto ).getRecipient();

    repositoryFileAclAceDtos.add( repositoryFileAclAceDto );

    doReturn( repositoryFileAclAceDtos ).when( fileService.defaultUnifiedRepositoryWebService )
      .getEffectiveAces( nullable( String.class ) );

    Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    doReturn( metadata ).when( fileService.repository ).getFileMetadata( nullable( String.class ) );

    RepositoryFile sourceFile = mock( RepositoryFile.class );
    doReturn( sourceFile ).when( fileService.repository ).getFileById( nullable( String.class ) );

    RepositoryFileDto destFileDto = mock( RepositoryFileDto.class );
    doReturn( destFileDto ).when( fileService ).toFileDto( sourceFile, null, false );

    RepositoryFile destFile = mock( RepositoryFile.class );
    doReturn( destFile ).when( fileService ).toFile( destFileDto );

    RepositoryFileAcl acl = mock( RepositoryFileAcl.class );
    doReturn( acl ).when( fileService.repository ).getAcl( acl );

    // Test 1 - canManage should be true at start
    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 2 - canManage should be false at start
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 3 - canManage should be false at start
    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 4 - canManage should be false at start
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 5 - canManage should be false at start
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 6 - canManage should be false at start
    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 7 - canManage should be false at start
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( "sessionName1" ).when( repositoryFileAclDto ).getOwner();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 8 - canManage should be false at start
    doReturn( true ).when( file ).isFolder();
    doReturn( true ).when( file ).isHidden();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    // Test 9
    StringKeyStringValueDto stringKeyStringValueDto3 = mock( StringKeyStringValueDto.class );
    doReturn( "_PERM_HIDDEN" ).when( stringKeyStringValueDto3 ).getKey();
    doReturn( "true" ).when( stringKeyStringValueDto3 ).getValue();
    stringKeyStringValueDtos.add( stringKeyStringValueDto3 );

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
    } catch ( GeneralSecurityException e ) {
      fail();
    }

    verify( fileService.defaultUnifiedRepositoryWebService, times( 9 ) ).getFile( nullable( String.class ) );
    verify( fileService.defaultUnifiedRepositoryWebService, times( 9 ) ).getAcl( nullable( String.class ) );
    verify( repositoryFileAclDto, times( 9 ) ).getOwner();
    verify( fileService.policy, times( 11 ) ).isAllowed( nullable( String.class ) );
    verify( fileService.repository, times( 9 ) ).getFileMetadata( nullable( String.class ) );
    verify( fileService.repository, times( 7 ) ).setFileMetadata( nullable( String.class ), any( Map.class ) );
    verify( file, times( 8 ) ).setHidden( anyBoolean() );
    verify( fileService.repository, times( 8 ) ).getFileById( nullable( String.class ) );
    verify( fileService, times( 8 ) ).toFileDto( any( RepositoryFile.class ), anySet(), anyBoolean() );
    verify( fileService, times( 8 ) ).toFile( any( RepositoryFileDto.class ) );
    verify( destFileDto, times( 8 ) ).setHidden( anyBoolean() );
    verify( fileService.repository, times( 8 ) ).getAcl( nullable( String.class ) );
    verify( fileService.repository, times( 7 ) )
      .updateFile( any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        nullable( String.class ) );
    verify( fileService.repository, times( 7 ) ).updateAcl( any( RepositoryFileAcl.class ) );
    verify( fileService.repository ).updateFolder( any( RepositoryFile.class ), nullable( String.class ) );
  }

  @Test
  public void doSetMetadataException() {

    String pathId = "path:to:file:file1.ext";

    List<StringKeyStringValueDto> stringKeyStringValueDtos = new ArrayList<StringKeyStringValueDto>();

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( fileService.policy ).isAllowed( AdministerSecurityAction.NAME );

    RepositoryFileDto file = mock( RepositoryFileDto.class );

    doReturn( file ).when( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );

    RepositoryFileAclDto repositoryFileAclDto = mock( RepositoryFileAclDto.class );
    doReturn( "sessionName" ).when( repositoryFileAclDto ).getOwner();

    doReturn( repositoryFileAclDto ).when( fileService.defaultUnifiedRepositoryWebService ).getAcl( nullable( String.class ) );

    IPentahoSession pentahoSession = mock( IPentahoSession.class );
    doReturn( pentahoSession ).when( fileService ).getSession();
    doReturn( "sessionName1" ).when( pentahoSession ).getName();

    try {
      fileService.doSetMetadata( pathId, stringKeyStringValueDtos );
      fail();
    } catch ( GeneralSecurityException e ) {
      //Should catch the exception
    }

    verify( fileService.defaultUnifiedRepositoryWebService ).getFile( nullable( String.class ) );
    verify( fileService.defaultUnifiedRepositoryWebService ).getAcl( nullable( String.class ) );
    verify( repositoryFileAclDto ).getOwner();
    verify( fileService.policy ).isAllowed( nullable( String.class ) );
  }

  @Test
  public void testDoGetFileAcl() {
    RepositoryFileDto file = mock( RepositoryFileDto.class );

    RepositoryFileAclDto fileAcl = mock( RepositoryFileAclDto.class );
    when( fileAcl.isEntriesInheriting() ).thenReturn( false );

    when( fileService.defaultUnifiedRepositoryWebService.getFile( nullable( String.class ) ) ).thenReturn( file );
    when( fileService.defaultUnifiedRepositoryWebService.getAcl( nullable( String.class ) ) ).thenReturn( fileAcl );

    doNothing().when( fileService ).addAdminRole( fileAcl );

    String pathId = "/usr/dir/file.txt";
    fileService.doGetFileAcl( pathId );

    verify( fileService ).addAdminRole( fileAcl );
  }

  @Test
  public void testDoGetTree() {
    String pathId = ":path:to:file:file1.ext";
    int depth = 1;
    String filter = "*|FOLDERS";
    boolean showHidden = true;
    boolean includeAcls = true;

    // Test 1
    doReturn( "test" ).when( fileService ).idToPath( nullable( String.class ) );

    RepositoryRequest mockRequest = mock( RepositoryRequest.class );
    doReturn( mockRequest ).when( fileService )
      .getRepositoryRequest( nullable( String.class ), anyBoolean(), anyInt(), nullable( String.class ) );

    RepositoryFileDto mockChildFile = mock( RepositoryFileDto.class );
    doReturn( "test" ).when( mockChildFile ).getId();

    RepositoryFileTreeDto mockChildDto = mock( RepositoryFileTreeDto.class );
    doReturn( mockChildFile ).when( mockChildDto ).getFile();

    List<RepositoryFileTreeDto> mockChildrenDto = new ArrayList<RepositoryFileTreeDto>();
    mockChildrenDto.add( mockChildDto );

    RepositoryFileTreeDto mockTreeDto = mock( RepositoryFileTreeDto.class );
    doReturn( mockChildrenDto ).when( mockTreeDto ).getChildren();
    doReturn( mockTreeDto ).when( fileService.defaultUnifiedRepositoryWebService ).getTreeFromRequest( mockRequest );

    doReturn( true ).when( fileService ).isShowingTitle( mockRequest );

    Collator mockCollator = mock( Collator.class );
    doReturn( mockCollator ).when( fileService ).getCollatorInstance();
    doNothing().when( fileService ).sortByLocaleTitle( mockCollator, mockTreeDto );

    Map<String, Serializable> fileMeta = new HashMap<String, Serializable>();
    fileMeta.put( IUnifiedRepository.SYSTEM_FOLDER, new Boolean( false ) );

    doReturn( fileMeta ).when( fileService.repository ).getFileMetadata( nullable( String.class ) );

    fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );

    verify( fileService, times( 1 ) ).idToPath( nullable( String.class ) );
    verify( mockRequest, times( 1 ) ).setIncludeAcls( anyBoolean() );
    verify( mockCollator, times( 1 ) ).setStrength( Collator.PRIMARY );
    verify( fileService, times( 1 ) ).sortByLocaleTitle( mockCollator, mockTreeDto );
    //verify( mockTreeDto ).setChildren( mockChildrenDto );

    // Test 2 - path id is null
    pathId = null;
    fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );

    verify( fileService, times( 1 ) )
      .getRepositoryRequest( eq( FileUtils.PATH_SEPARATOR ), anyBoolean(), anyInt(), nullable( String.class ) );

    // Test 3 - path id is set to the file utils path separator
    pathId = FileUtils.PATH_SEPARATOR;
    fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );

    verify( fileService, times( 2 ) )
      .getRepositoryRequest( eq( FileUtils.PATH_SEPARATOR ), anyBoolean(), anyInt(), nullable( String.class ) );

    // Test 3 - includeSystemFolders is false
    mockRequest.setIncludeSystemFolders( false );
    doReturn( mockTreeDto ).when( fileService.defaultUnifiedRepositoryWebService ).getTreeFromRequest( mockRequest );
  }

  @Test
  public void testDoRename() throws Exception {
    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repositoryFile.getPath() ).thenReturn( "/dir/file.txt" );
    when( repositoryFile.getName() ).thenReturn( "file.txt" );

    when( fileService.repository.getFile( nullable( String.class ) ) ).thenReturn( repositoryFile ).thenReturn( null ); // On the second call to repository.getFile() we are testing if the target object already exists, so for this test on the second call we need a null
    when( fileService.repository.getFileById( nullable( String.class ) ) ).thenReturn( repositoryFile );
    String pathId = ":dir:file.txt";
    String newName = "file1.txt";

    boolean success = fileService.doRename( pathId, newName );
    assertTrue( success );
  }

  @Test
  public void testDoRenameNegative() throws Exception {
    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repositoryFile.getPath() ).thenReturn( "/dir/file.txt" );
    when( repositoryFile.getName() ).thenReturn( "file.txt" );

    when( fileService.repository.getFile( nullable( String.class ) ) ).thenReturn( repositoryFile ).thenReturn( null ); // On the second call to repository.getFile() we are testing if the target object already exists, so for this test on the second call we need a null;
    String pathId = ":dir:file.txt";
    String newName = "file1.txt";

    boolean success = fileService.doRename( pathId, newName );
    assertFalse( success );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testDoRenameWhenTargetAlreadyExists() throws Exception{
    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repositoryFile.getPath() ).thenReturn( "/dir/file.txt" );
    when( repositoryFile.getName() ).thenReturn( "file.txt" );

    when( fileService.repository.getFile( nullable( String.class ) ) ).thenReturn( repositoryFile );
    String pathId = ":dir:file.txt";
    String newName = "file1.txt";

    boolean success = fileService.doRename( pathId, newName );
    assertFalse( success );
  }

  @Test
  public void testDoCreateDirs() throws Exception {
    String pathId = "path:to:file:file1.ext";

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto parentDir = mock( RepositoryFileDto.class );
    doReturn( "" ).when( parentDir ).getPath();
    doReturn( FileUtils.PATH_SEPARATOR ).when( parentDir ).getId();
    when( fileService.getRepoWs().getFile( FileUtils.PATH_SEPARATOR ) ).thenReturn( parentDir );

    when( fileService.getRepoWs().getFile( "/path" ) ).thenReturn( null );
    when( fileService.getRepoWs().getFile( "/to" ) ).thenReturn( null );
    when( fileService.getRepoWs().getFile( "/file" ) ).thenReturn( null );
    when( fileService.getRepoWs().getFile( "/file1.ext" ) ).thenReturn( null );

    RepositoryFileDto filePath = mock( RepositoryFileDto.class );
    doReturn( "/path" ).when( filePath ).getPath();
    doReturn( "/path" ).when( filePath ).getId();
    RepositoryFileDto fileTo = mock( RepositoryFileDto.class );
    doReturn( "/path/to" ).when( fileTo ).getPath();
    doReturn( "/path/to" ).when( fileTo ).getId();
    RepositoryFileDto fileFile = mock( RepositoryFileDto.class );
    doReturn( "/path/to/file" ).when( fileFile ).getPath();
    doReturn( "/path/to/file" ).when( fileFile ).getId();
    RepositoryFileDto fileFileExt = mock( RepositoryFileDto.class );
    doReturn( "/path/to/file/file1" ).when( fileFileExt ).getPath();
    doReturn( "/path/to/file/file1" ).when( fileFileExt ).getId();

    when( fileService.getRepoWs().createFolder( eq( "/" ), any( RepositoryFileDto.class ), eq( "/path" ) ) ).thenReturn(
      filePath );
    when( fileService.getRepoWs().createFolder( eq( "/path" ), any( RepositoryFileDto.class ), eq( "/path/to" ) ) )
      .thenReturn( fileTo );
    when(
      fileService.getRepoWs().createFolder( eq( "/path/to" ), any( RepositoryFileDto.class ), eq( "/path/to/file" ) ) )
      .thenReturn( fileFile );
    when(
      fileService.getRepoWs()
        .createFolder( eq( "/path/to/file" ), any( RepositoryFileDto.class ), eq( "/path/to/file/file1.ext" ) ) )
      .thenReturn( fileFileExt );

    assertTrue( fileService.doCreateDir( pathId ) );

    verify( fileService.getRepoWs(), times( 4 ) )
      .createFolder( nullable( String.class ), any( RepositoryFileDto.class ), nullable( String.class ) );
  }

  @Test
  public void testDoCreateDirsNegative() throws Exception {
    String pathId = "path:to:file:file1.ext";

    doReturn( "/path/to/file/file1.ext" ).when( fileService ).idToPath( pathId );

    RepositoryFileDto parentDir = mock( RepositoryFileDto.class );
    doReturn( "" ).when( parentDir ).getPath();
    doReturn( FileUtils.PATH_SEPARATOR ).when( parentDir ).getId();
    when( fileService.getRepoWs().getFile( FileUtils.PATH_SEPARATOR ) ).thenReturn( parentDir );

    RepositoryFileDto filePath = mock( RepositoryFileDto.class );
    doReturn( "/path" ).when( filePath ).getPath();
    doReturn( "/path" ).when( filePath ).getId();
    RepositoryFileDto fileTo = mock( RepositoryFileDto.class );
    doReturn( "/path/to" ).when( fileTo ).getPath();
    doReturn( "/path/to" ).when( fileTo ).getId();
    RepositoryFileDto fileFile = mock( RepositoryFileDto.class );
    doReturn( "/path/to/file" ).when( fileFile ).getPath();
    doReturn( "/path/to/file" ).when( fileFile ).getId();
    RepositoryFileDto fileFileExt = mock( RepositoryFileDto.class );
    doReturn( "/path/to/file/file1" ).when( fileFileExt ).getPath();
    doReturn( "/path/to/file/file1" ).when( fileFileExt ).getId();

    when( fileService.getRepoWs().getFile( "/path" ) ).thenReturn( filePath );
    when( fileService.getRepoWs().getFile( "/path/to" ) ).thenReturn( fileTo );
    when( fileService.getRepoWs().getFile( "/path/to/file" ) ).thenReturn( fileFile );
    when( fileService.getRepoWs().getFile( "/path/to/file/file1.ext" ) ).thenReturn( fileFileExt );

    assertFalse( fileService.doCreateDir( pathId ) );

    verify( fileService.getRepoWs(), times( 0 ) )
      .createFolder( nullable( String.class ), any( RepositoryFileDto.class ), nullable( String.class ) );


    when( fileService.getRepoWs().getFile( "/path" ) ).thenReturn( null );
    when( fileService.getRepoWs().createFolder( eq( "/" ), any( RepositoryFileDto.class ), eq( "/path" ) ) ).
      thenThrow( new InternalError( "negativetest" ) );

    try {
      fileService.doCreateDir( pathId );
    } catch ( InternalError e ) {
      assertEquals( e.getMessage(), "negativetest" );
    }
  }

  @Test
  public void testValidFolderName() throws FileService.InvalidNameException {
    FileService fs = mock( FileService.class );
    doCallRealMethod().when( fs ).doCreateDirSafe( nullable( String.class ) );
    doCallRealMethod().when( fs ).decode( nullable( String.class ) );
    doReturn( "New Folder" ).when( fs ).idToPath( nullable( String.class ) );
    doReturn( true ).when( fs ).doCreateDirFor( "New Folder" );

    StringBuffer sb = new StringBuffer( "!" );
    doReturn( sb ).when( fs ).doGetReservedChars();

    mockStatic( FileUtils.class );
    when( FileUtils.containsReservedCharacter( nullable( String.class ), any() ) ).thenReturn( false );

    assertTrue( fs.doCreateDirSafe( "New Folder" ) );
  }

  @Test
  public void testInvalidValidFolderNameWithDot() throws FileService.InvalidNameException {
    FileService fs = mock( FileService.class );
    doCallRealMethod().when( fs ).doCreateDirSafe( nullable( String.class ) );
    doCallRealMethod().when( fs ).decode( nullable( String.class ) );
    doReturn( "." ).when( fs ).idToPath( nullable( String.class ) );
    doReturn( true ).when( fs ).doCreateDirFor( "." );

    StringBuffer sb = new StringBuffer( "!" );
    doReturn( sb ).when( fs ).doGetReservedChars();

    mockStatic( FileUtils.class );
    when( FileUtils.containsReservedCharacter( nullable( String.class ), any() ) ).thenReturn( false );

    try {
      fs.doCreateDirSafe( "." );
      fail();
    } catch ( FileService.InvalidNameException e ) {
      assertNotNull( e );
    }
  }

  @Test
  public void testInvalidValidFolderNameWithEncodedDot() throws FileService.InvalidNameException {
    FileService fs = mock( FileService.class );
    doCallRealMethod().when( fs ).doCreateDirSafe( nullable( String.class ) );
    doCallRealMethod().when( fs ).decode( nullable( String.class ) );
    doReturn( "%2E" ).when( fs ).idToPath( nullable( String.class ) );
    doReturn( true ).when( fs ).doCreateDirFor( "%2E" );

    StringBuffer sb = new StringBuffer( "!" );
    doReturn( sb ).when( fs ).doGetReservedChars();

    mockStatic( FileUtils.class );
    when( FileUtils.containsReservedCharacter( nullable( String.class ), any() ) ).thenReturn( false );

    try {
      fs.doCreateDirSafe( "%2E" );
      fail();
    } catch ( FileService.InvalidNameException e ) {
      assertNotNull( e );
    }
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Class<?> arg ) {
      return true;
    }
  }
}
