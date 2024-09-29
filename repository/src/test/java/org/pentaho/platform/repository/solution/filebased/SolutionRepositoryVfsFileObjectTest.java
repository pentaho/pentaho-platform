/*!
 *
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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.ByteArrayInputStream;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class SolutionRepositoryVfsFileObjectTest {
  private static MockedStatic<PentahoSystem> pentahoSystemMock;
  private static IUnifiedRepository mockUnifiedRepository;

  @BeforeClass
  public static void beforeAll() {
    pentahoSystemMock = Mockito.mockStatic( PentahoSystem.class );
    mockUnifiedRepository = mock( IUnifiedRepository.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( eq( IUnifiedRepository.class ), any() ) )
      .thenAnswer( invocation -> mockUnifiedRepository );
  }

  @AfterClass
  public static void afterAll() {
    pentahoSystemMock.close();
  }

  @After
  public void afterEach() {
    reset( mockUnifiedRepository );
  }

  @Test
  public void initFileTest() {

    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    String dsRef = "/etc/mondrian/SteelWheels";
    SolutionRepositoryVfsFileObject fileObject = new SolutionRepositoryVfsFileObject( fileRef );
    SolutionRepositoryVfsFileObject fileObjectSpy = spy( fileObject );

    IAclNodeHelper aclNodeHelper = mock( IAclNodeHelper.class );
    doReturn( aclNodeHelper ).when( fileObjectSpy ).getAclHelper();

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( aclNodeHelper ).canAccess( file, EnumSet.of( RepositoryFilePermission.READ ) );

    IUnifiedRepository repository = mock( IUnifiedRepository.class );
    doReturn( file ).when( repository ).getFile( any() );
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

  @Test
  public void testFileRefConstructor() {
    String expectedFileRef = "dummyFileRef";
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject =
      new SolutionRepositoryVfsFileObject( "dummyFileRef" );
    assertEquals( expectedFileRef, solutionRepositoryVfsFileObject.getFileRef() );
  }

  @Test
  public void testGetRepo() {
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject =
      new SolutionRepositoryVfsFileObject( "dummyFileRef" );
    assertEquals( mockUnifiedRepository, solutionRepositoryVfsFileObject.getRepository() );
  }

  @Test
  public void testGetConvertHandler() {
    IRepositoryContentConverterHandler mockConvertHandler = mock( IRepositoryContentConverterHandler.class );
    pentahoSystemMock.when( () -> PentahoSystem.get( eq( IRepositoryContentConverterHandler.class ) ) )
      .thenReturn( mockConvertHandler );
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject =
      new SolutionRepositoryVfsFileObject( "dummyFileRef" );
    assertEquals( mockConvertHandler, solutionRepositoryVfsFileObject.getConverterHandler() );
  }

  //TODO: change or remove this test once getUrl is fixed or removed
  @Test
  public void testGetUrl() throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject = new SolutionRepositoryVfsFileObject( fileRef );
    assertNull( solutionRepositoryVfsFileObject.getURL() );
  }

  @Test
  public void testExists() throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    RepositoryFile mockRepoFile = mock( RepositoryFile.class );
    IAclNodeHelper mockAclHelper = mock( IAclNodeHelper.class );

    SolutionRepositoryVfsFileObject testObject = new SolutionRepositoryVfsFileObject( fileRef );
    SolutionRepositoryVfsFileObject.setTestAclHelper( mockAclHelper );

    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( mockRepoFile );
    when( mockAclHelper.canAccess( mockRepoFile, EnumSet.of( RepositoryFilePermission.READ ) ) ).thenReturn( true );
    assertTrue( testObject.exists() );

    verify( mockUnifiedRepository, times( 2 ) ).getFile( nullable( String.class ) );
    verify( mockAclHelper ).canAccess( mockRepoFile, EnumSet.of( RepositoryFilePermission.READ ) );
  }

  @Test
  public void testExistsNot() throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    IAclNodeHelper mockAclHelper = mock( IAclNodeHelper.class );

    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject = new SolutionRepositoryVfsFileObject( fileRef );
    SolutionRepositoryVfsFileObject.setTestAclHelper( mockAclHelper );

    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( null );
    when( mockAclHelper.canAccess( null, EnumSet.of( RepositoryFilePermission.READ ) ) ).thenReturn( true );

    assertFalse( solutionRepositoryVfsFileObject.exists() );

    verify( mockUnifiedRepository, times( 2 ) ).getFile( nullable( String.class ) );
    verify( mockAclHelper ).canAccess( null, EnumSet.of( RepositoryFilePermission.READ ) );
  }

  @Test
  public void testContentRelatedMethods() throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";

    RepositoryFile mockRepoFile = mock( RepositoryFile.class );
    IAclNodeHelper mockAclHelper = mock( IAclNodeHelper.class );
    SimpleRepositoryFileData mockFileData = mock( SimpleRepositoryFileData.class );

    when( mockUnifiedRepository.getFile( nullable( String.class ) ) ).thenReturn( mockRepoFile );
    when( mockAclHelper.canAccess( mockRepoFile, EnumSet.of( RepositoryFilePermission.READ ) ) ).thenReturn( true );
    when( mockUnifiedRepository.getDataForRead( any(), any() ) ).thenReturn( mockFileData );
    when( mockFileData.getStream() ).thenReturn( new ByteArrayInputStream( "some string".getBytes() ) );

    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject = new SolutionRepositoryVfsFileObject( fileRef );

    SolutionRepositoryVfsFileObject.setTestAclHelper( mockAclHelper );

    FileContent someFileContent = solutionRepositoryVfsFileObject.getContent();
    assertNotNull( someFileContent );
    assertFalse( solutionRepositoryVfsFileObject.isContentOpen() );
    someFileContent.getInputStream();
    assertTrue( solutionRepositoryVfsFileObject.isContentOpen() );
    someFileContent.close();
    assertFalse( solutionRepositoryVfsFileObject.isContentOpen() );

    someFileContent = solutionRepositoryVfsFileObject.getContent();
    someFileContent.getInputStream();
    assertTrue( solutionRepositoryVfsFileObject.isContentOpen() );
    solutionRepositoryVfsFileObject.close();
    assertFalse( solutionRepositoryVfsFileObject.isContentOpen() );
  }
}
