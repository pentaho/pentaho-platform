package org.pentaho.mantle.client.ui;

import org.pentaho.gwt.widgets.client.utils.ElementUtils;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;

public class CustomDropDown extends SimplePanel {

  private static final String STYLE = "custom-dropdown";
  private static final PopupPanel popup = new PopupPanel(true, false);

  private MenuBar menuBar;
  private Command command;
  private boolean enabled = true;
  private boolean pressed = true;
  
  public CustomDropDown(String labelText, MenuBar menuBar) {
    this.menuBar = menuBar;

    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);

    Label label = new Label(labelText, true);
    label.setStyleName("custom-dropdown-label");
    // label.addMouseListener(this);
    add(label);
    // prevent double-click from selecting text
    ElementUtils.preventTextSelection(getElement());
    ElementUtils.preventTextSelection(label.getElement());

    popup.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        pressed = false;
        if (enabled) {
          removeStyleDependentName("pressed");
          removeStyleDependentName("hover");
        }
      }
    });
    
    setStyleName(STYLE);
  }

  public CustomDropDown(String labelText, MenuBar menuBar, Command command) {
    this(labelText, menuBar);
    this.command = command;
  }

  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if ((event.getTypeInt() & Event.ONCLICK) == Event.ONCLICK) {
      if (enabled) {
        pressed = true;
        addStyleDependentName("pressed");
        removeStyleDependentName("hover");
        final PopupPanel popup = CustomDropDown.popup;
        popup.setWidget(menuBar);
        popup.setPopupPositionAndShow(new PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
            popup.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
          }
        });
      }
    } else if ((event.getTypeInt() & Event.ONMOUSEOVER) == Event.ONMOUSEOVER) {
      if (enabled) {
        addStyleDependentName("hover");
      }
    } else if ((event.getTypeInt() & Event.ONMOUSEOUT) == Event.ONMOUSEOUT) {
      if (enabled && !pressed) {
        removeStyleDependentName("pressed");
        removeStyleDependentName("hover");
      }
    } else if ((event.getTypeInt() & Event.ONMOUSEUP) == Event.ONMOUSEUP) {
      if (enabled) {
        removeStyleDependentName("pressed");
        if (command != null) {
          try {
            command.execute();
          } catch (Exception e) {
            // don't fail because some idiot you are calling fails
          }
        }
      }
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      removeStyleDependentName("disabled");
    } else {
      addStyleDependentName("disabled");
    }
  }

}
