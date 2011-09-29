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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class ExportFileCommand extends AbstractCommand {

  private RepositoryFile repositoryFile;

  public ExportFileCommand() {
  }

  public ExportFileCommand(RepositoryFile repositoryFile) {
    this.repositoryFile = repositoryFile;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    String path = repositoryFile.getPath();
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String exportURL = contextURL + "api/repo/files/" + SolutionBrowserPerspective.pathToId(path) + "/download";
    Window.open(exportURL, "_new", "");
  }

  public RepositoryFile getRepositoryFile() {
    return repositoryFile;
  }

  public void setRepositoryFile(RepositoryFile repositoryFile) {
    this.repositoryFile = repositoryFile;
  }

}
