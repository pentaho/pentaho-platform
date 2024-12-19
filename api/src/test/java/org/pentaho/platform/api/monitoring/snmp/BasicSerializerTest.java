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


package org.pentaho.platform.api.monitoring.snmp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by bgroves on 10/20/15.
 */

public class BasicSerializerTest {

  public IVariableSerializer.BasicSerializer serializer;

  @BeforeEach
  public void setUp() {
    serializer = new IVariableSerializer.BasicSerializer();
  }

  @Test
  public void testToInteger() {
    Integer integer = serializer.serializeToInt( new Integer( 10 ) );
    assertEquals( new Integer( 10 ), integer );

    integer = serializer.serializeToInt( "11" );
    assertEquals( new Integer( 11 ), integer );

    integer = serializer.serializeToInt( null );
    assertEquals( new Integer( -1 ), integer );

    integer = serializer.serializeToInt( new Object() );
    assertEquals( new Integer( 0 ), integer );
  }

  @Test
  public void testToString() {
    String string = serializer.serializeToString( new Integer( 10 ) );
    assertEquals( "10", string );

    string = serializer.serializeToString( null );
    assertEquals( "", string );

  }
}
