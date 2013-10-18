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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.web;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.pentaho.platform.util.web.HttpUtil;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

@SuppressWarnings( "nls" )
public class HttpUtilTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/web-solution";
  private static final String ALT_SOLUTION_PATH = "test-src/web-solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  public void testUtil() {
    startTest();
    // This should succeed
    String url = "http://www.pentaho.org/demo/news.html"; //$NON-NLS-1$
    String queryString =
        "http://www.pentaho.com/pentaho/ViewAction?solution=samples&path=analysis&action=query1.xaction"; //$NON-NLS-1$
    HttpClient client = HttpUtil.getClient();
    String urlContent = HttpUtil.getURLContent( url );
    HttpClientParams params = client.getParams();
    params.setBooleanParameter( "isDone", true ); //$NON-NLS-1$

    System.out.println( "Content of the URL : " + urlContent ); //$NON-NLS-1$
    try {
      StringBuffer nsb = new StringBuffer();
      System.out.println( "String buffer has : " + nsb ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      System.out.println( "Exception caught" ); //$NON-NLS-1$
    }

    try {
      InputStream is = HttpUtil.getURLInputStream( url );
      Reader reader = HttpUtil.getURLReader( url );
      is.close();
      reader.close();
    } catch ( Exception e ) {
      e.printStackTrace();
      System.out.println( "Exception caught" ); //$NON-NLS-1$
    }

    Map map = HttpUtil.parseQueryString( queryString );

    System.out.println( "Map's Contents are : " + map.toString() ); //$NON-NLS-1$
    try {
      StringBuffer nsb = new StringBuffer();
      HttpUtil.getURLContent_old( url, nsb );
      System.out.println( "Old String buffer has : " + nsb ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      System.out.println( "Exception caught" ); //$NON-NLS-1$
    }

    finishTest();
  }

  public static void main( String[] args ) {
    HttpUtilTest test = new HttpUtilTest();
    test.setUp();
    try {
      test.testUtil();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
