package org.pentaho.platform.repository.solution.filebased;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

import java.util.EnumSet;

import static org.mockito.Mockito.*;

public class SolutionRepositoryVfsFileObjectTest {


  @Test
  public void initFileTest() throws Exception {

    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    String dsRef = "/etc/mondrian/SteelWheels";
    SolutionRepositoryVfsFileObject fileObject = new SolutionRepositoryVfsFileObject( fileRef );
    SolutionRepositoryVfsFileObject fileObjectSpy = spy( fileObject );

    IAclNodeHelper aclNodeHelper = mock( IAclNodeHelper.class );
    doReturn( aclNodeHelper ).when( fileObjectSpy ).getAclHelper();

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( aclNodeHelper ).canAccess( file, EnumSet.of( RepositoryFilePermission.READ ) );

    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    doReturn( file ).when( repository ).getFile( fileRef );
    doReturn( repository ).when( fileObjectSpy ).getRepository();

    fileObjectSpy.getName();
    verify( repository, times( 1 ) ).getFile( eq( dsRef ) );
    verify( repository, times( 1 ) ).getFile( eq( fileRef ) );
    verify( aclNodeHelper, times( 1 ) ).canAccess( any( RepositoryFile.class ), eq(
        EnumSet.of( RepositoryFilePermission.READ ) ) );

    fileRef = "/etca/mondriana/SteelWheels/schema.xml";

    fileObject = new SolutionRepositoryVfsFileObject( fileRef );
    fileObjectSpy = spy( fileObject );

    doReturn( aclNodeHelper ).when( fileObjectSpy ).getAclHelper();
    doReturn( false ).when( aclNodeHelper ).canAccess( file, EnumSet.of( RepositoryFilePermission.READ ) );
    doReturn( repository ).when( fileObjectSpy ).getRepository();

    fileObjectSpy.getName();
    verify( repository, times( 2 ) ).getFile( eq( fileRef ) );
    verify( aclNodeHelper, times( 2 ) ).canAccess( any( RepositoryFile.class ), eq(
        EnumSet.of( RepositoryFilePermission.READ ) ) );
  }
}
