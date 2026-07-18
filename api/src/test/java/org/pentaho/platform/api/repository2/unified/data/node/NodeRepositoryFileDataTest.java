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



package org.pentaho.platform.api.repository2.unified.data.node;

import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertEquals;

public class NodeRepositoryFileDataTest {

  @Test
  public void testRepo() {
    DataNode node = new DataNode( "node" );
    NodeRepositoryFileData repo = new NodeRepositoryFileData( node, 100 );

    assertEquals( node, repo.getNode() );
    assertEquals( 100, repo.getDataSize() );
  }

}
