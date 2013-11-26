/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport.legacy;

import junit.framework.TestCase;
import org.pentaho.platform.plugin.services.importexport.ImportSource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class FileSolutionRepositoryImportSourceTest extends TestCase {
  public void testInitialize() throws Exception {

    final File tempFile = File.createTempFile( "junit", "tmp" );
    FileSolutionRepositoryImportSource importSource =
        new FileSolutionRepositoryImportSource( tempFile, "sample.xaction", "UTF-8" );
    assertEquals( 1, importSource.getCount() );
    final Iterable<ImportSource.IRepositoryFileBundle> files = importSource.getFiles();
    assertNotNull( files );
    final ImportSource.IRepositoryFileBundle bundle = files.iterator().next();
    assertNotNull( bundle );
    assertEquals( "", bundle.getPath() );
    assertNotNull( bundle.getFile() );
    assertEquals( "sample.xaction", bundle.getFile().getName() );
    assertFalse( bundle.getFile().isFolder() );

    final File sourceFile = new File( "./testdata" );
    assertTrue( "Make sure your current directory is the repository project", sourceFile.exists() );
    importSource = new FileSolutionRepositoryImportSource( sourceFile, "UTF-8" );
    assertEquals( 13, importSource.getCount() );

  }

  public void testGetFiles() throws Exception {
    {
      final File invalidFile = createTempFile( "tmp" );
      assertTrue( invalidFile.delete() );
      final FileSolutionRepositoryImportSource importSource =
          new FileSolutionRepositoryImportSource( invalidFile, "UTF-8" );
      assertEquals( 0, importSource.getCount() );
      final Iterable<ImportSource.IRepositoryFileBundle> files = importSource.getFiles();
      assertNotNull( files );
      assertFalse( files.iterator().hasNext() );
    }

    {
      final File validFile = createTempFile( "xaction" );
      final FileSolutionRepositoryImportSource importSource =
          new FileSolutionRepositoryImportSource( validFile, "UTF-8" );
      assertEquals( 1, importSource.getCount() );
      assertNotNull( importSource.getFiles() );
      final Iterator<ImportSource.IRepositoryFileBundle> iterator = importSource.getFiles().iterator();
      assertNotNull( iterator );
      final ImportSource.IRepositoryFileBundle file = iterator.next();
      assertNotNull( file );

      // Make sure remove works
      iterator.remove();
      assertEquals( 0, importSource.getCount() );

      assertFalse( iterator.hasNext() );
    }

    {
      validateImportSource( new File( "./testdata" ) );
      validateImportSource( new File( "./testdata/" ) );
      validateImportSource( new File( "testdata" ) );
      validateImportSource( new File( "testdata/" ) );
      validateImportSource( new File( "testdata" ).getAbsoluteFile() );
    }
  }

  private void validateImportSource( final File sourceFile ) {
    final Map<String, ImportSource.IRepositoryFileBundle> foldersFound =
        new HashMap<String, ImportSource.IRepositoryFileBundle>();
    final Map<String, ImportSource.IRepositoryFileBundle> filesFound =
        new HashMap<String, ImportSource.IRepositoryFileBundle>();
    assertTrue( "Make sure your current directory is the repository project", sourceFile.exists() );
    FileSolutionRepositoryImportSource importSource = new FileSolutionRepositoryImportSource( sourceFile, "UTF-8" );
    assertEquals( 13, importSource.getCount() );
    final Iterable<ImportSource.IRepositoryFileBundle> files = importSource.getFiles();
    assertNotNull( files );
    for ( Iterator<ImportSource.IRepositoryFileBundle> it = files.iterator(); it.hasNext(); ) {
      final ImportSource.IRepositoryFileBundle bundle = it.next();
      assertNotNull( bundle );
      assertNotNull( bundle.getFile() );
      if ( bundle.getFile().isFolder() ) {
        foldersFound.put( bundle.getFile().getName(), bundle );
      } else {
        filesFound.put( bundle.getFile().getName(), bundle );
      }
    }

    assertEquals( 10, filesFound.size() );
    assertNotNull( filesFound.get( "Empty.zip" ) );
    assertNotNull( filesFound.get( "Success.zip" ) );
    assertNotNull( filesFound.get( "TestZipFile.zip" ) );
    assertNotNull( filesFound.get( "pentaho-solutions.zip" ) );
    assertNotNull( filesFound.get( "Example1.xaction" ) );
    assertNotNull( filesFound.get( "Example2.xaction" ) );
    assertNotNull( filesFound.get( "Example3.xaction" ) );
    assertNotNull( filesFound.get( "HelloWorld.xaction" ) );

    assertEquals( "", filesFound.get( "TestZipFile.zip" ).getPath() );
    assertEquals( "TestZipFile.zip", filesFound.get( "TestZipFile.zip" ).getFile().getName() );
    assertEquals( File.separator + "pentaho-solutions" + File.separator + "getting-started", filesFound.get(
        "HelloWorld.xaction" ).getPath() );
    assertEquals( "HelloWorld.xaction", filesFound.get( "HelloWorld.xaction" ).getFile().getName() );

    assertEquals( 3, foldersFound.size() );
    assertNotNull( foldersFound.get( "testdata" ) );
    assertNotNull( foldersFound.get( "pentaho-solutions" ) );
    assertNotNull( foldersFound.get( "getting-started" ) );

    assertEquals( File.separator + "pentaho-solutions", foldersFound.get( "getting-started" ).getPath() );
  }

  public void testCreate() throws Exception {
    final File tempFile = File.createTempFile( "junit", "tmp" );
    try {
      new FileSolutionRepositoryImportSource( null, "UTF-8" );
      fail();
    } catch ( Exception success ) {
      //ignore
    }

    try {
      new FileSolutionRepositoryImportSource( tempFile, null );
      fail();
    } catch ( Exception success ) {
      //ignore
    }

    try {
      new FileSolutionRepositoryImportSource( tempFile, "" );
      fail();
    } catch ( Exception success ) {
      //ignore
    }

    try {
      new FileSolutionRepositoryImportSource( null, "filename.txt", "UTF-8" );
      fail();
    } catch ( Exception success ) {
      //ignore
    }

    try {
      new FileSolutionRepositoryImportSource( tempFile, null, "UTF-8" );
      fail();
    } catch ( Exception success ) {
      //ignored
    }

    try {
      new FileSolutionRepositoryImportSource( tempFile, "", "UTF-8" );
      fail();
    } catch ( Exception success ) {
      //ignored
    }

    try {
      new FileSolutionRepositoryImportSource( tempFile, "filename.txt", null );
      fail();
    } catch ( Exception success ) {
      //ignored
    }

    try {
      new FileSolutionRepositoryImportSource( tempFile, "filename.txt", "" );
      fail();
    } catch ( Exception success ) {
      //ignored
    }

    new FileSolutionRepositoryImportSource( tempFile, "UTF-8" );
  }

  private static File createTempFile( final String extension ) throws IOException {
    return File.createTempFile( "FileSolutionRepositoryImportSourceTest-", extension == null ? "" : "." + extension );
  }

  private static File createTempDir() throws IOException {
    final File dir = File.createTempFile( "FileSolutionRepositoryImportSourceTest-", "" );
    assertTrue( dir.delete() );
    assertTrue( dir.mkdir() );
    return dir;
  }
}
