package org.pentaho.platform.repository2.unified.webservices.jaxws;

import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultUnifiedRepositoryJaxwsWebServiceTest extends TestCase {

  public void testCreateBinaryFileFiltersUnsupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryJaxwsWebService ws =
      new DefaultUnifiedRepositoryJaxwsWebService( repositoryMock, mimeResolver );

    try {
      ws.createBinaryFile( "parent", fileDto( "blocked.unknown" ), binaryDataDto(), "msg" );
      fail( "Expected createBinaryFile to reject unsupported mime type" );
    } catch ( RuntimeException e ) {
      assertTrue( e.getMessage().contains( "unsupported file types" ) );
    }

    verify( repositoryMock, never() ).createFile( anyString(), any( RepositoryFile.class ),
      any( SimpleRepositoryFileData.class ), anyString() );
  }

  public void testCreateBinaryFileAllowsUnsupportedMimeTypeUnderEtcChildFolder() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "blocked.unknown" ) ).thenReturn( null );
    when( repositoryMock.getFileById( "etc-child-id" ) )
      .thenReturn( new RepositoryFile.Builder( "etc-child" ).path( "/etc/custom" ).folder( true ).build() );
    when( repositoryMock.createFile( eq( "etc-child-id" ), any( RepositoryFile.class ),
      any( SimpleRepositoryFileData.class ), eq( "msg" ) ) )
      .thenReturn( new RepositoryFile.Builder( "blocked.unknown" ).build() );

    DefaultUnifiedRepositoryJaxwsWebService ws =
      new DefaultUnifiedRepositoryJaxwsWebService( repositoryMock, mimeResolver );

    RepositoryFileDto result = ws.createBinaryFile( "etc-child-id", fileDto( "blocked.unknown" ), binaryDataDto(), "msg" );

    assertNotNull( result );
    verify( repositoryMock ).createFile( eq( "etc-child-id" ), any( RepositoryFile.class ),
      any( SimpleRepositoryFileData.class ), eq( "msg" ) );
  }

  public void testCreateBinaryFileWithAclFiltersUnsupportedMimeType() {
    IUnifiedRepository repositoryMock = mock( IUnifiedRepository.class );
    IPlatformMimeResolver mimeResolver = mock( IPlatformMimeResolver.class );
    when( mimeResolver.resolveMimeForFileName( "blocked.unknown" ) ).thenReturn( null );

    DefaultUnifiedRepositoryJaxwsWebService ws =
      new DefaultUnifiedRepositoryJaxwsWebService( repositoryMock, mimeResolver );

    try {
      ws.createBinaryFileWithAcl( "parent", fileDto( "blocked.unknown" ), binaryDataDto(), emptyAclDto(), "msg" );
      fail( "Expected createBinaryFileWithAcl to reject unsupported mime type" );
    } catch ( RuntimeException e ) {
      assertTrue( e.getMessage().contains( "unsupported file types" ) );
    }

    verify( repositoryMock, never() ).createFile( anyString(), any( RepositoryFile.class ),
      any( SimpleRepositoryFileData.class ), any(), anyString() );
  }

  private RepositoryFileDto fileDto( String name ) {
    RepositoryFileDto file = new RepositoryFileDto();
    file.setName( name );
    file.setFolder( false );
    return file;
  }

  private SimpleRepositoryFileDataDto binaryDataDto() {
    SimpleRepositoryFileData data = new SimpleRepositoryFileData(
      new ByteArrayInputStream( "test".getBytes() ), "UTF-8", "text/plain" );
    return SimpleRepositoryFileDataDto.convert( data );
  }

  private RepositoryFileAclDto emptyAclDto() {
    RepositoryFileAclDto acl = new RepositoryFileAclDto();
    acl.setAces( new ArrayList<RepositoryFileAclAceDto>(), false );
    return acl;
  }
}
