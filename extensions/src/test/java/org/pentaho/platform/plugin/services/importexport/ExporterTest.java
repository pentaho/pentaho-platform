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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.importexport.Exporter;
import static org.mockito.Mockito.*;
import org.junit.Assert;

public class ExporterTest extends TestCase {

  private static String FILE_PATH = "/path/to/file";
  private static String REPO_PATH = "/repo/path/to/file";

  private IUnifiedRepository unifiedRepository;
  private RepositoryFile repositoryFile;
  private Exporter exporter;

  public void setUp() throws Exception {
    super.setUp();

    // set up mock repository
    unifiedRepository = mock( IUnifiedRepository.class );
    repositoryFile = mock( RepositoryFile.class );

    // handle method calls
    when( unifiedRepository.getFile( REPO_PATH ) ).thenReturn( repositoryFile );

    // instantiate exporter here to reuse for each test
    exporter = new Exporter( unifiedRepository );
    exporter.setRepoPath( REPO_PATH );
    exporter.setFilePath( FILE_PATH );
  }

  public void testDoExportAsZip() throws Exception {
    when( repositoryFile.isFolder() ).thenReturn( true );

    when( repositoryFile.getPath() ).thenReturn( REPO_PATH );

    Serializable mockSerializable = mock( Serializable.class );
    when( repositoryFile.getId() ).thenReturn( mockSerializable );

    List<RepositoryFile> mockList = mock( List.class );
    when( unifiedRepository.getChildren( mockSerializable ) ).thenReturn( mockList );

    Iterator<RepositoryFile> mockIterator = mock( Iterator.class );
    RepositoryFile repositoryFile2 = mock( RepositoryFile.class );
    when( mockIterator.hasNext() ).thenReturn( true, false );
    when( mockIterator.next() ).thenReturn( repositoryFile2 );
    when( mockList.iterator() ).thenReturn( mockIterator );

    when( repositoryFile2.isFolder() ).thenReturn( false );

    when( repositoryFile2.getPath() ).thenReturn( REPO_PATH );

    Serializable mockSerializable2 = mock( Serializable.class );
    when( repositoryFile2.getId() ).thenReturn( mockSerializable2 );

    SimpleRepositoryFileData mockRepoFileData = mock( SimpleRepositoryFileData.class );
    when( unifiedRepository.getDataForRead( mockSerializable2, SimpleRepositoryFileData.class ) ).thenReturn(
        mockRepoFileData );

    InputStream mockInputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        return -1; // EOF
      }
    };
    when( mockRepoFileData.getStream() ).thenReturn( mockInputStream );

    File zipFile = exporter.doExportAsZip();
    Assert.assertEquals( "repoExport", zipFile.getName().substring( 0, 10 ) );// repoExport.length() = 10
    Assert.assertNotNull( zipFile );
    verify( unifiedRepository, times( 1 ) ).getFile( REPO_PATH );
    verify( repositoryFile, times( 1 ) ).isFolder();
    verify( repositoryFile, times( 1 ) ).getId();
    verify( repositoryFile, times( 1 ) ).getPath();
    verify( repositoryFile, times( 1 ) ).getId();
    verify( unifiedRepository, times( 1 ) ).getChildren( mockSerializable );
    verify( mockIterator, times( 2 ) ).hasNext();
    verify( mockIterator, times( 1 ) ).next();
    verify( mockList, times( 1 ) ).iterator();
    verify( repositoryFile2, times( 1 ) ).isFolder();
    verify( repositoryFile2, times( 1 ) ).getPath();
    verify( repositoryFile2, times( 1 ) ).getId();
    verify( unifiedRepository, times( 1 ) ).getDataForRead( mockSerializable2, SimpleRepositoryFileData.class );
    verify( mockRepoFileData, times( 1 ) ).getStream();
  }

  public void testExportDirectory() throws Exception {
    when( repositoryFile.isFolder() ).thenReturn( true );

    String name = "name";
    when( repositoryFile.getName() ).thenReturn( name );

    Serializable mockSerializable = mock( Serializable.class );
    when( repositoryFile.getId() ).thenReturn( mockSerializable );

    List<RepositoryFile> mockList = mock( List.class );
    when( unifiedRepository.getChildren( mockSerializable ) ).thenReturn( mockList );

    Iterator<RepositoryFile> mockIterator = mock( Iterator.class );
    RepositoryFile repositoryFile2 = mock( RepositoryFile.class );
    when( mockIterator.hasNext() ).thenReturn( true, false );
    when( mockIterator.next() ).thenReturn( repositoryFile2 );
    when( mockList.iterator() ).thenReturn( mockIterator );

    when( repositoryFile2.isFolder() ).thenReturn( false );

    Serializable mockSerializable2 = mock( Serializable.class );
    when( repositoryFile2.getId() ).thenReturn( mockSerializable2 );

    SimpleRepositoryFileData mockRepoFileData = mock( SimpleRepositoryFileData.class );
    when( unifiedRepository.getDataForRead( mockSerializable2, SimpleRepositoryFileData.class ) ).thenReturn(
        mockRepoFileData );

    InputStream mockInputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        return -1; // EOF
      }
    };
    when( mockRepoFileData.getStream() ).thenReturn( mockInputStream );

    String name2 = "name.txt";
    when( repositoryFile2.getName() ).thenReturn( name2 );

    File parentDir = new File( System.getProperty( "java.io.tmpdir" ) );
    File file = new File( parentDir, name );
    // already exists
    if ( file.isDirectory() ) {
      deleteDirectory( file );
    }
    exporter.exportDirectory( repositoryFile, parentDir );

    verify( repositoryFile, times( 1 ) ).isFolder();
    verify( repositoryFile, times( 1 ) ).getName();
    verify( repositoryFile, times( 1 ) ).getId();
    verify( unifiedRepository, times( 1 ) ).getChildren( mockSerializable );
    verify( mockIterator, times( 2 ) ).hasNext();
    verify( mockIterator, times( 1 ) ).next();
    verify( mockList, times( 1 ) ).iterator();
    verify( repositoryFile2, times( 1 ) ).getId();
    verify( unifiedRepository, times( 1 ) ).getDataForRead( mockSerializable2, SimpleRepositoryFileData.class );
    verify( mockRepoFileData, times( 1 ) ).getStream();
    verify( repositoryFile2, times( 1 ) ).getName();
  }

  public static void deleteDirectory( File file ) {
    if ( file.isDirectory() ) {
      String[] list = file.list();
      for ( int i = 0; i < list.length; i++ ) {
        deleteDirectory( new File( file, list[i] ) );
      }
    }
    file.delete();
  }

  public void testExportFile() throws Exception {
    Serializable mockSerializable = mock( Serializable.class );
    when( repositoryFile.getId() ).thenReturn( mockSerializable );

    SimpleRepositoryFileData mockRepoFileData = mock( SimpleRepositoryFileData.class );
    when( unifiedRepository.getDataForRead( mockSerializable, SimpleRepositoryFileData.class ) ).thenReturn(
        mockRepoFileData );

    InputStream mockInputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        return -1; // EOF
      }
    };
    when( mockRepoFileData.getStream() ).thenReturn( mockInputStream );

    String name = "name.txt";
    when( repositoryFile.getName() ).thenReturn( name );

    File mockFile = mock( File.class );
    when( mockFile.exists() ).thenReturn( true );
    when( mockFile.isDirectory() ).thenReturn( true );
    when( mockFile.getAbsolutePath() ).thenReturn( System.getProperty( "java.io.tmpdir" ) );

    exporter.exportFile( repositoryFile, mockFile );

    verify( repositoryFile, times( 1 ) ).getId();
    verify( unifiedRepository, times( 1 ) ).getDataForRead( mockSerializable, SimpleRepositoryFileData.class );
    verify( mockRepoFileData, times( 1 ) ).getStream();
    verify( repositoryFile, times( 1 ) ).getName();
    verify( mockFile, times( 1 ) ).exists();
    verify( mockFile, times( 1 ) ).isDirectory();
    verify( mockFile, times( 1 ) ).getAbsolutePath();
  }
}