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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.StringLayout;
import org.pentaho.platform.api.util.LogUtil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


public class Log4JRepositoryExportLog {

  private Logger logger;
  private OutputStream outputStream;
  private String logName;
  private Level logLevel;
  private Appender appender;
  private StringLayout layout;

  /**
   * Constructs an object that keeps track of additional fields for Log4j logging and writes/formats an html file to the
   * output stream provided.
   *
   * @param outputStream
   */
  Log4JRepositoryExportLog( OutputStream outputStream, Level logLevel, StringLayout layout ) {
    this.outputStream = outputStream;
    this.logLevel = logLevel;
    this.layout = layout;
    init();
  }

  /**
   * Constructs an object that keeps track of additional fields for Log4j logging and writes/formats an html file to the
   * output stream provided.
   *
   * @param outputStream
   */
  Log4JRepositoryExportLog( OutputStream outputStream, Level logLevel ) {
    this.outputStream = outputStream;
    this.logLevel = logLevel;
    RepositoryImportHTMLLayout htmlLayout = new RepositoryImportHTMLLayout( logLevel );
    htmlLayout.setTitle( "Repository Backup Log" );
    this.layout = htmlLayout;
    init();
  }

  private void init() {
    logName = "RepositoryExportLog." + getThreadName();
    logger = LogManager.getLogger( logName );
    LogUtil.setLevel( logger, logLevel );
    appender =
        LogUtil.makeAppender( logName, new OutputStreamWriter( outputStream, Charset.forName( "utf-8" ) ), this.layout );
    LogUtil.addAppender( appender, logger, logLevel );
  }

  public Logger getLogger() {
    return logger;
  }


  protected void endJob() {
    try {
      outputStream.write( appender.getLayout().getFooter() );
    } catch ( Exception e ) {
      System.out.println( e );
      // Don't try logging a log error.
    }
    LogUtil.removeAppender( appender, logger );
  }

  private String getThreadName() {
    return Thread.currentThread().getName();
  }
}
