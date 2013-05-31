/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.mantle.client.dialogs.SelectFolderDialog;

import com.google.gwt.user.client.Window;

public class AboutCommand extends AbstractCommand {

  public AboutCommand() {
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final SelectFolderDialog folderChooser = new SelectFolderDialog();
    folderChooser.setCallback(new IDialogCallback() {
      public void okPressed() {
        Window.alert(folderChooser.getSelectedPath());
      }

      public void cancelPressed() {
      }
    });
    folderChooser.center();
  }
}
