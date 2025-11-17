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


package org.pentaho.platform.web.http.api.resources.operations;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CopyFilesOperation_CopyTest {
  private IUnifiedRepository repo;
  private DefaultUnifiedRepositoryWebService webService;

  private RepositoryFile file1;
  private RepositoryFile file2;
  private static final String EXTENSION_PRPT = ".prpt";


  private RepositoryFile destFolder;
  private RepositoryFile folder1;
  private RepositoryFile folder2;

  private static final String SEPARATOR = "/";
  private static final String DEFAULT = "<def>";

  private static final String NAME_FILE_1 = "file-1" + EXTENSION_PRPT;
  private static final String NAME_FILE_2 = "file-2" + EXTENSION_PRPT;

  private static final String PATH_FILE_1 = "path/to/file1/" + NAME_FILE_1;
  private static final String PATH_FILE_2 = "path/to/file2/" + NAME_FILE_2;


  private static final String PATH_DEST_DIR = "/directory/destination";
  private static final String NAME_DEST_DIR = "destination";

  private static final String NAME_DIR_1 = "dir-1";
  private static final String NAME_DIR_2 = "dir-2";

  private static final String PATH_DIR_1 = "path/to/dir1/" + NAME_DIR_1;
  private static final String PATH_DIR_2 = "path/to/dir2/" + NAME_DIR_2;

  @Before
  public void init() {
    repo = mock( IUnifiedRepository.class );
    webService = mock( DefaultUnifiedRepositoryWebService.class );

    file1 = mockFile( generateID(), NAME_FILE_1, PATH_FILE_1 );
    file2 = mockFile( generateID(), NAME_FILE_2, PATH_FILE_2 );

    destFolder = mockFolder( generateID(), NAME_DEST_DIR, PATH_DEST_DIR );
    doReturn( PATH_DEST_DIR ).when( destFolder ).getPath();
    doReturn( destFolder ).when( repo ).getFile( PATH_DEST_DIR );
    Serializable destFolderId = destFolder.getId();

    doReturn( mockFile( generateID(), DEFAULT, DEFAULT ) ).when( repo )
      .createFile( eq( destFolderId ), nullable( RepositoryFile.class ), nullable( IRepositoryFileData.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    doReturn( mockFolder( generateID(), DEFAULT, DEFAULT ) ).when( repo )
      .createFolder( eq( destFolderId ), nullable( RepositoryFile.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    folder1 = mockFolder( generateID(), NAME_DIR_1, PATH_DIR_1 );
    folder2 = mockFolder( generateID(), NAME_DIR_2, PATH_DIR_2 );
  }

  @Test( expected = IllegalArgumentException.class )
  public void copyFails_whenDestIsNotFolder() {
    List<String> listOfFiles = new ArrayList<>();
    listOfFiles.add( DEFAULT );

    String destDirPath = "/destintion";
    mockFile( generateID(), DEFAULT, destDirPath );
    new CopyFilesOperation( repo, webService, listOfFiles, destDirPath, 2 );
  }

  @Test( expected = IllegalArgumentException.class )
  public void copyFails_whenNoFilesGiven() {
    new CopyFilesOperation( repo, webService, Collections.emptyList(), PATH_DEST_DIR, 2 );
  }

  @Test
  public void copyFiles_OverwriteMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( file1, file2 ), PATH_DEST_DIR,
        FileService.MODE_OVERWRITE );

    // emulate, file with the same name exists (file1).
    RepositoryFile conflictFile = mockFile( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_FILE_1 );

    operation = spy( operation );

    RepositoryFileDto fileDto = mock( RepositoryFileDto.class );
    doReturn( conflictFile ).when( operation ).toFile( fileDto );
    doReturn( fileDto ).when( operation ).toFileDto( eq( conflictFile ), nullable( Set.class ), nullable( Boolean.class ) );

    doReturn( conflictFile ).when( repo )
      .updateFile( eq( conflictFile ), nullable( IRepositoryFileData.class ), nullable( String.class ) );

    operation.execute();

    verify( repo, times( 1 ) )
      .updateFile( notNull( RepositoryFile.class ), nullable( IRepositoryFileData.class ), nullable( String.class ) );
    verify( repo )
      .updateFile( eq( conflictFile ), nullable( IRepositoryFileData.class ), nullable( String.class ) );

    verify( repo, times( 1 ) )
      .createFile( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFile( eq( destFolder.getId() ),
        argThat( file -> file != null && NAME_FILE_2.equals( file.getName() ) ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  @Test
  public void copyFiles_NoOverwriteMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( file1, file2 ), PATH_DEST_DIR,
        FileService.MODE_NO_OVERWRITE );

    // emulate, file with the same name exists (file1).
    mockFile( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_FILE_1 );

    operation.execute();

    // one file should be created, as there was 2 files, and 1 conflict
    verify( repo, times( 1 ) )
      .createFile( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFile( eq( destFolder.getId() ),
        argThat( file -> file != null && NAME_FILE_2.equals( file.getName() ) ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    verify( repo, never() )
      .createFolder( nullable( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  @Test
  public void copyFolders_NoOverwriteMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( folder1, folder2 ), PATH_DEST_DIR,
        FileService.MODE_NO_OVERWRITE );

    // emulate, folder with the same name exists (folder1).
    mockFolder( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_DIR_1 );

    // spy clone method to return the same folder
    doReturn( folder2 ).when( folder2 ).clone();

    RepositoryFile newFolder2 = mock( RepositoryFile.class );
    doReturn( generateID() ).when( newFolder2 ).getId();
    doReturn( true ).when( newFolder2 ).isFolder();
    doReturn( NAME_DIR_2 ).when( newFolder2 ).getName();
    doReturn( PATH_DEST_DIR + SEPARATOR + NAME_DIR_2 ).when( newFolder2 ).getPath();

    doReturn( newFolder2 ).when( repo )
      .createFolder( notNull( Serializable.class ),
        argThat( folder -> folder != null && ( NAME_DIR_2 ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    operation = spy( operation );
    doNothing().when( operation )
      .performFolderDeepCopy( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ), anyInt() );

    operation.execute();

    verify( repo, times( 1 ) )
      .createFolder( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFolder( eq( destFolder.getId() ),
        argThat( folder -> folder != null && ( NAME_DIR_2 ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    verify( operation, times( 1 ) )
      .performFolderDeepCopy( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ), anyInt() );
    verify( operation, times( 1 ) )
      .performFolderDeepCopy(
        argThat( folder -> folder != null && ( NAME_DIR_2 ).equals( folder.getName() ) ),
        argThat( folder -> folder != null && ( PATH_DEST_DIR + SEPARATOR + NAME_DIR_2 ).equals( folder.getPath() ) ), anyInt() );

    verify( repo, never() )
      .createFile( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), any( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  @Test
  public void copyFolders_NoOverwriteMode_NestedSelfCopy() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( destFolder ), PATH_DEST_DIR,
        FileService.MODE_NO_OVERWRITE );

    // spy clone method to return the same folder
    doReturn( destFolder ).when( destFolder ).clone();

    RepositoryFile newDestFolder = mock( RepositoryFile.class );
    doReturn( generateID() ).when( newDestFolder ).getId();
    doReturn( true ).when( newDestFolder ).isFolder();
    doReturn( NAME_DEST_DIR ).when( newDestFolder ).getName();
    doReturn( PATH_DEST_DIR + SEPARATOR + NAME_DEST_DIR ).when( newDestFolder ).getPath();

    doReturn( newDestFolder ).when( repo )
      .createFolder( notNull( Serializable.class ),
        argThat( folder -> folder != null && ( NAME_DEST_DIR ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    // newDestFolder is a child of the mocked destFolder
    // allows testing for infinite recursion protection when copying a folder into itself
    doReturn( List.of( newDestFolder ) ).when( repo )
      .getChildren( notNull( RepositoryRequest.class ) );

    operation = spy( operation );

    operation.execute();

    verify( repo, times( 1 ) )
      .createFolder( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFolder( eq( destFolder.getId() ),
        argThat( folder -> folder != null && ( NAME_DEST_DIR ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    verify( operation, times( 1 ) )
      .performFolderDeepCopy( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ), anyInt() );
    verify( operation, times( 1 ) )
      .performFolderDeepCopy(
        argThat( folder -> folder != null && ( NAME_DEST_DIR ).equals( folder.getName() ) ),
        argThat( folder -> folder != null && ( PATH_DEST_DIR + SEPARATOR + NAME_DEST_DIR ).equals( folder.getPath() ) ), anyInt() );

    verify( repo, never() )
      .createFile( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), any( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  @Test
  public void copyFiles_RenameMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( file1, file2 ), PATH_DEST_DIR,
        FileService.MODE_RENAME );

    // emulate, file with the same name exists (file1).
    RepositoryFile conflict = mockFile( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_FILE_1 );
    String conflictFilePath = conflict.getPath();
    RepositoryFileDto dtoConflictFile = mock( RepositoryFileDto.class );

    doReturn( dtoConflictFile ).when( webService ).getFile( eq( conflictFilePath ) );

    operation.execute();

    verify( repo, times( 2 ) )
      .createFile( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFile( eq( destFolder.getId() ),
        argThat( file -> file != null && "file-1-Copy.prpt".equals( file.getName() ) ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFile( eq( destFolder.getId() ),
        argThat( file -> file != null && NAME_FILE_2.equals( file.getName() ) ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    verify( repo, never() )
      .createFolder( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  @Test
  public void copyFolders_RenameMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( folder1, folder2 ), PATH_DEST_DIR,
        FileService.MODE_RENAME );

    // emulate, folder with the same name exists (folder2).
    RepositoryFile conflict = mockFolder( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_DIR_2 );
    String conflictFolderPath = conflict.getPath();
    RepositoryFileDto dtoConflictFolder = mock( RepositoryFileDto.class );

    doReturn( dtoConflictFolder ).when( webService ).getFile( eq( conflictFolderPath ) );

    doReturn( mockFolder( generateID(), NAME_DIR_1, PATH_DEST_DIR + SEPARATOR + NAME_DIR_1 ) ).when( repo )
      .createFolder( notNull( Serializable.class ),
        argThat( folder -> folder != null && ( NAME_DIR_1 ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    doReturn( mockFolder( generateID(), NAME_DIR_2 + "-Copy", PATH_DEST_DIR + SEPARATOR + NAME_DIR_2 + "-Copy" ) ).when( repo )
      .createFolder( notNull( Serializable.class ),
        argThat( folder -> folder != null && ( NAME_DIR_2 + "-Copy" ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    operation = spy( operation );

    operation.execute();

    verify( repo, times( 2 ) )
      .createFolder( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFolder( eq( destFolder.getId() ),
        argThat( folder -> folder != null && ( NAME_DIR_1 ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFolder( eq( destFolder.getId() ),
        argThat( folder -> folder != null && ( NAME_DIR_2 + "-Copy" ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    verify( operation, times( 2 ) )
      .performFolderDeepCopy( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ), anyInt() );
    verify( operation )
      .performFolderDeepCopy(
        argThat( folder -> folder != null && ( NAME_DIR_1 ).equals( folder.getName() ) ),
        argThat( folder -> folder != null && ( PATH_DEST_DIR + SEPARATOR + NAME_DIR_1 ).equals( folder.getPath() ) ), anyInt() );
    verify( operation )
      .performFolderDeepCopy(
        argThat( folder -> folder != null && ( NAME_DIR_2 ).equals( folder.getName() ) ),
        argThat( folder -> folder != null && ( PATH_DEST_DIR + SEPARATOR + NAME_DIR_2 + "-Copy" ).equals( folder.getPath() ) ), anyInt() );

    verify( repo, never() )
      .createFile( nullable( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  @Test
  public void copyFolders_RenameMode_NestedSelfCopy() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( destFolder ), PATH_DEST_DIR,
        FileService.MODE_RENAME );

    RepositoryFile newDestFolder = mock( RepositoryFile.class );
    doReturn( generateID() ).when( newDestFolder ).getId();
    doReturn( true ).when( newDestFolder ).isFolder();
    doReturn( NAME_DEST_DIR ).when( newDestFolder ).getName();
    doReturn( PATH_DEST_DIR + SEPARATOR + NAME_DEST_DIR ).when( newDestFolder ).getPath();

    doReturn( newDestFolder ).when( repo )
      .createFolder( notNull( Serializable.class ),
        argThat( folder -> folder != null && ( NAME_DEST_DIR ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    // newDestFolder is a child of the mocked destFolder
    // allows testing for infinite recursion protection when copying a folder into itself
    doReturn( List.of( newDestFolder ) ).when( repo )
      .getChildren( notNull( RepositoryRequest.class ) );

    operation = spy( operation );

    operation.execute();

    verify( repo, times( 1 ) )
      .createFolder( notNull( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );
    verify( repo )
      .createFolder( eq( destFolder.getId() ),
        argThat( folder -> folder != null && ( NAME_DEST_DIR ).equals( folder.getName() ) ),
        nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    verify( operation, times( 1 ) )
      .performFolderDeepCopy( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ), anyInt() );
    verify( operation )
      .performFolderDeepCopy(
        argThat( folder -> folder != null && ( NAME_DEST_DIR ).equals( folder.getName() ) ),
        argThat( folder -> folder != null && ( PATH_DEST_DIR + SEPARATOR + NAME_DEST_DIR ).equals( folder.getPath() ) ), anyInt() );

    verify( repo, never() )
      .createFile( nullable( Serializable.class ), nullable( RepositoryFile.class ),
        nullable( IRepositoryFileData.class ), nullable( RepositoryFileAcl.class ), nullable( String.class ) );
  }

  private List<String> getIdList( final RepositoryFile... files ) {
    return new ArrayList<String>() {
      {
        for ( RepositoryFile file : files ) {
          add( file.getId().toString() );
        }
      }
    };
  }

  private static String generateID() {
    return UUID.randomUUID().toString();
  }

  public RepositoryFile mockFile( Serializable id, String fileName, String path ) {
    return mockRepoFile( id, fileName, path, false );
  }

  public RepositoryFile mockFolder( Serializable id, String fileName, String path ) {
    return mockRepoFile( id, fileName, path, true );
  }

  private RepositoryFile mockRepoFile( Serializable id, String fileName, String path, boolean isFolder ) {
    RepositoryFile repoFile = mock( RepositoryFile.class );
    doReturn( id ).when( repoFile ).getId();
    doReturn( isFolder ).when( repoFile ).isFolder();
    doReturn( fileName ).when( repoFile ).getName();
    doReturn( path ).when( repoFile ).getPath();

    doReturn( repoFile ).when( repo ).getFileById( id );
    doReturn( repoFile ).when( repo ).getFile( path );
    return repoFile;
  }
}
