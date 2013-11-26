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
