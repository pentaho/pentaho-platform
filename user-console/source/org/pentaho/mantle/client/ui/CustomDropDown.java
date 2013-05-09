package org.pentaho.mantle.client.ui;

import org.pentaho.gwt.widgets.client.utils.ElementUtils;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class CustomDropDown extends HorizontalPanel implements HasText {

  private static final String STYLE = "custom-dropdown";
  private static final PopupPanel popup = new PopupPanel(true, false);

  private MenuBar menuBar;
  private Command command;
  private boolean enabled = true;
  private boolean pressed = false;
  private Label label = new Label("", false);

  public CustomDropDown(String labelText, MenuBar menuBar) {
    this.menuBar = menuBar;

    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);

    setText(labelText);
    label.setStyleName("custom-dropdown-label");
    // label.addMouseListener(this);
    add(label);
    Label dropDownArrow = new Label();
    add(dropDownArrow);
    setCellWidth(dropDownArrow, "100%");
    dropDownArrow.getElement().getParentElement().addClassName("custom-dropdown-arrow");
    
    // prevent double-click from selecting text
    ElementUtils.preventTextSelection(getElement());
    ElementUtils.preventTextSelection(label.getElement());

    popup.setStyleName("custom-dropdown-popup");
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
      if (enabled && !pressed) {
        pressed = true;
        addStyleDependentName("pressed");
        removeStyleDependentName("hover");
        final PopupPanel popup = CustomDropDown.popup;
        popup.setWidget(menuBar);
        popup.setPopupPositionAndShow(new PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
            popup.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() - 1);
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

  public String getText() {
    return label.getText();
  }

  public void setText(String text) {
    label.setText(text);
  }

  public static void hidePopup() {
    popup.hide();
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
