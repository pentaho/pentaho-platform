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


package org.pentaho.platform.repository2.unified.webservices;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import junit.framework.TestCase;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.webservices.NodeRepositoryFileDataDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Test class for DefaultUnifiedRepositoryWebService
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class DefaultUnifiedRepositoryWebServiceTest extends TestCase {
  private IUnifiedRepository repository;
  private IUnifiedRepositoryWebService repositoryWS;

  public void setUp() throws Exception {
    repository = new MockUnifiedRepository( new MockUserProvider() );
    repositoryWS = new DefaultUnifiedRepositoryWebService( repository );
  }

  public void testFileMetadata() throws Exception {
    final RepositoryFile testfile =
        repository.createFile( repository.getFile( "/etc" ).getId(), new RepositoryFile.Builder( "testfile" ).build(),
            new SimpleRepositoryFileData( new ByteArrayInputStream( "test".getBytes() ),
              "UTF-8", "text/plain" ), null );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository is setup correctly
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 0, fileMetadata.size() );
    }

    final List<StringKeyStringValueDto> metadata = new ArrayList<StringKeyStringValueDto>();
    metadata.add( new StringKeyStringValueDto( "sample key", "sample value" ) );
    metadata.add( new StringKeyStringValueDto( "complex key?", "\"an even more 'complex' value\"! {and them some}" ) );

    repositoryWS.setFileMetadata( testfile.getId().toString(), metadata );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository sees the metadata
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repository.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
    }
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure we can get the same metadata back via the web service
      final List<StringKeyStringValueDto> fileMetadata = repositoryWS.getFileMetadata( testfile.getId().toString() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
      assertTrue( metadata.get( 0 ).equals( fileMetadata.get( 0 ) ) || metadata.get( 0 )
        .equals( fileMetadata.get( 1 ) ) );
      assertTrue( metadata.get( 1 ).equals( fileMetadata.get( 0 ) ) || metadata.get( 1 )
        .equals( fileMetadata.get( 1 ) ) );
    }
  }

  public void testGetDeletedFiles() throws Exception {

    repository.createFile( repository.getFile( "/etc" ).getId(), new RepositoryFile.Builder( "firstFileToDelete" ).build(),
      new SimpleRepositoryFileData( new ByteArrayInputStream( "test".getBytes() ),
        "UTF-8", "text/plain" ), null );
    repository.createFile( repository.getFile( "/etc" ).getId(), new RepositoryFile.Builder( "secondFileToDelete" ).build(),
      new SimpleRepositoryFileData( new ByteArrayInputStream( "test".getBytes() ),
        "UTF-8", "text/plain" ), null );

    List<RepositoryFile> expectedList = new ArrayList<>();
    expectedList.add( repository.getFile( "/etc/firstFileToDelete" ) );
    expectedList.add( repository.getFile( "/etc/secondFileToDelete" ) );

    repository.deleteFile( repository.getFile( "/etc/secondFileToDelete" ).getId(), null );
    repository.deleteFile( repository.getFile( "/etc/firstFileToDelete" ).getId(), null );

    List<RepositoryFile> list = repository.getDeletedFiles();
    assertEquals( 2, list.size() );
    assertEquals( list, expectedList );
  }

  public void testCreateFileFiltersUnsupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    RepositoryFileDto fileDto = new RepositoryFileAdapter().marshal( new RepositoryFile.Builder( "blocked.unknown" ).build() );

    try {
      ws.createFile( "parent", fileDto, createNodeRepositoryFileDataDto(), "msg" );
      fail( "Expected createFile to reject unsupported mime type" );
    } catch ( RuntimeException e ) {
      assertTrue( e.getMessage().contains( "unsupported file types" ) );
    }

    verify( repositoryMock, never() ).createFile( anyString(), any( RepositoryFile.class ),
      any( NodeRepositoryFileData.class ), anyString() );
  }

  public void testCreateFileAllowsSupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "allowed.prpt" ) ).thenReturn( "text/prpt" );
    RepositoryFile createdFile = new RepositoryFile.Builder( "allowed.prpt" ).build();
    when( repositoryMock.createFile( eq( "parent" ), any( RepositoryFile.class ), any( NodeRepositoryFileData.class ),
      eq( "msg" ) ) ).thenReturn( createdFile );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    RepositoryFileDto fileDto = new RepositoryFileAdapter().marshal( new RepositoryFile.Builder( "allowed.prpt" ).build() );

    RepositoryFileDto result = ws.createFile( "parent", fileDto, createNodeRepositoryFileDataDto(), "msg" );

    assertNotNull( result );
    verify( repositoryMock ).createFile( eq( "parent" ), any( RepositoryFile.class ),
      any( NodeRepositoryFileData.class ), eq( "msg" ) );
  }

  public void testCreateFileAllowsUnsupportedMimeTypeUnderEtcChildFolder() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "blocked.unknown" ) ).thenReturn( null );
    when( repositoryMock.getFileById( "etc-child-id" ) )
      .thenReturn( new RepositoryFile.Builder( "etc-child" ).path( "/etc/custom" ).folder( true ).build() );
    RepositoryFile createdFile = new RepositoryFile.Builder( "blocked.unknown" ).build();
    when( repositoryMock.createFile( eq( "etc-child-id" ), any( RepositoryFile.class ), any( NodeRepositoryFileData.class ),
      eq( "msg" ) ) ).thenReturn( createdFile );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    RepositoryFileDto fileDto = new RepositoryFileAdapter().marshal( new RepositoryFile.Builder( "blocked.unknown" ).build() );

    RepositoryFileDto result = ws.createFile( "etc-child-id", fileDto, createNodeRepositoryFileDataDto(), "msg" );

    assertNotNull( result );
    verify( repositoryMock ).createFile( eq( "etc-child-id" ), any( RepositoryFile.class ),
      any( NodeRepositoryFileData.class ), eq( "msg" ) );
  }

  public void testCopyFileFiltersUnsupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "/public/blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );

    try {
      ws.copyFile( "file-id", "/public/blocked.unknown", "msg" );
      fail( "Expected copyFile to reject unsupported mime type" );
    } catch ( RuntimeException e ) {
      assertTrue( e.getMessage().contains( "unsupported file types" ) );
    }

    verify( repositoryMock, never() ).copyFile( anyString(), anyString(), anyString() );
  }

  public void testCopyFileAllowsSupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "/public/allowed.prpt" ) ).thenReturn( "text/prpt" );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    ws.copyFile( "file-id", "/public/allowed.prpt", "msg" );

    verify( repositoryMock ).copyFile( "file-id", "/public/allowed.prpt", "msg" );
  }

  public void testCopyFileAllowsWhenDestinationIsExistingFolder() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( repositoryMock.getFile( "/public" ) )
      .thenReturn( new RepositoryFile.Builder( "public" ).path( "/public" ).folder( true ).build() );
    when( mimeResolver.resolveMimeForFileName( "/public" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    ws.copyFile( "file-id", "/public", "msg" );

    verify( repositoryMock ).copyFile( "file-id", "/public", "msg" );
  }

  public void testCopyFileAllowsUnsupportedMimeTypeUnderEtcChildDestination() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "/etc/custom/blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    ws.copyFile( "file-id", "/etc/custom/blocked.unknown", "msg" );

    verify( repositoryMock ).copyFile( "file-id", "/etc/custom/blocked.unknown", "msg" );
  }

  public void testMoveFileFiltersUnsupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "/public/blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );

    try {
      ws.moveFile( "file-id", "/public/blocked.unknown", "msg" );
      fail( "Expected moveFile to reject unsupported mime type" );
    } catch ( RuntimeException e ) {
      assertTrue( e.getMessage().contains( "unsupported file types" ) );
    }

    verify( repositoryMock, never() ).moveFile( anyString(), anyString(), anyString() );
  }

  public void testMoveFileAllowsSupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "/public/allowed.prpt" ) ).thenReturn( "text/prpt" );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    ws.moveFile( "file-id", "/public/allowed.prpt", "msg" );

    verify( repositoryMock ).moveFile( "file-id", "/public/allowed.prpt", "msg" );
  }

  public void testMoveFileAllowsWhenDestinationIsExistingFolder() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( repositoryMock.getFile( "/public" ) )
      .thenReturn( new RepositoryFile.Builder( "public" ).path( "/public" ).folder( true ).build() );
    when( mimeResolver.resolveMimeForFileName( "/public" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    ws.moveFile( "file-id", "/public", "msg" );

    verify( repositoryMock ).moveFile( "file-id", "/public", "msg" );
  }

  public void testMoveFileAllowsUnsupportedMimeTypeUnderEtcChildDestination() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "/etc/custom/blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryWebService ws = new DefaultUnifiedRepositoryWebService( repositoryMock, mimeResolver );
    ws.moveFile( "file-id", "/etc/custom/blocked.unknown", "msg" );

    verify( repositoryMock ).moveFile( "file-id", "/etc/custom/blocked.unknown", "msg" );
  }

  private NodeRepositoryFileDataDto createNodeRepositoryFileDataDto() {
    NodeRepositoryFileDataAdapter adapter = new NodeRepositoryFileDataAdapter();
    return adapter.marshal( new NodeRepositoryFileData( new DataNode( "root" ), 0 ) );
  }

  /**
   * Mock ICurrentUserProvider for testing
   */
  private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    @Override
    public String getUser() {
      return MockUnifiedRepository.root().getName();
    }

    @Override
    public List<String> getRoles() {
      return new ArrayList<String>();
    }
  }
}
