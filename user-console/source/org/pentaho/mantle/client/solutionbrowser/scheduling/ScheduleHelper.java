package org.pentaho.mantle.client.solutionbrowser.scheduling;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ScheduleHelper {

  static {
    setupNativeHooks(new ScheduleHelper());
  }

  private static native void setupNativeHooks(ScheduleHelper scheduleHelper)
  /*-{
    $wnd.mantle_confirmBackgroundExecutionDialog = function(url) {
      @org.pentaho.mantle.client.solutionbrowser.scheduling.ScheduleHelper::confirmBackgroundExecutionDialog(Ljava/lang/String;)(url);      
    }
  }-*/;

  private static void showScheduleDialog(final String fileNameWithPath) {
    final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        // if we are still authenticated, perform the action, otherwise present login
        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }

          public void onSuccess(Boolean subscribable) {

            if (subscribable) {
              NewScheduleDialog dialog = new NewScheduleDialog(fileNameWithPath);
              dialog.center();
            } else {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), //$NON-NLS-1$
                  Messages.getString("noSchedulePermission"), false, false, true); //$NON-NLS-1$
              dialogBox.center();
            }
          }
        };
        MantleServiceCache.getService().hasAccess(fileNameWithPath, "", 3, callback);

      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            showScheduleDialog(fileNameWithPath);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public static void createSchedule(final RepositoryFile repositoryFile) {
    AbstractCommand scheduleCommand = new AbstractCommand() {

      private void schedule() {
        String extension = ""; //$NON-NLS-1$
        if (repositoryFile.getPath().lastIndexOf(".") > 0) { //$NON-NLS-1$
          extension = repositoryFile.getPath().substring(repositoryFile.getPath().lastIndexOf(".") + 1); //$NON-NLS-1$
          }

        if (SolutionBrowserPanel.getInstance().getExecutableFileExtensions().contains(extension)) {
          showScheduleDialog(repositoryFile.getPath());
              } else {
                  final MessageDialogBox dialogBox = new MessageDialogBox(
              Messages.getString("open"), Messages.getString("scheduleInvalidFileType", repositoryFile.getPath()), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$

                  dialogBox.setCallback(new IDialogCallback() {
                    public void cancelPressed() {
                    }

                    public void okPressed() {
                      dialogBox.hide();
                    }
                  });

                  dialogBox.center();
                  return;
                }
        
      }

      protected void performOperation() {
        schedule();
      }

      protected void performOperation(boolean feedback) {
        schedule();
      }

    };
    scheduleCommand.execute();
  }

  /**
   * The passed in URL has all the parameters set for background execution. We simply call GET on the URL and handle the response object. If the response object
   * contains a particular string then we display success message box.
   * 
   * @param url
   *          Complete url with all the parameters set for scheduling a job in the background.
   */
  private static void runInBackground(final String url) {

    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          /*
           * We are checking for this specific string because if the job was scheduled successfully by QuartzBackgroundExecutionHelper then the response is an
           * html that contains the specific string. We have coded this way because we did not want to touch the old way.
           */
          if ("true".equals(response.getHeader("background_execution"))) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), Messages.getString("backgroundJobScheduled"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }
        }
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
          Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  public static void confirmBackgroundExecutionDialog(final String url) {
    final String title = Messages.getString("confirm"); //$NON-NLS-1$
    final String message = Messages.getString("userParamBackgroundWarning"); //$NON-NLS-1$
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString(message)));

    final PromptDialogBox scheduleInBackground = new PromptDialogBox(title, Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$

    final IDialogCallback callback = new IDialogCallback() {
      public void cancelPressed() {
        scheduleInBackground.hide();
      }

      public void okPressed() {
        runInBackground(url);
      }
    };
    scheduleInBackground.setCallback(callback);
    scheduleInBackground.center();
  }

}