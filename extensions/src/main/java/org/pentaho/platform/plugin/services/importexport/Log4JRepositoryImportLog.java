/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.importexport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.StringLayout;
import org.pentaho.platform.api.util.LogUtil;
import org.slf4j.MDC;

public class Log4JRepositoryImportLog {

  private Logger logger;
  static final String FILE_KEY = "currentFile"; // Intentionally scoped as default
  private OutputStream outputStream;
  private String currentFilePath;
  private String logName;
  private String importRootPath;
  private Level logLevel;
  private Appender appender;
  private StringLayout layout;
  private Writer writer;

  /**
   * Constructs an object that keeps track of additional fields for Log4j logging and writes/formats an html file to the
   * output stream provided.
   *
   * @param outputStream
   */
  Log4JRepositoryImportLog( OutputStream outputStream, String importRootPath, Level logLevel, StringLayout layout ) {
    this.outputStream = outputStream;
    this.importRootPath = importRootPath;
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
  Log4JRepositoryImportLog( OutputStream outputStream, String importRootPath, Level logLevel ) {
    this.outputStream = outputStream;
    this.importRootPath = importRootPath;
    this.logLevel = logLevel;
    RepositoryImportHTMLLayout htmlLayout = new RepositoryImportHTMLLayout( logLevel );
    htmlLayout.setTitle( "Repository Import Log" );
    this.layout = htmlLayout;
    init();
  }

  private void init() {
    logName = "RepositoryImportLog." + getThreadName();
    logger = LogManager.getLogger( logName );
    LogUtil.setLevel( logger, logLevel );
    writer = new OutputStreamWriter( outputStream, StandardCharsets.UTF_8 );
    appender = LogUtil.makeAppender( logName, writer, this.layout );
    LogUtil.addAppender( appender, logger, logLevel );
  }

  public Logger getLogger() {
    return logger;
  }

  /**
   * @return the currentFilePath
   */
  public String getCurrentFilePath() {
    return currentFilePath;
  }

  /**
   * @param currentFilePath the currentFilePath to set
   */
  public void setCurrentFilePath( String currentFilePath ) {
    this.currentFilePath = currentFilePath;
    MDC.put( FILE_KEY, currentFilePath );
  }

  /**
   * @return the importRootPath
   */
  public String getImportRootPath() {
    return importRootPath;
  }

  protected void endJob() {
    LogUtil.removeAppender( appender, logger );
    try {
      writer.flush();
    } catch ( IOException e ) {
      System.out.println( e );
    }
  }

  private String getThreadName() {
    return Thread.currentThread().getName();
  }
}
