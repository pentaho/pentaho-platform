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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import org.pentaho.gwt.widgets.client.controls.DatePickerEx;
import org.pentaho.gwt.widgets.client.controls.TimePicker;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.ui.IChangeHandler;
import org.pentaho.gwt.widgets.client.utils.TimeUtil;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;

/**
 * @author Steven Barkdull
 * 
 */

public class RunOnceEditor extends VerticalPanel implements IChangeHandler {

  private static final String SCHEDULER_CAPTION_PANEL = "schedule-editor-caption-panel"; //$NON-NLS-1$

  private TimePicker startTimePicker = null;
  private DefaultFormat format = new DefaultFormat( DateTimeFormat.getFormat( PredefinedFormat.DATE_SHORT ) );
  private DatePickerEx startDatePicker = new DatePickerEx( format );
  private static final String DEFAULT_START_HOUR = "12"; //$NON-NLS-1$
  private static final String DEFAULT_START_MINUTE = "00"; //$NON-NLS-1$
  private static final TimeUtil.TimeOfDay DEFAULT_TIME_OF_DAY = TimeUtil.TimeOfDay.AM;
  private ICallback<IChangeHandler> onChangeHandler = null;
  private final MessageDialogBox errorBox =
      new MessageDialogBox( Messages.getString( "error" ), "", false, false, true );

  public RunOnceEditor( final TimePicker startTimePicker ) {
    setWidth( "100%" ); //$NON-NLS-1$

    CaptionPanel startDateCaptionPanel = new CaptionPanel( Messages.getString( "schedule.startDate" ) );
    startDateCaptionPanel.setStyleName( SCHEDULER_CAPTION_PANEL );
    startDateCaptionPanel.add( startDatePicker.getDatePicker() );
    add( startDateCaptionPanel );

    this.startTimePicker = startTimePicker;
    configureOnChangeHandler();
  }

  public Date getStartDate() {
    return startDatePicker.getSelectedDate();
  }

  public void setStartDate( Date d ) {
    startDatePicker.getDatePicker().setValue( d );
  }

  public String getStartTime() {
    return startTimePicker.getTime();
  }

  public void setStartTime( String strTime ) {
    startTimePicker.setTime( strTime );
  }

  public void reset( Date d ) {
    startTimePicker.setHour( DEFAULT_START_HOUR );
    startTimePicker.setMinute( DEFAULT_START_MINUTE );
    startTimePicker.setTimeOfDay( DEFAULT_TIME_OF_DAY );
    startDatePicker.getDatePicker().setValue( d );
  }

  public void setStartDateError( String errorMsg ) {
    if ( errorMsg != null && !errorBox.isShowing() ) {
      errorBox.setContent( new Label( errorMsg ) );
      errorBox.center();
    }
    // startDateLabel.setErrorMsg( errorMsg );
  }

  public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
    this.onChangeHandler = handler;
  }

  private void changeHandler() {
    if ( null != onChangeHandler ) {
      onChangeHandler.onHandle( this );
    }
  }

  private void configureOnChangeHandler() {
    final RunOnceEditor localThis = this;

    ICallback<IChangeHandler> handler = new ICallback<IChangeHandler>() {
      public void onHandle( IChangeHandler o ) {
        localThis.changeHandler();
      }
    };
    startTimePicker.setOnChangeHandler( handler );
    startDatePicker.setOnChangeHandler( handler );
  }
}
