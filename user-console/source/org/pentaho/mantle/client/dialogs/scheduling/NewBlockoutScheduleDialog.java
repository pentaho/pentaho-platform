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

package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.wizards.IWizardPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobTrigger;

public class NewBlockoutScheduleDialog extends ScheduleRecurrenceDialog {
  private boolean updateMode = false;

  public NewBlockoutScheduleDialog( final String filePath, final IDialogCallback callback, final boolean hasParams,
      final boolean isEmailConfValid ) {
    super( null, ScheduleDialogType.BLOCKOUT,
        Messages.getString( "newBlockoutSchedule" ), filePath, "", "", callback, hasParams, //$NON-NLS-1$
        isEmailConfValid );
  }

  public NewBlockoutScheduleDialog( final JsJob jsJob, final IDialogCallback callback, final boolean hasParams,
      final boolean isEmailConfValid ) {
    super( null, jsJob, callback, hasParams, isEmailConfValid, ScheduleDialogType.BLOCKOUT );
  }

  @Override
  protected boolean enableNext( int index ) {
    return true;
  }

  @Override
  protected boolean enableFinish( int index ) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected boolean onNext( IWizardPanel nextPanel, IWizardPanel previousPanel ) {
    return super.enableNext( getIndex() );
  }

  @Override
  protected boolean onFinish() {
    if ( !super.enableFinish( getIndex() ) ) {
      return false;
    }
    JsJobTrigger trigger = getJsJobTrigger();
    JSONObject schedule = getSchedule();

    // TODO -- Add block out verification that it is not completely blocking an existing schedule
    if ( updateMode ) {
      addBlockoutPeriod( schedule, trigger, "update?jobid=" + URL.encodeQueryString( editJob.getJobId() ) ); //$NON-NLS-1$
    } else {
      addBlockoutPeriod( schedule, trigger, "add" ); //$NON-NLS-1$
    }
    getCallback().okPressed();

    return true;
  }

  public void setUpdateMode() {
    updateMode = true;
  }
}
