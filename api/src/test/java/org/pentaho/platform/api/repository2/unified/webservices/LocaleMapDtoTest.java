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
public class LocaleMapDtoTest {

  @Test
  public void testDto() {

    LocaleMapDto dto = new LocaleMapDto();
    String localeMock = "localeMock";
    List<StringKeyStringValueDto> propertiesMock = new ArrayList<StringKeyStringValueDto>();
    dto.setLocale( localeMock );
    assertEquals( dto.getLocale(), localeMock );
    dto.setProperties( propertiesMock );
    assertEquals( dto.getProperties(), propertiesMock );

  }
}
