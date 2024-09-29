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

package org.pentaho.platform.web.http.api.resources.operations;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Ivan Nikolaichuk
 */

/**
 * Goals of this test are:
 * 1. verify, that recursion ends.
 * 2. verify, that accurate number of files and folders were created.
 *
 * <p/>
 * Tests are called N_LevelDeepness, where by deepness meant folders-hierarchy deepness.
 * <p/>
 * Example:
 * <p/>
 * 1 level deepness: -folder
 *                      -file
 *                      -file
 * <p/>
 * 3 level deepness: -folder
 *                      -folder
 *                          -file
 *                      -folder
 *                          -folder
 */
public class CopyFilesOperation_DeepFolderCopyTest {
  private static IUnifiedRepository repo;
  private static CopyFilesOperation operation;
  private RepositoryFile from;
  private RepositoryFile to;

  @Before
  public void init() {
    repo = mock( IUnifiedRepository.class );
    operation = mock( CopyFilesOperation.class );
    doReturn( repo ).when( operation ).getRepository();

    from = mockFolder();
    to = mockFolder();

    doReturn( mockFolder() ).when( repo )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );

    doCallRealMethod().when( operation ).performFolderDeepCopy( any( RepositoryFile.class ),
      any( RepositoryFile.class ), anyInt() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void deepCopyOfFile_Fails() {
    RepositoryFile file1 = mockFile();
    RepositoryFile file2 = mockFile();
    doCallRealMethod().when( operation ).performFolderDeepCopy( file1, file2, null );

    operation.performFolderDeepCopy( file1, file2, null );
  }

  @Test
  public void one_LevelDeepness() {
    List<RepositoryFile> files = listOfMockedFiles( 3 );
    mockRequest( from, files );

    operation.performFolderDeepCopy( from, to, CopyFilesOperation.DEFAULT_DEEPNESS );

    performVerification( 0, files.size() );
  }

  @Test
  public void two_LevelDeepness() {
    int numberOfFolders = 3;
    int filesInEachFolder = 3;
    List<RepositoryFile> folders = listOfMockedFolders( numberOfFolders );
    mockRequest( from, folders );

    for ( RepositoryFile folder : folders ) {
      mockRequest( folder, listOfMockedFiles( filesInEachFolder ) );
    }

    operation.performFolderDeepCopy( from, to, CopyFilesOperation.DEFAULT_DEEPNESS );

    // one stands for root folder.
    performVerification( numberOfFolders, numberOfFolders * filesInEachFolder );
  }

  @Test
  public void six_LevelDeepness_Folders() {
    int expectedFolders = 62;
    int foldersInEachFolderNotLastLevel = 2;

    List<RepositoryFile> foldersSecondLevel = listOfMockedFolders( foldersInEachFolderNotLastLevel );
    List<RepositoryFile> foldersThirdLevel = listOfMockedFolders( foldersInEachFolderNotLastLevel );
    List<RepositoryFile> foldersForthLevel = listOfMockedFolders( foldersInEachFolderNotLastLevel );
    List<RepositoryFile> foldersFiveLevel = listOfMockedFolders( foldersInEachFolderNotLastLevel );
    List<RepositoryFile> foldersSixLevel = listOfMockedFolders( foldersInEachFolderNotLastLevel );

    mockRequest( from, foldersSecondLevel );

    for ( RepositoryFile folder : foldersSecondLevel ) {
      mockRequest( folder, foldersThirdLevel );
    }

    for ( RepositoryFile folder : foldersThirdLevel ) {
      mockRequest( folder, foldersForthLevel );
    }

    for ( RepositoryFile folder : foldersForthLevel ) {
      mockRequest( folder, foldersFiveLevel );
    }

    for ( RepositoryFile folder : foldersFiveLevel ) {
      mockRequest( folder, foldersSixLevel );
    }

    operation.performFolderDeepCopy( from, to, CopyFilesOperation.DEFAULT_DEEPNESS );

    performVerification( expectedFolders, 0 );
  }


  /**
   * Verifies: - performFolderDeepCopy method was called {@code newFolders + 1} times, where 1 performs as the first
   * method call.
   * <p/>
   * - there were created {@code numberOfFiles} files. - there were created {@code newFolders} folders.
   */
  private void performVerification( int newFolders, int newFiles ) {
    // 1 stands for first method-call
    verify( operation, times( newFolders + 1 ) )
      .performFolderDeepCopy( any( RepositoryFile.class ), any( RepositoryFile.class ), anyInt() );

    verify( repo, times( newFolders ) )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );

    verify( repo, times( newFiles ) ).createFile( anyString(), any( RepositoryFile.class ), nullable(
      IRepositoryFileData.class ), nullable( String.class ) );
  }

  /**
   * Enforces to return provided {@code children}, when making a RepositoryRequest to know {@code folder} content.
   */
  private void mockRequest( RepositoryFile folder, List<RepositoryFile> children ) {
    RepositoryRequest mockedRequest = mock( RepositoryRequest.class );
    doReturn( mockedRequest ).when( operation ).createRepoRequest( eq( folder ), anyInt() );
    doReturn( children ).when( repo ).getChildren( mockedRequest );
  }

  private List<RepositoryFile> listOfMockedFiles( final int files ) {
    return listOfMockedRepoFiles( files, false );
  }

  private List<RepositoryFile> listOfMockedFolders( final int files ) {
    return listOfMockedRepoFiles( files, true );
  }

  private List<RepositoryFile> listOfMockedRepoFiles( final int files, final boolean isFolder ) {
    return new ArrayList<RepositoryFile>() { {
        for ( int i = 0; i < files; i++ ) {
          if ( isFolder ) {
            add( mockFolder() );
          } else {
            add( mockFile() );
          }
        }
      }
    };
  }

  public static RepositoryFile mockFile( String id ) {
    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( id ).when( file ).getId();
    doReturn( false ).when( file ).isFolder();

    return file;
  }

  public static RepositoryFile mockFile() {
    return mockFile( generateID() );
  }


  public static RepositoryFile mockFolder( String id ) {
    RepositoryFile folder = mock( RepositoryFile.class );
    doReturn( id ).when( folder ).getId();
    doReturn( true ).when( folder ).isFolder();

    return folder;
  }

  public static RepositoryFile mockFolder() {
    return mockFolder( generateID() );
  }

  private static String generateID() {
    return UUID.randomUUID().toString();
  }
}
