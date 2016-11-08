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

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog;

import java.util.ArrayList;
import java.util.List;

public class ShareFileCommand implements Command {

  private FilePropertiesDialog.Tabs defaultTab = FilePropertiesDialog.Tabs.PERMISSION;

  private String moduleBaseURL = GWT.getModuleBaseURL();
  private String moduleName = GWT.getModuleName();
  private String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
  private static final int MANAGE_ACLS = 3;

  private List<RepositoryFile> selectedList;

  public ShareFileCommand() {
  }

  private String solutionPath = null;

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  public void execute() {
    final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    if ( this.getSolutionPath() != null ) {
      selectedList = new ArrayList<RepositoryFile>();
      sbp.getFile( this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          selectedList.add( repositoryFile );
          performOperation();
        }
      } );
    } else {
      selectedList = sbp.getFilesListPanel().getRepositoryFiles();
      performOperation();
    }
  }

  public void performOperation() {

    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );

    if ( selectedList != null && selectedList.size() == 1 ) {
      final RepositoryFile item = selectedList.get( 0 );

      // Checking if the user has access to manage permissions
      String url =
          contextURL
              + "api/repo/files/" + SolutionBrowserPanel.pathToId( item.getPath() ) + "/canAccess?permissions=" + MANAGE_ACLS; //$NON-NLS-1$ //$NON-NLS-2$
      RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
      try {
        builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        builder.sendRequest( null, new RequestCallback() {

          public void onError( Request request, Throwable exception ) {
            FilePropertiesDialog dialog =
                new FilePropertiesDialog( item, new PentahoTabPanel(), null, defaultTab, false );
            dialog.showTab( defaultTab );
            dialog.center();

            event.setMessage( exception.getMessage() );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }

          public void onResponseReceived( Request request, Response response ) {
            FilePropertiesDialog dialog =
                new FilePropertiesDialog( item, new PentahoTabPanel(), null, defaultTab, Boolean.parseBoolean( response
                    .getText() ) );
            dialog.showTab( FilePropertiesDialog.Tabs.PERMISSION );
            dialog.center();

            event.setMessage( "Success" );
            EventBusUtil.EVENT_BUS.fireEvent( event );
          }
        } );
      } catch ( RequestException e ) {
        FilePropertiesDialog dialog = new FilePropertiesDialog( item, new PentahoTabPanel(), null, defaultTab, false );
        dialog.showTab( FilePropertiesDialog.Tabs.PERMISSION );
        dialog.center();

        event.setMessage( e.getMessage() );
        EventBusUtil.EVENT_BUS.fireEvent( event );
      }

    }
  }

}
