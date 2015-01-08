package org.pentaho.platform.repository.solution.filebased;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.jcr.IAclNodeHelper;

import static org.mockito.Mockito.*;

public class SolutionRepositoryVfsFileObjectTest {


  @Test
  public void initFileTest() throws Exception {
    String datasource = "SteelWheels";
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    SolutionRepositoryVfsFileObject fileObject = new SolutionRepositoryVfsFileObject( fileRef );
    SolutionRepositoryVfsFileObject fileObjectSpy = spy( fileObject );

    IAclNodeHelper aclNodeHelper = mock( IAclNodeHelper.class );
    doReturn( aclNodeHelper ).when( fileObjectSpy ).getAclHelper();
    doReturn( true ).when( aclNodeHelper ).hasAccess( datasource, IAclNodeHelper.DatasourceType.MONDRIAN );

    RepositoryFile file = mock( RepositoryFile.class );

    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    doReturn( file ).when( repository ).getFile( fileRef );
    doReturn( repository ).when( fileObjectSpy ).getRepository();

    fileObjectSpy.getName();
    verify( repository, times( 1 ) ).getFile( anyString() );

    fileRef = "/etca/mondriana/SteelWheels/schema.xml";

    fileObject = new SolutionRepositoryVfsFileObject( fileRef );
    fileObjectSpy = spy( fileObject );

    doReturn( aclNodeHelper ).when( fileObjectSpy ).getAclHelper();
    doReturn( false ).when( aclNodeHelper ).hasAccess( datasource, IAclNodeHelper.DatasourceType.MONDRIAN );
    doReturn( repository ).when( fileObjectSpy ).getRepository();

    fileObjectSpy.getName();
    verify( repository, times( 1 ) ).getFile( anyString() );
  }
}
