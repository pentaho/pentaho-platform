package org.pentaho.mantle.client.solutionbrowser.scheduling;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;

public class NewBlockoutScheduleDialog extends NewScheduleDialog
{
  public NewBlockoutScheduleDialog(final String filePath,
                                   final IDialogCallback callback,
                                   final boolean hasParams, final boolean isEmailConfValid)
  {
    super(ScheduleDialogType.BLOCKOUT, Messages.getString("newBlockoutSchedule"), filePath, callback, hasParams, isEmailConfValid);
  }

  public NewBlockoutScheduleDialog(final JsJob jsJob, final IDialogCallback callback, final boolean hasParams, final boolean isEmailConfValid, final boolean showScheduleName) {
    super(jsJob, callback, hasParams, isEmailConfValid, showScheduleName, ScheduleDialogType.BLOCKOUT);
  }
}
