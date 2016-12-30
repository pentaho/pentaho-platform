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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by bgroves on 10/23/15.
 */
public class RepositoryFileTreeTest {

  private static final RepositoryFile REPO_FILE = Mockito.mock( RepositoryFile.class );
  private static final RepositoryFile REPO_FILE_CHILD = Mockito.mock( RepositoryFile.class );
  private static final RepositoryFileTree FILE_TREE_CHILD = new RepositoryFileTree( REPO_FILE_CHILD,
    new ArrayList<RepositoryFileTree>() );
  private static final List<RepositoryFileTree> CHILDREN = Arrays.asList( FILE_TREE_CHILD );
  private static final Boolean VERSION_ENABLED = Boolean.TRUE;
  private static final Boolean VERSION_COMMENT_ENABLED = Boolean.FALSE;

  private RepositoryFileTree fileTree;

  @Before
  public void setUp() {
    fileTree = new RepositoryFileTree( REPO_FILE, CHILDREN );
    fileTree.setVersioningEnabled( VERSION_ENABLED );
    fileTree.setVersionCommentEnabled( VERSION_COMMENT_ENABLED );
  }

  @Test
  public void testFileTree() {
    Assert.assertEquals( REPO_FILE, fileTree.getFile() );
    Assert.assertEquals( VERSION_ENABLED, fileTree.getVersioningEnabled() );
    Assert.assertEquals( VERSION_COMMENT_ENABLED, fileTree.getVersionCommentEnabled() );
    Assert.assertNotEquals( 31, fileTree.hashCode() );
    Assert.assertNotNull( fileTree.toString() );
    Assert.assertEquals( 1, fileTree.getChildren().size() );

    Mockito.when( REPO_FILE.isFolder() ).thenReturn( true );
    Assert.assertTrue( fileTree.toString().contains( "/" ) );

    try {
      RepositoryFileTree throwError = new RepositoryFileTree( null, null );
      Assert.fail( "Should of thrown an IllegalArgumentException" );
    } catch ( Exception e ) {
      // Pass
    }

    // Testing variations of equals
    Assert.assertTrue( fileTree.equals( fileTree ) );
    Assert.assertFalse( fileTree.equals( null ) );
    Assert.assertFalse( fileTree.equals( new String() ) );
  }

  @Test
  public void testBuilder() {
    RepositoryFile nullFile =
        new RepositoryFile( null, null, false, false, true,
      false, null, null, null, null, false, null, null, null, null, null, null, null, null, new Long( 1 ), null, null );
    RepositoryFileTree.Builder nullBuilder = new RepositoryFileTree.Builder( nullFile );
    RepositoryFileTree anotherFileTree = nullBuilder.build();
    Assert.assertFalse( anotherFileTree.equals( fileTree ) );
    Assert.assertEquals( nullFile, nullBuilder.getFile() );

    RepositoryFileTree.Builder builder = new RepositoryFileTree.Builder( fileTree );
    RepositoryFileTree dupFileTree = builder.build();
    Assert.assertTrue( fileTree.equals( dupFileTree ) );
    fileTree.compareTo( dupFileTree );
    Mockito.verify( REPO_FILE ).compareTo( Mockito.any( RepositoryFile.class ) );

    List<RepositoryFileTree.Builder> children = builder.getChildren();
    Assert.assertEquals( 1, children.size() );
    builder.child( nullBuilder );
    children = builder.getChildren();
    Assert.assertEquals( 2, children.size() );
    Assert.assertTrue( children.contains( nullBuilder ) );

    builder = new RepositoryFileTree.Builder( REPO_FILE_CHILD  );
    Assert.assertFalse( fileTree.equals( builder.build() ) );
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
