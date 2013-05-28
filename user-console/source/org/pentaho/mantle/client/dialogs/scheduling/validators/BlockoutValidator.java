package org.pentaho.mantle.client.dialogs.scheduling.validators;

import static org.pentaho.gwt.widgets.client.utils.TimeUtil.TimeOfDay.PM;

import org.pentaho.gwt.widgets.client.controls.TimePicker;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.DurationValues;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.ENDS_TYPE;

public class BlockoutValidator implements IUiValidator {

  private ScheduleEditor scheduleEditor;

  public BlockoutValidator(ScheduleEditor scheduleEditor) {
    this.scheduleEditor = scheduleEditor;
  }

  @Override
  public boolean isValid() {
    boolean isValid = true;
    if (ENDS_TYPE.DURATION.equals(this.scheduleEditor.getBlockoutEndsType())) {
      DurationValues durationValues = this.scheduleEditor.getDurationValues();
      isValid &= durationValues.days != 0 || durationValues.hours != 0 || durationValues.minutes != 0;

    } else {
      TimePicker startTimePicker = this.scheduleEditor.getStartTimePicker();
      int startTimeHour = Integer.parseInt(startTimePicker.getHour());
      int startTimeMinute = Integer.parseInt(startTimePicker.getMinute());

      int startTime = startTimeMinute + (startTimeHour + (PM.equals(startTimePicker.getTimeOfDay()) ? 12 : 0)) * 60;

      TimePicker endTimePicker = this.scheduleEditor.getBlockoutEndTimePicker();
      int endTimeHour = Integer.parseInt(endTimePicker.getHour());
      int endTimeMinute = Integer.parseInt(endTimePicker.getMinute());

      int endTime = endTimeMinute + (endTimeHour + (PM.equals(endTimePicker.getTimeOfDay()) ? 12 : 0)) * 60;

      isValid &= endTime > startTime;
    }
    return isValid;
  }

  @Override
  public void clear() {
    // No values needing to be cleared
  }

}
