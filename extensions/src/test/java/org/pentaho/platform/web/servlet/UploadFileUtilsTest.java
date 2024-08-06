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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.google.gwt.thirdparty.guava.common.net.MediaType;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UploadFileUtilsTest {
  private static final Log logger = LogFactory.getLog( UploadFileUtilsTest.class );

  @Before
  public void setupPentahoSystem() {
    PentahoSystem.setApplicationContext(
      new StandaloneApplicationContext( "test-src/web-servlet-solution/FileUploadTest/Test1", "" ) );
  }

  @Test
  public void testCheckExtensions() throws Exception {
    UploadFileUtils testUtils = new UploadFileUtils( null );
    assertFalse( testUtils.checkExtension( null, false ) );
    assertFalse( testUtils.checkExtension( "exe", false ) );
    assertFalse( testUtils.checkExtension( "app", false ) );
    assertFalse( testUtils.checkExtension( "html", false ) );
    assertTrue( testUtils.checkExtension( "txt", false ) );
    assertTrue( testUtils.checkExtension( "zip", false ) );
    assertTrue( testUtils.checkExtension( "gzip", false ) );
    assertTrue( testUtils.checkExtension( "gz", false ) );
    assertTrue( testUtils.checkExtension( "dat", false ) );
    assertTrue( testUtils.checkExtension( "csv", false ) );
    assertTrue( testUtils.checkExtension( "tar", false ) );
    assertFalse( testUtils.checkExtension( "tar.gz", false ) );
    assertTrue( testUtils.checkExtension( "tgz", false ) );
    // As of PPP-4675, the default value of allowNoExtensions is false,
    // so we will test the default (false), then the explicit value of false
    assertFalse( testUtils.checkExtension( "", false ) );
    testUtils.setAllowsNoExtension( false );
    assertFalse( testUtils.checkExtension( "", false ) );
    testUtils.setAllowsNoExtension( true );
    assertTrue( testUtils.checkExtension( "", false ) );
  }

  @Test
  public void testGetAllExtensions() {
    UploadFileUtils testUtils = new UploadFileUtils( null );
    assertNull( testUtils.removeFileName( null ) );
    assertEquals( "", testUtils.removeFileName( "" ) );
    assertEquals( "", testUtils.removeFileName( "aaaa" ) );
    assertEquals( "", testUtils.removeFileName( " bb " ) );
    assertEquals( "a", testUtils.removeFileName( "b.a" ) );
    assertEquals( "b.a", testUtils.removeFileName( "c.b.a" ) );
    assertEquals( "c.b.a", testUtils.removeFileName( "d.c.b.a" ) );
    // BACKLOG-29017: Special characters
    assertEquals( "_________-__________-_____test.csv",
      testUtils.removeFileName( "TestCSVspecialchar.$&#%^( )!- ;[ ]{ }`@-_+=',test.csv" ) );
    assertEquals( "Tes____CSVspecialchar._________-__________-_____test.dat",
      testUtils.removeFileName( "_.Tes~ºª_CSVspecialchar.$&#%^( )!- ;[ ]{ }`@-_+=',test.dat" ) );
  }

  @Test
  public void testZipSlipZipFile() throws Exception {
    assertEquals( List.of( 0L, 1L ),
      testZipSlipFile( "src/test/resources/UploadFileUtilsTest/zip-slip.zip", MediaType.ZIP.toString() ) );
  }

  @Test
  public void testZipSlipZipFileWithOnlyInvalidFiles() throws Exception {
    assertEquals( List.of( 0L, 0L ),
      testZipSlipFile( "src/test/resources/UploadFileUtilsTest/invalid.zip", MediaType.ZIP.toString() ) );
  }

  @Test
  public void testZipSlipTarFile() throws Exception {
    assertEquals( List.of( 0L, 1L ),
      testZipSlipFile( "src/test/resources/UploadFileUtilsTest/zip-slip.tar", MediaType.TAR.toString() ) );
  }

  @Test
  public void testZipSlipTarFileWithOnlyInvalidFiles() throws Exception {
    assertEquals( List.of( 0L, 0L ),
      testZipSlipFile( "src/test/resources/UploadFileUtilsTest/invalid.tar", MediaType.TAR.toString() ) );
  }

  private List<Long> testZipSlipFile( String filename, String contentType ) throws Exception {
    Path outOfBoundsDir = Files.createTempDirectory( null );
    Path tempDir = Files.createTempDirectory( outOfBoundsDir, null );

    logger.debug( "Testing zip slip in dir: " + tempDir );

    IApplicationContext appContext = mock( IApplicationContext.class );
    when( appContext.getSolutionPath( any() ) ).thenReturn( tempDir.toString() );
    PentahoSystem.setApplicationContext( appContext );

    final Path testFilePath = Paths.get( filename );
    final File testFile = testFilePath.toFile();

    final DiskFileItem diskFileItem =
      new DiskFileItem( "fileData", contentType, true, testFile.getName(), 100000000, testFile.getParentFile() );

    InputStream input = new FileInputStream( testFile );
    OutputStream os = diskFileItem.getOutputStream();
    IOUtils.copy( input, os );

    UploadFileUtils utils = new UploadFileUtils( null );
    utils.setShouldUnzip( true );
    utils.setTemporary( false );
    utils.setFileName( testFilePath.getFileName().toString() );
    utils.setWriter( new PrintWriter( System.out ) );
    utils.setUploadedFileItem( diskFileItem );

    if ( utils.process() ) {
      long outOfBoundsDirCount;
      long currentDirCount;

      try ( Stream<Path> files = Files.list( tempDir ) ) {
        currentDirCount =
          files.filter( file -> !file.getFileName().equals( testFilePath.getFileName() ) && !Files.isDirectory( file ) )
            .count();
      }

      try ( Stream<Path> files = Files.list( outOfBoundsDir ) ) {
        outOfBoundsDirCount =
          files.filter( file -> !file.getFileName().equals( testFilePath.getFileName() ) && !Files.isDirectory( file ) )
            .count();
      }

      return List.of( outOfBoundsDirCount, currentDirCount );
    }

    return null;
  }
}
