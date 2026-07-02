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


package org.pentaho.mantle.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.mantle.client.messages.Messages;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleEntryPoint implements EntryPoint, IResourceBundleLoadCallback {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    ResourceBundle messages = new ResourceBundle();
    Messages.setResourceBundle( messages );
    messages.loadBundle(
      GWT.getModuleBaseURL() + "messages/",
      "mantleMessages",
      true,
      MantleEntryPoint.this );
    initializeNativeHooks();
  }

  private void initializeNativeHooks() {
    new MantleUtils();
  }

  public void bundleLoaded( String bundleName ) {
    Window.setTitle( Messages.getString( "productName" ) ); //$NON-NLS-1$

    MantleApplication mantle = MantleApplication.getInstance();
    mantle.loadApplication();

    RootPanel loadingPanel = RootPanel.get( "loading" ); //$NON-NLS-1$
    if ( loadingPanel != null ) {
      loadingPanel.removeFromParent();
      loadingPanel.setVisible( false );
      loadingPanel.setHeight( "0px" ); //$NON-NLS-1$
    }
  }
}
