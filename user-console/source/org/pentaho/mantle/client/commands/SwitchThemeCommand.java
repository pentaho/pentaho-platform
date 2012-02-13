package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: nbaker Date: 5/13/11
 */
public class SwitchThemeCommand extends AbstractCommand {

  private String theme;

  public SwitchThemeCommand(String theme) {
    this.theme = theme;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    MantleServiceCache.getService().setTheme(theme, new AsyncCallback<Void>() {
      public void onFailure(Throwable throwable) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("settingThemeFailed"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }

      public void onSuccess(Void aVoid) {
        // forcing a setTimeout to fix a problem in IE BISERVER-6385
        Scheduler.get().scheduleDeferred(new Command() {
          public void execute() {
            Window.Location.reload();
          }
        });
      }
    });

  }

}
