package org.pentaho.mantle.client.solutionbrowser.tree;

import org.pentaho.mantle.client.solutionbrowser.MantlePopupPanel;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class SolutionTreeWrapper extends SimplePanel {
  SolutionTree tree;
  
  public SolutionTreeWrapper(SolutionTree tree) {
    super();
    this.tree = tree;
    add(tree);
    setStyleName("files-list-panel"); //$NON-NLS-1$
    sinkEvents(Event.MOUSEEVENTS);
  }

  public void onBrowserEvent(Event event) {
    if (((DOM.eventGetButton(event) & NativeEvent.BUTTON_RIGHT) == NativeEvent.BUTTON_RIGHT && (DOM.eventGetType(event) & Event.ONMOUSEUP) == Event.ONMOUSEUP)) {
      // bring up a popup with 'create new folder' option
      final int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
      final int top = Window.getScrollTop() + DOM.eventGetClientY(event);
      handleRightClick(left, top);
    } else {
      super.onBrowserEvent(event);
    }
  }

  private void handleRightClick(int left, int top) {
    final PopupPanel popupMenu = MantlePopupPanel.getInstance(true);
    popupMenu.setPopupPosition(left, top);
    MenuBar menuBar = new MenuBar(true);
    menuBar.setAutoOpen(true);
    popupMenu.setWidget(menuBar);
    popupMenu.show();
  }

}
