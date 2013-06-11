package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.SelectFolderDialog;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author Rowell Belen
 */
public abstract class ScheduleOutputLocationDialog extends PromptDialogBox
{
  private String filePath;

  private TextBox scheduleNameTextBox = new TextBox();
  private static TextBox scheduleLocationTextBox = new TextBox();
  private static HandlerRegistration changeHandlerReg = null;
  private static HandlerRegistration keyHandlerReg = null;

  static {
    scheduleLocationTextBox.setText(getDefaultSaveLocation());
  }

  private static native String getDefaultSaveLocation()
  /*-{
    return window.top.HOME_FOLDER;
  }-*/;

  public ScheduleOutputLocationDialog(final String filePath){
    super(Messages.getString("newSchedule"), Messages.getString("nextStep"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    this.filePath = filePath;
    createUI();
    setupCallbacks();
  }

  private void createUI() {
    VerticalPanel content = new VerticalPanel();

    HorizontalPanel scheduleNameLabelPanel = new HorizontalPanel();
    Label scheduleNameLabel = new Label(Messages.getString("scheduleName"));
    scheduleNameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

    Label scheduleNameInfoLabel = new Label(Messages.getString("scheduleNameInfo"));
    scheduleNameInfoLabel.setStyleName("msg-Label");

    scheduleNameLabelPanel.add(scheduleNameLabel);
    scheduleNameLabelPanel.add(scheduleNameInfoLabel);

    String defaultName = filePath.substring(filePath.lastIndexOf(":") + 1, filePath.lastIndexOf("."));
    scheduleNameTextBox.getElement().setId("schedule-name-input");
    scheduleNameTextBox.setText(defaultName);

    content.add(scheduleNameLabelPanel);
    content.add(scheduleNameTextBox);

    Label scheduleLocationLabel = new Label(Messages.getString("generatedContentLocation"));
    scheduleLocationLabel.setStyleName(ScheduleEditor.SCHEDULE_LABEL);
    content.add(scheduleLocationLabel);

    Button browseButton = new Button(Messages.getString("select"));
    browseButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        final SelectFolderDialog selectFolder = new SelectFolderDialog();
        selectFolder.setCallback(new IDialogCallback() {
          public void okPressed() {
            scheduleLocationTextBox.setText(selectFolder.getSelectedPath());
          }

          public void cancelPressed() {
          }
        });
        selectFolder.center();
      }
    });
    browseButton.setStyleName("pentaho-button");

    ChangeHandler ch = new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        updateButtonState();
      }
    };
    KeyUpHandler kh = new KeyUpHandler() {
      public void onKeyUp(KeyUpEvent event) {
        updateButtonState();
      }
    };

    if (keyHandlerReg != null) {
      keyHandlerReg.removeHandler();
    }
    if (changeHandlerReg != null) {
      changeHandlerReg.removeHandler();
    }
    keyHandlerReg = scheduleNameTextBox.addKeyUpHandler(kh);
    changeHandlerReg = scheduleLocationTextBox.addChangeHandler(ch);
    scheduleNameTextBox.addChangeHandler(ch);

    scheduleLocationTextBox.getElement().setId("generated-content-location");
    HorizontalPanel locationPanel = new HorizontalPanel();
    scheduleLocationTextBox.setEnabled(false);
    locationPanel.add(scheduleLocationTextBox);
    locationPanel.setCellVerticalAlignment(scheduleLocationTextBox, HasVerticalAlignment.ALIGN_MIDDLE);
    locationPanel.add(browseButton);

    content.add(locationPanel);

    setContent(content);
    content.getElement().getStyle().clearHeight();
    content.getParent().setHeight("100%");

    okButton.getParent().getParent().setStyleName("schedule-dialog-button-panel");

    updateButtonState();
    setSize("650px", "450px");
  }

  private void setupCallbacks(){
    setValidatorCallback(new IDialogValidatorCallback() {
      @Override
      public boolean validate() {
        String name = scheduleNameTextBox.getText();
        String alphaNumeric = "^[a-zA-Z0-9_\\.\\- ]+$"; //$NON-NLS-1$
        // make sure it matches regex
        boolean isValid = name.matches(alphaNumeric);
        if(!isValid){
          MessageDialogBox errorDialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("enterAlphaNumeric", name), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
          errorDialog.center();
        }
        return isValid;
      }
    });

    setCallback(new IDialogCallback() {
      @Override
      public void okPressed() {
        onSelect(scheduleNameTextBox.getText(), scheduleLocationTextBox.getText());
      }

      @Override
      public void cancelPressed() {
      }
    });
  }

  private void updateButtonState() {
    boolean hasLocation = !StringUtils.isEmpty(scheduleLocationTextBox.getText());
    boolean hasName = !StringUtils.isEmpty(scheduleNameTextBox.getText());
    okButton.setEnabled(hasLocation && hasName);
  }

  protected abstract void onSelect(String name, String outputLocationPath);

}
