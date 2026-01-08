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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author wseyler
 * 
 */
public class OverwritePromptDialog extends MessageDialogBox {
  /**
   * 
   */
  private static final String RADIO_GROUP_NAME = "radioGroup"; //$NON-NLS-1$
  protected RadioButton overwriteRb;
  protected RadioButton renameRb;
  protected RadioButton noRenameOrOverwriteRb;

  public OverwritePromptDialog() {
    super(
      Messages.getString( "overwritePromptDialogTitle" ),
      new VerticalFlexPanel(),
      Messages.getString( "ok" ),
      Messages.getString( "cancel" ) );

    VerticalFlexPanel rootPanel = (VerticalFlexPanel) getContent();

    Label overwriteInstructions = new Label( Messages.getString( "overwriteInstructions" ) );
    overwriteInstructions.getElement().setId( DOM.createUniqueId() );

    Label selectOption = new Label( Messages.getString( "selectOption" ) );
    renameRb = new RadioButton( RADIO_GROUP_NAME, Messages.getString( "renameRbTitle" ) );
    overwriteRb = new RadioButton( RADIO_GROUP_NAME, Messages.getString( "overwriteRbTitle" ) );
    noRenameOrOverwriteRb = new RadioButton( RADIO_GROUP_NAME, Messages.getString( "noOverwriteOrRenameRbTitle" ) );

    renameRb.setValue( true );

    rootPanel.add( overwriteInstructions );
    rootPanel.add( selectOption );
    rootPanel.add( renameRb );
    rootPanel.add( overwriteRb );
    rootPanel.add( noRenameOrOverwriteRb );

    setAriaDescribedBy( overwriteInstructions.getElement().getId() );

    // Rename is a safe choice.
    setFocusWidget( okButton );
  }

  public int getOverwriteMode() {
    if ( overwriteRb.getValue() ) {
      return 1;
    } else if ( renameRb.getValue() ) {
      return 2;
    } else if ( noRenameOrOverwriteRb.getValue() ) {
      return 3;
    }
    return 2; // Default to rename
  }
}
