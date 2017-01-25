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

package org.pentaho.platform.api.repository2.unified.data.node;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DataNodeTest {
  public static final String NODE_NAME = "newNode";
  public static final String NODE_ID = "nodeId";
  public static final String ADD_1_NODE = "addOneNode";
  public static final String ADD_2_NODE = "addTwoNode";
  public static final String ADD_3_NODE = "addThreeNode";

  public static final String PROP_1_NAME = "prop1";
  public static final String PROP_2_NAME = "prop2";
  public static final String PROP_3_NAME = "prop3";
  public static final String PROP_4_NAME = "prop4";
  public static final String PROP_5_NAME = "prop5";
  public static final String PROP_6_NAME = "prop6";

  public static final List<String> PROP_NAMES = new ArrayList<String>( Arrays
      .asList( PROP_1_NAME, PROP_2_NAME, PROP_3_NAME, PROP_4_NAME, PROP_5_NAME, PROP_6_NAME ) );

  public static final String PROP_1 = "propertyOne";
  public static final boolean PROP_2 = false;
  public static final long PROP_3 = 10;
  public static final double PROP_4 = 10.00000001;
  public static final Date PROP_5 = new Date();
  public static final String NODE_REF_NAME = "refDataNode";
  public static final DataNodeRef PROP_6 = new DataNodeRef( NODE_REF_NAME );

  public DataNode node;

  @Before
  public void setUp() {
    node = new DataNode( NODE_NAME );
    node.setId( NODE_ID );
  }

  @Test
  public void testDataNode() {
    assertEquals( NODE_NAME, node.getName() );
    assertEquals( NODE_ID, node.getId() );

    assertEquals( -269422715, node.hashCode() );
    assertEquals( "newNode/ {}\n", node.toString() );

    assertFalse( node.equals( new DataNode( "diffNodeName" ) ) );

    DataNode sameNode = new DataNode( NODE_NAME );
    sameNode.setId( NODE_ID );
    assertTrue( node.equals( sameNode ) );

    DataNode origNode = new DataNode( NODE_NAME );
    setChildNodes( origNode );
    setProperties( origNode );

    assertNotEquals( origNode.hashCode(), node.hashCode() );

    DataNode dupNode = new DataNode( NODE_NAME );
    setChildNodes( dupNode );
    setProperties( dupNode );

    // Test all aspects of toString
    assertEquals( origNode.toString(), dupNode.toString() );

    // Test some variations of equals
    assertTrue( origNode.equals( dupNode ) );
    assertFalse( node.equals( dupNode ) );
    assertFalse( node.equals( null ) );
    assertFalse( node.equals( new String() ) );
    DataNode diffNameNode = new DataNode( "diffName" );
    diffNameNode.setId( NODE_ID );
    assertFalse( node.equals( diffNameNode ) );

    // Test null ID check
    dupNode.setId( null );
    origNode.setId( NODE_ID );
    assertFalse( dupNode.equals( origNode ) );
    assertFalse( origNode.equals( dupNode ) );

    // Test null name
    DataNode anotherDupNode = new DataNode( null );
    anotherDupNode.setId( NODE_ID );
    setChildNodes( anotherDupNode );
    setProperties( anotherDupNode );
    assertFalse( anotherDupNode.equals( origNode ) );

    // Test object equals
    assertTrue( node.equals( node ) );
  }

  @Test
  public void testProperties() {
    assertEquals( 0, getNumberOfProperties( node ) );

    setProperties( node );

    assertEquals( 6, getNumberOfProperties( node ) );

    for ( String propName : PROP_NAMES ) {
      assertTrue( node.hasProperty( propName ) );
    }

    DataProperty propTwo = node.getProperty( PROP_2_NAME );
    assertEquals( PROP_2_NAME, propTwo.getName() );
    assertEquals( PROP_2, propTwo.getBoolean() );
    assertFalse( node.hasProperty( "blah" ) );
  }

  @Test
  public void testNodes() {
    assertEquals( 0, getNumberOfNodes( node ) );

    setChildNodes( node );

    assertEquals( ADD_1_NODE, node.getNode( ADD_1_NODE ).getName() );
    assertNotNull( ADD_2_NODE, node.getNode( ADD_2_NODE ).getName() );

    DataNode nodeThree = new DataNode( ADD_3_NODE );
    node.addNode( nodeThree );
    assertEquals( nodeThree, node.getNode( ADD_3_NODE ) );

    assertTrue( node.hasNode( ADD_2_NODE ) );
    assertFalse( node.hasNode( "noNode" ) );

    assertEquals( 3, getNumberOfNodes( node ) );
  }

  private void setProperties( DataNode node ) {
    node.setProperty( PROP_1_NAME, PROP_1 );
    node.setProperty( PROP_2_NAME, PROP_2 );
    node.setProperty( PROP_3_NAME, PROP_3 );
    node.setProperty( PROP_4_NAME, PROP_4 );
    node.setProperty( PROP_5_NAME, PROP_5 );
    node.setProperty( PROP_6_NAME, PROP_6 );

  }

  private void setChildNodes( DataNode node ) {
    node.addNode( ADD_1_NODE );
    DataNode nodeTwo = new DataNode( ADD_2_NODE );
    node.addNode( nodeTwo );
  }

  private int getNumberOfProperties( DataNode node ) {
    Iterable<DataProperty> props = node.getProperties();
    int cnt = 0;
    for ( DataProperty p : props ) {
      cnt += 1;
    }
    return cnt;
  }

  private int getNumberOfNodes( DataNode node ) {
    Iterable<DataNode> nodes = node.getNodes();
    int cnt = 0;
    for ( DataNode n : nodes ) {
      cnt += 1;
    }
    return cnt;
  }
}
