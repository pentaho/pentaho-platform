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

package org.pentaho.platform.api.util;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.StringLayout;

import java.io.OutputStream;

public interface IRepositoryExportLogger extends Log {


  /**
   * Initiates an import job. Each call creates a new log associated with the current thread.
   *
   * @param outputStream Will receive the html content of the log
   * @param logLevel     The log level to be logged.
   * @param layout       The layout to be use.
   */
  void startJob( OutputStream outputStream, Level logLevel, StringLayout layout );

  /**
   * Initiates an import job. Each call creates a new log associated with the current thread.
   *
   * @param outputStream Will receive the html content of the log
   * @param logLevel     The log level to be logged.
   */
  void startJob( OutputStream outputStream, Level logLevel );

  /**
   * Makes an "End Import Job" log entry and releases memory associated with this log.
   */
  void endJob();

  /**
   * Log informational data. Should be called when the starting a new file and when finishing that file.
   *
   * @param s The information message to be logged.
   */
  void info( String s );

  /**
   * Log an error.
   *
   * @param s The Error message to be logged.
   */
  void error( String s );

  /**
   * Log debug information
   *
   * @param s The debug message to be logged
   */
  void debug( String s );

  /**
   * Log error information
   *
   * @param e The exception to be logged.
   */
  void error( Exception e );

  /**
   * Allows a class to check if an ImportLogger has been instantiated for the current thread.
   *
   * @return true if the logger is present.
   */
  boolean hasLogger();
}
