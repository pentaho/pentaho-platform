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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.jfreereport.helper;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.file.FileContentItem;
import org.pentaho.reporting.libraries.repository.file.FileRepository;
import org.pentaho.test.platform.utils.TestResourceLocation;

public class PentahoURLRewriterTest {

  public static final String TEST_SRC = TestResourceLocation.TEST_RESOURCES;

  @Test
  public void testRewriteWithNullPattern() throws Exception {
    String pattern = null;
    PentahoURLRewriter rewriter = new PentahoURLRewriter( pattern );

    File dataDirectory = new File(TEST_SRC + "/solution/test/reporting/");
    FileRepository dataRepository = new FileRepository( dataDirectory );

    File contentEntryBackend = new File(TEST_SRC + "/solution/test/reporting/contentEntryBackend");
    File dataEntityBackend = new File(TEST_SRC + "/solution/test/reporting/dataEntityBackend");

    ContentEntity contentEntry = new FileContentItem( dataRepository.getRoot(), contentEntryBackend );
    ContentEntity dataEntity = new FileContentItem( dataRepository.getRoot(), dataEntityBackend );

    String result = rewriter.rewrite( contentEntry, dataEntity );

    assertEquals( "dataEntityBackend", result );
  }

  @Test
  public void testRewriteWithSimplePattern() throws Exception {
    String pattern = TEST_SRC + "/solution/test/reporting/system";
    PentahoURLRewriter rewriter = new PentahoURLRewriter( pattern );

    File dataDirectory = new File(TEST_SRC + "/solution/test/reporting/");
    FileRepository dataRepository = new FileRepository( dataDirectory );

    File contentEntryBackend = new File(TEST_SRC + "/solution/test/reporting/contentEntryBackend");
    File dataEntityBackend = new File(TEST_SRC + "/solution/test/reporting/dataEntityBackend");

    ContentEntity contentEntry = new FileContentItem( dataRepository.getRoot(), contentEntryBackend );
    ContentEntity dataEntity = new FileContentItem( dataRepository.getRoot(), dataEntityBackend );

    String result = rewriter.rewrite( contentEntry, dataEntity );

    assertEquals( pattern, result );
  }

  @Test
  public void testRewriteWithPatternParam() throws Exception {
    String pattern = TEST_SRC + "/solution/test/reporting/system/{0}/param";
    PentahoURLRewriter rewriter = new PentahoURLRewriter( pattern );

    File dataDirectory = new File(TEST_SRC + "/solution/test/reporting/");
    FileRepository dataRepository = new FileRepository( dataDirectory );

    File contentEntryBackend = new File(TEST_SRC + "/solution/test/reporting/contentEntryBackend");
    File dataEntityBackend = new File(TEST_SRC + "/solution/test/reporting/dataEntityBackend");

    ContentEntity contentEntry = new FileContentItem( dataRepository.getRoot(), contentEntryBackend );
    ContentEntity dataEntity = new FileContentItem( dataRepository.getRoot(), dataEntityBackend );

    String result = rewriter.rewrite( contentEntry, dataEntity );

    assertEquals(TEST_SRC + "/solution/test/reporting/system/dataEntityBackend/param", result );
  }

  @Test
  public void testRewriteRoot() throws Exception {
    String pattern = TEST_SRC + "/solution/test/reporting/system/{0}/param";
    PentahoURLRewriter rewriter = new PentahoURLRewriter( pattern );

    File dataDirectory = new File( "/" );
    FileRepository dataRepository = new FileRepository( dataDirectory );

    File contentEntryBackend = new File(TEST_SRC + "/solution/test/reporting/contentEntryBackend");
    File dataEntityBackend = new File(TEST_SRC + "/solution/test/reporting/dataEntityBackend");

    ContentEntity contentEntry = new FileContentItem( dataRepository.getRoot(), contentEntryBackend );
    ContentEntity dataEntity = new FileContentItem( dataRepository.getRoot(), dataEntityBackend );

    String result = rewriter.rewrite( contentEntry, dataEntity );
    assertEquals(TEST_SRC + "/solution/test/reporting/system/dataEntityBackend/param", result );
  }

}
