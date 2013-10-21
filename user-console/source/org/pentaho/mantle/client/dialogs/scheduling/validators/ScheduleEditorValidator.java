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

import org.pentaho.mantle.client.dialogs.scheduling.ScheduleEditor;

public class ScheduleEditorValidator implements IUiValidator {
  protected ScheduleEditor scheduleEditor;

  protected RecurrenceEditorValidator recurrenceEditorValidator;

  protected RunOnceEditorValidator runOnceEditorValidator;

  protected CronEditorValidator cronEditorValidator;

  protected BlockoutValidator blockoutValidator;

  public ScheduleEditorValidator( ScheduleEditor scheduleEditor ) {
    this.scheduleEditor = scheduleEditor;
    this.recurrenceEditorValidator = new RecurrenceEditorValidator( this.scheduleEditor.getRecurrenceEditor() );
    this.runOnceEditorValidator = new RunOnceEditorValidator( this.scheduleEditor.getRunOnceEditor() );
    this.cronEditorValidator = new CronEditorValidator( this.scheduleEditor.getCronEditor() );
    this.blockoutValidator = new BlockoutValidator( scheduleEditor );
  }

  public boolean isValid() {
    boolean isValid = true;

    switch ( scheduleEditor.getScheduleType() ) {
      case RUN_ONCE:
        isValid &= runOnceEditorValidator.isValid();
        break;
      case SECONDS: // fall through
      case MINUTES: // fall through
      case HOURS: // fall through
      case DAILY: // fall through
      case WEEKLY: // fall through
      case MONTHLY: // fall through
      case YEARLY:
        isValid &= recurrenceEditorValidator.isValid();
        break;
      case CRON:
        isValid &= cronEditorValidator.isValid();
        break;
      default:
    }

    if ( this.scheduleEditor.isBlockoutDialog() ) {
      isValid &= blockoutValidator.isValid();
    }

    return isValid;
  }

  public void clear() {
    recurrenceEditorValidator.clear();
    runOnceEditorValidator.clear();
    cronEditorValidator.clear();
  }
}
