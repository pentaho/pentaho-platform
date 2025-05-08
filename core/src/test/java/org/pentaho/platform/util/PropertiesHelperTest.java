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


package org.pentaho.platform.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class PropertiesHelperTest {
  @Test
  public void testSegmentsPropertiesIntoMultipleProperties() throws Exception {
    Properties properties = new Properties();
    properties.setProperty( "foodmart.name", "Foodmart" );
    properties.setProperty( "foodmart.connectString", "jdbc:mondrian:host=aplace" );
    properties.setProperty( "sample.name", "Samples" );
    properties.setProperty( "sample.connectString", "jdbc:mondrian:host=overthere" );
    properties.setProperty( "name", "noName" );
    properties.setProperty( "connectString", "jdbc:mondrian:blah" );
    List<Properties> list = PropertiesHelper.segment( properties );
    Collections.sort( list, new Comparator<Properties>() {
      @Override public int compare( Properties o1, Properties o2 ) {
        return o1.getProperty( "name" ).compareTo( o2.getProperty( "name" ) );
      }
    } );
    Properties foodmart = list.get( 0 );
    Assert.assertEquals( "Foodmart", foodmart.getProperty( "name" ) );
    Assert.assertEquals( "jdbc:mondrian:host=aplace", foodmart.getProperty( "connectString" ) );
    Properties sample = list.get( 1 );
    Assert.assertEquals( "Samples", sample.getProperty( "name" ) );
    Assert.assertEquals( "jdbc:mondrian:host=overthere", sample.getProperty( "connectString" ) );
    Properties noname = list.get( 2 );
    Assert.assertEquals( "noName", noname.getProperty( "name" ) );
    Assert.assertEquals( "jdbc:mondrian:blah", noname.getProperty( "connectString" ) );
  }
}
