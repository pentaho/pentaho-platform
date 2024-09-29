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

package org.pentaho.test.platform.web.http.api.resources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.JerseyUtil;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JerseyUtilTest {
  
  private final static String EXPECTED_VALUE = "EXPECTED_VALUE";
  private final static String KEY = "KEY";
  
  @Test
  public void testDummy() {
    Map<String, String[]> parameterMap = new HashMap<String, String[]>();
    parameterMap.put( KEY, new String[] { EXPECTED_VALUE } );

    HttpServletRequest request = mock( HttpServletRequest.class );
    when( request.getParameterMap() ).thenReturn( parameterMap );

    MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();

    Map<String, String[]> resultMap = JerseyUtil.correctPostRequest( formParams, request ).getParameterMap();
    Assert.assertEquals( EXPECTED_VALUE, resultMap.get( KEY )[0] );
  }

}