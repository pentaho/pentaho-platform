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

package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.api.util.ITempFileDeleter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class StandaloneApplicationContext implements IApplicationContext {

  private String solutionRootPath;

  private String fullyQualifiedServerUrl;
  private String applicationPath;

  private Object context;

  private Properties properties = new Properties();

  private final List<IPentahoSystemEntryPoint> entryPointsList = new ArrayList<IPentahoSystemEntryPoint>();

  private final List<IPentahoSystemExitPoint> exitPointsList = new ArrayList<IPentahoSystemExitPoint>();

  public StandaloneApplicationContext( final String solutionRootPath, final String applicationPath,
                                       final Object context ) {
    this( solutionRootPath, applicationPath );
    this.context = context;
  }

  public StandaloneApplicationContext( final String solutionRootPath, final String applicationPath ) {

    this.solutionRootPath = solutionRootPath;
    this.applicationPath = applicationPath;
    fullyQualifiedServerUrl = null;
  }

  public void setFullyQualifiedServerURL( final String fullyQualifiedServerUrl ) {
    this.fullyQualifiedServerUrl = fullyQualifiedServerUrl;
  }

  public String getFileOutputPath( final String path ) {
    return solutionRootPath + File.separator + path;
  }

  public String getSolutionPath( final String path ) {
    return solutionRootPath + File.separator + path;
  }

  public void setSolutionRootPath( final String solutionRootPath ) {
    this.solutionRootPath = solutionRootPath;
  }

  public File createTempFile( final IPentahoSession session, final String prefix, final String extn, boolean trackFile )
    throws IOException {
    return createTempFile( session, prefix, extn, new File( getSolutionPath( "system/tmp" ) ), trackFile ); //$NON-NLS-1$
  }

  public File createTempFile( final IPentahoSession session, final String prefix, final String extn,
      final File parentDir, boolean trackFile ) throws IOException {
    ITempFileDeleter fileDeleter = null;
    if ( session == null ) {
      return null;
    }
    if ( trackFile ) {
      fileDeleter = (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
    }
    String name = session.getName();
    final String newPrefix =
        new StringBuilder().append( prefix ).append( name.substring( 0, name.length() > 10 ? 10 : name.length() ) )
            .append( '-' ).toString();
    if ( parentDir != null ) {
      parentDir.mkdirs();
    }
    final File file = File.createTempFile( newPrefix, extn, parentDir );
    if ( fileDeleter != null ) {
      fileDeleter.trackTempFile( file );
    } else {
      // There is no deleter, so cleanup on VM exit. (old behavior)
      file.deleteOnExit();
    }
    return file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IApplicationContext#getServerName()
   */
  public String getPentahoServerName() {
    return ""; //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IApplicationContext#getServerPort()
   */
  public int getServerPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Deprecated
  public void setBaseUrl( final String baseUrl ) {
    // DO NOTHING
  }

  @Deprecated
  public String getBaseUrl() {
    return PentahoRequestContextHolder.getRequestContext().getContextPath();
  }

  public String getFullyQualifiedServerURL() {
    return fullyQualifiedServerUrl;
  }

  public String getApplicationPath( final String path ) {
    return applicationPath + File.separator + path;
  }

  public String getProperty( final String key ) {
    return properties.getProperty( key );
  }

  public String getProperty( final String key, final String defaultValue ) {
    return properties.getProperty( key, defaultValue );
  }

  public void setProperties( final Properties props ) {
    properties = props;
  }

  public void addEntryPointHandler( final IPentahoSystemEntryPoint entryPoint ) {
    entryPointsList.add( entryPoint );
  }

  public void removeEntryPointHandler( final IPentahoSystemEntryPoint entryPoint ) {
    entryPointsList.remove( entryPoint );
  }

  public void addExitPointHandler( final IPentahoSystemExitPoint exitPoint ) {
    exitPointsList.add( exitPoint );
  }

  public void removeExitPointHandler( final IPentahoSystemExitPoint exitPoint ) {
    exitPointsList.remove( exitPoint );
  }

  public void invokeEntryPoints() {
    for ( int i = 0; i < entryPointsList.size(); i++ ) {
      entryPointsList.get( i ).systemEntryPoint();
    }
  }

  public void invokeExitPoints() {
    for ( int i = 0; i < exitPointsList.size(); i++ ) {
      exitPointsList.get( i ).systemExitPoint();
    }
  }

  public Object getContext() {
    return context;
  }

  public void setContext( final Object context ) {
    this.context = context;
  }

  public String getSolutionRootPath() {
    return solutionRootPath;
  }
}
