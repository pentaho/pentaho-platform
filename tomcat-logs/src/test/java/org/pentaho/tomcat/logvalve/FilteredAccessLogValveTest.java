/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.tomcat.logvalve;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that {@link FilteredAccessLogValve} masks credentials before they reach the access log, and that it
 * still delegates correctly to the inherited Tomcat {@code AccessLogValve.log(CharArrayWriter)} implementation.
 *
 * <p>The valve is driven without starting the Tomcat lifecycle: {@code rotatable} is turned off (which makes
 * {@code rotate()} a no-op) and the inherited {@code writer} is supplied directly, so the real Tomcat write path
 * executes against an in-memory sink.</p>
 */
public class FilteredAccessLogValveTest {

  /** Exposes the inherited protected {@code writer} so the real Tomcat write path can be driven in-memory. */
  private static class TestableFilteredAccessLogValve extends FilteredAccessLogValve {
    void useWriter( PrintWriter printWriter ) {
      this.writer = printWriter;
    }
  }

  private TestableFilteredAccessLogValve valve;
  private StringWriter sink;

  @Before
  public void setUp() {
    valve = new TestableFilteredAccessLogValve();
    valve.setRotatable( false );
    sink = new StringWriter();
    valve.useWriter( new PrintWriter( sink, true ) );
  }

  private static CharArrayWriter message( String text ) {
    CharArrayWriter writer = new CharArrayWriter();
    writer.write( text, 0, text.length() );
    return writer;
  }

  /** What actually reached the log, with only the single trailing separator the valve appends removed. */
  private String logged() {
    String written = sink.toString();
    String separator = System.lineSeparator();
    return written.endsWith( separator ) ? written.substring( 0, written.length() - separator.length() ) : written;
  }

  @Test
  public void masksPasswordFollowedByAmpersand() {
    valve.log( message( "POST /pentaho/j_spring_security_check?j_username=admin&j_password=secret&locale=en" ) );

    assertEquals( "POST /pentaho/j_spring_security_check?j_username=admin&j_password=***&locale=en", logged() );
  }

  @Test
  public void masksPasswordFollowedBySpace() {
    valve.log( message( "GET /pentaho/home?j_password=hunter2 HTTP/1.1" ) );

    assertEquals( "GET /pentaho/home?j_password=*** HTTP/1.1", logged() );
  }

  @Test
  public void masksPasswordAtEndOfLine() {
    valve.log( message( "GET /pentaho/home?j_password=trailingSecret" ) );

    assertEquals( "GET /pentaho/home?j_password=***", logged() );
  }

  @Test
  public void masksEveryOccurrence() {
    valve.log( message( "j_password=one&x=1&j_password=two" ) );

    assertEquals( "j_password=***&x=1&j_password=***", logged() );
    assertFalse( logged().contains( "one" ) );
    assertFalse( logged().contains( "two" ) );
  }

  @Test
  public void masksEmptyPasswordValue() {
    valve.log( message( "j_password=&next=1" ) );

    assertEquals( "j_password=***&next=1", logged() );
  }

  @Test
  public void doesNotMaskTheUsername() {
    valve.log( message( "j_username=admin&other=j_passwordish" ) );

    assertEquals( "j_username=admin&other=j_passwordish", logged() );
  }

  @Test
  public void leavesUnrelatedMessagesUntouched() {
    String line = "127.0.0.1 - - [01/Sep/2026:10:00:00 +0000] \"GET /pentaho/home HTTP/1.1\" 200 1234";

    valve.log( message( line ) );

    assertEquals( line, logged() );
  }

  @Test
  public void appendsTheLineSeparatorFromTheInheritedValve() {
    valve.log( message( "j_password=secret" ) );

    assertTrue( "the inherited AccessLogValve is expected to terminate each entry with the platform line separator",
      sink.toString().endsWith( System.lineSeparator() ) );
  }

  @Test
  public void handlesEmptyMessage() {
    valve.log( message( "" ) );

    assertEquals( "", logged() );
  }

  /** Guards the helper above: exactly one separator-terminated entry per log() call, so a double write is visible. */
  @Test
  public void writesExactlyOneEntryPerCall() {
    valve.log( message( "j_password=secret" ) );

    assertEquals( 1, countSeparators( sink.toString() ) );
    assertEquals( "j_password=***", logged() );

    valve.log( message( "second" ) );

    assertEquals( 2, countSeparators( sink.toString() ) );
  }

  private static int countSeparators( String text ) {
    String separator = System.lineSeparator();
    int count = 0;
    int index = text.indexOf( separator );
    while ( index >= 0 ) {
      count++;
      index = text.indexOf( separator, index + separator.length() );
    }
    return count;
  }

  /** With no writer wired up the inherited valve must swallow the write, not propagate a failure to the request. */
  @Test
  public void doesNotThrowWhenNoWriterIsConfigured() {
    TestableFilteredAccessLogValve unwired = new TestableFilteredAccessLogValve();
    unwired.setRotatable( false );

    unwired.log( message( "j_password=secret" ) );
  }

  @Test
  public void neverLeaksTheClearTextPassword() {
    valve.log( message( "j_username=admin&j_password=SuperSecret123!&submit=Login" ) );

    assertFalse( "the clear-text password must never reach the access log", logged().contains( "SuperSecret123!" ) );
    assertTrue( logged().contains( "j_password=***" ) );
    assertTrue( logged().contains( "j_username=admin" ) );
  }
}
