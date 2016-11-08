/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util.versionchecker;

import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;
import org.pentaho.versionchecker.IVersionCheckDataProvider;

import java.util.Map;

public class PentahoVersionCheckDataProvider implements IVersionCheckDataProvider {

  /**
   * The version information for the pentaho platform is in the core jar - that is the fallback position. The
   * VersionHelper implementation however should be in a .jar file with correct manifest.
   */
  protected static final VersionInfo versionInfo;

  static {
    //
    // Allow override of product id information
    //
    IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class, null );
    if ( versionHelper != null ) {
      versionInfo = VersionHelper.getVersionInfo( versionHelper.getClass() );
    } else {
      versionInfo = VersionHelper.getVersionInfo( PentahoSystem.class );
    }
  }

  protected int versionRequestFlags = IVersionCheckDataProvider.DEPTH_MINOR_MASK
      + IVersionCheckDataProvider.DEPTH_GA_MASK;

  public void setVersionRequestFlags( final int flags ) {
    versionRequestFlags = flags;
  }

  /**
   * Returns the application id (code) for this application (the pentaho platform)
   */
  public String getApplicationID() {
    return PentahoVersionCheckDataProvider.versionInfo == null ? null : PentahoVersionCheckDataProvider.versionInfo
        .getProductID();
  }

  /**
   * Returns the application version number found in the manifest
   */
  public String getApplicationVersion() {
    return PentahoVersionCheckDataProvider.versionInfo == null ? null : PentahoVersionCheckDataProvider.versionInfo
        .getVersionNumber();
  }

  /**
   * Returns the base url for the connection to the pentaho version checking server. Currently, there is no reason
   * to use anything other than the default.
   */
  public String getBaseURL() {
    return null;
  }

  /**
   * Returns the extra information that can be provided.
   */
  public Map getExtraInformation() {
    return null;
  }

  protected int computeOSMask() {
    try {
      String os = System.getProperty( "os.name" ); //$NON-NLS-1$
      if ( os != null ) {
        os = os.toLowerCase();
        if ( os.indexOf( "windows" ) >= 0 ) { //$NON-NLS-1$
          return IVersionCheckDataProvider.DEPTH_WINDOWS_MASK;
        } else if ( os.indexOf( "mac" ) >= 0 ) { //$NON-NLS-1$
          return IVersionCheckDataProvider.DEPTH_MAC_MASK;
        } else if ( os.indexOf( "linux" ) >= 0 ) { //$NON-NLS-1$
          return IVersionCheckDataProvider.DEPTH_LINUX_MASK;
        } else {
          return IVersionCheckDataProvider.DEPTH_ALL_MASK;
        }
      }
    } catch ( Exception e ) {
      // ignore any issues
    }
    return IVersionCheckDataProvider.DEPTH_ALL_MASK;
  }

  /**
   * generates the depth flags
   */
  public int getDepth() {

    int depth = versionRequestFlags + computeOSMask();
    return depth;
  }
}
