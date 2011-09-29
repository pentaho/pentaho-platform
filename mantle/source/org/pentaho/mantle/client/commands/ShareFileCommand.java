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

import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.ui.MantleTabPanel;

import com.google.gwt.user.client.Command;

public class ShareFileCommand implements Command {

  public ShareFileCommand() {
  }

  public void execute() {
    SolutionBrowserPerspective sbp = SolutionBrowserPerspective.getInstance();
    List<RepositoryFile> selectedList = sbp.getFilesListPanel().getRepositoryFiles();
    if (selectedList != null && selectedList.size() == 1) {
      RepositoryFile item = selectedList.get(0);
    FilePropertiesDialog dialog = new FilePropertiesDialog(item, PluginOptionsHelper.getEnabledOptions(item.getName()), SolutionBrowserPerspective
        .getInstance().isAdministrator(), new MantleTabPanel(), null, FilePropertiesDialog.Tabs.PERMISSION);
    dialog.showTab(FilePropertiesDialog.Tabs.PERMISSION);
    dialog.center();
  }
  }

}
