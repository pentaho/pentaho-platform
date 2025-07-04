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
