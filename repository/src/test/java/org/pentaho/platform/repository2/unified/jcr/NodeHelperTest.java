/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
