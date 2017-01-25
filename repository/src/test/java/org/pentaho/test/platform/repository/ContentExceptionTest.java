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

package org.pentaho.test.platform.repository;

import junit.framework.TestCase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
