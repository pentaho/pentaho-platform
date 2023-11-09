package org.pentaho.mantle.client.ui.tabs;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.MenuItem;
import org.pentaho.gwt.widgets.client.utils.MenuBarUtils;

public class TabContextMenuBar extends com.google.gwt.user.client.ui.MenuBar {

  String heightPx;
  String maxHeightPx;

  int height;
  int top;
  int menuBarTop;

  public TabContextMenuBar( boolean vertical ) {
    super( vertical );
  }

  @Override
  public void onBrowserEvent( Event event ) {
    super.onBrowserEvent( event );

    MenuItem item = MenuBarUtils.findItem( this, DOM.eventGetTarget( event ) );
    int type = event.getTypeInt();
    switch ( type ) {
      case Event.ONMOUSEOVER: {
        if ( item != null ) {
          DecoratedPopupPanel popup = MenuBarUtils.getPopup( this );

          // Set the menuBar height to be the sum of it's menuItem's heights
          height = MenuBarUtils.calculatePopupHeight( item.getSubMenu() );

          top = item.getAbsoluteTop();
          menuBarTop = this.getAbsoluteTop();

          // if this menu is at the bottom of the screen, try to give it more space
          if( Window.getClientHeight() - top < height ){
            top = Window.getClientHeight() - height;
          }

          // if popup top is above the menuBar, bring it just beneath the menuBar
          if ( top < menuBarTop ) {
            top = menuBarTop + 1;
          }

          heightPx = height + "px";
          maxHeightPx = "calc( 100vh - " + top + "px )";
          popup.getWidget().getElement().getStyle().setProperty( "height", heightPx );
          popup.getWidget().getElement().getStyle().setProperty( "maxHeight", maxHeightPx );

          //Set popup's left to be next to the parent menuBar
          popup.setPopupPosition( this.getOffsetWidth(), top );
        }
        break;
      }
    }
  }
}
