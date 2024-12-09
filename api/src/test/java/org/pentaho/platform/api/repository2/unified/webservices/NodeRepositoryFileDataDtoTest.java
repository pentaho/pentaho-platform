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


package org.pentaho.platform.api.repository2.unified.webservices;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 30-07-2018.
 */
public class NodeRepositoryFileDataDtoTest {
  @Test
  public void testDtoNode() {

    NodeRepositoryFileDataDto dto = new NodeRepositoryFileDataDto();
    DataNodeDto nodeMock = new DataNodeDto();
    dto.setNode( nodeMock );
    assertEquals( nodeMock, dto.getNode() );

  }

  @Test
  public void testDtoDataSize() {

    NodeRepositoryFileDataDto dto = new NodeRepositoryFileDataDto();
    int dataSize = 100;
    dto.setDataSize( dataSize );
    assertEquals( dataSize, dto.getDataSize() );

  }
}
