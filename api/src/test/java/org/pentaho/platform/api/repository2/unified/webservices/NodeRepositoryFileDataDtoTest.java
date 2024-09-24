/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.repository2.unified.webservices;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 30-07-2018.
 */
public class NodeRepositoryFileDataDtoTest {
  @Test
  public void testDto() {

    NodeRepositoryFileDataDto dto = new NodeRepositoryFileDataDto();
    DataNodeDto nodeMock = new DataNodeDto();
    dto.setNode( nodeMock );
    assertEquals( dto.getNode(), nodeMock );

  }
}
