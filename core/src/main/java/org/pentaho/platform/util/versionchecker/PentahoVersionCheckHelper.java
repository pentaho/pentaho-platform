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

package org.pentaho.platform.util.versionchecker;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.versionchecker.IVersionCheckDataProvider;
import org.pentaho.versionchecker.IVersionCheckErrorHandler;
import org.pentaho.versionchecker.IVersionCheckResultHandler;
import org.pentaho.versionchecker.VersionChecker;

import java.util.ArrayList;

/**
 * Avoid loading this class without reflection, so if someone deletes the versionchecker.jar, there will be no
 * class loading exceptions
 * 
 * @author Will Gorman
 * 
 */
public class PentahoVersionCheckHelper {

  protected boolean ignoreExistingUpdates = false;

  protected ArrayList resultList = new ArrayList();

  protected int versionRequestFlags = -1;

  public void setVersionRequestFlags( final int versionRequestFlags ) {
    this.versionRequestFlags = versionRequestFlags;
  }

  public void setIgnoreExistingUpdates( final boolean ignoreExistingUpdates ) {
    this.ignoreExistingUpdates = ignoreExistingUpdates;
  }

  public ArrayList getResults() {
    return resultList;
  }

  public void performUpdate() {
    IVersionCheckResultHandler resultHandler = new IVersionCheckResultHandler() {
      public void processResults( String results ) {
        // parse xml results vs spewing out xml?
        resultList.add( results );
      }
    };

    IVersionCheckErrorHandler errorHandler = new IVersionCheckErrorHandler() {
      public void handleException( Exception e ) {
        resultList.add( "<vercheck><error><![CDATA[" + e.getMessage() + "]]></error></vercheck>" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    };

    // PentahoVersionCheckDataProvider dataProvider = new PentahoVersionCheckDataProvider();
    IVersionCheckDataProvider dataProvider =
        PentahoSystem.get( IVersionCheckDataProvider.class, "IVersionCheckDataProvider", null ); //$NON-NLS-1$
    if ( dataProvider == null ) {
      dataProvider = new PentahoVersionCheckDataProvider();
    }

    if ( versionRequestFlags != -1 ) {
      dataProvider.setVersionRequestFlags( versionRequestFlags );
    }

    VersionChecker vc = new VersionChecker();

    vc.setDataProvider( dataProvider );
    vc.addResultHandler( resultHandler );
    vc.addErrorHandler( errorHandler );

    vc.performCheck( ignoreExistingUpdates );
  }
}
