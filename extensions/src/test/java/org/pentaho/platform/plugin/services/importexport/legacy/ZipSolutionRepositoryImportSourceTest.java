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
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.zip.ZipInputStream;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class ZipSolutionRepositoryImportSourceTest extends TestCase {

  public void testCreate() throws Exception {
    try {
      new ZipSolutionRepositoryImportSource( null, "UTF-8" );
      fail( "Null input stream should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    try {
      new ZipSolutionRepositoryImportSource( new ZipInputStream( new ByteArrayInputStream( "".getBytes() ) ), null );
      fail( "Null character set should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    try {
      new ZipSolutionRepositoryImportSource( new ZipInputStream( new ByteArrayInputStream( "".getBytes() ) ), "" );
      fail( "Empty character set should throw exception" );
    } catch ( Exception success ) {
      //ignored
    }

    // Should work w/o error
    final ZipSolutionRepositoryImportSource inputSource =
        new ZipSolutionRepositoryImportSource( new ZipInputStream( new ByteArrayInputStream( "".getBytes() ) ),
          "UTF-8" );
    assertEquals( 0, inputSource.getCount() ); // NOTE: initialize hasn't been called ... so the size would be 0
  }

  public void testInitialize() throws Exception {
    {
      ZipInputStream zis = null;
      try {
        zis = getZipInputStream( "testdata/Success.zip" );
        final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource( zis, "UTF-8" );
        assertEquals( 7, importSource.getCount() );
      } finally {
        IOUtils.closeQuietly( zis );
      }
    }

    {
      ZipInputStream zis = null;
      try {
        zis = getZipInputStream( "testdata/Empty.zip" );
        final ZipSolutionRepositoryImportSource importSource = new ZipSolutionRepositoryImportSource( zis, "UTF-8" );
        assertEquals( 0, importSource.getCount() );
      } finally {
        IOUtils.closeQuietly( zis );
      }
    }
  }

  public void testGetFiles() throws Exception {

  }

  public void testGetCount() throws Exception {

  }

  private ZipInputStream getZipInputStream( final String path ) throws FileNotFoundException {
    return new ZipInputStream( new FileInputStream( path ) );
  }
}
