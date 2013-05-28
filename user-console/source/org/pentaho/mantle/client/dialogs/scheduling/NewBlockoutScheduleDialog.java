package org.pentaho.mantle.client.dialogs.scheduling;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobTrigger;

import com.google.gwt.json.client.JSONObject;

public class NewBlockoutScheduleDialog extends NewScheduleDialog {
  private boolean updateMode = false;

  public NewBlockoutScheduleDialog(final String filePath, final IDialogCallback callback, final boolean hasParams,
      final boolean isEmailConfValid) {
    super(ScheduleDialogType.BLOCKOUT, Messages.getString("newBlockoutSchedule"), filePath, callback, hasParams, //$NON-NLS-1$
        isEmailConfValid);
  }

  public NewBlockoutScheduleDialog(final JsJob jsJob, final IDialogCallback callback, final boolean hasParams,
      final boolean isEmailConfValid, final boolean showScheduleName) {
    super(jsJob, callback, hasParams, isEmailConfValid, showScheduleName, ScheduleDialogType.BLOCKOUT);
  }

  @Override
  protected boolean onFinish() {
    JsJobTrigger trigger = getJsJobTrigger();
    JSONObject schedule = getSchedule();

    // TODO -- Add block out verification that it is not completely blocking an existing schedule
    if (updateMode) {
      addBlockoutPeriod(schedule, trigger, "update?jobid=" + editJob.getJobId()); //$NON-NLS-1$
    } else {
      addBlockoutPeriod(schedule, trigger, "add"); //$NON-NLS-1$
    }
    getCallback().okPressed();

    return true;
  }

  public void setUpdateMode() {
    updateMode = true;
  }
}
