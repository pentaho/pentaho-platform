/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
