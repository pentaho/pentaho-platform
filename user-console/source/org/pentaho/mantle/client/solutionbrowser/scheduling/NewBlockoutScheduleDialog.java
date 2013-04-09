package org.pentaho.mantle.client.solutionbrowser.scheduling;

import com.google.gwt.json.client.JSONObject;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobTrigger;

public class NewBlockoutScheduleDialog extends NewScheduleDialog
{
  private boolean updateMode = false;

  public NewBlockoutScheduleDialog(final String filePath,
                                   final IDialogCallback callback,
                                   final boolean hasParams, final boolean isEmailConfValid)
  {
    super(ScheduleDialogType.BLOCKOUT, Messages.getString("newBlockoutSchedule"), filePath, callback, hasParams, isEmailConfValid);
  }

  public NewBlockoutScheduleDialog(final JsJob jsJob, final IDialogCallback callback, final boolean hasParams, final boolean isEmailConfValid, final boolean showScheduleName) {
    super(jsJob, callback, hasParams, isEmailConfValid, showScheduleName, ScheduleDialogType.BLOCKOUT);
  }

  @Override
  protected boolean onFinish() {
    JsJobTrigger trigger = getJsJobTrigger();
    JSONObject schedule = getSchedule();

    if(updateMode) {
      addBlockoutPeriod(schedule, trigger, "update/" + editJob.getJobId());
    } else {
      addBlockoutPeriod(schedule, trigger, "add");
    }

    return true;
  }

  public void setUpdateMode() {
    updateMode = true;
  }
}
