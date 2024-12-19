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


package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog.Tabs;

public class FilePropertiesCommand extends AbstractFilePropertiesCommand {

  public FilePropertiesCommand() {
  }

  public FilePropertiesCommand( RepositoryFile repositoryFile ) {
    this.setRepositoryFile( repositoryFile );
  }

  private String solutionPath = null;

  @Override
  public String getSolutionPath() {
    return solutionPath;
  }

  @Override
  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  @Override
  protected Tabs getActiveTab() {
    return Tabs.GENERAL;
  }
}
