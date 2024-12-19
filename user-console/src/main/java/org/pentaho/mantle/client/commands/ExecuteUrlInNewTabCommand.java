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

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.ui.tabs.MantleTabPanel;

public class ExecuteUrlInNewTabCommand extends AbstractCommand {
  private String xml;
  private String url;
  private String tabName;
  private String tabToolTip;

  public ExecuteUrlInNewTabCommand() {
    setupNativeHooks( this );
  }

  public ExecuteUrlInNewTabCommand( String url, String xml, String tabName, String tabToolTip ) {
    this();
    this.xml = xml;
    this.url = url;
    this.tabName = tabName;
    this.tabToolTip = tabToolTip;
  }

  public static void setupNativeHooks() {
    new ExecuteUrlInNewTabCommand();
  }

  private static native void setupNativeHooks( ExecuteUrlInNewTabCommand cmd )
  /*-{
    $wnd.mantle_execute_url_in_new_tab = function(url, xml, tabName, tabToolTip) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      cmd.@org.pentaho.mantle.client.commands.ExecuteUrlInNewTabCommand::executeInNewTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(url, xml, tabName, tabToolTip);
    }
  }-*/;

  protected void performOperation() {
    MantleTabPanel contentTabPanel = SolutionBrowserPanel.getInstance().getContentTabPanel();
    contentTabPanel.showNewURLTab( this.tabName, this.tabToolTip, "about:blank", false ); //$NON-NLS-1$
    NamedFrame namedFrame = ( (IFrameTabPanel) contentTabPanel.getSelectedTab().getContent() ).getFrame();
    final FormPanel form = new FormPanel( namedFrame );
    RootPanel.get().add( form );
    form.setMethod( FormPanel.METHOD_POST );
    form.setAction( url );
    form.add( new Hidden( "reportXml", URL.encode( xml ) ) ); //$NON-NLS-1$
    form.submit();
    ( (IFrameTabPanel) contentTabPanel.getSelectedTab().getContent() ).setForm( form );
  }

  protected void performOperation( final boolean feedback ) {
    performOperation();
  }

  /**
   * This method is called via JSNI
   */
  private void executeInNewTab( String url, String xml, String tabName, String tabToolTip ) {
    ExecuteUrlInNewTabCommand command = new ExecuteUrlInNewTabCommand( url, xml, tabName, tabToolTip );
    command.execute();
  }

}
