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
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.BufferedContentItem;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@SuppressWarnings( { "all" } )
public class SimpleOutputHandlerTest extends TestCase {

  public void test1() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    TestOutputHandler.contentItem = new SimpleContentItem();
    factory.defineObject( "testoutut", TestOutputHandler.class.getName(), StandaloneObjectFactory.Scope.LOCAL );
    PentahoSystem.registerObjectFactory( factory );

    StandaloneSession session = new StandaloneSession();

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    SimpleOutputHandler handler = new SimpleOutputHandler( out, false );

    MimeTypeListener listener = new MimeTypeListener();
    handler.setMimeTypeListener( listener );
    assertEquals( listener, handler.getMimeTypeListener() );

    assertFalse( handler.allowFeedback() );
    handler.setSession( session );
    assertEquals( session, handler.getSession() );

    handler.setContentItem( new SimpleContentItem(), null, null );

    IContentItem content2 = handler.getOutputContentItem( "bogus", "testoutut:bogus", null, null );
    assertEquals( TestOutputHandler.contentItem, content2 );

    content2 = handler.getOutputContentItem( "bogus", "bogus", null, null );
    assertNull( content2 );

    content2 = handler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );

    assertEquals( out, content2.getOutputStream( null ) );
    assertFalse( handler.contentDone() );
    assertNull( handler.getFeedbackContentItem() );
    assertFalse( handler.contentDone() );

    handler.setOutputPreference( -1 );
    assertEquals( -1, handler.getOutputPreference() );

    handler.setMimeType( "test/test" );
    assertEquals( "test/test", handler.getMimeType() );

    handler.setOutput( "bogus", null );

    handler.setOutput( "file:bogus", null );

    IContentItem content3 = new BufferedContentItem( null );
    OutputStream os = content3.getOutputStream( null );
    os.write( "test data".getBytes() );
    content3.closeOutputStream();
    handler.setOutput( IOutputHandler.CONTENT, content3 );

    assertEquals( "test data", new String( out.toByteArray() ) );

    handler.setOutput( IOutputHandler.CONTENT, "+test data2" );
    assertEquals( "test data+test data2", new String( out.toByteArray() ) );

  }

  /*
   * public void test2() throws Exception {
   * 
   * StandaloneSession session = new StandaloneSession();
   * 
   * OutputStream out = new MockExceptionOutputStream(); IContentItem content = new SimpleContentItem(out);
   * 
   * SimpleOutputHandler handler = new SimpleOutputHandler( content, true );
   * 
   * assertTrue( handler.allowFeedback() );
   * 
   * assertFalse( handler.contentDone() ); assertNotNull( handler.getFeedbackContentItem() ); assertTrue(
   * handler.contentDone() ); IContentItem content2 = handler.getFeedbackContentItem();
   * 
   * assertEquals( content.getOutputStream(null), content2.getOutputStream(null) );
   * 
   * IContentItem content3 = new BufferedContentItem( null ); content3.getOutputStream(null).write(
   * "test data".getBytes() ); content3.closeOutputStream(); handler.setOutput( IOutputHandler.CONTENT, content3);
   * 
   * }
   */
  public void test3() throws Exception {

    StandaloneSession session = new StandaloneSession();

    OutputStream out = new MockExceptionOutputStream();

    SimpleOutputHandler handler = new SimpleOutputHandler( out, false );

    assertFalse( handler.allowFeedback() );
    handler.setSession( session );
    assertEquals( session, handler.getSession() );

    IContentItem content2 = handler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );

    assertEquals( out, content2.getOutputStream( null ) );
    assertNull( handler.getFeedbackContentItem() );

    try {
      handler.setOutput( IOutputHandler.CONTENT, "test data" );
      fail( "Exception not detected." );
    } catch ( Exception ex ) {
      // Test passed.
    }

  }

  public void test4() throws Exception {

    StandaloneSession session = new StandaloneSession();

    OutputStream out = new ByteArrayOutputStream();

    SimpleOutputHandler handler = new SimpleOutputHandler( out, true );

    assertTrue( handler.allowFeedback() );

    assertNotNull( handler.getFeedbackContentItem() );
    IContentItem content2 = handler.getFeedbackContentItem();

    assertEquals( out, content2.getOutputStream( null ) );

    OutputStream out3 = new ByteArrayOutputStream();

    handler.setOutputStream( out3, IOutputHandler.RESPONSE, IOutputHandler.CONTENT );
    IContentItem content3 = handler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null );

    assertEquals( out3, content3.getOutputStream( null ) );

  }

  /*public void testGetOutputContentItem_object_name_important() {
    OutputStream out = new ByteArrayOutputStream();
    SimpleOutputHandler handler = new SimpleOutputHandler( out, false );

    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    factory.defineObject( "contentrepo", TestOutputHandler.class.getName(), StandaloneObjectFactory.Scope.GLOBAL );
    PentahoSystem.registerObjectFactory( factory );
    // Verify we can look up content items when the objectName is required
    IContentItem contentItem = handler.getOutputContentItem( "contentrepo", "myreport", null, null );
    assertNotNull( contentItem );
  }*/
}
