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


package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class DefaultRepositoryVersionManager implements IRepositoryVersionManager {

  private boolean masterVersioning = true;
  private boolean masterVersionComments = true;
  private IPlatformMimeResolver platformMimeResolver;

  /**
   * This implmementation of IRepositoryVersionManager determines whether a repository element
   * should be versioned based on setting in the MimeType class.  These settings are defined
   * in the various importHandlerMimeTypeDefinitions.xml files for the server and plugins.
   * 
   * The masterVersioning and masterVersionComments flags default to true
   * These two fields represent master switches. If a switch is false, then
   * that function is disabled for this repository no matter what MimeType is
   * affected. If the switch is true then the effective value of the flag
   * will be dictated by the MimeType definition.
   * 
   * @Author tkafalas
   */
  public DefaultRepositoryVersionManager() {
    platformMimeResolver = PentahoSystem.get( IPlatformMimeResolver.class );

    Boolean systemVersioningEnabled =
        PentahoSystem.get( Boolean.class, "versioningEnabled", PentahoSessionHolder.getSession() );
    if ( systemVersioningEnabled != null ) {
      masterVersioning = systemVersioningEnabled;
    }

    Boolean systemVersionCommentsEnabled =
        PentahoSystem.get( Boolean.class, "versionCommentsEnabled", PentahoSessionHolder.getSession() );
    if ( systemVersionCommentsEnabled != null ) {
      masterVersionComments = systemVersionCommentsEnabled;
    }
  }

  @Override
  public boolean isVersionCommentEnabled( String fullPath ) {
    if ( !isVersioningEnabled( fullPath ) || !masterVersionComments )
      return false;
    IMimeType mimeType = platformMimeResolver.resolveMimeTypeForFileName( fullPath );
    if ( mimeType == null ) {
      return false;
    }
    return mimeType.isVersionCommentEnabled();
  }

  @Override
  public boolean isVersioningEnabled( String fullPath ) {
    if ( !masterVersioning )
      return false;
    IMimeType mimeType = platformMimeResolver.resolveMimeTypeForFileName( fullPath );
    if ( mimeType == null ) {
      return false;
    }
    return mimeType.isVersionEnabled();
  }

  // Used for unit tests
  public void setPlatformMimeResolver( IPlatformMimeResolver platformMimeResolver ) {
    this.platformMimeResolver = platformMimeResolver;
  }

  public void setMasterVersioning( boolean masterVersioningEnabled ) {
    this.masterVersioning = masterVersioningEnabled;
  }

  public void setMasterVersionComments( boolean masterVersionCommentsEnabled ) {
    this.masterVersionComments = masterVersionCommentsEnabled;
  }

}
