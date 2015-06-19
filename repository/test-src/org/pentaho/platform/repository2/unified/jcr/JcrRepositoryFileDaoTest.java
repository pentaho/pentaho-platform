package org.pentaho.platform.repository2.unified.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;

public class JcrRepositoryFileDaoTest extends DefaultUnifiedRepositoryBase {

  @Before
  public void beforeTest() {
    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );
  }

  @Test
  //Running within defined date
  public void testUpdateFile1() throws Exception {

    RepositoryFile newFile = createFile( "JcrRepositoryFileDaoTest1.test" );
    IRepositoryFileData dataMock = new SampleRepositoryFileData( "", true, 0 );

    Date startDate = new Date();
    newFile = new RepositoryFile.Builder( newFile ).createdDate( startDate ).build();
    repo.updateFile( newFile, dataMock, "edition #2" );

    List<VersionSummary> summaries = repo.getVersionSummaries( newFile.getId() );
    Date lastVersionDate = summaries.get( summaries.size() - 1 ).getDate();
    assertEquals( "incorrect version date", lastVersionDate, startDate );

  }
  
  
  @Test
  //Running without defined date
  public void testUpdateFile2() throws Exception {

    RepositoryFile newFile = createFile( "JcrRepositoryFileDaoTest1.test" );
    IRepositoryFileData dataMock = new SampleRepositoryFileData( "", true, 0 );

    Date startDate = new Date();
    newFile = new RepositoryFile.Builder( newFile ).createdDate( null ).build();
    repo.updateFile( newFile, dataMock, "edition #2" );

    Date finishDate = new Date();

    List<VersionSummary> summaries = repo.getVersionSummaries( newFile.getId() );
    Date lastVersionDate = summaries.get( summaries.size() - 1 ).getDate();
    if ( lastVersionDate.before( startDate ) || lastVersionDate.after( finishDate ) ) {
      fail( "incorrect version date" );
    }

  }

  private RepositoryFile createFile( String fileName ) throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );

    Date oldDate = new Date();
    RepositoryFile newFile = createSampleFile( parentFolder.getPath(), fileName, "sdfdf", false, 5, true );
    newFile = new RepositoryFile.Builder( newFile ).createdDate( oldDate ).build();

    return newFile;

  }

}
