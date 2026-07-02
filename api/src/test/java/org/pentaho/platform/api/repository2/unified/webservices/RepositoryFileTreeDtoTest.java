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
 * Created by efreitas on 30-07-2018.
 */
public class RepositoryFileTreeDtoTest {

  @Test
  public void testDto() {
    RepositoryFileTreeDto dto = new RepositoryFileTreeDto();
    RepositoryFileDto fileMock = new RepositoryFileDto();
    List<RepositoryFileTreeDto> childrenMock = new ArrayList<>();
    dto.setChildren( childrenMock );
    assertEquals( dto.getChildren(), childrenMock );
    dto.setFile( fileMock );
    assertEquals( dto.getFile(), fileMock );
  }
}
