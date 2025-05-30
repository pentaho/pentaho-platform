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


package org.pentaho.platform.api.engine;

import java.io.File;
import java.io.IOException;

/**
 * Defines a set of methods to retrieve information about the application environment.
 * <p>
 * There is one context per server application (web application), so the class can be used as a place to share
 * global application data.
 * 
 * @version 1.0
 */
public interface IApplicationContext {

  /**
   * Retrieves the fully qualified path to the location of the Hitachi Vantara solution, appending the path given in the
   * parameter. The path is formatted appropriately for the platform that the application is running on.
   * 
   * @param path
   *          a path to a location that exists relative to the solution tree
   * @return fully qualified path to the requested location in the solution tree
   */
  public String getSolutionPath( String path );

  public String getSolutionRootPath();

  /**
   * Used for content output (temporary and otherwise), returns a fully qualified path suitable for creating a
   * <tt>File</tt> object from.
   * 
   * @param path
   *          Relative path within the solution to the file location. Solution path will be pre-pended
   * @return Fully qualified path
   */
  public String getFileOutputPath( String path );

  /**
   * Retrieves the descriptive name of the platform application.
   * <p>
   * The Pentaho server name should specified in the system settings configuration file, using the
   * <code>name</code> element
   * 
   * @return the descriptive server name as specified in the system settings, or
   *         "Pentaho Business Intelligence Platform" by default.
   */
  public String getPentahoServerName();

  /**
   * @deprecated Returns a URL to the server application, up to and including the context.
   *             <p>
   *             The URL that is returned is derived from the server context, and thus will include the protocol,
   *             host name, port, and application context root.
   * 
   * @return the URL to the server application context root. Use getFullyQualifiedServerURL instead
   */
  @Deprecated
  public String getBaseUrl();

  /**
   * Returns a fully qualified URL to the server application, up to and including the context.
   * <p>
   * This method should only be used if delivering an offline content or in a scenario where fully qualified url is
   * required.
   * 
   * @return the URL to the server application context root.
   */
  public String getFullyQualifiedServerURL();

  /**
   * Returns the path to the web application or standalone application. This is only used in a few places, like
   * loading the portlet localization messages.
   * 
   * @param path
   *          a path to a location that exists relative to the application root directory
   * @return the path to the server application root
   */
  public String getApplicationPath( String path );

  /**
   * If there were any other properties set (for example, initParams in the servlet context), this will let you
   * have access to all of those properties that are set.
   * 
   * @param key
   *          property Name
   * @return string property value
   */
  public String getProperty( String key );

  /**
   * If there were any other properties set (for example, initParams in the servlet context), this will let you
   * have access to all of those properties that are set.
   * 
   * @param key
   *          property Name
   * @param defaultValue
   *          default value if the property is not specified.
   * @return string property value
   */
  public String getProperty( String key, String defaultValue );

  /**
   * Adds an entry point handler. The entry point handler is called when actions start on a particular thread.
   * 
   * @param entryPoint
   */
  public void addEntryPointHandler( IPentahoSystemEntryPoint entryPoint );

  /**
   * Removes an entry point handler.
   * 
   * @param entryPoint
   */
  public void removeEntryPointHandler( IPentahoSystemEntryPoint entryPoint );

  /**
   * Adds an exit point handler. The exit point handler is called when actions stop on a particular thread
   * 
   * @param entryPoint
   */
  public void addExitPointHandler( IPentahoSystemExitPoint exitPoint );

  /**
   * Removes an exit point handler.
   * 
   * @param exitPoint
   */
  public void removeExitPointHandler( IPentahoSystemExitPoint exitPoint );

  /**
   * Invokes all entry point handlers.
   */
  public void invokeEntryPoints();

  /**
   * Invokes all exit point handlers.
   */
  public void invokeExitPoints();

  public void setFullyQualifiedServerURL( String url );

  /**
   * @deprecated
   * @param url
   *          Use setFullyQualifiedServerURL instead
   */
  @Deprecated
  public void setBaseUrl( String url );

  public void setSolutionRootPath( String path );

  public Object getContext();

  public void setContext( Object context );

  /**
   * Creates a temporary file in the specified parent folder and optionally tracks it for deletion on session
   * termination
   * 
   * @param session
   *          - IPentahoSession
   * @param prefix
   *          - file prefix
   * @param extension
   *          - file extension
   * @param parentDir
   *          - parent folder to create the temp file in
   * @param trackFile
   *          - true = add it to the session deleter
   * @return
   * @throws IOException
   */
  public File createTempFile( final IPentahoSession session, final String prefix, final String extension,
      final File parentDir, boolean trackFile ) throws IOException;

  /**
   * Creates a temporary file in the system/tmp solutions folder
   * 
   * @param session
   *          - IPentahoSession
   * @param prefix
   *          - file prefix
   * @param extension
   *          - file extension
   * @param trackFile
   *          - true = add it to the session deleter
   * @return
   * @throws IOException
   */
  public File createTempFile( final IPentahoSession session, final String prefix, final String extension,
      boolean trackFile ) throws IOException;

}
