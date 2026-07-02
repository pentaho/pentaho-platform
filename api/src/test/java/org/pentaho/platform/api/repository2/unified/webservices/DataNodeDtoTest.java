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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 27-07-2018.
 */
public class DataNodeDtoTest {

  @Test
  public void testDto() {

    DataNodeDto dto = new DataNodeDto();
    List<DataNodeDto> mockChildNodes = new ArrayList<DataNodeDto>( 0 );
    List<DataPropertyDto> mockChildProperties = new ArrayList<DataPropertyDto>( 0 );
    String mockID = "mockID";
    dto.setId( mockID );
    assertEquals( dto.getId(), mockID );
    String mockName = "mockName";
    dto.setName( mockName );
    assertEquals( dto.getName(), mockName );
    dto.setChildNodes( mockChildNodes );
    assertEquals( dto.getChildNodes(), mockChildNodes );
    dto.setChildProperties( mockChildProperties );
    assertEquals( dto.getChildProperties(), mockChildProperties );

  }
}
