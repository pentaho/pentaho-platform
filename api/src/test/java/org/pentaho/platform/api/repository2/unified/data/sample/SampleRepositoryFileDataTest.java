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


package org.pentaho.platform.api.repository2.unified.data.sample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by bgroves on 11/6/15.
 */
public class SampleRepositoryFileDataTest {
  private static final String STRING = "String";
  private static final Boolean BOOLEAN = false;
  private static final Integer INTEGER = 10;

  @Test
  public void testGetters() {
    SampleRepositoryFileData file = new SampleRepositoryFileData( STRING, BOOLEAN, INTEGER );

    assertEquals( STRING, file.getSampleString() );
    assertEquals( BOOLEAN, file.getSampleBoolean() );
    assertTrue( INTEGER.equals( file.getSampleInteger() ) );
    assertEquals( STRING.length() + 2, file.getDataSize() );
  }
}
