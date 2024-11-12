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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class ExportFileCommand extends AbstractCommand {

  private RepositoryFile repositoryFile;

  public ExportFileCommand() {
  }

  public ExportFileCommand( RepositoryFile repositoryFile ) {
    this.repositoryFile = repositoryFile;
  }

  private String solutionPath = null;

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  protected void performOperation() {

    final SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
    if ( this.getSolutionPath() != null ) {
      sbp.getFile( this.getSolutionPath(), new SolutionFileHandler() {
        @Override
        public void handle( RepositoryFile repositoryFile ) {
          ExportFileCommand.this.setRepositoryFile( repositoryFile );
          performOperation( true );
        }
      } );
    } else {
      performOperation( true );
    }
  }

  protected void performOperation( boolean feedback ) {
    String path = repositoryFile.getPath();
    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    String exportURL =
        contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId( path ) + "/download?withManifest=true";
    Window.open( exportURL, "_new", "" );

    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );
    event.setMessage( "Success" );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

  public RepositoryFile getRepositoryFile() {
    return repositoryFile;
  }

  public void setRepositoryFile( RepositoryFile repositoryFile ) {
    this.repositoryFile = repositoryFile;
  }

}
