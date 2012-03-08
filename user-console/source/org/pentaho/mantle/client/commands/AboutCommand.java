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

import java.util.Date;

import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AboutCommand extends AbstractCommand {

  public AboutCommand() {
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    if(MantleApplication.mantleRevisionOverride != null && MantleApplication.mantleRevisionOverride.length() > 0) {
      showAboutDialog(MantleApplication.mantleRevisionOverride);
    } else {
    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(String version) {
          showAboutDialog(version);
        }
      };
      MantleServiceCache.getService().getVersion(callback);
    }
  }

  private void showAboutDialog(String version) {
    String licenseInfo = Messages.getString("licenseInfo", ""+((new Date()).getYear()+1900));
    String releaseLabel = Messages.getString("release");
    PromptDialogBox dialogBox = new PromptDialogBox(Messages.getString("aboutDialogTitle"), Messages.getString("ok"), null, false, true); //$NON-NLS-1$

   	    VerticalPanel aboutContent = new VerticalPanel();
    aboutContent.add(new Label(releaseLabel + " " + version));
   	    aboutContent.add(new HTML(licenseInfo));
   	    
   	    dialogBox.setContent(aboutContent);
        dialogBox.center();
      }
}
