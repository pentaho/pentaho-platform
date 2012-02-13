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
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.tabs.PentahoTab;
import org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import com.google.gwt.user.client.ui.Widget;

public class FilePropertiesDialog extends PromptDialogBox {
  public enum Tabs {
    GENERAL, PERMISSION, HISTORY
  }

  private PentahoTabPanel propertyTabs;
  private GeneralPanel generalTab;
  private PermissionsPanel permissionsTab;
  private GeneratedContentPanel generatedContentTab;

  public FilePropertiesDialog(RepositoryFile fileSummary, final PentahoTabPanel propertyTabs, final IDialogCallback callback, Tabs defaultTab) {
    super(fileSummary.getName() + " " + Messages.getString("properties"), Messages.getString("ok"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    boolean isInTrash = fileSummary.getPath().contains("/.trash/pho:");
    setContent(propertyTabs);

    generalTab = new GeneralPanel(this, fileSummary);
    if (!fileSummary.isFolder()) {
      generatedContentTab = new GeneratedContentPanel(SolutionBrowserPanel.pathToId(fileSummary.getPath()));
    }
    if (!isInTrash) {
      permissionsTab = new PermissionsPanel(fileSummary);
    }

    generalTab.getElement().setId("filePropertiesGeneralTab");
    if (!isInTrash) {
      permissionsTab.getElement().setId("filePropertiesPermissionsTab");
    }
    okButton.getElement().setId("filePropertiesOKButton");
    cancelButton.getElement().setId("filePropertiesCancelButton");

    super.setCallback(new IDialogCallback() {

      public void cancelPressed() {
        if (callback != null) {
          callback.cancelPressed();
        }
      }

      public void okPressed() {
        applyPanel();
        if (callback != null) {
          callback.okPressed();
        }
      }
    });
    this.propertyTabs = propertyTabs;
    this.propertyTabs.addTab(Messages.getString("general"), Messages.getString("general"), false, generalTab);
    if (permissionsTab != null) {
      this.propertyTabs.addTab(Messages.getString("share"), Messages.getString("share"), false, permissionsTab);
    }
    if (generatedContentTab != null) {
      this.propertyTabs.addTab(Messages.getString("history"), Messages.getString("history"), false, generatedContentTab);
    }
    getWidget().setHeight("100%"); //$NON-NLS-1$
    getWidget().setWidth("100%"); //$NON-NLS-1$
    setPixelSize(490, 420);
    showTab(defaultTab);
  }

  private void applyPanel() {
    for (int i=0;i<propertyTabs.getTabCount();i++) {
      Widget w = propertyTabs.getTab(i).getContent();
      if (w instanceof IFileModifier) {
        ((IFileModifier) w).apply();
      }
    }
  }

  public void showTab(Tabs tab) {
    for (int i=0; i<propertyTabs.getTabCount(); i++) {
      PentahoTab pTab = propertyTabs.getTab(i);
      switch(tab) {
        case GENERAL:
          if (pTab.getContent() == generalTab) {
            propertyTabs.selectTab(pTab);
          }
          break;
        case PERMISSION:
          if (pTab.getContent() == permissionsTab) {
            propertyTabs.selectTab(pTab);
          }
          break;
        case HISTORY:
          if (pTab.getContent() == generatedContentTab) {
            propertyTabs.selectTab(pTab);
          }
          break;
        default:
          break;
      }
    }
  }
}
