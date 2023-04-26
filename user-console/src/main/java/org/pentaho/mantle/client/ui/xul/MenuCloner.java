package org.pentaho.mantle.client.ui.xul;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.mantle.client.ui.BurgerMenuBar;

public class MenuCloner {

  private static MenuCloner menuCloner = new MenuCloner();

  public static MenuCloner getInstance() {
    return menuCloner;
  }

  public static MenuBar cloneMenuBar( MenuBar menuBar, boolean vertical ) {
    MenuBar menuBarClone = new MenuBar( vertical );
    menuBarClone.setAnimationEnabled( menuBar.isAnimationEnabled() );
    menuBarClone.setAutoOpen( menuBar.getAutoOpen() );
    menuBarClone.setFocusOnHoverEnabled( menuBar.isFocusOnHoverEnabled() );
    setUIObjectElement( menuBarClone, (Element) menuBar.getElement().cloneNode( true ) );

//    GWT.log( menuBar.toString() );
//    GWT.log("");
//    GWT.log( menuBarClone.toString());

    BurgerMenuBar.getMenuBarAllItems( menuBar ).forEach( ( UIObject uio ) -> {
      if ( uio instanceof MenuItemSeparator ) {
        menuBarClone.addSeparator( cloneMenuItemSeparator( (MenuItemSeparator) uio ) );
      } else {
        MenuItem menuItemClone;
        if ( uio instanceof PentahoMenuItem ) {
          menuItemClone = clonePentahoMenuItem( (PentahoMenuItem) uio );
        } else if ( uio instanceof CheckBoxMenuItem ) {
          menuItemClone = cloneCheckBoxMenuItem( (CheckBoxMenuItem) uio );
        } else if ( uio instanceof MenuItem ) {
          menuItemClone = cloneMenuItem( (MenuItem) uio, vertical );
        } else {
          throw new RuntimeException("Logic not implemented to copy menu related object: " + uio.getClass());
        }
        setUIObjectElement( menuItemClone, uio.getElement() );
        menuBarClone.addItem(menuItemClone);
      }


      //      String id = uio.getElement().getId();
      //      if ( id.isEmpty() ) {
      //        id = "-";
      //      }
      //      GWT.log( indentation + id + " | " + uio.getClass().getSimpleName() );
      //      GWT.log( uio.getClass().getName() );
      //      if ( uio instanceof MenuItem ) {
      //        MenuBar submenu = ( (MenuItem) uio ).getSubMenu();
      //        if ( submenu != null ) {
      //          menuPrinting( submenu, indentation + "\t" );
      //        }
      //      }
    } );

    return menuBarClone;
  }

  private static native void setUIObjectElement( UIObject uiObject, Element element) /*-{
    uiObject.@com.google.gwt.user.client.ui.UIObject::replaceElement(*)(element);
  }-*/;

  public static MenuItem cloneMenuItem( MenuItem menuItem, boolean vertical ) {
    MenuItem menuItemClone = new MenuItem( menuItem.getText(), menuItem.getScheduledCommand() );
    return cloneMenuItem( menuItem, menuItemClone, vertical );
  }

  public static MenuItem cloneMenuItem( MenuItem menuItem, MenuItem menuItemClone, boolean vertical ) {
    menuItemClone.setEnabled( menuItem.isEnabled() );
    menuItemClone.setVisible( menuItem.isVisible() );
//    menuItemClone.set

    MenuBar subMenuBar = menuItem.getSubMenu();
    if ( subMenuBar != null ) {
      menuItemClone.setSubMenu( cloneMenuBar( subMenuBar, vertical ) );
    }
    return menuItemClone;
  }

  public static MenuItemSeparator cloneMenuItemSeparator( MenuItemSeparator menuItemSeparator ) {
    return null;
  }

  public static PentahoMenuItem clonePentahoMenuItem( PentahoMenuItem pentahoMenuItem ) {
    return null;
  }

  public static CheckBoxMenuItem cloneCheckBoxMenuItem( CheckBoxMenuItem checkBoxMenuItem ) {
    return null;
  }

  // ///////////////////////////////////////////////////////////////
  private static void cloneHandlers(Widget w){
    HandlerManager widgetHandlerManger = getHandlerManager( w );
//    widgetHandlerManger.getHandlerCount(  )
  }

  private static native HandlerManager getHandlerManager( Widget w ) /*-{
    return w.@com.google.gwt.user.client.ui.Widget::getHandlerManager()();
  }-*/;

//  private static native HandlerManager getHandlers( HandlerManager h ) /*-{
//    h.@com.google.gwt.event.shared.HandlerManager::
//  }-*/;
}
