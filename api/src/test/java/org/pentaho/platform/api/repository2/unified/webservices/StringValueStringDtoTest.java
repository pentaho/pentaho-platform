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



package org.pentaho.platform.api.repository2.unified.webservices;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 30-07-2018.
 */
public class StringValueStringDtoTest {

  @Test
  public void testDto() {
    StringKeyStringValueDto dto = new StringKeyStringValueDto();
    String keyMock = "key";
    String valueMock = "value";
    dto.setKey( keyMock );
    assertEquals( dto.getKey(), keyMock );
    dto.setValue( valueMock );
    assertEquals( dto.getValue(), valueMock );
  }

}
