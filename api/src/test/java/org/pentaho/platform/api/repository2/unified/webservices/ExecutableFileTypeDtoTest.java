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
public class ExecutableFileTypeDtoTest {

  @Test
  public void testDto() {

    ExecutableFileTypeDto dto = new ExecutableFileTypeDto();
    String descriptionMock = "descMock";
    String extensionMock = "extensionMock";
    String titleMock = "titleMock";
    boolean canEditMock = true;
    boolean canScheduleMock = true;
    dto.setDescription( descriptionMock );
    assertEquals( dto.getDescription(), descriptionMock );
    dto.setExtension( extensionMock );
    assertEquals( dto.getExtension(), extensionMock );
    dto.setTitle( titleMock );
    assertEquals( dto.getTitle(), titleMock );
    dto.setCanEdit( canEditMock );
    assertEquals( dto.isCanEdit(), canEditMock );
    dto.setCanSchedule( canScheduleMock );
    assertEquals( dto.isCanSchedule(), canScheduleMock );

  }
}
