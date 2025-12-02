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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Goals of this test are:
 * 1. verify, that recursion ends.
 * 2. verify, that accurate number of files and folders were created.
 * <p/>
 * Tests are called N_LevelDeepness, whereby deepness meant folders-hierarchy deepness.
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

  @Before
  public void init() {
    repo = mock( IUnifiedRepository.class );
    operation = mock( CopyFilesOperation.class );
    doReturn( repo ).when( operation ).getRepository();

    doReturn( mockFolder() ).when( repo )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );

    doCallRealMethod().when( operation ).performFolderDeepCopy( nullable( RepositoryFile.class ),
      nullable( RepositoryFile.class ), nullable( Integer.class) );

    doCallRealMethod().when( operation ).performFolderDeepCopy( nullable( RepositoryFile.class ),
      nullable( RepositoryFile.class ), nullable( Integer.class ), nullable( RepositoryFile.class ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void deepCopyFromFile_Fails() {
    operation.performFolderDeepCopy( mockFile(), mockFolder(), CopyFilesOperation.DEFAULT_DEEPNESS );
  }

  @Test( expected = IllegalArgumentException.class )
  public void deepCopyToFile_Fails() {
    operation.performFolderDeepCopy( mockFolder(), mockFile(), CopyFilesOperation.DEFAULT_DEEPNESS );
  }

  @Test( expected = IllegalArgumentException.class )
  public void deepCopyFromNull_Fails() {
    operation.performFolderDeepCopy( null, mockFolder(), CopyFilesOperation.DEFAULT_DEEPNESS );
  }

  @Test( expected = IllegalArgumentException.class )
  public void deepCopyToNull_Fails() {
    operation.performFolderDeepCopy( mockFolder(), null, CopyFilesOperation.DEFAULT_DEEPNESS );
  }

  @Test
  public void deepCopyDeepnessNull() {
    int numberOfFolders = 3;
    int filesInEachFolder = 3;
    List<RepositoryFile> folders = listOfMockedFolders( numberOfFolders );
    RepositoryFile from = mockFolder();
    mockRequest( from, folders );
    RepositoryFile to = mockFolder();

    for ( RepositoryFile folder : folders ) {
      mockRequest( folder, listOfMockedFiles( filesInEachFolder ) );
    }

    operation.performFolderDeepCopy( from, to, null );

    // one stands for root folder.
    performVerification( numberOfFolders, numberOfFolders * filesInEachFolder );
  }

  @Test
  public void deepCopyDeepnessNegative() {
    int numberOfFolders = 3;
    int filesInEachFolder = 3;
    List<RepositoryFile> folders = listOfMockedFolders( numberOfFolders );
    RepositoryFile from = mockFolder();
    mockRequest( from, folders );
    RepositoryFile to = mockFolder();

    for ( RepositoryFile folder : folders ) {
      mockRequest( folder, listOfMockedFiles( filesInEachFolder ) );
    }

    operation.performFolderDeepCopy( from, to, -1 );

    // one stands for root folder.
    performVerification( numberOfFolders, numberOfFolders * filesInEachFolder );
  }

  @Test
  public void one_LevelDeepness() {
    List<RepositoryFile> files = listOfMockedFiles( 3 );
    RepositoryFile from = mockFolder();
    mockRequest( from, files );
    RepositoryFile to = mockFolder();

    operation.performFolderDeepCopy( from, to, CopyFilesOperation.DEFAULT_DEEPNESS );

    performVerification( 0, files.size() );
  }

  @Test
  public void nestedSelfCopy() {
    // Simulate copying /a into /a
    // Source folder /a has one subfolder: /a/b
    // When copying /a into /a, the new /a/a subfolder should be skipped to avoid infinite recursion
    // /a/b should be copied into /a/a/b
    // all files should be copied

    // mock "from" source folder structure
    RepositoryFile from = mockFolderWithPath( "/a" );
    RepositoryFile subfolder = mockFolderWithPath( "/a/b" );
    // mock "to" destination folder created apriori, as per actual use case
    // for use case details see `CopyFileOperation` functions: `copyRenameMode` and `copyNoOverrideMode`
    RepositoryFile to = mockFolderWithPath( "/a/a" );

    // mock "from" source folder children, includes new "to" folder /a/a created before performFolderDeepCopy call
    mockRequest( from, List.of( to, subfolder, mockFile() ) );

    // mock subfolder /a/b children
    mockRequest( subfolder, listOfMockedFiles( 2 ) );

    // mock /a/a/b child creation via repo.createFolder() call
    RepositoryFile createdSubfolder = mockFolderWithPath( "/a/a/b" );
    doAnswer( invocation -> {
      RepositoryFile createdFolder = invocation.getArgument( 1 );
      if ( createdFolder.getPath().equals( "/a/b" ) ) {
        // add newly created /a/a/b folder as child of /a/a to simulate actual structure
        mockRequest( to, List.of( createdSubfolder ) );
        return createdSubfolder;
      }
      return mockFolder();
    } ).when( repo )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );

    operation.performFolderDeepCopy( from, to, CopyFilesOperation.DEFAULT_DEEPNESS );

    // Executes performFolderDeepCopy() for:
    // - Root call (1): /a -> /a/a
    // - subfolder (1): /a/b -> /a/a/b
    // - to (0): /a/a folder is the new child folder that does not belong to the original /a folder structure,
    //            skip to avoid infinite recursion
    // Total: 2 calls
    verify( operation, times( 2 ) )
      .performFolderDeepCopy( any( RepositoryFile.class ), any( RepositoryFile.class ), anyInt(),
        any( RepositoryFile.class ) );
    verify( operation, times( 1 ) ).performFolderDeepCopy(
      argThat( arg -> arg.getPath().equals( "/a" ) ),
      argThat( arg -> arg.getPath().equals( "/a/a" ) ),
      anyInt(), argThat( arg -> arg.getPath().equals( "/a/a" ) ) );
    verify( operation, times( 1 ) ).performFolderDeepCopy(
      argThat( arg -> arg.getPath().equals( "/a/b" ) ),
      any( RepositoryFile.class ), // /a/b/a/b
      anyInt(), argThat( a -> a.getPath().equals( "/a/a" ) ) );

    // Should create one folder:
    // - ancestorSubfolder (1): /a/b -> /a/a/b
    // Total: 1 folder
    verify( repo, times( 1 ) )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );
    verify( repo, times( 1 ) ).createFolder(
      eq( to.getId() ),
      argThat( arg -> arg.getPath().equals( "/a/b" ) ),
      nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    // Should create files:
    // - 1 file from /a folder: /a -> /a/a
    // - 2 files from /a/b folder: /a/b -> /a/a/b
    // Total: 3 files
    verify( repo, times( 3 ) ).createFile( anyString(), any( RepositoryFile.class ), nullable(
      IRepositoryFileData.class ), nullable( String.class ) );
    verify( repo, times( 1 ) ).createFile(
      eq( to.getId() ), // /a/a
      any( RepositoryFile.class ),
      nullable( IRepositoryFileData.class ), nullable( String.class ) );
    verify( repo, times( 2 ) ).createFile(
      eq( createdSubfolder.getId() ), // /a/a/b
      any( RepositoryFile.class ),
      nullable( IRepositoryFileData.class ), nullable( String.class ) );
  }

  @Test
  public void nestedSelfCopyIntoSubfolder() {
    // Simulate copying /a into /a/b
    // Source folder /a has one subfolder: /a/b
    // When copying /a into /a/b, the new /a/b/a subfolder should be skipped to avoid infinite recursion
    // /a/b should be copied into /a/b/a/b
    // all files should be copied

    // mock "from" source folder structure
    RepositoryFile from = mockFolderWithPath( "/a" );
    RepositoryFile ancestorSubfolder = mockFolderWithPath( "/a/b" );

    // mock "from" source folder children
    mockRequest( from, List.of( ancestorSubfolder, mockFile() ) );

    // mock "to" destination folder created apriori, as per actual use case
    // for use case details see `CopyFileOperation` functions: `copyRenameMode` and `copyNoOverrideMode`
    RepositoryFile to = mockFolderWithPath( "/a/b/a" );

    // mock ancestorSubfolder children, includes new "to" folder /a/b/a created before performFolderDeepCopy call
    mockRequest( ancestorSubfolder, List.of( to, mockFile(), mockFile() ) );

    // mock /a/b/a/b child creation via repo.createFolder() call
    RepositoryFile createdSubfolderB = mockFolderWithPath( "/a/b/a/b" );
    doAnswer( invocation -> {
      RepositoryFile createdFolder = invocation.getArgument( 1 );
      if ( createdFolder.getPath().equals( "/a/b" ) ) {
        // add newly created /a/b/a/b folder as child of /a/b/a to simulate actual structure
        mockRequest( to, List.of( createdSubfolderB ) );
        return createdSubfolderB;
      }
      return mockFolder();
    } ).when( repo )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );

    operation.performFolderDeepCopy( from, to, CopyFilesOperation.DEFAULT_DEEPNESS );

    // Should process children of:
    // - Root call (1): /a -> /a/b/a
    // - ancestorSubFolder (1): /a/b -> /a/b/a/b
    // - to (0) - /a/b/a folder is the new child folder that does not belong to the original /a folder structure,
    //            skip to avoid infinite recursion
    // Total: 2 calls
    verify( operation, times( 2 ) )
      .performFolderDeepCopy( any( RepositoryFile.class ), any( RepositoryFile.class ), anyInt(),
        any( RepositoryFile.class ) );
    verify( operation, times( 1 ) ).performFolderDeepCopy(
      argThat( arg -> arg.getPath().equals( "/a" ) ),
      argThat( arg -> arg.getPath().equals( "/a/b/a" ) ),
      anyInt(), argThat( arg -> arg.getPath().equals( "/a/b/a" ) ) );
    verify( operation, times( 1 ) ).performFolderDeepCopy(
      argThat( arg -> arg.getPath().equals( "/a/b" ) ),
      any( RepositoryFile.class ), // /a/b/a/b
      anyInt(), argThat( a -> a.getPath().equals( "/a/b/a" ) ) );

    // Should create folder:
    // - ancestorSubfolder (1): /a/b -> /a/b/a/b
    // Total: 1 folder
    verify( repo, times( 1 ) )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), nullable( RepositoryFileAcl.class ),
        nullable( String.class ) );
    verify( repo, times( 1 ) ).createFolder(
      eq( to.getId() ),
      argThat( arg -> arg.getPath().equals( "/a/b" ) ),
      nullable( RepositoryFileAcl.class ), nullable( String.class ) );

    // Should create files:
    // - 1 file from sourceChildren (1) - from /a to /a/b/a
    // - 2 files from ancestorSubfolder (2) - from /a/b to /a/b/a/b
    // Total: 3 files
    verify( repo, times( 3 ) ).createFile( anyString(), any( RepositoryFile.class ), nullable(
      IRepositoryFileData.class ), nullable( String.class ) );
  }

  @Test
  public void two_LevelDeepness() {
    int numberOfFolders = 3;
    int filesInEachFolder = 3;
    List<RepositoryFile> folders = listOfMockedFolders( numberOfFolders );
    RepositoryFile from = mockFolder();
    mockRequest( from, folders );
    RepositoryFile to = mockFolder();

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

    RepositoryFile from = mockFolder();
    mockRequest( from, foldersSecondLevel );
    RepositoryFile to = mockFolder();

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
      .performFolderDeepCopy( nullable( RepositoryFile.class ), nullable( RepositoryFile.class ),
        nullable( Integer.class ), nullable( RepositoryFile.class ) );

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
    return new ArrayList<>() { {
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

  public static RepositoryFile mockFolderWithPath( String path ) {
    RepositoryFile folder = mockFolder( generateID() );
    doReturn( path ).when( folder ).getPath();

    return folder;
  }

  private static String generateID() {
    return UUID.randomUUID().toString();
  }
}
