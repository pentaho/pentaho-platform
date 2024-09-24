package org.pentaho.mantle.client.ui.tabs;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.MenuBar;

public class MantleTabMenuItem extends com.google.gwt.user.client.ui.MenuItem {
  private MantleTab mantleTab;

  public MantleTabMenuItem( MantleTab tab ) {
    super( tab.getLabelText(), (Scheduler.ScheduledCommand) null );
    mantleTab = tab;
    refreshContextMenu();
  }

  public void refreshContextMenu() {
    MenuBar contextMenuBar = mantleTab.getContextMenuBar( true );
    contextMenuBar.addStyleName( "tabsMenuContextSubMenu" );
    setSubMenu( contextMenuBar );
  }
}
