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

package org.pentaho.mantle.client.workspace;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import org.pentaho.gwt.widgets.client.controls.DateTimePicker;
import org.pentaho.gwt.widgets.client.controls.DateTimePicker.Layout;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;
import java.util.HashSet;

public class FilterDialog extends PromptDialogBox {

  private MultiWordSuggestOracle resourceOracle = new MultiWordSuggestOracle();
  private SuggestBox resourceSuggestBox = new SuggestBox( resourceOracle );

  private CheckBox afterCheckBox = new CheckBox( Messages.getString( "after" ) );
  private CheckBox beforeCheckBox = new CheckBox( Messages.getString( "before" ) );
  private DateTimePicker afterDateBox = new DateTimePicker( Layout.HORIZONTAL );
  private DateTimePicker beforeDateBox = new DateTimePicker( Layout.HORIZONTAL );

  private ListBox userListBox = new ListBox( false );
  private ListBox scheduleStateListBox = new ListBox( false );
  private ListBox scheduleTypeListBox = new ListBox( false );

  public FilterDialog() {
    super(
        Messages.getString( "filterSchedules" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public FilterDialog( JsArray<JsJob> jobs, IDialogCallback callback ) {
    super(
        Messages.getString( "filterSchedules" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    initUI( jobs );
    // setSize("800px", "500px");
    setCallback( callback );
  }

  /**
   * @param jobs
   */
  public void initUI( JsArray<JsJob> jobs ) {
    if ( jobs != null ) {
      for ( int i = 0; i < jobs.length(); i++ ) {
        resourceOracle.add( jobs.get( i ).getShortResourceName() );
      }
    }

    resourceSuggestBox.setWidth( "240px" );
    userListBox.setWidth( "200px" );
    userListBox.getElement().getStyle().setTextTransform( Style.TextTransform.CAPITALIZE );
    scheduleStateListBox.setWidth( "200px" );
    scheduleTypeListBox.setWidth( "200px" );

    // next execution filter
    CaptionPanel executionFilterCaptionPanel = new CaptionPanel( Messages.getString( "executionTime" ) );
    FlexTable executionFilterPanel = new FlexTable();
    executionFilterPanel.setWidget( 0, 0, beforeCheckBox );
    executionFilterPanel.setWidget( 0, 1, beforeDateBox );
    executionFilterPanel.setWidget( 1, 0, afterCheckBox );
    executionFilterPanel.setWidget( 1, 1, afterDateBox );
    executionFilterCaptionPanel.add( executionFilterPanel );

    afterCheckBox.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
      public void onValueChange( ValueChangeEvent<Boolean> event ) {
        afterDateBox.setEnabled( event.getValue() );
      }
    } );

    beforeCheckBox.addValueChangeHandler( new ValueChangeHandler<Boolean>() {
      public void onValueChange( ValueChangeEvent<Boolean> event ) {
        beforeDateBox.setEnabled( event.getValue() );
      }
    } );
    beforeDateBox.setEnabled( beforeCheckBox.getValue() );
    afterDateBox.setEnabled( afterCheckBox.getValue() );

    final String showAll = Messages.getString( "showAll" );
    // user filter
    int selectedIndex = getSelectedIndex( userListBox );
    userListBox.clear();
    userListBox.addItem( showAll );
    HashSet<String> uniqueUsers = new HashSet<String>();
    if ( jobs != null ) {
      for ( int i = 0; i < jobs.length(); i++ ) {
        uniqueUsers.add( jobs.get( i ).getUserName() );
      }
    }
    for ( String user : uniqueUsers ) {
      userListBox.addItem( user );
    }
    userListBox.setSelectedIndex( selectedIndex );

    // state filter
    scheduleStateListBox.setVisibleItemCount( 1 );
    selectedIndex = getSelectedIndex( scheduleStateListBox );
    scheduleStateListBox.clear();
    // NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
    scheduleStateListBox.addItem( showAll );
    scheduleStateListBox.addItem( "Normal" );
    scheduleStateListBox.addItem( "Paused" );
    scheduleStateListBox.addItem( "Complete" );
    scheduleStateListBox.addItem( "Error" );
    scheduleStateListBox.addItem( "Blocked" );
    scheduleStateListBox.addItem( "Unknown" );
    scheduleStateListBox.setSelectedIndex( selectedIndex );

    // state filter
    scheduleTypeListBox.setVisibleItemCount( 1 );
    selectedIndex = getSelectedIndex( scheduleTypeListBox );
    scheduleTypeListBox.clear();
    // NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED, UNKNOWN
    scheduleTypeListBox.addItem( showAll );
    scheduleTypeListBox.addItem( "Daily" );
    scheduleTypeListBox.addItem( "Weekly" );
    scheduleTypeListBox.addItem( "Monthly" );
    scheduleTypeListBox.addItem( "Yearly" );
    scheduleTypeListBox.setSelectedIndex( selectedIndex );

    FlexTable filterPanel = new FlexTable();
    filterPanel.setWidget( 0, 0, new Label( Messages.getString( "scheduledResource" ) ) );
    filterPanel.setWidget( 1, 0, resourceSuggestBox );

    filterPanel.setWidget( 2, 0, new Label( Messages.getString( "_user" ) ) );
    filterPanel.setWidget( 3, 0, userListBox );

    filterPanel.setWidget( 4, 0, new Label( Messages.getString( "scheduleState" ) ) );
    filterPanel.setWidget( 5, 0, scheduleStateListBox );

    filterPanel.setWidget( 6, 0, new Label( Messages.getString( "scheduleType" ) ) );
    filterPanel.setWidget( 7, 0, scheduleTypeListBox );

    filterPanel.setWidget( 8, 0, executionFilterCaptionPanel );

    setContent( filterPanel );
  }

  public String getUserFilter() {
    return userListBox.getItemText( userListBox.getSelectedIndex() );
  }

  public String getTypeFilter() {
    return scheduleTypeListBox.getItemText( scheduleTypeListBox.getSelectedIndex() );
  }

  public String getStateFilter() {
    return scheduleStateListBox.getItemText( scheduleStateListBox.getSelectedIndex() );
  }

  public Date getBeforeDate() {
    if ( beforeCheckBox.getValue() ) {
      return beforeDateBox.getDate();
    }
    return null;
  }

  public Date getAfterDate() {
    if ( afterCheckBox.getValue() ) {
      return afterDateBox.getDate();
    }
    return null;
  }

  public String getResourceName() {
    return resourceSuggestBox.getText();
  }

  public CheckBox getAfterCheckBox() {
    return afterCheckBox;
  }

  private int getSelectedIndex( ListBox listBox ) {
    int selectedIndex = listBox.getSelectedIndex();
    if ( selectedIndex == -1 ) {
      selectedIndex = 0;
    }
    return selectedIndex;
  }
}
