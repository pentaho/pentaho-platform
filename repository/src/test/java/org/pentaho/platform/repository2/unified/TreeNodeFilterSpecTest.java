/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.platform.repository2.unified;

import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.platform.api.repository2.unified.RepositoryRequest.FILTER_SEPARATOR;
import static org.pentaho.platform.api.repository2.unified.RepositoryRequest.FILTER_WILDCARD;
import static org.pentaho.platform.repository2.unified.TreeNodeFilterSpec.DEFAULT_CHILD_NODE_FILTER;
import static org.pentaho.platform.repository2.unified.TreeNodeFilterSpec.FILE_FILTER_TOKEN;
import static org.pentaho.platform.repository2.unified.TreeNodeFilterSpec.FILTER_TOKEN_SEPARATOR;
import static org.pentaho.platform.repository2.unified.TreeNodeFilterSpec.FOLDER_FILTER_TOKEN;
import static org.pentaho.platform.repository2.unified.TreeNodeFilterSpec.INSIDE_FILTER_TOKEN_SEPARATOR;

public class TreeNodeFilterSpecTest {
  private static final String KTR = FILTER_WILDCARD + ".ktr";
  private static final String KJB = FILTER_WILDCARD + ".kjb";
  private static final String PRPT = FILTER_WILDCARD + ".prpt";
  private static final String SALES_FOLDER = "sales" + FILTER_WILDCARD;
  private static final String YEAR_FOLDER = "2026" + FILTER_WILDCARD;

  /**
   * the two ktr/kjb patterns as they are written inside a single clause
   */
  private static final String KTR_AND_KJB = KTR + INSIDE_FILTER_TOKEN_SEPARATOR + KJB;

  @Test
  public void testLegacyFilterIsNotStructured() {
    assertFalse( TreeNodeFilterSpec.parse( null ).isStructured() );
    assertFalse( TreeNodeFilterSpec.parse( DEFAULT_CHILD_NODE_FILTER ).isStructured() );
    assertFalse( TreeNodeFilterSpec.parse( KTR ).isStructured() );
  }

  @Test
  public void testFileFilterOnly() {
    // the patterns are deliberately spaced out, they must come back normalized
    TreeNodeFilterSpec spec =
      TreeNodeFilterSpec.parse( FILE_FILTER_TOKEN + KTR + INSIDE_FILTER_TOKEN_SEPARATOR + " " + KJB );

    assertTrue( spec.isStructured() );
    assertEquals( KTR_AND_KJB, spec.getFileFilter() );
    assertNull( spec.getFolderFilter() );
  }

  @Test
  public void testFolderFilterOnly() {
    TreeNodeFilterSpec spec = TreeNodeFilterSpec.parse( FOLDER_FILTER_TOKEN + SALES_FOLDER );

    assertTrue( spec.isStructured() );
    assertEquals( SALES_FOLDER, spec.getFolderFilter() );
    assertNull( spec.getFileFilter() );
  }

  @Test
  public void testBothFilters() {
    TreeNodeFilterSpec spec = TreeNodeFilterSpec.parse(
      FILE_FILTER_TOKEN + PRPT + FILTER_TOKEN_SEPARATOR + FOLDER_FILTER_TOKEN + YEAR_FOLDER );

    assertEquals( PRPT, spec.getFileFilter() );
    assertEquals( YEAR_FOLDER, spec.getFolderFilter() );
  }

  @Test
  public void testStructuredFilterSurvivesTheLegacyFilterParser() {
    String structuredFilter = FILE_FILTER_TOKEN + KTR_AND_KJB;
    RepositoryRequest request = new RepositoryRequest( "/public", true, -1,
      structuredFilter + FILTER_SEPARATOR + RepositoryRequest.FILES_TYPE_FILTER.FILES_FOLDERS );

    assertEquals( RepositoryRequest.FILES_TYPE_FILTER.FILES_FOLDERS, request.getTypes() );
    assertEquals( structuredFilter, request.getChildNodeFilter() );

    TreeNodeFilterSpec spec = TreeNodeFilterSpec.parse( request.getChildNodeFilter() );
    assertTrue( spec.isStructured() );
    assertEquals( KTR_AND_KJB, spec.getFileFilter() );
    assertNull( spec.getFolderFilter() );
  }

  @Test
  public void testUnknownClauseIsRejected() {
    assertInvalid( FILE_FILTER_TOKEN + KTR + FILTER_TOKEN_SEPARATOR + "bogus=1" );
  }

  @Test
  public void testHasStructuredTokens() {
    assertTrue( TreeNodeFilterSpec.hasStructuredTokens( FILE_FILTER_TOKEN + KTR ) );
    assertTrue( TreeNodeFilterSpec.hasStructuredTokens( FOLDER_FILTER_TOKEN + SALES_FOLDER ) );
    assertFalse( TreeNodeFilterSpec.hasStructuredTokens( KTR ) );
    assertFalse( TreeNodeFilterSpec.hasStructuredTokens( null ) );
  }

  @Test
  public void testFallbackDegradesStructuredFilter() {
    RepositoryRequest request = new RepositoryRequest( "/public", true, -1, FILE_FILTER_TOKEN + KTR
      + FILTER_SEPARATOR + RepositoryRequest.FILES_TYPE_FILTER.FILES_FOLDERS );

    assertTrue( TreeNodeFilterSpec.applyFallback( request ) );
    assertEquals( DEFAULT_CHILD_NODE_FILTER, request.getChildNodeFilter() );
  }

  @Test
  public void testFallbackLeavesLegacyFilterUntouched() {
    RepositoryRequest request = new RepositoryRequest( "/public", true, -1,
      KTR + FILTER_SEPARATOR + RepositoryRequest.FILES_TYPE_FILTER.FILES_FOLDERS );

    assertFalse( TreeNodeFilterSpec.applyFallback( request ) );
    assertEquals( KTR, request.getChildNodeFilter() );
  }

  @Test
  public void testFallbackHandlesNullRequest() {
    assertFalse( TreeNodeFilterSpec.applyFallback( null ) );
  }

  @Test
  public void testEmptyPatternIsRejected() {
    assertInvalid( FILE_FILTER_TOKEN + KTR + INSIDE_FILTER_TOKEN_SEPARATOR + INSIDE_FILTER_TOKEN_SEPARATOR );
    assertInvalid( FOLDER_FILTER_TOKEN );
  }

  @Test
  public void testIllegalCharactersAreRejected() {
    assertInvalid( FILE_FILTER_TOKEN + "/etc/" + FILTER_WILDCARD );
    assertInvalid( FOLDER_FILTER_TOKEN + SALES_FOLDER + "'" );
  }

  /**
   * the legacy filter separator cannot appear inside a pattern, because it delimits the legacy filter itself
   */
  @Test
  public void testLegacyFilterSeparatorIsRejected() {
    assertInvalid( FILE_FILTER_TOKEN + KTR + FILTER_SEPARATOR + PRPT );
  }

  @Test
  public void testControlCharactersAreRejected() {
    assertInvalid( FILE_FILTER_TOKEN + "sales\treport" );
    assertInvalid( FILE_FILTER_TOKEN + "sales\rreport" );
    assertInvalid( FILE_FILTER_TOKEN + "sales\nreport" );
  }

  private static void assertInvalid( String childNodeFilter ) {
    try {
      TreeNodeFilterSpec.parse( childNodeFilter );
      fail( "IllegalArgumentException should have been thrown for '" + childNodeFilter + "'" );
    } catch ( IllegalArgumentException e ) {
      // expected
    }
  }
}
