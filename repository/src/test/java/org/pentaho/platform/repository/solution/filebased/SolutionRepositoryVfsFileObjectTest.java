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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository.solution.filebased;


import mockit.Deencapsulation;
import mockit.Verifications;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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

  @Test
  public void testFileRefConstructor() {
    String expectedFileRef = "dummyFileRef";
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject =
      new SolutionRepositoryVfsFileObject( "dummyFileRef" );
    assertThat( solutionRepositoryVfsFileObject.getFileRef(), is( expectedFileRef ) );
  }

  @Test
  public void testGetRepo( @mockit.Mocked IUnifiedRepository mockUnifiedRepository ) {
    Deencapsulation.setField( SolutionRepositoryVfsFileObject.class, "repository", mockUnifiedRepository );
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject =
      new SolutionRepositoryVfsFileObject( "dummyFileRef" );
    assertThat( solutionRepositoryVfsFileObject.getRepository(), is( mockUnifiedRepository ) );
  }

  @Test
  public void testGetConvertHandler( @mockit.Mocked final IRepositoryContentConverterHandler mockConvertHandler ) {
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject =
      new SolutionRepositoryVfsFileObject( "dummyFileRef" );
    Deencapsulation.setField( solutionRepositoryVfsFileObject, "converterHandler", mockConvertHandler );
    assertThat( solutionRepositoryVfsFileObject.getConverterHandler(), is( mockConvertHandler ) );
  }

  //TODO: change or remove this test once getUrl is fixed or removed
  @Test
  public void testGetUrl() throws FileSystemException, MalformedURLException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject = new SolutionRepositoryVfsFileObject( fileRef );
    assertThat( solutionRepositoryVfsFileObject.getURL(), is( nullValue() ) );
  }

  @Test
  public void testExists( @mockit.Mocked final RepositoryFile mockRepoFile,
                          @mockit.Mocked final IAclNodeHelper mockAclHelper,
                          @mockit.Mocked final IUnifiedRepository mockUnifiedRepository ) throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    Deencapsulation.setField( SolutionRepositoryVfsFileObject.class, "repository", mockUnifiedRepository );
    SolutionRepositoryVfsFileObject testObject = new SolutionRepositoryVfsFileObject( fileRef );
    Deencapsulation.setField( testObject, "aclHelper", mockAclHelper );
    new mockit.NonStrictExpectations() {
      //CHECKSTYLE IGNORE check FOR NEXT 4 LINES
      {
        mockUnifiedRepository.getFile( anyString ); result = mockRepoFile;
        mockAclHelper.canAccess( mockRepoFile, EnumSet.of( RepositoryFilePermission.READ ) ); result = true;
      }
    };
    assertThat( testObject.exists(), is( true ) );

    new Verifications() {
      //CHECKSTYLE IGNORE check FOR NEXT 4 LINES
      {
        mockUnifiedRepository.getFile( anyString ); times = 2;
        mockAclHelper.canAccess( mockRepoFile, EnumSet.of( RepositoryFilePermission.READ ) ); times = 1;
      }
    };
  }

  @Test
  @SuppressWarnings( { "checkstyle:onestatementperline", "multiple statements help understand the mock definition" } )
  public void testExistsNot( @mockit.Mocked final IAclNodeHelper mockAclHelper,
                             @mockit.Mocked final IUnifiedRepository mockUnifiedRepository )
    throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    Deencapsulation.setField( SolutionRepositoryVfsFileObject.class, "repository", mockUnifiedRepository );
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject = new SolutionRepositoryVfsFileObject( fileRef );
    Deencapsulation.setField( solutionRepositoryVfsFileObject, "aclHelper", mockAclHelper );
    new mockit.NonStrictExpectations() {
      //CHECKSTYLE IGNORE check FOR NEXT 4 LINES
      {
        mockUnifiedRepository.getFile( anyString ); result = null;
        mockAclHelper.canAccess( null, EnumSet.of( RepositoryFilePermission.READ ) ); result = true;
      }
    };
    assertThat( solutionRepositoryVfsFileObject.exists(), is( false ) );
    new Verifications() {
      //CHECKSTYLE IGNORE check FOR NEXT 4 LINES
      {
        mockUnifiedRepository.getFile( anyString ); times = 2;
        mockAclHelper.canAccess( null, EnumSet.of( RepositoryFilePermission.READ ) ); times = 1;
      }
    };
  }

  @Test
  @SuppressWarnings( { "checkstyle:onestatementperline", "multiple statements help understand the mock definition" } )
  public void testContentRelatedMethods( @mockit.Mocked final RepositoryFile mockRepoFile,
                                         @mockit.Mocked final IAclNodeHelper mockAclHelper,
                                         @mockit.Mocked final IUnifiedRepository mockUnifiedRepository,
                                         @mockit.Mocked final SimpleRepositoryFileData mockFileData )
    throws FileSystemException {
    String fileRef = "/etc/mondrian/SteelWheels/schema.xml";
    new mockit.NonStrictExpectations() {
      //CHECKSTYLE IGNORE check FOR NEXT 6 LINES
      {
        mockUnifiedRepository.getFile( anyString ); result = mockRepoFile;
        mockUnifiedRepository.getDataForRead( (Serializable) any, (Class) any ); result = mockFileData;
        mockAclHelper.canAccess( mockRepoFile, EnumSet.of( RepositoryFilePermission.READ ) ); result = true;
        mockFileData.getStream(); result = new ByteArrayInputStream( "some string".getBytes() );
      }
    };
    Deencapsulation.setField( SolutionRepositoryVfsFileObject.class, "repository", mockUnifiedRepository );
    SolutionRepositoryVfsFileObject solutionRepositoryVfsFileObject = new SolutionRepositoryVfsFileObject( fileRef );
    Deencapsulation.setField( solutionRepositoryVfsFileObject, "aclHelper", mockAclHelper );
    FileContent someFileContent = solutionRepositoryVfsFileObject.getContent();
    assertThat( someFileContent, is( notNullValue() ) );
    assertThat( solutionRepositoryVfsFileObject.isContentOpen(), is( false ) );
    someFileContent.getInputStream();
    assertThat( solutionRepositoryVfsFileObject.isContentOpen(), is( true ) );
    someFileContent.close();
    assertThat( solutionRepositoryVfsFileObject.isContentOpen(), is( false ) );

    someFileContent = solutionRepositoryVfsFileObject.getContent();
    someFileContent.getInputStream();
    assertThat( solutionRepositoryVfsFileObject.isContentOpen(), is( true ) );
    solutionRepositoryVfsFileObject.close();
    assertThat( solutionRepositoryVfsFileObject.isContentOpen(), is( false ) );
  }
}
