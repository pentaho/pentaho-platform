/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.dialogs;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

public class ManageContentDialog extends PromptDialogBox {

  public enum STATE {
    EDIT, SHARE, SCHEDULE
  }

  private RadioButton editRadioButton = new RadioButton( "manage" ); //$NON-NLS-1$
  private RadioButton shareRadioButton = new RadioButton( "manage" ); //$NON-NLS-1$
  private RadioButton scheduleRadioButton = new RadioButton( "manage" ); //$NON-NLS-1$

  public ManageContentDialog() {
    super(
        Messages.getString( "manageContent" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    editRadioButton.setText( Messages.getString( "edit" ) ); //$NON-NLS-1$
    shareRadioButton.setText( Messages.getString( "share" ) ); //$NON-NLS-1$
    scheduleRadioButton.setText( Messages.getString( "schedule" ) ); //$NON-NLS-1$

    VerticalPanel contentPanel = new VerticalPanel();
    contentPanel.add( new Label( Messages.getString( "manageContentSelectFunction" ) ) ); //$NON-NLS-1$
    contentPanel.add( new HTML( "<BR>" ) ); //$NON-NLS-1$
    contentPanel.add( editRadioButton );
    contentPanel.add( shareRadioButton );
    contentPanel.add( scheduleRadioButton );

    setContent( contentPanel );
  }

  public STATE getState() {
    if ( editRadioButton.getValue() ) {
      return STATE.EDIT;
    }
    if ( shareRadioButton.getValue() ) {
      return STATE.SHARE;
    }
    return STATE.SCHEDULE;
  }

}
