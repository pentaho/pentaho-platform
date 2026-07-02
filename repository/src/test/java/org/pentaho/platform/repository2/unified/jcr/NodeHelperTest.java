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


package org.pentaho.platform.repository2.unified.jcr;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;

public class NodeHelperTest {

  private static Logger mLog = LoggerFactory.getLogger( NodeHelperTest.class );
  private Node node;

  @Before
  public void setup() {
    node = Mockito.mock( Node.class );
  }

  @After
  public void destroy() {
  }

  @Test
  public void testaddNode() {
    mLog.info( "testaddNode.." );

    try {
      NodeHelper.addNode( node, "PREFIX", "TEST_NODE" );
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testaddNode2() {
    mLog.info( "testaddNode2.." );

    try {
      NodeHelper.addNode( node, "PREFIX", "TEST_NODE", "NODE_PARAM" );
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testCheckAddNode() {
    mLog.info( "testCheckAddNode.." );

    try {
      NodeHelper.checkAddNode( node, "TEST_NODE" );
    } catch ( Exception e ) {
      fail();
    }
  }

  @Test
  public void testCreateDataNode() {
    mLog.info( "testCreateDataNode.." );

    try {
      DataNode lDataNode = NodeHelper.createDataNode( "TEST_NODE" );

      assertTrue( "TEST_NODE".equals( lDataNode.getName() ) );

    } catch ( Exception e ) {
      fail();
    }
  }
}
