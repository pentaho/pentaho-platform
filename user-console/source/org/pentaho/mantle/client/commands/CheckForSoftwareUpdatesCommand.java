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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

public class CheckForSoftwareUpdatesCommand extends AbstractCommand {

  public CheckForSoftwareUpdatesCommand() {
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final String url = GWT.getHostPageBaseURL() + "api/version/softwareUpdates"; //$NON-NLS-1$
    RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    requestBuilder.setHeader( "accept", "text/plain" );
    try {
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "softwareUpdates" ), Messages.getString( "noUpdatesAvailable" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }

        public void onResponseReceived( Request request, Response response ) {
          Document doc = XMLParser.parse( response.getText() );
          NodeList updates = doc.getElementsByTagName( "update" ); //$NON-NLS-1$
          if ( updates.getLength() > 0 ) {
            FlexTable updateTable = new FlexTable();
            updateTable.setStyleName( "backgroundContentTable" ); //$NON-NLS-1$
            updateTable.setWidget( 0, 0, new Label( Messages.getString( "version" ) ) ); //$NON-NLS-1$
            updateTable.setWidget( 0, 1, new Label( Messages.getString( "type" ) ) ); //$NON-NLS-1$
            updateTable.setWidget( 0, 2, new Label( Messages.getString( "os" ) ) ); //$NON-NLS-1$
            updateTable.setWidget( 0, 3, new Label( Messages.getString( "link" ) ) ); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName( 0, 0, "backgroundContentHeaderTableCell" ); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName( 0, 1, "backgroundContentHeaderTableCell" ); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName( 0, 2, "backgroundContentHeaderTableCell" ); //$NON-NLS-1$
            updateTable.getCellFormatter().setStyleName( 0, 3, "backgroundContentHeaderTableCellRight" ); //$NON-NLS-1$

            for ( int i = 0; i < updates.getLength(); i++ ) {
              Element updateElement = (Element) updates.item( i );
              String version = updateElement.getAttribute( "version" ); //$NON-NLS-1$
              String type = updateElement.getAttribute( "type" ); //$NON-NLS-1$
              String os = updateElement.getAttribute( "os" ); //$NON-NLS-1$
              // String title = updateElement.getAttribute("title");
              String downloadURL = updateElement.getElementsByTagName( "downloadurl" ).item( 0 ).toString(); //$NON-NLS-1$
              downloadURL = downloadURL.substring( downloadURL.indexOf( "http" ), downloadURL.indexOf( "]" ) ); //$NON-NLS-1$ //$NON-NLS-2$
              updateTable.setWidget( i + 1, 0, new Label( version ) );
              updateTable.setWidget( i + 1, 1, new Label( type ) );
              updateTable.setWidget( i + 1, 2, new Label( os ) );
              updateTable
                  .setWidget(
                    i + 1,
                    3,
                    new HTML(
                      "<A HREF=\"" + downloadURL + "\" target=\"_blank\" title=\"" + downloadURL + "\">" + Messages
                        .getString( "download" ) + "</A>" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
              updateTable.getCellFormatter().setStyleName( i + 1, 0, "backgroundContentTableCell" ); //$NON-NLS-1$
              updateTable.getCellFormatter().setStyleName( i + 1, 1, "backgroundContentTableCell" ); //$NON-NLS-1$
              updateTable.getCellFormatter().setStyleName( i + 1, 2, "backgroundContentTableCell" ); //$NON-NLS-1$
              updateTable.getCellFormatter().setStyleName( i + 1, 3, "backgroundContentTableCellRight" ); //$NON-NLS-1$
              if ( i == updates.getLength() - 1 ) {
                // last
                updateTable.getCellFormatter().setStyleName( i + 1, 0, "backgroundContentTableCellBottom" ); //$NON-NLS-1$
                updateTable.getCellFormatter().setStyleName( i + 1, 1, "backgroundContentTableCellBottom" ); //$NON-NLS-1$
                updateTable.getCellFormatter().setStyleName( i + 1, 2, "backgroundContentTableCellBottom" ); //$NON-NLS-1$
                updateTable.getCellFormatter().setStyleName( i + 1, 3, "backgroundContentTableCellBottomRight" ); //$NON-NLS-1$
              }
            }
            PromptDialogBox versionPromptDialog =
                new PromptDialogBox(
                    Messages.getString( "softwareUpdateAvailable" ), Messages.getString( "ok" ), null, false, true, updateTable ); //$NON-NLS-1$ //$NON-NLS-2$
            versionPromptDialog.center();
          } else {
            MessageDialogBox dialogBox =
                new MessageDialogBox(
                    Messages.getString( "softwareUpdates" ), Messages.getString( "noUpdatesAvailable" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }
        }
      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
      // showError(e);
    }
  }

}
