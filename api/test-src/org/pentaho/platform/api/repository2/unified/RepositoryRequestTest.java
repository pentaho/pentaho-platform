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
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by bgroves on 10/28/15.
 */
public class RepositoryRequestTest {

  private static final String PATH = "path";
  private static final boolean SHOW_HIDE = true;
  private static final Integer DEPTH = 2;
  private static final String LEGACY_FILTER = "FILES||ODD_FILTER";
  private static final String INLCUDE_ONE = "includeOne";
  private static final String INCLUDE_TWO = "includeTwo";
  private static final Set<String> INCLUDE_SET = new HashSet<String>( Arrays.asList( INLCUDE_ONE, INCLUDE_TWO ) );
  private static final String EXCLUDE_ONE = "excludeOne";
  private static final String EXCLUDE_TWO = "excludeTwo";
  private static final Set<String> EXCLUDE_SET = new HashSet<String>( Arrays.asList( EXCLUDE_ONE, EXCLUDE_TWO ) );
  private static final boolean INCLUDE_ACLS = true;
  private static final String CHILD_FILTER = "childFilter";

  private RepositoryRequest request;

  @Before
  public void setUp() {
    request = new RepositoryRequest( PATH, SHOW_HIDE, DEPTH, LEGACY_FILTER );
    request.setTypes( RepositoryRequest.FILES_TYPE_FILTER.FILES );
    request.setIncludeMemberSet( INCLUDE_SET );
    request.setExcludeMemberSet( EXCLUDE_SET );
    request.setIncludeAcls( INCLUDE_ACLS );
    request.setChildNodeFilter( CHILD_FILTER );
  }

  @Test
  public void testRepositoryRequest() {
    // Test no arg constructor
    RepositoryRequest defaultRequest = new RepositoryRequest();
    assertFalse( defaultRequest.isShowHidden() );
    assertEquals( RepositoryRequest.FILES_TYPE_FILTER.FILES_FOLDERS, defaultRequest.getTypes() );
    assertNull( defaultRequest.getIncludeMemberSet() );
    assertNull( defaultRequest.getExcludeMemberSet() );
    assertEquals( new Integer( -1 ), defaultRequest.getDepth() );
    assertNull( defaultRequest.getPath() );
    assertFalse( defaultRequest.isIncludeAcls() );
    assertNull( defaultRequest.getChildNodeFilter() );

    // Test constructor with nulls
    RepositoryRequest nullRequest = new RepositoryRequest( null, null, null, null );
    assertFalse( nullRequest.isShowHidden() );
    assertEquals( new Integer( -1 ), nullRequest.getDepth() );

    // Test constructor with values
    assertEquals( SHOW_HIDE, request.isShowHidden() );
    assertEquals( RepositoryRequest.FILES_TYPE_FILTER.FILES, request.getTypes() );
    assertEquals( INCLUDE_SET, request.getIncludeMemberSet() );
    assertEquals( EXCLUDE_SET, request.getExcludeMemberSet() );
    assertEquals( DEPTH, request.getDepth() );
    assertEquals( PATH, request.getPath() );
    assertEquals( INCLUDE_ACLS, request.isIncludeAcls() );
    assertEquals( CHILD_FILTER, request.getChildNodeFilter() );

    boolean newShowHide = !SHOW_HIDE;
    request.setShowHidden( newShowHide );
    assertEquals( newShowHide, request.isShowHidden() );

    String newPath = "newPath";
    request.setPath( newPath );
    assertEquals( newPath, request.getPath() );

    // Test absence of include/exclude member sets
    String legacyFilter = "includeMembers=(include)|excludeMembers=(exclude)";
    try {
      new RepositoryRequest( PATH, SHOW_HIDE, DEPTH, legacyFilter );
      fail( "RuntimeException should of been thrown" );
    } catch ( RuntimeException e ) {
      // Pass
    }

  }
}
