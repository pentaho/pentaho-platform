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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class SimpleParameterProviderTest extends TestCase {

  public void testConstructors() {

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.setParameter( "param", "value" );
    assertEquals( "param value if wrong", "value", params.getStringParameter( "param", null ) );

    Map map = new HashMap();
    map.put( "param2", "value2" );
    params = new SimpleParameterProvider( map );
    assertEquals( "param value if wrong", "value2", params.getStringParameter( "param2", null ) );
    assertTrue( params.hasParameter( "param2" ) );
    assertFalse( params.hasParameter( "bogus" ) );

    params = new SimpleParameterProvider( null );
    assertEquals( "param value if wrong", null, params.getStringParameter( "param2", null ) );

  }

  public void testMap() {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put( "int", new Integer( 100 ) );
    paramMap.put( "long", new Long( 200 ) );

    SimpleParameterProvider params = new SimpleParameterProvider( paramMap );
    validateInteger( params );
    validateLong( params );
    Iterator it = params.getParameterNames();
    int n = 0;
    while ( it.hasNext() ) {
      n++;
      String name = (String) it.next();
      assertTrue( "param name is wrong", "int".equals( name ) || "long".equals( name ) );
    }
    assertEquals( "wrong number of parameters", 2, n );

    paramMap = new HashMap<String, Object>();
    paramMap.put( "int", new Integer( 100 ) );
    params = new SimpleParameterProvider();
    params.setParameters( paramMap );
    validateInteger( params );
    assertEquals( "param value is wrong", -1, params.getLongParameter( "long", -1 ) );
  }

  public void testConvert() {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put( "int-1", new Object[] { new Integer( 100 ) } );
    paramMap.put( "int-2", new Object[] { new Integer( 100 ), new Integer( 999 ) } );
    paramMap.put( "long", new Long( 200 ) );

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.copyAndConvertParameters( paramMap );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int-1", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int-1", -1 ) );
    assertEquals( "param value is wrong", "200", params.getStringParameter( "long", null ) );
    assertEquals( "param value is wrong", 200, params.getLongParameter( "long", -1 ) );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int-2", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int-2", -1 ) );
    assertEquals( "param value is wrong", -1, params.getLongParameter( "int-3", -1 ) );
    Object val = params.getArrayParameter( "int-2", null );
    assertNotNull( "param value is wrong", val );

  }

  public void testArrays() {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put( "int-1", new Object[] { new Integer( 100 ) } );
    paramMap.put( "int-2", new Object[] { new Integer( 100 ), new Integer( 999 ) } );

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.copyAndConvertParameters( paramMap );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int-1", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int-1", -1 ) );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int-2", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int-2", -1 ) );
    assertEquals( "param value is wrong", -1, params.getLongParameter( "int-3", -1 ) );
    Object[] val = params.getArrayParameter( "int-2", null );
    assertNotNull( "param value is wrong", val );
    assertEquals( "param value is wrong", 100, val[0] );
    assertEquals( "param value is wrong", 999, val[1] );

    Object[][] value2 = new Object[0][0];
    params.setParameter( "2darray", value2 );
    assertEquals( "param value is wrong", value2, params.getParameter( "2darray" ) );
    assertEquals( "param value is wrong", null, params.getStringParameter( "2darray", null ) );

  }

  public void testResultSet() {

    MemoryResultSet data = new MemoryResultSet();

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.setParameter( "data", data );
    assertTrue( params.hasParameter( "data" ) );
    assertEquals( "param value is wrong", data, params.getListParameter( "data" ) );

  }

  public void testAdditional() {

    String paramStr = "base?int=100&long=200";
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put( "_PENTAHO_ADDITIONAL_PARAMS_", paramStr );

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.copyAndConvertAdditionalParameters( paramMap );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int", -1 ) );
    assertEquals( "param value is wrong", "200", params.getStringParameter( "long", null ) );
    assertEquals( "param value is wrong", 200, params.getLongParameter( "long", -1 ) );

    paramStr = "int=300&long=400";
    paramMap.put( "_PENTAHO_ADDITIONAL_PARAMS_", paramStr );

    params = new SimpleParameterProvider();
    params.copyAndConvertAdditionalParameters( paramMap );
    assertEquals( "param value is wrong", "300", params.getStringParameter( "int", null ) );
    assertEquals( "param value is wrong", 300, params.getLongParameter( "int", -1 ) );
    assertEquals( "param value is wrong", "400", params.getStringParameter( "long", null ) );
    assertEquals( "param value is wrong", 400, params.getLongParameter( "long", -1 ) );
  }

  public void testIntegers() {

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.setParameter( "int", new Integer( 100 ) );

    validateInteger( params );
  }

  public void validateInteger( SimpleParameterProvider params ) {

    assertEquals( "param value is wrong", 100, params.getParameter( "int" ) );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int", -1 ) );
    BigDecimal decimal = params.getDecimalParameter( "int", null );
    assertNotNull( "param value is wrong", decimal );
    assertEquals( "param value is wrong", 100, decimal.intValue() );

    String[] strs = params.getStringArrayParameter( "int", new String[0] );
    assertNotNull( "param value is wrong", strs );
    assertEquals( "param value is wrong", "100", strs[0] );

  }

  public void testLongs() {

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.setParameter( "long", new Long( 200 ) );

    validateLong( params );
  }

  public void testDates() throws Exception {

    SimpleParameterProvider params = new SimpleParameterProvider();
    String dateStr = DateFormat.getInstance().format( new Date() );
    Date now = DateFormat.getInstance().parse( dateStr );
    params.setParameter( "date", now );
    params.setParameter( "date2", dateStr );

    assertEquals( "wrong date", now, params.getDateParameter( "date", null ) );
    assertEquals( "wrong date", now, params.getDateParameter( "bogus", now ) );
    assertEquals( "wrong date", now, params.getDateParameter( "date2", null ) );
    assertNull( "wrong date", params.getDateParameter( "bogus", null ) );

  }

  public void validateLong( SimpleParameterProvider params ) {

    assertEquals( "param value is wrong", (long) 200, params.getParameter( "long" ) );
    assertEquals( "param value is wrong", "200", params.getStringParameter( "long", null ) );
    assertEquals( "param value is wrong", 200, params.getLongParameter( "long", -1 ) );
    BigDecimal decimal = params.getDecimalParameter( "long", null );
    assertNotNull( "param value is wrong", decimal );
    assertEquals( "param value is wrong", 200, decimal.intValue() );

    params.setParameter( "long", (long) 200 );
    assertEquals( "param value is wrong", (long) 200, params.getParameter( "long" ) );

    String[] strs = params.getStringArrayParameter( "long", new String[0] );
    assertNotNull( "param value is wrong", strs );
    assertEquals( "param value is wrong", "200", strs[0] );

  }

  public void testStrings() {

    SimpleParameterProvider params = new SimpleParameterProvider();
    params.setParameter( "int", "100" );
    assertEquals( "param value is wrong", "100", params.getParameter( "int" ) );
    assertEquals( "param value is wrong", "100", params.getStringParameter( "int", null ) );
    assertEquals( "param value is wrong", 100, params.getLongParameter( "int", 0 ) );
    BigDecimal decimal = params.getDecimalParameter( "int", null );
    assertNotNull( "param value is wrong", decimal );
    assertEquals( "param value is wrong", 100, decimal.intValue() );

    String[] strs = params.getStringArrayParameter( "int", new String[0] );
    assertNotNull( "param value is wrong", strs );
    assertEquals( "param value is wrong", "100", strs[0] );

  }

}
