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

import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.controls.DateRangeEditor;
import org.pentaho.gwt.widgets.client.controls.ErrorLabel;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.ui.IChangeHandler;
import org.pentaho.gwt.widgets.client.utils.TimeUtil;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;

/**
 * @author Steven Barkdull
 * 
 */
@SuppressWarnings( "deprecation" )
public class CronEditor extends VerticalPanel implements IChangeHandler {
  private static final String CRON_LABEL = "cron-label"; //$NON-NLS-1$

  private TextBox cronTb = new TextBox();
  private DateRangeEditor dateRangeEditor = null;
  private ErrorLabel cronLabel = null;
  private ICallback<IChangeHandler> onChangeHandler;

  public CronEditor() {
    super();
    setWidth( "100%" ); //$NON-NLS-1$

    Label l = new Label( Messages.getString( "schedule.cronLabel" ) );
    l.setStylePrimaryName( CRON_LABEL );
    cronLabel = new ErrorLabel( l );

    add( cronLabel );
    add( cronTb );

    dateRangeEditor = new DateRangeEditor( new Date() );
    add( dateRangeEditor );
    configureOnChangeHandler();
  }

  public void reset( Date d ) {
    cronTb.setText( "" ); //$NON-NLS-1$
    dateRangeEditor.reset( d );
  }

  public String getCronString() {
    return cronTb.getText();
  }

  public void setCronString( String cronStr ) {
    this.cronTb.setText( cronStr );
  }

  public Date getStartDate() {
    return dateRangeEditor.getStartDate();
  }

  public void setStartDate( Date d ) {
    dateRangeEditor.setStartDate( d );
  }

  public Date getEndDate() {
    return dateRangeEditor.getEndDate();
  }

  public void setEndDate( Date d ) {
    dateRangeEditor.setEndDate( d );
  }

  public void setNoEndDate() {
    dateRangeEditor.setNoEndDate();
  }

  public boolean isEndBy() {
    return dateRangeEditor.isEndBy();
  }

  public void setEndBy() {
    dateRangeEditor.setEndBy();
  }

  public boolean isNoEndDate() {
    return dateRangeEditor.isNoEndDate();
  }

  public String getStartTime() {
    return TimeUtil.get0thTime();
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return DateRangeEditor
   */
  public DateRangeEditor getDateRangeEditor() {
    return dateRangeEditor;
  }

  public void setCronError( String errorMsg ) {
    cronLabel.setErrorMsg( errorMsg );
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
    final CronEditor localThis = this;
    KeyboardListener keyboardListener = new KeyboardListener() {
      public void onKeyDown( Widget sender, char keyCode, int modifiers ) {
      }

      public void onKeyPress( Widget sender, char keyCode, int modifiers ) {
      }

      public void onKeyUp( Widget sender, char keyCode, int modifiers ) {
        localThis.changeHandler();
      }
    };
    ICallback<IChangeHandler> handler = new ICallback<IChangeHandler>() {
      public void onHandle( IChangeHandler o ) {
        localThis.changeHandler();
      }
    };
    cronTb.addKeyboardListener( keyboardListener );
    dateRangeEditor.setOnChangeHandler( handler );
  }
}
