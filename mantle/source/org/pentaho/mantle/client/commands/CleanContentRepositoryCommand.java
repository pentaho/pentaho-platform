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

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CleanContentRepositoryCommand extends AbstractCommand {

  private static final int DEFAULT_DAYS_BACK = 90;
  
  int daysBack = DEFAULT_DAYS_BACK;

  public CleanContentRepositoryCommand() {
    this(DEFAULT_DAYS_BACK);
  }
  
  public CleanContentRepositoryCommand(int daysBack) {
    this.daysBack = daysBack;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Integer numItemsCleaned) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), Messages.getString("cleanContentRepositorySuccess"), false, //$NON-NLS-1$ //$NON-NLS-2$
            false, true);
        dialogBox.center();
      }
    };
    MantleServiceCache.getService().cleanContentRepository(daysBack, callback);
  }

}
