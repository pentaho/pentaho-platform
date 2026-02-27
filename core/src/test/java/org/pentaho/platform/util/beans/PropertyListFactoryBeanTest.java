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

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

public class PropertyListFactoryBeanTest {
  @Test
  public void testBreaksPropertiesFileIntoAList() throws Exception {
    PropertyListFactoryBean bean = new PropertyListFactoryBean();
    Properties properties = new Properties();
    properties.setProperty( "foodmart.name", "Foodmart" );
    properties.setProperty( "foodmart.connectString", "jdbc:mondrian:host=aplace" );
    properties.setProperty( "sample.name", "Samples" );
    properties.setProperty( "sample.connectString", "jdbc:mondrian:host=overthere" );
    bean.setProperties( properties );
    List<Properties> list = (List<Properties>) bean.getObject();
    Properties foodmart = list.get( 0 );
    Assert.assertEquals( "Foodmart", foodmart.getProperty( "name" ) );
    Assert.assertEquals( "jdbc:mondrian:host=aplace", foodmart.getProperty( "connectString" ) );
    Properties sample = list.get( 1 );
    Assert.assertEquals( "Samples", sample.getProperty( "name" ) );
    Assert.assertEquals( "jdbc:mondrian:host=overthere", sample.getProperty( "connectString" ) );
  }
}
