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

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.gwt.widgets.client.tabs.PentahoTab;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

public class SaveCommand extends AbstractCommand {

  boolean isSaveAs = false;
  private String name;
  private String path;
  private SolutionFileInfo.Type type;
  private String tabName;
  private String solution;
  private final String spinnerId = "SaveCommand";

  public SaveCommand() {
  }

  public SaveCommand( boolean isSaveAs ) {
    this.isSaveAs = isSaveAs;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final SolutionBrowserPanel navigatorPerspective = SolutionBrowserPanel.getInstance();

    retrieveCachedValues( navigatorPerspective.getContentTabPanel().getCurrentFrame() );
    boolean forceReload = false;
    if ( FileChooserDialog.getIsDirty() ) {
      forceReload = true;
      WaitPopup.getInstance().setVisibleById( true, spinnerId );
      FileChooserDialog.setIsDirty( Boolean.FALSE );
    }
    RepositoryFileTreeManager.getInstance().fetchRepositoryFileTree( new AsyncCallback<RepositoryFileTree>() {
      public void onFailure( Throwable caught ) {
      }

      public void onSuccess( RepositoryFileTree tree ) {

        retrieveCachedValues( navigatorPerspective.getContentTabPanel().getCurrentFrame() );

        if ( isSaveAs || name == null ) {
          String fileDir = "";
          if ( path != null && !StringUtils.isEmpty( path ) ) {
            // If has extension
            if ( path.endsWith( name ) ) {
              fileDir = path.substring( 0, path.lastIndexOf( "/" ) );
            } else {
              fileDir = path;
            }

          }
          WaitPopup.getInstance().setVisibleById( false, spinnerId );
          final FileChooserDialog dialog =
              new FileChooserDialog(
                  FileChooserMode.SAVE,
                  fileDir,
                  tree,
                  false,
                  true,
                  Messages.getString( "save" ), Messages.getString( "save" ), navigatorPerspective.getSolutionTree().isShowHiddenFiles() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          dialog.setSubmitOnEnter( MantleApplication.submitOnEnter );
          if ( isSaveAs ) {
            dialog.setTitle( Messages.getString( "saveAs" ) ); //$NON-NLS-1$
            dialog.setText( Messages.getString( "saveAs" ) ); //$NON-NLS-1$
          } else {
            dialog.setTitle( Messages.getString( "save" ) ); //$NON-NLS-1$
            dialog.setText( Messages.getString( "save" ) ); //$NON-NLS-1$
          }
          // TODO Uncomment the line below and delete the line after that once gwtwidets have been branched
          dialog.addFileChooserListener( new FileChooserListener() {

            public void dialogCanceled() {

            }

            @Override
            public void fileSelected( final RepositoryFile file, String filePath, String fileName, String title ) {
              SaveCommand.this.type = SolutionFileInfo.Type.XACTION;
              SaveCommand.this.name = fileName;
              SaveCommand.this.path = filePath;
              tabName = name;
              if ( tabName.indexOf( "analysisview.xaction" ) != -1 ) {
                // trim off the analysisview.xaction from the localized-name
                tabName = tabName.substring( 0, tabName.indexOf( "analysisview.xaction" ) - 1 );
              }

              JsArrayString extensions =
                  getPossibleExtensions( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId() );
              String fileExtension = null;
              if ( extensions.length() == 1 ) {
                fileExtension = extensions.get( 0 );
              }

              if ( dialog.doesSelectedFileExist( fileExtension ) ) {
                dialog.hide();
                PromptDialogBox overWriteDialog =
                    new PromptDialogBox(
                        Messages.getString( "question" ), Messages.getString( "yes" ), Messages.getString( "no" ), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        false, true );
                overWriteDialog.setContent( new Label( Messages.getString( "fileExistsOverwrite" ), false ) ); //$NON-NLS-1$
                overWriteDialog.setCallback( new IDialogCallback() {
                  public void okPressed() {
                    doSaveAs( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type,
                        true );
                    Window.setTitle( Messages.getString( "productName" ) + " - " + name ); //$NON-NLS-1$ //$NON-NLS-2$
                    FileChooserDialog.setIsDirty( Boolean.TRUE );
                  }

                  public void cancelPressed() {
                    dialog.show();
                  }
                } );
                overWriteDialog.center();
              } else {

                // [Fix for PIR-833]
                if ( file != null && !file.isFolder() && !fileName.equals( title )
                    && filePath.endsWith( file.getName() ) ) {
                  SaveCommand.this.path = filePath.substring( 0, filePath.lastIndexOf( "/" + file.getName() ) );
                }

                doSaveAs( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(),
                  name, path, type, true );
                Window.setTitle( Messages.getString( "productName" ) + " - " + name ); //$NON-NLS-1$ //$NON-NLS-2$
                persistFileInfoInFrame();
                // navigatorPerspective.addRecent(fullPathWithName, name);
                clearValues();
              }
            }

            @Override
            public void fileSelectionChanged( RepositoryFile file, String filePath, String fileName, String title ) {
              // TODO Auto-generated method stub

            }

          } );
          dialog.center();
        } else {
          doSaveAs( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type, true );
          clearValues();
        }
        WaitPopup.getInstance().setVisibleById( false, spinnerId );
      }
    }, forceReload, null, null, SolutionBrowserPanel.getInstance().getSolutionTree().isShowHiddenFiles() );
  }

  /**
   * @param elementId
   *          Id of the PUC tab containing the frame to look for a possible extensions callback in
   * @return All possible extensions provided by the frame.
   */
  private native JsArrayString getPossibleExtensions( String elementId )/*-{
    var frame = $doc.getElementById(elementId);
    frame = frame.contentWindow;
    frame.focus();
    if (frame.getPossibleFileExtensions) {
      return frame.getPossibleFileExtensions();
    }
    if (frame.gCtrlr.repositoryBrowserController.getPossibleFileExtensions) {
      return frame.gCtrlr.repositoryBrowserController.getPossibleFileExtensions();
    }
    return [];
  }-*/;

  private void persistFileInfoInFrame() {
    SolutionFileInfo fileInfo = new SolutionFileInfo();
    fileInfo.setName( this.name );
    fileInfo.setPath( this.path );
    fileInfo.setType( this.type );
    SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().setFileInfo( fileInfo );
  }

  private void clearValues() {
    name = null;
    path = null;
    type = null;
  }

  private void retrieveCachedValues( IFrameTabPanel tabPanel ) {
    clearValues();
    SolutionFileInfo info = tabPanel.getFileInfo();
    if ( info != null ) {
      this.name = info.getName();
      this.path = info.getPath();
      this.type = info.getType();
    }
  }

  private void doSaveAs( String elementId, String filename, String path, SolutionFileInfo.Type type, boolean overwrite,
      boolean showBusy ) {
    WaitPopup.getInstance().setVisible( true );
    this.doSaveAs( elementId, filename, path, type, overwrite );
    WaitPopup.getInstance().setVisible( false );
    FileChooserDialog.setIsDirty( Boolean.TRUE );
  }

  private void doSaveAs( String elementId, String filename, String path,
                         SolutionFileInfo.Type type, boolean overwrite ) {

    String unableToSaveMessage = Messages.getString( "unableToSaveMessage" );
    String save = Messages.getString( "save" );

    doSaveAsNativeWrapper( elementId, filename, path, type, overwrite, save, unableToSaveMessage );
  }

  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   * 
   * @param elementId
   */
  private native void doSaveAsNativeWrapper( String elementId, String filename, String path,
      SolutionFileInfo.Type type, boolean overwrite, String save, String unableToSaveMessage )
  /*-{

    var errorCallback = function() {
      window.top.mantle_showMessage(save, unableToSaveMessage);
    }

    var frame = $doc.getElementById(elementId);
    frame = frame.contentWindow;
    frame.focus();

    if(frame.pivot_initialized) {
      // do jpivot save
      var actualFileName = filename;
      if (filename.indexOf("analysisview.xaction") == -1) {
        actualFileName = filename + ".analysisview.xaction";
      } else {
        // trim off the analysisview.xaction from the localized-name
        filename = filename.substring(0, filename.indexOf("analysisview.xaction")-1);
      }
      frame.controller.saveAs(actualFileName, filename, path, overwrite);
    } else if (frame.handle_puc_save) {
      try {
        var result = frame.handle_puc_save(path, filename, overwrite, errorCallback);
        //We need to decode the result, but we double encoded '/' and '\' in URLEncoder.js to work around a Tomcat issue
        var almostDecodedResult = result.replace(/%255C/g, "%5C").replace(/%252F/g, "%2F");
        //Now we decode
        var decodedResult = decodeURIComponent(almostDecodedResult);
        //if(result) {
        this.@org.pentaho.mantle.client.commands.SaveCommand::doTabRename()();
        //CHECKSTYLE IGNORE LineLength FOR NEXT 2 LINES
        this.@org.pentaho.mantle.client.commands.SaveCommand::addToRecentList(Ljava/lang/String;)(decodedResult);
        this.@org.pentaho.mantle.client.commands.SaveCommand::setDeepLinkUrl(Ljava/lang/String;)(decodedResult);
        //}        
      } catch (e) {
        //TODO: externalize message once a solution to do so is found.
        $wnd.mantle_showMessage("Error","Error encountered while saving: "+e);
      }
    } else {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      $wnd.mantle_showMessage("Error","The plugin has not defined a handle_puc_save function to handle the save of the content");
    }
    $wnd.mantle_setIsRepoDirty(true);
    $wnd.mantle_isBrowseRepoDirty=true;
  }-*/;

  // used via JSNI
  private void doTabRename() {
    if ( tabName != null ) { // Save-As does not modify the name of the tab.
      PentahoTab tab = SolutionBrowserPanel.getInstance().getContentTabPanel().getSelectedTab();
      tab.setLabelText( tabName );
      tab.setLabelTooltip( tabName );
    }
  }

  // used via JSNI
  private void addToRecentList( String fullPathWithName ) {
    if ( fullPathWithName != null && fullPathWithName.contains( name ) ) {
      int index = name.lastIndexOf( "." );
      String nameWithoutExtension = name;
      if ( index != -1 ) {
        nameWithoutExtension = name.substring( 0, index );
      }
      SolutionBrowserPanel.getInstance().addRecent( fullPathWithName, nameWithoutExtension );
    }
  }

  // used via JSNI
  private void setDeepLinkUrl( String fullPathWithName ) {
    SolutionBrowserPanel.getInstance().setDeepLinkUrl( fullPathWithName );
  }
}
