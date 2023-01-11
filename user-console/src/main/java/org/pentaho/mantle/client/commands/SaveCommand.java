/*!
 *
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
 *
 * Copyright (c) 2002 - 2020 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.user.client.ui.FocusPanel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.tabs.PentahoTab;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;

public class SaveCommand extends AbstractCommand {

  boolean isSaveAs = false;
  private String name;
  private String path;
  private SolutionFileInfo.Type type;
  private String tabName;

  // Needs to be a class field so that it can be called from JavaScript.
  private CommandResultCallback performOperationCallback;

  public SaveCommand() {
  }

  public SaveCommand( boolean isSaveAs ) {
    this.isSaveAs = isSaveAs;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    performOperationAsync( feedback, null );
  }

  protected void performOperationAsync( boolean feedback, final CommandResultCallback callback ) {

    final SolutionBrowserPanel navigatorPerspective = SolutionBrowserPanel.getInstance();

    retrieveCachedValues( navigatorPerspective.getContentTabPanel().getCurrentFrame() );

    if ( isSaveAs || name == null ) {
      String fileDir = "";
      if ( path != null && !StringUtils.isEmpty( path ) ) {
        // If has extension
        if ( path.endsWith( name ) ) {
          fileDir = path.substring( 0, path.lastIndexOf( '/' ) );
        } else {
          fileDir = path;
        }
      }

      final String okButtonText = Messages.getString( "save" );
      final String operationText = isSaveAs ? Messages.getString( "saveAs" ) : Messages.getString( "save" );
      final boolean showHiddenFiles = navigatorPerspective.getSolutionTree().isShowHiddenFiles();

      final FileChooserDialog dialog = new FileChooserDialog( FileChooserMode.SAVE,
        fileDir, null, false, true, operationText, okButtonText, showHiddenFiles );

      dialog.setSubmitOnEnter( MantleApplication.submitOnEnter );
      dialog.setTitle( operationText );

      dialog.addFileChooserListener( new FileChooserListener() {
        @Override
        public void dialogCanceled() {
          if ( callback != null ) {
            callback.onCanceled();
          }
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

          JsArrayString extensions = getPossibleExtensions(
            navigatorPerspective.getContentTabPanel().getCurrentFrameElementId() );

          final String fileExtension = extensions.length() == 1 ? extensions.get( 0 ) : null;

          if ( dialog.doesSelectedFileExist( fileExtension ) ) {
            dialog.hide();

            PromptDialogBox overWriteDialog = new PromptDialogBox(
              Messages.getString( "question" ), Messages.getString( "yes" ), Messages.getString( "no" ),
              false, true
            );

            FocusPanel focusPanel = new FocusPanel(  new Label( Messages.getString( "fileExistsOverwrite" ), false ) );
            focusPanel.setStyleName( "dialogsFocusPanel" );
            overWriteDialog.setContent( focusPanel );
            overWriteDialog.setCallback( new IDialogCallback() {
              public void okPressed() {
                // Save as, overwriting existing file.

                if ( fileExtension != null && tabName.endsWith( fileExtension ) ) {
                  tabName = tabName.substring( 0, tabName.lastIndexOf( fileExtension ) );
                }

                final CommandResultCallback composedCallback = composeCallbacks( new CommandResultCallback() {
                  @Override
                  public void onSuccess() {
                    Window.setTitle( Messages.getString( "productName" ) + " - " + name );
                    persistFileInfoInFrame();
                    // Shouldn't clearValues() be called like in the other cases?
                  }

                  @Override
                  public void onCanceled() {
                    // Shouldn't clearValues() be called like in the other cases?
                  }

                  @Override
                  public void onError( Throwable error ) {
                    // Shouldn't clearValues() be called like in the other cases?
                  }
                }, callback );

                doSaveAs( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(),
                  name, path, type, true, composedCallback );
              }

              public void cancelPressed() {
                // Let the user choose another file name, then.
                dialog.show();
              }
            } );

            overWriteDialog.center();
          } else {
            // Save as a new file.

            // [Fix for PIR-833]
            if ( file != null && !file.isFolder() && !fileName.equals( title )
              && filePath.endsWith( file.getName() ) ) {
              SaveCommand.this.path = filePath.substring( 0, filePath.lastIndexOf( "/" + file.getName() ) );
            }

            final CommandResultCallback composedCallback = composeCallbacks( new CommandResultCallback() {
              @Override
              public void onSuccess() {
                Window.setTitle( Messages.getString( "productName" ) + " - " + name );
                persistFileInfoInFrame();
                clearValues();
              }

              @Override
              public void onCanceled() {
                clearValues();
              }

              @Override
              public void onError( Throwable error ) {
                clearValues();
              }
            }, callback );

            doSaveAs( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(),
              name, path, type, true, composedCallback );
          }
        }

        @Override
        public void fileSelectionChanged( RepositoryFile file, String filePath, String fileName, String title ) {
          // NOOP
        }
      } );

      dialog.center();
    } else {
      // Save an existing file.
      final CommandResultCallback composedCallback = composeCallbacks( new CommandResultCallback() {
        @Override
        public void onSuccess() {
          clearValues();
        }

        @Override
        public void onCanceled() {
          clearValues();
        }

        @Override
        public void onError( Throwable error ) {
          clearValues();
        }
      }, callback );

      doSaveAs( navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, path, type,
          true, composedCallback );
    }
  }

  /**
   * @param elementId
   *          Id of the PUC tab containing the frame to look for a possible extensions callback in
   * @return All possible extensions provided by the frame.
   */
  private native JsArrayString getPossibleExtensions( String elementId )
  /*-{
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
                        final CommandResultCallback callback ) {

    // JSNI can only call methods on class fields.
    performOperationCallback = wrapCallbackWithFinally( callback, new Runnable() {
      @Override
      public void run() {
        performOperationCallback = null;
      }
    } );

    String unableToSaveMessage = Messages.getString( "unableToSaveMessage" );
    String save = Messages.getString( "save" );
    String error = Messages.getString( "error" );
    String errorEncounteredWhileSaving = Messages.getString( "error.EncounteredWhileSaving" );
    String invalidFilename = Messages.getString( "filenameContainsIllegalCharacters" );

    doSaveAsNativeWrapper( elementId, filename, path, type, overwrite, save, unableToSaveMessage, error,
      errorEncounteredWhileSaving, invalidFilename );
  }

  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   *
   * @param save
   *          - externalize message save
   * @param unableToSaveMessage
   *          - externalize message unable to save
   * @param error
   *          - externalize message error
   * @param errorEncounteredWhileSaving
   *          - externalize message errorEncounteredWhileSaving
   */
  private native void doSaveAsNativeWrapper( String elementId, String filename, String path,
    SolutionFileInfo.Type type, boolean overwrite, String save, String unableToSaveMessage, String error,
    String errorEncounteredWhileSaving, String invalidFilename )
  /*-{
    var frame = $doc.getElementById(elementId);
    frame = frame.contentWindow;
    frame.focus();

    // Check if the name has illegal characters
    if( /[\x00-\x1F\x7F]/.test(filename) ) {
      window.parent.mantle_showMessage(save, invalidFilename);

      $wnd.mantle_setIsRepoDirty(true);
      $wnd.mantle_isBrowseRepoDirty = true;

      // Must be an Error instance, for JSNI interop to work.
      e = new Error(invalidFilename);

      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      that.@org.pentaho.mantle.client.commands.SaveCommand::performOperationCallbackError(Lcom/google/gwt/core/client/JavaScriptException;)(e);
    }

    if (frame.pivot_initialized) {
      // do jpivot save
      var actualFileName = filename;
      if (filename.indexOf("analysisview.xaction") === -1) {
        actualFileName = filename + ".analysisview.xaction";
      } else {
        // trim off the analysisview.xaction from the localized-name
        filename = filename.substring(0, filename.indexOf("analysisview.xaction")-1);
      }
      frame.controller.saveAs(actualFileName, filename, path, overwrite);
      $wnd.mantle_setIsRepoDirty(true);
      $wnd.mantle_isBrowseRepoDirty=true;

      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      this.@org.pentaho.mantle.client.commands.SaveCommand::performOperationCallbackSuccess()();

    } else if (frame.handle_puc_save) {
      var isSavedSuccessfully = true;
      var that = this;

      var finallyCallback = function() {
        $wnd.mantle_setIsRepoDirty(true);
        $wnd.mantle_isBrowseRepoDirty = true;
      };

      var errorCallback = function(e) {
        innerErrorCallback(e, save, unableToSaveMessage);
      };

      var innerErrorCallback = function(e, errorTitle, errorDetailMessage) {

        window.parent.mantle_showMessage(errorTitle, errorDetailMessage);

        isSavedSuccessfully = false;

        finallyCallback();

        if (!(e instanceof Error)) {
          // Must be an Error instance, for JSNI interop to work.
          e = new Error(errorDetailMessage);
        }

        //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
        that.@org.pentaho.mantle.client.commands.SaveCommand::performOperationCallbackError(Lcom/google/gwt/core/client/JavaScriptException;)(e);
      };

      var canceledCallback = function() {

        isSavedSuccessfully = false;

        finallyCallback();

        //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
        that.@org.pentaho.mantle.client.commands.SaveCommand::performOperationCallbackCanceled()();
      };

      var successCallback = function(result) {
        // We need to decode the result,
        // but we double encoded '/' and '\' in URLEncoder.js to work around a Tomcat issue.
        var almostDecodedResult = result.replace(/%255C/g, "%5C").replace(/%252F/g, "%2F");

        // Now we decode.
        var decodedResult = decodeURIComponent(almostDecodedResult);

        that.@org.pentaho.mantle.client.commands.SaveCommand::doTabRename()();

        //CHECKSTYLE IGNORE LineLength FOR NEXT 2 LINES
        that.@org.pentaho.mantle.client.commands.SaveCommand::addToRecentList(Ljava/lang/String;)(decodedResult);
        that.@org.pentaho.mantle.client.commands.SaveCommand::setDeepLinkUrl(Ljava/lang/String;)(decodedResult);

        finallyCallback();

        //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
        that.@org.pentaho.mantle.client.commands.SaveCommand::performOperationCallbackSuccess()();
      };

      try {
        var result = frame.handle_puc_save(path, filename, overwrite, errorCallback, canceledCallback);
        if (isSavedSuccessfully) {
          if(result && typeof result.then === "function") {
            // Assume it is a Promise. Not really saved yet. Wait for the result.
            result.then(successCallback, function(error) {
                if(error == null) {
                  // Promise.reject() to cancel.
                  canceledCallback();
                } else {
                  // Promise.reject(new Error(...)) to signal a real error.
                  errorCallback(error);
                }
            });
          } else {
            // Legacy code path.
            // Assume the result is the string with the actual path used.
            successCallback(result);
          }
        } // otherwise, either errorCallback or canceledCallback were already called.
      } catch(e) {
        innerErrorCallback(e, error, errorEncounteredWhileSaving + e);
      }
    } else {
      var msgDetailNotImplemented =
          "The plugin has not defined a handle_puc_save function to handle the save of the content";

      $wnd.mantle_showMessage(error, msgDetailNotImplemented);

      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      this.@org.pentaho.mantle.client.commands.SaveCommand::performOperationCallbackError(Lcom/google/gwt/core/client/JavaScriptException;)(new Error(msgDetailNotImplemented));
    }
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
      SolutionBrowserPanel.getInstance().addRecent( fullPathWithName, name );
    }
  }

  // used via JSNI
  private void setDeepLinkUrl( String fullPathWithName ) {
    SolutionBrowserPanel.getInstance().setDeepLinkUrl( fullPathWithName );
  }

  /**
   * Enables calling the performOperationCallback's onSuccess method from JSNI.
   *
   * Used via JSNI.
   */
  private void performOperationCallbackSuccess() {
    if ( performOperationCallback != null ) {
      performOperationCallback.onSuccess();
    }
  }

  /**
   * Enables calling the performOperationCallback's onCanceled method from JSNI.
   *
   * Used via JSNI.
   */
  private void performOperationCallbackCanceled() {
    if ( performOperationCallback != null ) {
      performOperationCallback.onCanceled();
    }
  }

  /**
   * Enables calling the performOperationCallback's onError method from JSNI.
   *
   * Used via JSNI.
   * @param error - The error.
   */
  private void performOperationCallbackError( JavaScriptException error ) {
    if ( performOperationCallback != null ) {
      performOperationCallback.onError( error );
    }
  }

  private CommandResultCallback wrapCallbackWithFinally(
      final CommandResultCallback callback,
      final Runnable finallyCleanup ) {

    return new CommandResultCallback() {
      @Override
      public void onSuccess() {
        finallyCleanup.run();

        if ( callback != null ) {
          callback.onSuccess();
        }
      }

      @Override
      public void onCanceled() {
        finallyCleanup.run();

        if ( callback != null ) {
          callback.onCanceled();
        }
      }

      @Override
      public void onError( Throwable error ) {
        finallyCleanup.run();

        if ( callback != null ) {
          callback.onError( error );
        }
      }
    };
  }

  private CommandResultCallback composeCallbacks(
      final CommandResultCallback callbackA,
      final CommandResultCallback callbackB ) {

    if ( callbackA == null ) {
      return callbackB;
    }

    if ( callbackB == null ) {
      return callbackA;
    }

    return new CommandResultCallback() {
      @Override
      public void onSuccess() {
        callbackA.onSuccess();
        callbackB.onSuccess();
      }

      @Override
      public void onCanceled() {
        callbackA.onCanceled();
        callbackB.onCanceled();
      }

      @Override
      public void onError( Throwable error ) {
        callbackA.onError( error );
        callbackB.onError( error );
      }
    };
  }
}
