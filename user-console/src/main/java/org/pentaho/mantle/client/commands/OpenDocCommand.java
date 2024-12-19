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


package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;

import java.util.HashMap;

/**
 * Executes the Open Document command.
 * 
 * @author nbaker / dkincade
 */
public class OpenDocCommand extends AbstractCommand {
  private String documentationURL;

  public OpenDocCommand() {

  }

  /**
   * Executes the command to open the help documentation. Based on the subscription setting, the document being
   * opened will be the CE version of the document or the EE version of the document.
   */
  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    MantleSettingsManager.getInstance().getMantleSettings( new AsyncCallback<HashMap<String, String>>() {

      public void onSuccess( HashMap<String, String> result ) {
        documentationURL = result.get( "documentation-url" );

        boolean isExternalDocumentation =
          documentationURL.startsWith( "http:" ) || documentationURL.startsWith( "https:" );

        if ( !documentationURL.startsWith( "/" ) && !isExternalDocumentation ) {
          // we're working with a relative URL, this is relative to the web-app not the GWT module
          documentationURL = GWT.getHostPageBaseURL() + documentationURL;
        }
        Window.open( documentationURL, "_blank", "" ); //$NON-NLS-1$ //$NON-NLS-2$
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );
  }

}
