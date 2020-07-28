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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileServiceTest {

  private static final String PATH_CONTROL_CHARACTER = "Create Control Character \u0017 File.xml";
  private static final String PATH_SPECIAL_CHARACTERS = "éÉèÈçÇºªüÜ@£§";
  private static final String PATH_JAPANESE_CHARACTERS = "キャラクター";
  private static final String PATH_OK_CHARACTERS = "file_test.xml";
  public static final String UTF_8 = StandardCharsets.UTF_8.name();

  private FileService fileService;

  @Before
  public void setUp() throws Exception {
    fileService = spy( FileService.class );
  }


  @Test
  public void testCheckIfForceFlushIsSetOnCreateFile() throws Exception {
    InputStream inputStream = new ByteArrayInputStream( new byte[] {} );
    RepositoryFileOutputStream fileOutputStream = mock( RepositoryFileOutputStream.class );
    when( fileService.getRepositoryFileOutputStream( anyString() ) ).thenReturn( fileOutputStream );
    fileService.createFile( UTF_8, PATH_OK_CHARACTERS, inputStream );
    verify( fileOutputStream, times( 1 ) ).forceFlush( true );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void testCreateFile_Forbidden_ControlCharactersFound() throws Exception {
    InputStream inputStream = mock( InputStream.class );

    fileService.createFile( UTF_8, PATH_CONTROL_CHARACTER, inputStream );
    verify( fileService, times( 0 ) ).idToPath( PATH_CONTROL_CHARACTER );
  }

  @Test
  public void testCreateFile_Special_Characters() throws Exception {
    InputStream inputStream = mock( InputStream.class );
    RepositoryFileOutputStream repositoryFileOutputStream = mock( RepositoryFileOutputStream.class );

    doReturn( repositoryFileOutputStream ).when( fileService ).getRepositoryFileOutputStream( anyString() );
    doReturn( 1 ).when( fileService ).copy( inputStream, repositoryFileOutputStream );

    fileService.createFile( UTF_8, PATH_SPECIAL_CHARACTERS, inputStream );
    verify( inputStream ).close();
  }

  @Test
  public void testCreateFile_Japanese_Characters() throws Exception {
    InputStream inputStream = mock( InputStream.class );
    RepositoryFileOutputStream repositoryFileOutputStream = mock( RepositoryFileOutputStream.class );

    doReturn( repositoryFileOutputStream ).when( fileService ).getRepositoryFileOutputStream( anyString() );
    doReturn( 1 ).when( fileService ).copy( inputStream, repositoryFileOutputStream );

    fileService.createFile( UTF_8, PATH_JAPANESE_CHARACTERS, inputStream );
    verify( inputStream ).close();
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void testDoCreateDirSafe_ControlCharactersFound() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    fileService.doCreateDirSafe( PATH_CONTROL_CHARACTER );
  }

  @Test
  public void testDoCreateDirSafe_Special_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    doReturn( true ).when( fileService ).doCreateDirFor( anyString() );

    assertTrue( fileService.doCreateDirSafe( PATH_SPECIAL_CHARACTERS ) );
  }

  @Test
  public void testDoCreateDirSafe_Japanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    doReturn( true ).when( fileService ).doCreateDirFor( anyString() );

    assertTrue( fileService.doCreateDirSafe( PATH_JAPANESE_CHARACTERS ) );
  }

  @Test
  public void testIsValidFolderName_DecodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertFalse( fileService.isValidFolderName( PATH_CONTROL_CHARACTER ) );
  }

  @Test
  public void testIsValidFolderName_EncodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertFalse(
      fileService.isValidFolderName( encode( PATH_CONTROL_CHARACTER ) ) );
  }

  @Test
  public void testIsValidFolderName_DecodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFolderName( PATH_JAPANESE_CHARACTERS ) );
  }

  @Test
  public void testIsValidFolderName_EncodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFolderName( encode( PATH_JAPANESE_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFolderName_DecodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFolderName( PATH_SPECIAL_CHARACTERS ) );
  }

  @Test
  public void testIsValidFolderName_EncodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFolderName( encode( PATH_SPECIAL_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFolderName_Decoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames = { ".", ".." };
    for ( String invalidName : invalidNames ) {
      assertFalse( fileService.isValidFolderName( invalidName ) );
    }
  }

  @Test
  public void testIsValidFolderName_Encoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames = { ".", ".." };
    for ( String invalidName : invalidNames ) {
      assertFalse(
        fileService.isValidFolderName( encode( invalidName ) ) );
    }
  }

  @Test
  public void testIsValidFileName_DecodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertFalse( fileService.isValidFileName( PATH_CONTROL_CHARACTER ) );
  }

  @Test
  public void testIsValidFileName_EncodedControl_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String pathControlCharacter = PATH_CONTROL_CHARACTER;

    assertFalse(
      fileService.isValidFileName( encode( pathControlCharacter ) ) );
  }

  @Test
  public void testIsValidFileName_DecodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFileName( PATH_JAPANESE_CHARACTERS ) );
  }

  @Test
  public void testIsValidFileName_EncodedJapanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFileName( encode( PATH_JAPANESE_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFileName_DecodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue( fileService.isValidFileName( PATH_SPECIAL_CHARACTERS ) );
  }

  @Test
  public void testIsValidFileName_EncodedSpecial_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();

    assertTrue(
      fileService.isValidFileName( encode( PATH_SPECIAL_CHARACTERS ) ) );
  }

  @Test
  public void testIsValidFileName_Decoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames =
      { null, "", " ", " x", "x ", " x ", "\tx", "x\t", "\tx\t", "\rx", "x\r", "\rx\r", "\nx", "x\n", "\nx\n" };
    for ( String invalidName : invalidNames ) {
      assertFalse( fileService.isValidFileName( invalidName ) );
    }
  }

  @Test
  public void testIsValidFileName_Encoded_InvalidCases() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    String[] invalidNames =
      { "", " x", "x ", " x ", "\tx", "x\t", "\tx\t", "\rx", "x\r", "\rx\r", "\nx", "x\n", "\nx\n" };
    for ( String invalidName : invalidNames ) {
      assertFalse(
        fileService.isValidFileName( encode( invalidName ) ) );
    }
  }

  private static String encode( String pathControlCharacter ) throws UnsupportedEncodingException {
    return URLEncoder.encode( pathControlCharacter, UTF_8 );
  }
}
