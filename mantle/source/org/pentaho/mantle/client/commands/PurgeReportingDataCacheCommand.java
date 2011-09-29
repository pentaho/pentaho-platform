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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;


import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: nbaker
 * Date: 3/9/11
 */
public class PurgeReportingDataCacheCommand extends AbstractCommand {
  public PurgeReportingDataCacheCommand() {
  }

  protected void performOperation() {
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(Void result) {
        MessageDialogBox dialogBox = new MessageDialogBox(
            Messages.getString("info"), Messages.getString("reportingDataCacheFlushedSuccessfully"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }
    };
    MantleServiceCache.getService().purgeReportingDataCache(callback);
  }

  protected void performOperation(final boolean feedback) {
    // do nothing
  }

}
