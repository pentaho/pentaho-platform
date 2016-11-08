/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 */
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
    properties.setProperty( "sample.connectString", "jdbc:mondrian4:host=overthere" );
    properties.setProperty( "name", "noName" );
    properties.setProperty( "connectString", "jdbc:mondrian4:blah" );
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
    Assert.assertEquals( "jdbc:mondrian4:host=overthere", sample.getProperty( "connectString" ) );
    Properties noname = list.get( 2 );
    Assert.assertEquals( "noName", noname.getProperty( "name" ) );
    Assert.assertEquals( "jdbc:mondrian4:blah", noname.getProperty( "connectString" ) );
  }
}
