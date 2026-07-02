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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by bgroves on 10/23/15.
 */
public class RepositoryFileTreeTest {

  private static final RepositoryFile REPO_FILE = mock( RepositoryFile.class );
  private static final RepositoryFile REPO_FILE_CHILD = mock( RepositoryFile.class );
  private static final RepositoryFileTree FILE_TREE_CHILD = new RepositoryFileTree( REPO_FILE_CHILD,
    new ArrayList<RepositoryFileTree>() );
  private static final List<RepositoryFileTree> CHILDREN = Arrays.asList( FILE_TREE_CHILD );
  private static final Boolean VERSION_ENABLED = Boolean.TRUE;
  private static final Boolean VERSION_COMMENT_ENABLED = Boolean.FALSE;

  private RepositoryFileTree fileTree;

  @BeforeEach
  public void setUp() {
    fileTree = new RepositoryFileTree( REPO_FILE, CHILDREN );
    fileTree.setVersioningEnabled( VERSION_ENABLED );
    fileTree.setVersionCommentEnabled( VERSION_COMMENT_ENABLED );
  }

  @Test
  public void testFileTree() {
    assertEquals( REPO_FILE, fileTree.getFile() );
    assertEquals( VERSION_ENABLED, fileTree.getVersioningEnabled() );
    assertEquals( VERSION_COMMENT_ENABLED, fileTree.getVersionCommentEnabled() );
    assertNotEquals( 31, fileTree.hashCode() );
    assertNotNull( fileTree.toString() );
    assertEquals( 1, fileTree.getChildren().size() );

    when( REPO_FILE.isFolder() ).thenReturn( true );
    assertTrue( fileTree.toString().contains( "/" ) );

    try {
      RepositoryFileTree throwError = new RepositoryFileTree( null, null );
      fail( "Should of thrown an IllegalArgumentException" );
    } catch ( Exception e ) {
      // Pass
    }

    // Testing variations of equals
    assertTrue( fileTree.equals( fileTree ) );
    assertFalse( fileTree.equals( null ) );
    assertFalse( fileTree.equals( new String() ) );
  }

  @Test
  public void testBuilder() {
    RepositoryFile nullFile =
        new RepositoryFile( "null", null, false, false, true,
      false, null, null, null, null, false, null, null, null, null, null, null, null, null, new Long( 1 ), null, null );
    RepositoryFileTree.Builder nullBuilder = new RepositoryFileTree.Builder( nullFile );
    RepositoryFileTree anotherFileTree = nullBuilder.build();
    assertFalse( anotherFileTree.equals( fileTree ) );
    assertEquals( nullFile, nullBuilder.getFile() );

    RepositoryFileTree.Builder builder = new RepositoryFileTree.Builder( fileTree );
    RepositoryFileTree dupFileTree = builder.build();
    assertTrue( fileTree.equals( dupFileTree ) );
    fileTree.compareTo( dupFileTree );
    verify( REPO_FILE ).compareTo( any( RepositoryFile.class ) );

    List<RepositoryFileTree.Builder> children = builder.getChildren();
    assertEquals( 1, children.size() );
    builder.child( nullBuilder );
    children = builder.getChildren();
    assertEquals( 2, children.size() );
    assertTrue( children.contains( nullBuilder ) );

    builder = new RepositoryFileTree.Builder( REPO_FILE_CHILD  );
    assertFalse( fileTree.equals( builder.build() ) );
  }

  @Test
  public void testBuilderWithNullChildren() {
    RepositoryFileTree fileTreeChild = new RepositoryFileTree( REPO_FILE_CHILD, null );
    List<RepositoryFileTree> children = Arrays.asList( fileTreeChild );
    RepositoryFileTree fileTreeWithChildren = new RepositoryFileTree( REPO_FILE_CHILD, children );
    RepositoryFileTree.Builder builder = new RepositoryFileTree.Builder( fileTreeWithChildren );
    builder.build();
  }
}
