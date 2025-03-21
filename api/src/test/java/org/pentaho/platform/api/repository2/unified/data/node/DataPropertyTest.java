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


package org.pentaho.platform.api.repository2.unified.data.node;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
