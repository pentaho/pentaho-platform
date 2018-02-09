/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
