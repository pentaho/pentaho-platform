/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserClipboard;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wseyler
 * 
 */
public class CutFilesCommand extends AbstractCommand {
  private List<FileItem> repositoryFiles;

  public CutFilesCommand() {
  }

  /**
   * @param selectedItems
   */
  public CutFilesCommand( List<FileItem> selectedItems ) {
    super();
    this.repositoryFiles = selectedItems;
  }

  private String solutionPath = null;

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
   */
  @Override
  protected void performOperation() {

      SolutionBrowserPanel sbp = SolutionBrowserPanel.getInstance();
      StringTokenizer stk=new StringTokenizer(this.getSolutionPath(),"\t");
      for(int i=0;i<stk.countTokens();i++){
          sbp.getFile( stk.tokenAt(i), new SolutionFileHandler() {
              @Override
              public void handle( RepositoryFile repositoryFile ) {
                  if ( repositoryFiles == null ) {
                      repositoryFiles = new ArrayList<FileItem>();
                  }
                  repositoryFiles.add( new FileItem( repositoryFile, null, null, false, null ) );
                  performOperation( false );
              }
          } );
      }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation(boolean)
   */
  @Override
  protected void performOperation( boolean feedback ) {
    SolutionBrowserClipboard clipBoard = SolutionBrowserClipboard.getInstance();
    clipBoard.setDataForCut( repositoryFiles );
    clipBoard.setMimeType( "jcrFiles/list" );

    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );
    event.setMessage( "Success" );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

}
