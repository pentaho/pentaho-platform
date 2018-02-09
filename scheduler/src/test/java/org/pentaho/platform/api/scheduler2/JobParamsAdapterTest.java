/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JobParamsAdapterTest {
  @Test
  public void testMarshal() throws Exception {
    JobParamsAdapter adapter = new JobParamsAdapter();

    Map<String, Serializable> dataMap = new HashMap<String, Serializable>();
    dataMap.put( "a", "A" );
    dataMap.put( "bb", "[B]" );
    dataMap.put( "ccc", "[C].[CCC]" );
    dataMap.put( "dddd", "[D].[DDD,ddd]" );
    dataMap.put( "eeeee", null );
    dataMap.put( null, "FFFFFF" );
    JobParams expectedJobParams = createJobParams( new JobParam[]{
      createJobParam( "a", "A" ),
      createJobParam( "bb", "[B]" ),
      createJobParam( "ccc", "[C].[CCC]" ),
      createJobParam( "dddd", "[D].[DDD,ddd]" )
    } );

    final JobParams resultJobParams = adapter.marshal( dataMap );

    assertNotNull( resultJobParams );
    assertNotNull( resultJobParams.jobParams );
    java.util.Arrays.sort( resultJobParams.jobParams, new JobParamWholeComparator() );
    assertJobParamArrayEquals( "", expectedJobParams.jobParams, resultJobParams.jobParams );
  }

  @Test
  public void testMarshalMultiValue() throws Exception {
    JobParamsAdapter adapter = new JobParamsAdapter();

    Map<String, Serializable> dataMap = new HashMap<String, Serializable>();
    dataMap.put( "a", "A" );
    dataMap.put( "bb", "[B]" );
    ArrayList<String> cValue = castAsArrayList( new String[] { "[C].[CCC]", "[D].[DDD,ddd]" } );
    dataMap.put( "ccc", cValue );
    ArrayList<String> eValue = castAsArrayList( new String[] { null, "FFFFFF" } );
    dataMap.put( "eeeee", eValue );
    JobParams expectedJobParams = createJobParams( new JobParam[]{
      createJobParam( "a", "A" ),
      createJobParam( "bb", "[B]" ),
      createJobParam( "ccc", "[C].[CCC]" ),
      createJobParam( "ccc", "[D].[DDD,ddd]" ),
      createJobParam( "eeeee", "FFFFFF" )
    } );

    final JobParams resultJobParams = adapter.marshal( dataMap );

    assertNotNull( resultJobParams );
    assertNotNull( resultJobParams.jobParams );
    Arrays.sort( resultJobParams.jobParams, new JobParamWholeComparator() );
    assertJobParamArrayEquals( "", expectedJobParams.jobParams, resultJobParams.jobParams );
  }

  @Test
  public void testMarshalRemovesVariableDuplicate() throws Exception {
    JobParamsAdapter adapter = new JobParamsAdapter();

    Map<String, Serializable> dataMap = new HashMap<String, Serializable>();
    HashMap<String, String> variables = new HashMap<>();
    variables.put( "test1", "val1" );
    variables.put( "test2", "val2" );
    HashMap<String, String> parameters = new HashMap<>();
    parameters.put( "test2", "val2Updated" );

    dataMap.put( "variables", variables );
    dataMap.put( "parameters", parameters );
    dataMap.put( "test3", "val3" );


    JobParams expectedJobParams = createJobParams( new JobParam[]{
      createJobParam( "test1", "val1" ),
      createJobParam( "test2", "val2Updated" ),
      createJobParam( "test3", "val3" )
    } );

    final JobParams resultJobParams = adapter.marshal( dataMap );

    assertNotNull( resultJobParams );
    assertNotNull( resultJobParams.jobParams );

    Arrays.sort( resultJobParams.jobParams, new JobParamWholeComparator() );
    assertJobParamArrayEquals( "", expectedJobParams.jobParams, resultJobParams.jobParams );
  }

  private void assertJobParamArrayEquals( String msg, JobParam[] expected, final JobParam[] actual ) {
    Assert.assertNotNull( msg + " null", actual );
    Assert.assertEquals( msg + " length", expected.length, actual.length );
    for ( int i = 0; i < expected.length; i++ ) {
      assertJobParamEquals( msg + " [" + i + "]", expected[i], actual[i] );
    }
  }

  private void assertJobParamEquals( String msg, final JobParam expected, final JobParam actual ) {
    Assert.assertEquals( msg + " .name", expected.name, actual.name );
    Assert.assertEquals( msg + " .value", expected.value, actual.value );
  }

  @Test
  public void testUnmarshal() throws Exception {
    JobParamsAdapter adapter = new JobParamsAdapter();

    Map<String, Serializable> expectedDataMap = new HashMap<String, Serializable>();
    expectedDataMap.put( "a", "A" );
    expectedDataMap.put( "bb", "[B]" );
    expectedDataMap.put( "ccc", "[C].[CCC]" );
    expectedDataMap.put( "dddd", "[D].[DDD,ddd]" );
    JobParams dataJobParams = createJobParams( new JobParam[]{
      createJobParam( "a", "A" ),
      createJobParam( "bb", "[B]" ),
      createJobParam( "ccc", "[C].[CCC]" ),
      createJobParam( "dddd", "[D].[DDD,ddd]" )
    } );

    Map<String, Serializable> resultMap = adapter.unmarshal( dataJobParams );

    Assert.assertNotNull( "resultMap", resultMap );
    Assert.assertEquals( "resultMap.size", expectedDataMap.size(), resultMap.size() );
    for ( String key : expectedDataMap.keySet() ) {
      assertEquals( "resultMap[" + key + "]", expectedDataMap.get( key ), resultMap.get( key ) );
    }
  }

  @Test
  public void testUnmarshalMultiValue() throws Exception {
    JobParamsAdapter adapter = new JobParamsAdapter();

    Map<String, Serializable> expectedDataMap = new HashMap<String, Serializable>();
    expectedDataMap.put( "a", "A" );
    expectedDataMap.put( "bb", "[B]" );
    final ArrayList<String> cValue = castAsArrayList( new String[] { "[C].[CCC]", "[D].[DDD,ddd]" } );
    expectedDataMap.put( "ccc", cValue );
    JobParams dataJobParams = createJobParams( new JobParam[]{
      createJobParam( "a", "A" ),
      createJobParam( "bb", "[B]" ),
      createJobParam( "ccc", "[C].[CCC]" ),
      createJobParam( "ccc", "[D].[DDD,ddd]" )
    } );

    Map<String, Serializable> resultMap = adapter.unmarshal( dataJobParams );

    Assert.assertNotNull( "resultMap", resultMap );
    Assert.assertEquals( "resultMap.size", expectedDataMap.size(), resultMap.size() );
    for ( String key : new String[] { "a", "bb" } ) {
      assertEquals( "resultMap[" + key + "]", expectedDataMap.get( key ), resultMap.get( key ) );
    }

    String key = "ccc";
    assertTrue( "resultMap[" + key + "] is collection", resultMap.get( key ) instanceof Collection );
    Collection<?> actualCValue = (Collection<?>) resultMap.get( key );
    assertEquals( "resultMap[" + key + "].size", cValue.size(), actualCValue.size() );
    assertTrue( "resultMap[" + key + "] all expected values", cValue.containsAll( actualCValue ) );

  }

  JobParam createJobParam( String n, String v ) {
    JobParam r = new JobParam();
    r.name = n;
    r.value = v;
    return r;
  }

  JobParams createJobParams( JobParam[] v ) {
    JobParams r = new JobParams();
    r.jobParams = v;
    return r;
  }

  static class JobParamWholeComparator implements Comparator<JobParam> {

    @Override
    public int compare( JobParam arg0, JobParam arg1 ) {
      int r = arg0.name.compareTo( arg1.name );
      if ( r != 0 ) {
        return r;
      }
      return arg0.value.compareTo( arg1.value );
    }

  }
  ArrayList<String> castAsArrayList( String[] values ) {
    if ( values == null ) {
      return null;
    }
    ArrayList<String> list = new ArrayList<String>( values.length );
    for ( String v: values ) {
      list.add( v );
    }
    return list;
  }

}
