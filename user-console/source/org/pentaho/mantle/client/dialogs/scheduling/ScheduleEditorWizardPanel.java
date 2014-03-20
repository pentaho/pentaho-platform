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

import java.util.Date;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.ui.IChangeHandler;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardPanel;
import org.pentaho.mantle.client.dialogs.scheduling.validators.ScheduleEditorValidator;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author wseyler
 * 
 */
public class ScheduleEditorWizardPanel extends AbstractWizardPanel {

  private static final String PENTAHO_SCHEDULE = "pentaho-schedule-create"; //$NON-NLS-1$

  ScheduleEditor scheduleEditor;

  ScheduleEditorValidator scheduleEditorValidator;

  public ScheduleEditorWizardPanel( final AbstractWizardDialog.ScheduleDialogType type ) {
    super();

    scheduleEditor = new ScheduleEditor( type );
    scheduleEditorValidator = new ScheduleEditorValidator( scheduleEditor );
    init();
    layout();
  }

  /**
   * 
   */
  private void init() {
    ICallback<IChangeHandler> chHandler = new ICallback<IChangeHandler>() {
      @Override
      public void onHandle( IChangeHandler se ) {
        panelWidgetChanged( ScheduleEditorWizardPanel.this );
      }
    };
    scheduleEditor.setOnChangeHandler( chHandler );
  }

  /**
   * 
   */
  private void layout() {
    addStyleName( PENTAHO_SCHEDULE );
    this.add( scheduleEditor, CENTER );
    panelWidgetChanged( null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.gwt.widgets.client.wizards.IWizardPanel#getName()
   */
  @Override
  public String getName() {
    return Messages.getString( "schedule.scheduleEdit" );
  }

  @Override
  public boolean canContinue() {
    return scheduleEditorValidator.isValid();
  }

  @Override
  public boolean canFinish() {
    return scheduleEditorValidator.isValid();
  }

  protected void panelWidgetChanged( Widget changedWidget ) {
    panelChanged();
  }

  public ScheduleEditor.ScheduleType getScheduleType() {
    return scheduleEditor.getScheduleType();
  }

  public ScheduleEditor getScheduleEditor() {
    return scheduleEditor;
  }

  /**
   * @return
   */
  public String getCronString() {
    return scheduleEditor.getCronString();
  }

  public Date getStartDate() {
    return scheduleEditor.getStartDate();
  }

  public String getStartTime() {
    return scheduleEditor.getStartTime();
  }

  public String getBlockoutStartTime() {
    if ( scheduleEditor.getStartTimePicker() != null ) {
      return scheduleEditor.getStartTimePicker().getTime();
    }
    return null;
  }

  public String getBlockoutEndTime() {
    if ( scheduleEditor.getBlockoutEndTimePicker() != null ) {
      return scheduleEditor.getBlockoutEndTimePicker().getTime();
    }
    return null;
  }

  public String getTimeZone() {
    if ( scheduleEditor.getTimeZonePicker() != null ) {
      ListBox tzPicker = scheduleEditor.getTimeZonePicker();
      int selIndex = tzPicker.getSelectedIndex();
      if ( selIndex != -1 ) { // Something is selected
        return tzPicker.getValue( selIndex );
      }
      return null;
    }
    return null;
  }

  public Date getEndDate() {
    return scheduleEditor.getEndDate();
  }

  public int getRepeatCount() {
    // Repeat forever
    return -1;
  }

  public String getRepeatInterval() {
    return scheduleEditor.getRepeatInSecs().toString();
  }

}
