/*!
 *
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
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.GenericEvent;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.ui.PerspectiveManager;

public class MantleUtils {

  static {
    setupNativeHooks( new MantleUtils() );
  }

  public void setSchedulesPerspective() {
    PerspectiveManager.getInstance().setPerspective( PerspectiveManager.SCHEDULES_PERSPECTIVE );
  }

  public void setBrowserPerspective() {
    PerspectiveManager.getInstance().setPerspective( PerspectiveManager.BROWSER_PERSPECTIVE );
  }

  public boolean containsExtension( String extension ) {
     return SolutionBrowserPanel.getInstance().getExecutableFileExtensions().contains( extension );
  }

  public void sendRequest( RequestBuilder executableTypesRequestBuilder ) {
    try {
      executableTypesRequestBuilder.sendRequest( null, EmptyRequestCallback.getInstance() );
    } catch ( RequestException e ) {
      // IGNORE
    }
  }

  public void fireRefreshFolderEvent( String outputLocation ) {
    GenericEvent event = new GenericEvent();
    event.setEventSubType( "RefreshFolderEvent" );
    event.setStringParam( outputLocation );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

  public void handleRepositoryFileSelection( String solutionPath, Boolean isAdhoc) {
    final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    sbp.getFile( solutionPath, new SolutionFileHandler() {
      @Override
      public void handle( RepositoryFile repositoryFile ) {
        if( isAdhoc ) {
          openAdhocDialog( true );
        } else {
          FileItem fileItem = new FileItem( repositoryFile, null, null, false, null );
          checkSchedulePermissionAndDialog( fileItem.getRepositoryFile().getId(), fileItem.getPath() );
        }
      }
    } );
  }

  private native void openAdhocDialog( boolean feedback ) /*-{
    $wnd.pho.showDialog(feedback);
  }-*/;

  private native void checkSchedulePermissionAndDialog( String repositoryFileId, String repositoryFilePath ) /*-{
    $wnd.pho.checkSchedulePermissionAndDialog(repositoryFileId, repositoryFilePath);
  }-*/;

  public boolean checkSelectedPerspective() {
    return !PerspectiveManager.getInstance().getActivePerspective().getId().equals(
      PerspectiveManager.SCHEDULES_PERSPECTIVE );
  }

  public static native String getSchedulerPluginContextURL()/*-{
    if (typeof $wnd.pho.getSchedulerPluginContextURL !== "undefined" ) {
      return $wnd.pho.getSchedulerPluginContextURL();
    } else {
      // fallback to the default location when the getter is unavailable
      // e.g. race condition loading GWT JavaScript code, PUC JS executes before the Scheduler Plugin JS is loaded
      return $wnd.location.protocol + "//" + $wnd.location.host + $wnd.CONTEXT_PATH + "plugin/scheduler-plugin/";
    }
  }-*/;

  private static native void setupNativeHooks( MantleUtils utils )
  /*-{
    $wnd.mantle.setSchedulesPerspective = function() {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      utils.@org.pentaho.mantle.client.MantleUtils::setSchedulesPerspective()();
    }

    $wnd.mantle.setBrowserPerspective = function() {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      utils.@org.pentaho.mantle.client.MantleUtils::setBrowserPerspective()();
    }

    $wnd.mantle.containsExtension = function(extension) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return utils.@org.pentaho.mantle.client.MantleUtils::containsExtension(Ljava/lang/String;)(extension);
    }

    $wnd.mantle.sendRequest = function(executableTypesRequestBuilder) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      utils.@org.pentaho.mantle.client.MantleUtils::sendRequest(Lcom/google/gwt/http/client/RequestBuilder;)(executableTypesRequestBuilder);
    }

    $wnd.mantle.fireRefreshFolderEvent = function(outputLocation) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      utils.@org.pentaho.mantle.client.MantleUtils::fireRefreshFolderEvent(Ljava/lang/String;)(outputLocation);
    }

    $wnd.mantle.handleRepositoryFileSelection = function(solutionPath, isAdhoc) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      utils.@org.pentaho.mantle.client.MantleUtils::handleRepositoryFileSelection(Ljava/lang/String;Ljava/lang/Boolean;)(solutionPath, isAdhoc);
    }

    $wnd.mantle.checkSelectedPerspective = function() {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return utils.@org.pentaho.mantle.client.MantleUtils::checkSelectedPerspective()();
    }
   }-*/;
}
