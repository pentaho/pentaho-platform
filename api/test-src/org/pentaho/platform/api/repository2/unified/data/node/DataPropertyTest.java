/*!
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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified.data.node;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DataPropertyTest {

  private static final String PROP_NAME = "propName";
  private static final String PROP_VALUE = "1";
  private static final DataNode.DataPropertyType PROP_TYPE = DataNode.DataPropertyType.STRING;

  @Test
  public void testDataProperty() {
    DataProperty prop = new DataProperty( PROP_NAME, PROP_VALUE, PROP_TYPE );

    assertEquals( PROP_NAME, prop.getName() );
    assertEquals( new String( PROP_VALUE ), prop.getString() );
    assertEquals( PROP_TYPE, prop.getType() );

    assertFalse( prop.getBoolean() );
    assertEquals( 1, prop.getLong() );
    assertEquals( 1.0, prop.getDouble(), .001 );
    assertNotNull( prop.getRef() );

    try {
      assertNull( prop.getDate() );
      fail( "Should not be a date" );
    } catch ( Exception e ) {
      // Pass
    }

    Date theDate = new Date();
    DataProperty theProp = new DataProperty( null, theDate, null );
    assertEquals( theDate, theProp.getDate() );

    theProp = new DataProperty( null, null, null );
    assertNull( theProp.getDate() );
    assertEquals( 29791, theProp.hashCode() );

    assertEquals( "DataProperty [name=" + PROP_NAME + ", type=" + PROP_TYPE + ", value=" + PROP_VALUE + "]",
      prop.toString() );

    assertFalse( prop.equals( null ) );
    assertTrue( prop.equals( prop ) );
    assertFalse( prop.equals( new String() ) );
    assertTrue( prop.equals( new DataProperty( PROP_NAME, PROP_VALUE, PROP_TYPE ) ) );

    DataProperty nullProp = new DataProperty( null, PROP_VALUE, PROP_TYPE );
    assertFalse( prop.equals( nullProp ) );
    assertFalse( nullProp.equals( prop ) );
    nullProp = new DataProperty( null, null, PROP_TYPE );
    assertFalse( prop.equals( nullProp ) );
    assertFalse( nullProp.equals( prop ) );
    nullProp = new DataProperty( null, null, null );
    assertFalse( prop.equals( nullProp ) );
    assertFalse( nullProp.equals( prop ) );

    // Test null values
    assertNull( nullProp.getRef() );
  }
}
