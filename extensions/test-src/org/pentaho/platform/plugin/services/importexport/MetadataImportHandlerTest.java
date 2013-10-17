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

package org.pentaho.platform.plugin.services.importexport;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class MetadataImportHandlerTest extends TestCase {
  private IUnifiedRepository repository;

  @Override
  protected void setUp() throws Exception {
    // Define a repository for testing
    repository = new MockUnifiedRepository( new MockUserProvider() );
    repository.createFolder( repository.getFile( "/etc" ).getId(), new RepositoryFile.Builder( "metadata" ).folder(
        true ).build(), "initialization" );
  }

  public void testCreation() throws Exception {
    try {
      new MetadataImportHandler( null );
      fail( "Invalid parameters should throw exception" );
    } catch ( IllegalArgumentException success ) {
    }

    assertTrue( !StringUtils.isEmpty( new MetadataImportHandler( repository ).getName() ) );
  }

  /*
   * public void testDoImport() throws Exception { { final MetadataImportHandler handler = new
   * MetadataImportHandler(repository); try { handler.doImport(null, null, null, true);
   * fail("Null data should throw exception"); } catch (Exception success) { }
   * 
   * // The destination path and comment are not required for this handler handler.doImport(new ArrayList(), null, null,
   * true); handler.doImport(new ArrayList(), "", "", true); }
   * 
   * ZipInputStream zis = null; try { // Use the test ZIP file and the ZipSolutionRepositoryImportSource zis =
   * getZipInputStream("testdata/pentaho-solutions.zip"); final ZipSolutionRepositoryImportSource importSource = new
   * ZipSolutionRepositoryImportSource(zis, "UTF-8"); assertEquals("The test should start with exactly 65 files", 65,
   * importSource.getCount());
   * 
   * final MetadataImportHandler handler = new MetadataImportHandler(repository);
   * handler.doImport(importSource.getFiles(), "/public/user", "import comment", true);
   * 
   * // The import handler should only process / remove files related to importing metadata files
   * assertEquals("The test should have ended without processing 60 files", 60, importSource.getCount());
   * 
   * // Make sure the metadata was processed correctly final RepositoryFile etcMetadata =
   * repository.getFile(PentahoMetadataDomainRepositoryInfo.getMetadataFolderPath()); assertNotNull(etcMetadata);
   * assertTrue(etcMetadata.isFolder()); final List<RepositoryFile> children =
   * repository.getChildren(etcMetadata.getId()); assertNotNull(children); assertEquals(5, children.size()); } finally {
   * IOUtils.closeQuietly(zis); } }
   */
  private ZipInputStream getZipInputStream( final String path ) throws FileNotFoundException {
    return new ZipInputStream( new FileInputStream( path ) );
  }

  /**
   *
   */
  private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
    @Override
    public String getUser() {
      return MockUnifiedRepository.root().getName();
    }

    @Override
    public List<String> getRoles() {
      return new ArrayList<String>();
    }
  }
}
