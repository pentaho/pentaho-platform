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


package org.pentaho.test.platform.web.http.api.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.JerseyUtil;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedMap;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class JerseyUtilTest {

  private final static String EXPECTED_VALUE = "EXPECTED_VALUE";
  private final static String KEY = "KEY";

  @Test
  public void testDummy() {
    Map<String, String[]> parameterMap = new HashMap<String, String[]>();
    parameterMap.put( KEY, new String[] { EXPECTED_VALUE } );

    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getParameterMap() ).thenReturn( parameterMap );

    MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();

    Map<String, String[]> resultMap = JerseyUtil.correctPostRequest( formParams, request ).getParameterMap();
    Assert.assertEquals( EXPECTED_VALUE, resultMap.get( KEY )[0] );
  }

}