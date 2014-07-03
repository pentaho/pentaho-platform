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

package org.pentaho.mantle.client.dialogs;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

/**
 * @author wseyler
 * 
 */
public class OverwritePromptDialog extends PromptDialogBox {
  /**
   * 
   */
  private static final String RADIO_GROUP_NAME = "radioGroup"; //$NON-NLS-1$
  protected RadioButton overwriteRb;
  protected RadioButton renameRb;
  protected RadioButton noRenameOrOverwriteRb;

  public OverwritePromptDialog() {
    super(
        Messages.getString( "overwritePromptDialogTitle" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    VerticalPanel rootPanel = new VerticalPanel();
    Label overwriteInstructions = new Label( Messages.getString( "overwriteInstructions" ) ); //$NON-NLS-1$
    Label selectOption = new Label( Messages.getString( "selectOption" ) ); //$NON-NLS-1$
    renameRb = new RadioButton( RADIO_GROUP_NAME, Messages.getString( "renameRbTitle" ) ); //$NON-NLS-1$
    overwriteRb = new RadioButton( RADIO_GROUP_NAME, Messages.getString( "overwriteRbTitle" ) ); //$NON-NLS-1$
    noRenameOrOverwriteRb = new RadioButton( RADIO_GROUP_NAME, Messages.getString( "noOverwriteOrRenameRbTitle" ) ); //$NON-NLS-1$
    renameRb.setValue( true );

    rootPanel.add( overwriteInstructions );
    rootPanel.add( selectOption );
    rootPanel.add( renameRb );
    rootPanel.add( overwriteRb );
    rootPanel.add( noRenameOrOverwriteRb );

    setContent( rootPanel );
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
