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


package org.pentaho.test.platform.web.doubles;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ApplicationContextDouble implements IApplicationContext {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog( ApplicationContextDouble.class );

  // ~ Instance fields =================================================================================================

  private String fullyQualifiedServerUrl;

  private Set<IPentahoSystemEntryPoint> entryPoints = new HashSet<IPentahoSystemEntryPoint>();

  private Set<IPentahoSystemExitPoint> exitPoints = new HashSet<IPentahoSystemExitPoint>();

  private String solutionRootPath;

  private Object context;

  // ~ Constructors ====================================================================================================

  @SuppressWarnings( "nls" )
  public ApplicationContextDouble() {
    super();
    if ( logger.isDebugEnabled() ) {
      logger.debug( "looking for info as system properties" );
    }
    solutionRootPath = System.getProperty( "org.pentaho.doubles.ApplicationContext.solutionRootPath" );
    fullyQualifiedServerUrl = System.getProperty( "org.pentaho.doubles.ApplicationContext.baseUrl" );
    if ( logger.isDebugEnabled() ) {
      logger.debug( "solutionRootPath=" + solutionRootPath );
      logger.debug( "fullyQualifiedServrUrl=" + fullyQualifiedServerUrl );
    }
    if ( StringUtils.isBlank( solutionRootPath ) || StringUtils.isBlank( fullyQualifiedServerUrl ) ) {
      throw new IllegalArgumentException( "missing required system properties" );
    }
  }

  // ~ Methods =========================================================================================================

  public void addEntryPointHandler( IPentahoSystemEntryPoint entryPoint ) {
    entryPoints.add( entryPoint );
  }

  public void addExitPointHandler( IPentahoSystemExitPoint exitPoint ) {
    exitPoints.add( exitPoint );
  }

  public String getApplicationPath( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFullyQualifiedServerURL() {
    return fullyQualifiedServerUrl;
  }

  public Object getContext() {
    return context;
  }

  public String getFileOutputPath( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getPentahoServerName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProperty( String arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSolutionPath( String path ) {
    return solutionRootPath + "/" + path; //$NON-NLS-1$
  }

  public String getSolutionRootPath() {
    return solutionRootPath;
  }

  public void invokeEntryPoints() {
    // TODO Auto-generated method stub

  }

  public void invokeExitPoints() {
    // TODO Auto-generated method stub

  }

  public void removeEntryPointHandler( IPentahoSystemEntryPoint entryPoint ) {
    entryPoints.remove( entryPoint );
  }

  public void removeExitPointHandler( IPentahoSystemExitPoint exitPoint ) {
    exitPoints.remove( exitPoint );
  }

  public void setFullyQualifiedServerURL( String fullyQualifiedServerUrl ) {
    this.fullyQualifiedServerUrl = fullyQualifiedServerUrl;
  }

  public void setContext( Object context ) {
    this.context = context;
  }

  public void setSolutionRootPath( String solutionRootPath ) {
    this.solutionRootPath = solutionRootPath;

  }

  public File createTempFile( final IPentahoSession session, final String prefix, final String extn, boolean trackFile )
    throws IOException {
    return createTempFile( session, prefix, extn, new File( getSolutionPath( "system/tmp" ) ), trackFile ); //$NON-NLS-1$
  }

  public File createTempFile( final IPentahoSession session, final String prefix, final String extn,
      final File parentDir, boolean trackFile ) throws IOException {
    ITempFileDeleter fileDeleter = null;
    if ( ( session != null ) && trackFile ) {
      fileDeleter = (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
    }
    final String newPrefix =
        new StringBuilder().append( prefix ).append( session.getId().substring( 0, 10 ) ).append( '-' ).toString();
    final File file = File.createTempFile( newPrefix, extn, parentDir );
    if ( fileDeleter != null ) {
      fileDeleter.trackTempFile( file );
    } else {
      // There is no deleter, so cleanup on VM exit. (old behavior)
      file.deleteOnExit();
    }
    return file;
  }

  @Deprecated
  public String getBaseUrl() {
    return PentahoRequestContextHolder.getRequestContext().getContextPath();
  }

  @Deprecated
  public void setBaseUrl( String url ) {
    // DO NOTHING
  }

}
