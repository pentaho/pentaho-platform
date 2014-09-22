/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.importexport;

import junit.framework.TestCase;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.Exporter;

public class ExporterTest extends TestCase {

  private static String FILE_PATH = "/path/to/file";
  private static String REPO_PATH = "/repo/path/to/file";

  private IUnifiedRepository unifiedRepository;
  private RepositoryFile repositoryFile;
  private Exporter exporter;

  public void setUp() throws Exception {
    super.setUp();
    /*
     * 
     * // set up mock repository unifiedRepository = mock(IUnifiedRepository.class); repositoryFile =
     * mock(RepositoryFile.class);
     * 
     * // handle method calls when(unifiedRepository.getFile(REPO_PATH)).thenReturn(repositoryFile);
     * 
     * // instantiate exporter here to reuse for each test exporter = new Exporter(unifiedRepository);
     * exporter.setRepoPath(REPO_PATH); exporter.setFilePath(FILE_PATH);
     */

  }

  public void tearDown() throws Exception {

  }

  public void testDoExportAsFile() throws Exception {

  }

  public void testDoExportAsDirectory() throws Exception {

  }

  public void testDoExportAsZip() throws Exception {

  }

  /*
   * public void testDoExportAsZip() throws Exception {
   * 
   * }
   */

  public void testExportDirectory() throws Exception {

  }

  public void testExportFile() throws Exception {

  }

  public void testGetUnifiedRepository() throws Exception {

  }

  public void testSetUnifiedRepository() throws Exception {

  }

  public void testGetRepoPath() throws Exception {

  }

  public void testSetRepoPath() throws Exception {

  }

  public void testGetFilePath() throws Exception {

  }

  public void testSetFilePath() throws Exception {

  }

  public void testGetRepoWs() throws Exception {

  }

  public void testSetRepoWs() throws Exception {

  }
}
