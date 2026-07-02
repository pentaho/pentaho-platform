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
 * Created by efreitas on 27-07-2018.
 */
public class DataPropertyDtoTest {

  @Test
  public void testDto() {

    DataPropertyDto dto = new DataPropertyDto();
    String mockValue = "mockID";
    dto.setValue( mockValue );
    assertEquals( dto.getValue(), mockValue );
    String mockName = "mockName";
    dto.setName( mockName );
    assertEquals( dto.getName(), mockName );
    int mockType = 999;
    dto.setType( mockType );
    assertEquals( dto.getType(), mockType );

  }

}
