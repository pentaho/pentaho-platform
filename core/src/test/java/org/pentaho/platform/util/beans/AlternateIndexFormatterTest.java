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


package org.pentaho.platform.util.beans;

import static org.junit.Assert.*;

import org.junit.Test;

public class AlternateIndexFormatterTest {

  private static final String SIMPLE_NAME = "simpleName";

  private static final String INDEXED_NAME = "indexedName_0";

  private static final String INDEXED_NAME_CONVERTED = "indexedName[0]";

  private static final String UNDERSCORE_NAME = "indexed_Name";


  @Test
  public void testFormat_indexed() {
    AlternateIndexFormatter formatter = new AlternateIndexFormatter();
    String actual = formatter.format( INDEXED_NAME );
    assertEquals( "indexed name should be formated", INDEXED_NAME_CONVERTED, actual );
  }

  @Test
  public void testFormat_underscore() {
    AlternateIndexFormatter formatter = new AlternateIndexFormatter();
    String actual = formatter.format( UNDERSCORE_NAME );
    assertEquals( "underscore without number should not be formated", UNDERSCORE_NAME, actual );
  }

  @Test
  public void testFormat_simpleName() {
    AlternateIndexFormatter formatter = new AlternateIndexFormatter();
    String actual = formatter.format( SIMPLE_NAME );
    assertEquals( "simple name should not be formated", SIMPLE_NAME, actual );
  }

}
