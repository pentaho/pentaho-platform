/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Apr 5, 2011 
 * @author wseyler
 */


package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

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
    super("Overwrite", Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    VerticalPanel rootPanel = new VerticalPanel();
    Label overwriteInstructions = new Label(Messages.getString("overwriteInstructions")); //$NON-NLS-1$
    Label selectOption = new Label(Messages.getString("selectOption")); //$NON-NLS-1$
    renameRb = new RadioButton(RADIO_GROUP_NAME, Messages.getString("renameRbTitle")); //$NON-NLS-1$
    overwriteRb = new RadioButton(RADIO_GROUP_NAME, Messages.getString("overwriteRbTitle")); //$NON-NLS-1$
    noRenameOrOverwriteRb = new RadioButton(RADIO_GROUP_NAME, Messages.getString("noOverwriteOrRenameRbTitle"));  //$NON-NLS-1$
    renameRb.setValue(true);
   
    rootPanel.add(overwriteInstructions);
    rootPanel.add(selectOption);
    rootPanel.add(renameRb);
    rootPanel.add(overwriteRb);
    rootPanel.add(noRenameOrOverwriteRb);
    
    setContent(rootPanel);
  }
  
  public int getOverwriteMode() {
    if (overwriteRb.getValue()) {
      return 1;
    } else if (renameRb.getValue()) {
      return 2;
    } else if (noRenameOrOverwriteRb.getValue()) {
      return 3;
    }
    return 2; // Default to rename
  }
}
