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

package org.pentaho.platform.plugin.services.importexport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This class was derived from Log4j HTML
 * <p>
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 *
 * @author tkafalas
 */
public class RepositoryTextLayout implements StringLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;
  public static final String LINE_SEP = System.getProperty( "line.separator" );
  private static final String REGEXP = Strings.LINE_SEPARATOR.equals( "\n" ) ? "\n" : Strings.LINE_SEPARATOR + "|\n";

  private Level loggerLogLevel;

  // output buffer appended to when format() is invoked
  private StringBuffer sbuf = new StringBuffer( BUF_SIZE );

  String title = "Log4J Log Messages";

  public RepositoryTextLayout( Level loggerLogLevel ) {
    super();
    this.loggerLogLevel = loggerLogLevel;
  }

  /**
   * The <b>Title</b> option takes a String value. This option sets the document title of the generated HTML document.
   *
   * <p>
   * Defaults to 'Log4J Log Messages'.
   */
  public void setTitle( String title ) {
    this.title = title;
  }

  /**
   * Returns the current value of the <b>Title</b> option.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the content type output by this layout, i.e "text/html".
   */
  public String getContentType() {
    return "text/plain";
  }

  @Override
  public Map<String, String> getContentFormat() {
    return null;
  }

  public String format( LogEvent event ) {

    Level logLevel = event.getLevel();
    if ( sbuf.capacity() > MAX_CAPACITY ) {
      sbuf = new StringBuffer( BUF_SIZE );
    } else {
      sbuf.setLength( 0 );
    }

    sbuf.append( LINE_SEP );

    DateFormat df = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
    Date date = new Date();
    date.setTime( event.getTimeMillis() );
    String time = null;
    try {
      time = df.format( date );
    } catch ( Exception ex ) {
      StatusLogger.getLogger().error( "Error occurred while converting date.", ex );
    }

    sbuf.append( time );

    // File/Folder
    String currentFile = ThreadContext.get( "currentFile" );
    if ( currentFile != null && currentFile.length() > 0 ) {
      sbuf.append( "\t" );
      sbuf.append( currentFile );
    }
    // debug level
    sbuf.append( "\t" );
    sbuf.append( String.valueOf( event.getLevel() ) );

    // Message
    sbuf.append( "\t" );
    sbuf.append( event.getMessage() );

    return sbuf.toString();
  }

  /**
   * Returns appropriate headers.
   */
  public byte[] getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( title );
    return sbuf.toString().getBytes( StandardCharsets.UTF_8 );
  }

  @Override
  public byte[] toByteArray( LogEvent event ) {
    return format( event ).getBytes( StandardCharsets.UTF_8 );
  }

  @Override
  public String toSerializable( LogEvent event ) {
    return format( event );
  }

  /**
   * Returns the appropriate footers.
   */
  public byte[] getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "\n\nEnd of Log\n\n" );
    return sbuf.toString().getBytes( StandardCharsets.UTF_8 );
  }

  /**
   * The layout does not handle the throwable contained in logging events. Hence, this method return <code>true</code>.
   */
  public boolean ignoresThrowable() {
    return true;
  }

  @Override
  public Charset getCharset() {
    return StandardCharsets.UTF_8;
  }

  @Override
  public void encode( LogEvent source, ByteBufferDestination destination ) {

  }
}
