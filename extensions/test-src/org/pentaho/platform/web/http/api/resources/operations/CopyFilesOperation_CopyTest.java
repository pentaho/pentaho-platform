/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.operations;


import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

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
  private static final String PATH_DIR_2 = "path/to/dir1/" + NAME_DIR_2;

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
      .createFile( eq( destFolderId ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        any( RepositoryFileAcl.class ), anyString() );

    doReturn( mockFolder( generateID(), DEFAULT, DEFAULT ) ).when( repo )
      .createFolder( eq( destFolderId ), any( RepositoryFile.class ), any( RepositoryFileAcl.class ), anyString() );

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
  @SuppressWarnings( "unchecked" )
  public void copyFails_whenNoFilesGiven() {
    new CopyFilesOperation( repo, webService, Collections.emptyList(), PATH_DEST_DIR, 2 );
  }

  @Test
  public void copyFiles_OverwriteMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( file1, file2 ), PATH_DEST_DIR,
        FileService.MODE_OVERWRITE );

    // emulate, file with the same name exists.
    RepositoryFile conflictFile = mockFile( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_FILE_1 );

    operation = spy( operation );

    RepositoryFileDto fileDto = mock( RepositoryFileDto.class );
    doReturn( conflictFile ).when( operation ).toFile( fileDto );
    doReturn( fileDto ).when( operation ).toFileDto( eq( conflictFile ), anySet(), anyBoolean() );

    doReturn( conflictFile ).when( repo )
      .updateFile( eq( conflictFile ), any( IRepositoryFileData.class ), anyString() );

    operation.execute();

    verify( repo ).updateFile( eq( conflictFile ), any( IRepositoryFileData.class ), anyString() );
  }

  @Test
  public void copyFiles_NoOverwriteMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( file1, file2 ), PATH_DEST_DIR,
        FileService.MODE_NO_OVERWRITE );

    // emulate, file with the same name exists.
    mockFile( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_FILE_1 );

    operation.execute();

    Serializable destFolderId = destFolder.getId();

    // one file should be created, as there was 2 files, and 1 conflict
    verify( repo, times( 1 ) )
      .createFile( eq( destFolderId ), any( RepositoryFile.class ), any( IRepositoryFileData.class ), any(
        RepositoryFileAcl.class ), anyString() );
    verify( repo, never() )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), any( RepositoryFileAcl.class ),
        anyString() );
  }

  @Test
  public void copyFolders_NoOverwriteMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( folder1, folder2 ), PATH_DEST_DIR,
        FileService.MODE_NO_OVERWRITE );

    // emulate, file with the same name exists.
    mockFolder( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_DIR_1 );

    operation = spy( operation );
    doNothing().when( operation )
      .performFolderDeepCopy( any( RepositoryFile.class ), any( RepositoryFile.class ), anyInt() );

    operation.execute();

    Serializable destFolderId = destFolder.getId();

    verify( repo, times( 1 ) ).createFolder( eq( destFolderId ), any( RepositoryFile.class ),
      any( RepositoryFileAcl.class ), anyString() );

    verify( operation, times( 1 ) )
      .performFolderDeepCopy( any( RepositoryFile.class ), any( RepositoryFile.class ), anyInt() );

    verify( repo, never() )
      .createFile( any( RepositoryFile.class ), any( RepositoryFile.class ), any( IRepositoryFileData.class ), any(
        RepositoryFileAcl.class ), anyString() );
  }

  @Test
  public void copyFiles_RenameMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( file1, file2 ), PATH_DEST_DIR,
        FileService.MODE_RENAME );

    // emulate, file with the same name exists.
    RepositoryFile conflict = mockFile( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_FILE_1 );
    String conflictFilePath = conflict.getPath();
    RepositoryFileDto dtoConflictFile = mock( RepositoryFileDto.class );

    doReturn( dtoConflictFile ).when( webService ).getFile( eq( conflictFilePath ) );

    operation.execute();

    verify( repo, times( 2 ) )
      .createFile( eq( destFolder.getId() ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        any( RepositoryFileAcl.class ), anyString() );

    verify( repo, never() )
      .createFolder( any( RepositoryFile.class ), any( RepositoryFile.class ), any( RepositoryFileAcl.class ),
        anyString() );
  }

  @Test
  public void copyFolders_RenameMode() {
    CopyFilesOperation operation =
      new CopyFilesOperation( repo, webService, getIdList( folder1, folder2 ), PATH_DEST_DIR,
        FileService.MODE_RENAME );

    // emulate, file with the same name exists.
    RepositoryFile conflict = mockFolder( generateID(), DEFAULT, PATH_DEST_DIR + SEPARATOR + NAME_DIR_2 );
    String conflictFolderPath = conflict.getPath();
    RepositoryFileDto dtoConflictFolder = mock( RepositoryFileDto.class );

    doReturn( dtoConflictFolder ).when( webService ).getFile( eq( conflictFolderPath ) );

    operation = spy( operation );

    operation.execute();

    verify( repo, times( 2 ) )
      .createFolder( eq( destFolder.getId() ), any( RepositoryFile.class ), any( RepositoryFileAcl.class ),
        anyString() );

    verify( operation, times( 2 ) )
      .performFolderDeepCopy( any( RepositoryFile.class ), any( RepositoryFile.class ), anyInt() );

    verify( repo, never() )
      .createFile( any( Serializable.class ), any( RepositoryFile.class ), any( IRepositoryFileData.class ),
        any( RepositoryFileAcl.class ), anyString() );
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
