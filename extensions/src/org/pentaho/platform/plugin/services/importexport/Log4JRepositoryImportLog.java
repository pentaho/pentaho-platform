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

package org.pentaho.platform.plugin.services.importexport;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.WriterAppender;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class Log4JRepositoryImportLog {

  private Logger logger;
  static final String FILE_KEY = "currentFile"; // Intentionally scoped as default
  private OutputStream outputStream;
  private String currentFilePath;
  private String logName;
  private String importRootPath;
  private Level logLevel;
  private WriterAppender writeAppender;

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
    init();
  }

  private void init() {
    logName = "RepositoryImportLog." + getThreadName();
    logger = Logger.getLogger( logName );
    logger.setLevel( logLevel );
    RepositoryImportHTMLLayout htmlLayout = new RepositoryImportHTMLLayout( logLevel );
    htmlLayout.setTitle( "Repository Import Log" );
    writeAppender = new WriterAppender( htmlLayout, new OutputStreamWriter( outputStream, Charset.forName( "utf-8" ) ) );
    logger.addAppender( writeAppender );
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
   * @param currentFilePath
   *          the currentFilePath to set
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
    try {
      outputStream.write( writeAppender.getLayout().getFooter().getBytes() );
    } catch ( Exception e ) {
      System.out.println( e );
      // Don't try logging a log error.
    }
    logger.removeAppender( logName );
  }

  private String getThreadName() {
    return Thread.currentThread().getName();
  }

}
