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

import java.io.InputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FileServiceTest {

  private static final String PATH_CONTROL_CHARACTER = "Create Control Character \u0017 File.xml";
  private static final String PATH_SPECIAL_CHARACTERS = "éÉèÈçÇºªüÜ@£§";
  private static final String PATH_JAPANESE_CHARACTERS = "キャラクター";

  private FileService fileService;

  @Before
  public void setUp() throws Exception {
    fileService = spy( FileService.class );
  }

  @Test( expected = FileService.InvalidNameException.class )
  public void testCreateFile_Forbidden_ControlCharactersFound() throws Exception {
    String charsetName = "charsetName";
    InputStream inputStream = mock( InputStream.class );

    fileService.createFile( charsetName, PATH_CONTROL_CHARACTER, inputStream );
    verify( fileService, times( 0 ) ).idToPath( PATH_CONTROL_CHARACTER );
  }

  @Test
  public void testCreateFile_Special_Characters() throws Exception {
    String charsetName = "charsetName";
    InputStream inputStream = mock( InputStream.class );
    RepositoryFileOutputStream repositoryFileOutputStream = mock( RepositoryFileOutputStream.class );

    doReturn( repositoryFileOutputStream ).when( fileService ).getRepositoryFileOutputStream( anyString() );
    doReturn( 1 ).when( fileService ).copy( inputStream, repositoryFileOutputStream );

    fileService.createFile( charsetName, PATH_SPECIAL_CHARACTERS, inputStream );
  }

  @Test
  public void testCreateFile_Japanese_Characters() throws Exception {
    String charsetName = "charsetName";
    InputStream inputStream = mock( InputStream.class );
    RepositoryFileOutputStream repositoryFileOutputStream = mock( RepositoryFileOutputStream.class );

    doReturn( repositoryFileOutputStream ).when( fileService ).getRepositoryFileOutputStream( anyString() );
    doReturn( 1 ).when( fileService ).copy( inputStream, repositoryFileOutputStream );

    fileService.createFile( charsetName, PATH_JAPANESE_CHARACTERS, inputStream );
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

    fileService.doCreateDirSafe( PATH_SPECIAL_CHARACTERS );
  }

  @Test
  public void testDoCreateDirSafe_Japanese_Characters() throws Exception {
    DefaultUnifiedRepositoryWebService RepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    doReturn( RepoWs ).when( fileService ).getRepoWs();
    doReturn( true ).when( fileService ).doCreateDirFor( anyString() );

    fileService.doCreateDirSafe( PATH_JAPANESE_CHARACTERS );
  }

}


