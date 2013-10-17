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

import org.apache.commons.logging.Log;
import org.apache.log4j.Level;

import java.io.OutputStream;

/**
 * * Below is a sample of how to use this class to generate a log file. 1) You must use startJob(OutputStream,
 * ImportPath) to instantiate the log and supply it with an output stream to hold the log and the root folder of the
 * import.
 * 
 * 2) Call setCurrentFilePath() each time you start processing a new import file. The log shows the file being imported
 * so it must be registered.
 * 
 * 3) Call endJob() when the import is done to log the finish and release resources. If the the import terminates
 * abnormally this call should be in the finally block.
 * 
 * 
 * Sample code taken from RepositoryImportLogTest
 * 
 * FileOutputStream fileStream = new FileOutputStream(outputFile); logger =
 * PentahoSystem.get(IRepositoryImportLogger.class);
 * 
 * //You must call this method to start posting the log. logger.startJob(fileStream, "/import/Path");
 * 
 * logger.setCurrentFilePath("/path/file1path"); logger.debug("Some more detail here"); logger.info("Success");
 * 
 * logger.setCurrentFilePath("path/file2path");
 * 
 * //Simulate an exception try { throw new RuntimeException("forced exception"); } catch (Exception e) {
 * logger.error(e); } //End of job logger.endJob();
 * 
 * @author TKafalas
 * 
 */
public interface IRepositoryImportLogger extends Log {

  /**
   * Initiates an import job. Each call creates a new log associated with the current thread.
   * 
   * @param outputStream
   *          Will receive the html content of the log
   * @param importRootPath
   *          The root import dir receiving the import
   * @param logLevel
   *          The log level to be logged.
   */
  void startJob( OutputStream outputStream, String importRootPath, Level logLevel );

  /**
   * Registers the file being worked on. Each log entry will list the path to the file being processed. Call this method
   * just before processing the next file. It will automatically post a "Start File Import" entry in the log.
   * 
   * @param currentFilePath
   *          path to file being imported
   */
  void setCurrentFilePath( String currentFilePath );

  /**
   * Makes an "End Import Job" log entry and releases memory associated with this log.
   */
  void endJob();

  /**
   * Log informational data. Should be called when the starting a new file and when finishing that file.
   * 
   * @param s
   *          The information message to be logged.
   */
  void info( String s );

  /**
   * Log an error.
   * 
   * @param s
   *          The Error message to be logged.
   */
  void error( String s );

  /**
   * Log debug information
   * 
   * @param s
   *          The debug message to be logged
   */
  void debug( String s );

  /**
   * Log error information
   * 
   * @param e
   *          The exception to be logged.
   */
  void error( Exception e );

  /**
   * Allows a class to check if an ImportLogger has been instantiated for the current thread.
   * 
   * @return true if the logger is present.
   */
  boolean hasLogger();
}
