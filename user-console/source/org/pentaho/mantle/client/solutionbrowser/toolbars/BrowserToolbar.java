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

package org.pentaho.mantle.client.solutionbrowser.toolbars;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.mantle.client.MantleMenuBar;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.images.ImageUtil;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author wseyler
 * 
 */
public class BrowserToolbar extends Toolbar {

  ToolbarButton refreshBtn;

  MenuBar miscMenus = new MantleMenuBar( true );

  public BrowserToolbar() {
    super();

    // Formatting stuff
    setHorizontalAlignment( ALIGN_RIGHT );
    setStyleName( "pentaho-titled-toolbar" );
    setHeight( "29px" ); //$NON-NLS-1$
    setWidth( "100%" ); //$NON-NLS-1$

    createMenus();
  }

  /**
   * 
   */
  private void createMenus() {
    addSpacer( 5 );
    Label label = new Label( Messages.getString( "browse" ) );
    label.setStyleName( "pentaho-titled-toolbar-label" );
    add( label ); //$NON-NLS-1$
    add( GLUE );

    Image refreshImage = ImageUtil.getThemeableImage( "icon-small", "icon-refresh" );
    Image refreshDisabledImage = ImageUtil.getThemeableImage( "icon-small", "icon-refresh", "disabled" );

    refreshBtn = new ToolbarButton( refreshImage, refreshDisabledImage );
    refreshBtn.setCommand( new RefreshRepositoryCommand() );
    refreshBtn.setToolTip( Messages.getString( "refresh" ) ); //$NON-NLS-1$
    add( refreshBtn );
  }

}
