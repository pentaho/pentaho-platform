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


package org.pentaho.platform.api.repository2.unified;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by bgroves on 10/26/15.
 */
public class VersionSummaryTest {

  private static final String ID = "id";
  private static final String FILE_ID = "fileId";
  private static final Boolean ACL_CHANGE = true;
  private static final Date DATE = new Date();
  private static final String AUTHOR = "ScrumMaster";
  private static final String MESSAGE = "Hello World";
  private static final String LABEL_ONE = "labelOne";
  private static final String LABEL_TWO = "labelTwo";
  private static final List<String> LABELS = Arrays.asList( LABEL_ONE, LABEL_TWO );

  private VersionSummary versionSummary;

  @BeforeEach
  public void setUp() {
    versionSummary = new VersionSummary( ID, FILE_ID, ACL_CHANGE, DATE, AUTHOR, MESSAGE, LABELS );
  }

  @Test
  public void testVersionSummary() {
    try {
      VersionSummary failure = new VersionSummary( null, FILE_ID, ACL_CHANGE, DATE, AUTHOR, MESSAGE, LABELS );
      fail( "Should of thrown an IllegalArgumentException" );
    } catch ( Exception e ) {
      // Pass
    }

    try {
      VersionSummary failure = new VersionSummary( ID, FILE_ID, ACL_CHANGE, DATE, "", MESSAGE, LABELS );
      fail( "Should of thrown an IllegalArgumentException" );
    } catch ( Exception e ) {
      // Pass
    }

    try {
      VersionSummary failure = new VersionSummary( ID, FILE_ID, ACL_CHANGE, DATE, null, MESSAGE, LABELS );
      fail( "Should of thrown an IllegalArgumentException" );
    } catch ( Exception e ) {
      // Pass
    }

    try {
      VersionSummary failure = new VersionSummary( ID, FILE_ID, ACL_CHANGE, DATE, " ", MESSAGE, LABELS );
      fail( "Should of thrown an IllegalArgumentException" );
    } catch ( Exception e ) {
      // Pass
    }

    assertEquals( MESSAGE, versionSummary.getMessage() );
    assertEquals( DATE, versionSummary.getDate() );
    assertEquals( AUTHOR, versionSummary.getAuthor() );
    assertEquals( ID, versionSummary.getId() );
    assertEquals( FILE_ID, versionSummary.getVersionedFileId() );
    assertEquals( DATE, versionSummary.getDate() );
    assertEquals( ACL_CHANGE, versionSummary.isAclOnlyChange() );
    assertEquals( LABELS, versionSummary.getLabels() );

    assertNotNull( versionSummary.toString() );
    assertNotEquals( 31, versionSummary.hashCode() );
  }

  @Test
  public void testEquals() {
    assertTrue( versionSummary.equals( versionSummary ) );
    assertFalse( versionSummary.equals( null ) );
    assertFalse( versionSummary.equals( new String() ) );
    assertFalse( versionSummary.equals( new VersionSummary( "diffId", FILE_ID, ACL_CHANGE, DATE, AUTHOR, MESSAGE,
      LABELS ) ) );
    assertTrue( versionSummary.equals( new VersionSummary( ID, FILE_ID, ACL_CHANGE, DATE, AUTHOR, MESSAGE,
      LABELS ) ) );
  }
}
