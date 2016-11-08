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

package org.pentaho.platform.plugin.action.versionchecker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.versionchecker.PentahoVersionCheckReflectHelper;

import java.util.List;

/**
 * Version Check Component This component makes a call to pentaho's server to see if a new version is a vailable.
 * 
 * Uses reflection helper so that versioncheck.jar can be deleted without problems
 * 
 * input param "ignoreExistingUpdates" - if true, ignore existing updates discovered
 * 
 * @author Will Gorman
 * 
 */
public class PentahoVersionCheckComponent extends ComponentBase {

  private static final long serialVersionUID = 8178713714323100555L;

  private static final String DOCUMENT = "document"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog( PentahoVersionCheckComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  public void done() {

  }

  @Override
  protected boolean executeAction() {

    String output = null;

    boolean ignoreExistingUpdates = getInputBooleanValue( "ignoreExistingUpdates", true ); //$NON-NLS-1$

    // pull out release flags from the releaseFlags string
    int versionRequestFlags = -1;
    try {
      Object releaseFlagsObj = getInputValue( "releaseFlags" ); //$NON-NLS-1$
      String releaseFlags = ""; //$NON-NLS-1$
      if ( releaseFlagsObj instanceof String[] ) {
        String[] arr = (String[]) releaseFlagsObj;
        if ( arr.length > 0 ) {
          releaseFlags += arr[0];
          for ( int i = 1; i < arr.length; i++ ) {
            releaseFlags += "," + arr[i]; //$NON-NLS-1$
          }
        }
      } else {
        releaseFlags = releaseFlagsObj.toString();
      }

      if ( releaseFlags != null ) {
        releaseFlags = releaseFlags.toLowerCase();
        boolean requestMajorReleases = releaseFlags.indexOf( "major" ) >= 0; //$NON-NLS-1$
        boolean requestMinorReleases = releaseFlags.indexOf( "minor" ) >= 0; //$NON-NLS-1$
        boolean requestRCReleases = releaseFlags.indexOf( "rc" ) >= 0; //$NON-NLS-1$
        boolean requestGAReleases = releaseFlags.indexOf( "ga" ) >= 0; //$NON-NLS-1$
        boolean requestMilestoneReleases = releaseFlags.indexOf( "milestone" ) >= 0; //$NON-NLS-1$

        versionRequestFlags =
            ( requestMajorReleases ? 4 : 0 ) + ( requestMinorReleases ? 8 : 0 ) + ( requestRCReleases ? 16 : 0 )
                + ( requestGAReleases ? 32 : 0 ) + ( requestMilestoneReleases ? 64 : 0 );
      }
    } catch ( Exception e ) {
      // ignore errors
    }

    if ( PentahoVersionCheckReflectHelper.isVersionCheckerAvailable() ) {
      List results = PentahoVersionCheckReflectHelper.performVersionCheck( ignoreExistingUpdates, versionRequestFlags );
      output = PentahoVersionCheckReflectHelper.logVersionCheck( results, getLogger() );
    } else {
      output =
          "<vercheck><error><[!CDATA[" + Messages.getInstance().getString( "VersionCheck.VERSION_CHECK_DISABLED" ) + "]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    if ( isDefinedOutput( PentahoVersionCheckComponent.DOCUMENT ) ) {
      setOutputValue( PentahoVersionCheckComponent.DOCUMENT, output );
    }
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

}
