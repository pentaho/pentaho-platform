/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.dialogs.scheduling.validators;

import org.pentaho.gwt.widgets.client.controls.TimePicker;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.DurationValues;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor.ENDS_TYPE;

import static org.pentaho.gwt.widgets.client.utils.TimeUtil.TimeOfDay.PM;

public class BlockoutValidator implements IUiValidator {

  private ScheduleEditor scheduleEditor;

  public BlockoutValidator( ScheduleEditor scheduleEditor ) {
    this.scheduleEditor = scheduleEditor;
  }

  @Override
  public boolean isValid() {
    boolean isValid = true;
    if ( ENDS_TYPE.DURATION.equals( this.scheduleEditor.getBlockoutEndsType() ) ) {
      DurationValues durationValues = this.scheduleEditor.getDurationValues();
      isValid &= durationValues.days != 0 || durationValues.hours != 0 || durationValues.minutes != 0;

    } else {
      TimePicker startTimePicker = this.scheduleEditor.getStartTimePicker();
      int startTimeHour = Integer.parseInt( startTimePicker.getHour() );
      int startTimeMinute = Integer.parseInt( startTimePicker.getMinute() );

      int startTime =
          startTimeMinute + ( startTimeHour + ( PM.equals( startTimePicker.getTimeOfDay() ) ? 12 : 0 ) ) * 60;

      TimePicker endTimePicker = this.scheduleEditor.getBlockoutEndTimePicker();
      int endTimeHour = Integer.parseInt( endTimePicker.getHour() );
      int endTimeMinute = Integer.parseInt( endTimePicker.getMinute() );

      int endTime = endTimeMinute + ( endTimeHour + ( PM.equals( endTimePicker.getTimeOfDay() ) ? 12 : 0 ) ) * 60;

      isValid &= endTime > startTime;
    }
    return isValid;
  }

  @Override
  public void clear() {
    // No values needing to be cleared
  }

}
