/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.test.platform.web;

import org.pentaho.platform.util.web.HttpUtil;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

@SuppressWarnings( "nls" )
public class HttpUtilIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-solution";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-solution";
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
    String urlContent = HttpUtil.getURLContent( url );

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
    HttpUtilIT test = new HttpUtilIT();
    test.setUp();
    try {
      test.testUtil();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
