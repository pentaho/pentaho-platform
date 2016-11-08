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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

  @Before
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
