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


package org.pentaho.test.platform.repository;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.platform.api.repository.ContentException;

@SuppressWarnings( "nls" )
public class ContentExceptionTest extends TestCase {
  private Logger logger = LogManager.getLogger( ContentExceptionTest.class );

  public void testContentException2() {
    logger.info( "Expected: Exception will be caught and thrown as a Content Exception" );
    ContentException ce1 = new ContentException( "A test Content Exception has been thrown" );
    System.out.println( "ContentException :" + ce1 );
    assertTrue( true );
  }

  public void testContentException3() {
    logger.info( "Expected: A Content Exception will be created with Throwable as a parameter" );
    ContentException ce2 = new ContentException( new Throwable( "This is a throwable exception" ) );
    System.out.println( "ContentException" + ce2 );
    assertTrue( true );
  }

  public void testContentException4() {
    logger.info( "Expected: Exception will be caught and thrown as a Content Exception" );
    ContentException ce3 = new ContentException( "A test UI Exception has been thrown", new Throwable() );
    System.out.println( "ContentException :" + ce3 );
    assertTrue( true );
  }
}
