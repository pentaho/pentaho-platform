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
import org.pentaho.platform.engine.core.output.BufferedContentItem;
import org.pentaho.platform.engine.core.output.MultiContentItem;
import org.pentaho.platform.engine.core.output.MultiOutputStream;
import org.pentaho.platform.engine.core.output.SimpleContentItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings( { "all" } )
public class OutputTest extends TestCase {

  public void testBufferedContentItem() throws Exception {

    TestContentListener listener = new TestContentListener();

    BufferedContentItem content = new BufferedContentItem( listener );

    OutputStream out = content.getOutputStream( null );

    content.setMimeType( "text/test" );

    out.write( "test sting".getBytes() );

    content.closeOutputStream();

    InputStream in = content.getInputStream();

    byte[] b = new byte[100];
    int n = in.read( b );

    assertEquals( "test sting", new String( b, 0, n ) );
    assertEquals( "text/test", listener.mimeType );
  }

  public void testMultiContentItem() {

    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream out2 = new ByteArrayOutputStream();

    SimpleContentItem item1 = new SimpleContentItem( out1 );
    SimpleContentItem item2 = new SimpleContentItem( out2 );

    MultiContentItem multiContent = new MultiContentItem();
    multiContent.addContentItem( item1 );
    multiContent.addContentItem( item2 );

    byte[] in = "abcd".getBytes();
    String outStr1 = "";
    String outStr2 = "";

    try {
      OutputStream multi = multiContent.getOutputStream( "" );
      multi.write( 'a' );
      multi.write( in, 1, 2 );
      multi.write( in );
      multiContent.closeOutputStream();
    } catch ( IOException e ) {
      // we should not get here
      assertEquals( "IOException", null, e );
    }
    outStr1 = new String( out1.toByteArray() );
    outStr2 = new String( out2.toByteArray() );

    assertEquals( "Output stream 1", "abcabcd", outStr1 );
    assertEquals( "Output stream 2", "abcabcd", outStr2 );

  }

  public void testMultiContentItemErrors() {

    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    OutputStream out2 = new MockExceptionOutputStream();

    SimpleContentItem item1 = new SimpleContentItem( out1 );
    SimpleContentItem item2 = new SimpleContentItem( out2 );

    MultiContentItem multiContent = new MultiContentItem();
    multiContent.addContentItem( item1 );
    multiContent.addContentItem( item2 );

    byte[] in = "abcd".getBytes();
    String outStr1 = "";
    String outStr2 = "";

    try {
      OutputStream multi = multiContent.getOutputStream( "" );
      multi.write( 'a' );
      // we should not get here
      assertFalse( "IOException expected", true );
    } catch ( IOException e ) {
      assertNotNull( "IOException expected", e );
    }
    try {
      multiContent.closeOutputStream();
    } catch ( Exception e ) {
      // we should not get here
      assertEquals( "IOException", null, e );
    }
    outStr1 = new String( out1.toByteArray() );

    assertEquals( "Output stream 1", "a", outStr1 );

  }

  public void testMultiStream() {

    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    ByteArrayOutputStream out2 = new ByteArrayOutputStream();

    ByteArrayOutputStream[] outs = new ByteArrayOutputStream[] { out1, out2 };

    MultiOutputStream multi = new MultiOutputStream( outs );
    byte[] in = "abcd".getBytes();
    String outStr1 = "";
    String outStr2 = "";

    try {
      multi.write( 'a' );
      multi.write( in, 1, 2 );
      multi.write( in );
      multi.close();
    } catch ( IOException e ) {
      // we should not get here
      assertEquals( "IOException", null, e );
    }
    outStr1 = new String( out1.toByteArray() );
    outStr2 = new String( out2.toByteArray() );

    assertEquals( "Output stream 1", "abcabcd", outStr1 );
    assertEquals( "Output stream 2", "abcabcd", outStr2 );
  }

  public void testMultiStreamErrors() {

    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    MockExceptionOutputStream out2 = new MockExceptionOutputStream();
    ByteArrayOutputStream out3 = new ByteArrayOutputStream();

    OutputStream[] outs = new OutputStream[] { out1, out2, out3 };

    MultiOutputStream multi = new MultiOutputStream( outs );
    byte[] in = "abcd".getBytes();
    String outStr1 = "";
    String outStr2 = "";

    try {
      multi.write( 'a' );
    } catch ( IOException e ) {
      // we expect to get here
      assertEquals( "IOException", "Test Exception", e.getMessage() );
    }
    try {
      multi.write( in, 1, 2 );
    } catch ( IOException e ) {
      // we expect to get here
      assertEquals( "IOException", "Test Exception", e.getMessage() );
    }
    try {
      multi.write( in );
    } catch ( IOException e ) {
      // we expect to get here
      assertEquals( "IOException", "Test Exception", e.getMessage() );
    }
    try {
      multi.close();
    } catch ( IOException e ) {
      // we expect to get here
      assertEquals( "IOException", "Test Exception", e.getMessage() );
    }

    outStr1 = new String( out1.toByteArray() );
    outStr2 = new String( out3.toByteArray() );

    assertEquals( "Output stream 1", "abcabcd", outStr1 );
    assertEquals( "Output stream 2", "abcabcd", outStr2 );
  }

  public void testSimpleContentItem() throws Exception {

    OutputStream out = new ByteArrayOutputStream();
    SimpleContentItem content = new SimpleContentItem();
    content.setMimeType( "test/test" );
    content.setOutputStream( out );
    content.setName( "testname" );

    // assertEquals("wrong value", null, content.getPath() );
    assertNotNull( content.getPath() ); // SimpleContentItem changed to not return null for path.
    assertEquals( "wrong value", "test/test", content.getMimeType() );
    assertEquals( "wrong value", null, content.getInputStream() );
    assertEquals( "wrong value", out, content.getOutputStream( null ) );

    // these should not throw errors
    content.closeOutputStream();

    out = new MockExceptionOutputStream();
    content.setOutputStream( out );

    content.closeOutputStream();

  }

}
