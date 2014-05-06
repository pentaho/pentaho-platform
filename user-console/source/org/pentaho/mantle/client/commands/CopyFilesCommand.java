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
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserFile;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wseyler
 * 
 */
public class CopyFilesCommand extends AbstractCommand {

  public CopyFilesCommand() {
  }

    private String solutionPath = null;
    private String fileNames = null;
    private String fileIds = null;

    private List<SolutionBrowserFile> filesToCopy = new ArrayList();

    public String getSolutionPath() {
        return solutionPath;
    }

    public void setSolutionPath(String solutionPath) {
        this.solutionPath = solutionPath;
    }

    public String getFileNames() {
        return fileNames;
    }

    public void setFileNames(String fileNames) {
        this.fileNames = fileNames;
    }

    public String getFileIds() {
        return fileIds;
    }

    public void setFileIds(String fileIds) {
        this.fileIds = fileIds;
    }

    /*
           * (non-Javadoc)
           *
           * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
           */
  @Override
  protected void performOperation() {

      if ( this.getSolutionPath() != null && this.getFileNames()!=null && this.getFileIds()!=null ) {
          StringTokenizer pathTk=new StringTokenizer(this.getSolutionPath(),"\t");
          StringTokenizer nameTk=new StringTokenizer(this.getFileNames(),"\t");
          StringTokenizer idTk=new StringTokenizer(this.getFileIds(),"\t");
          //Build Arrays since we cannot pass complex objects from the js bus
          for(int i=0;i<pathTk.countTokens();i++){
              filesToCopy.add(new SolutionBrowserFile(idTk.tokenAt(i),nameTk.tokenAt(i),pathTk.tokenAt(i)));
          }
          performOperation( false );
      } else {
          performOperation( true );
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
    clipBoard.setClipboardItemsByIdForCopy(filesToCopy);
    clipBoard.setMimeType( "jcrFiles/list" );

    final SolutionFileActionEvent event = new SolutionFileActionEvent();
    event.setAction( this.getClass().getName() );
    event.setMessage( "Success" );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

}
