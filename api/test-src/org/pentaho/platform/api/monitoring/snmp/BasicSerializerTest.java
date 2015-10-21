/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.api.monitoring.snmp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by bgroves on 10/20/15.
 */

public class BasicSerializerTest {

  public IVariableSerializer.BasicSerializer serializer;

  @Before
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
