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


package org.pentaho.platform.repository2.unified.jcr;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

public class JcrRepositoryFileDaoIT extends DefaultUnifiedRepositoryBase {

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


  @Test
  public void getFileById_ReturnsNull_WhenDoesNotExist() throws Exception {
    loginAsRepositoryAdmin();
    RepositoryFile file = repo.getFileById( UUID.randomUUID().toString() );
    assertNull( file );
  }

  @Test
  public void getFileById_ReturnsFile_WhenExists() throws Exception {
    RepositoryFile file = createFile( "file-to-be-returned-by-id.test" );
    assertNotNull( file.getId() );

    loginAsRepositoryAdmin();
    RepositoryFile found = repo.getFileById( file.getId() );
    assertEquals( file.getName(), found.getName() );
  }
}
