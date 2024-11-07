/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionFileActionEvent;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserClipboard;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wseyler
 */
public class CutFilesCommand extends AbstractCommand {

  public CutFilesCommand() {
  }

  private String solutionPath = null;
  private String fileNames = null;
  private String fileIds = null;

  private List<SolutionBrowserFile> filesToCut = new ArrayList();

  public String getSolutionPath() {
    return solutionPath;
  }

  public void setSolutionPath( String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  public String getFileNames() {
    return fileNames;
  }

  public void setFileNames( String fileNames ) {
    this.fileNames = fileNames;
  }

  public String getFileIds() {
    return fileIds;
  }

  public void setFileIds( String fileIds ) {
    this.fileIds = fileIds;
  }

  private final SolutionFileActionEvent event = new SolutionFileActionEvent( this.getClass().getName() );

  /*
     * (non-Javadoc)
     *
     * @see org.pentaho.mantle.client.commands.AbstractCommand#performOperation()
     */
  @Override
  protected void performOperation() {
    event.setMessage( "Click" );
    EventBusUtil.EVENT_BUS.fireEvent( event );

    if ( this.getSolutionPath() != null && this.getFileNames() != null && this.getFileIds() != null ) {
      StringTokenizer pathTk = new StringTokenizer( this.getSolutionPath(), "\n" );
      StringTokenizer nameTk = new StringTokenizer( this.getFileNames(), "\n" );
      StringTokenizer idTk = new StringTokenizer( this.getFileIds(), "\n" );
      //Build Arrays since we cannot pass complex objects from the js bus
      for ( int i = 0; i < pathTk.countTokens(); i++ ) {
        filesToCut.add( new SolutionBrowserFile( idTk.tokenAt( i ), nameTk.tokenAt( i ), pathTk.tokenAt( i ) ) );
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
    clipBoard.setClipboardItemsForCut( filesToCut );
    clipBoard.setMimeType( "jcrFiles/list" );

    event.setMessage( "Success" );
    EventBusUtil.EVENT_BUS.fireEvent( event );
  }

}
