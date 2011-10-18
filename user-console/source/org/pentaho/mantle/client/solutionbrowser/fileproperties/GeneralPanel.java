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

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Document;

public class GeneralPanel extends FlexTable implements IFileModifier {

  Label nameLabel = new Label();
  Label locationLabel = new Label();
  Label sourceLabel = new Label();
  Label typeLabel = new Label();
  Label sizeLabel = new Label();
  Label lastModifiedDateLabel = new Label();
//  IFileSummary fileSummary;
  RepositoryFile fileSummary;
  
  public GeneralPanel(RepositoryFile fileSummary) {
    super();
    this.fileSummary = fileSummary;
    setWidget(0, 0, new Label(Messages.getString("name")+":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(0, 1, nameLabel);
    setWidget(1, 0, new Label(Messages.getString("location")+":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(1, 1, locationLabel);
    setWidget(2, 0, new Label(Messages.getString("source") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(2, 1, sourceLabel);
    setWidget(3, 0, new Label(Messages.getString("type") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(3, 1, typeLabel);
    setWidget(4, 0, new Label(Messages.getString("size") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(4, 1, sizeLabel);
    Label lbl = new Label(Messages.getString("lastModified") +":"); //$NON-NLS-1$ //$NON-NLS-2$
    lbl.addStyleName("nowrap"); //$NON-NLS-1$
    setWidget(5, 0, lbl);
    setWidget(5, 1, lastModifiedDateLabel);
    init();
  }

  public void apply() {
  }

  public void init() {
//    fileSummary = file;
//    // possibly hit server to pull all this data
//    if (fileInfo == null) {
//      populateUIFromServer();
//    } else {
//      nameLabel.setText(fileInfo.localizedName);
//      locationLabel.setText(fileInfo.solution+fileInfo.path);
//      sourceLabel.setText(fileInfo.solution+fileInfo.path+"/"+fileInfo.name); //$NON-NLS-1$
//      this.typeLabel.setText(getFileTypeDescription(fileInfo.type, fileInfo.pluginTypeName));
//      NumberFormat numberFormat = NumberFormat.getDecimalFormat();
//      sizeLabel.setText(numberFormat.format(fileInfo.size/1000.00)+" KB"); //$NON-NLS-1$
//      lastModifiedDateLabel.setText(fileInfo.lastModifiedDate.toString());
//    }
    nameLabel.setText(fileSummary.getTitle());
    locationLabel.setText(fileSummary.getPath().substring(0, fileSummary.getPath().lastIndexOf("/")));
    sourceLabel.setText(fileSummary.getName());
    typeLabel.setText(fileSummary.getName().substring(fileSummary.getName().lastIndexOf(".")));
      NumberFormat numberFormat = NumberFormat.getDecimalFormat();
    sizeLabel.setText(numberFormat.format(fileSummary.getFileSize()/1000.00)+" KB");
    // TODO Need to find out why last modified date is coming in as null
    lastModifiedDateLabel.setText(fileSummary.getLastModifiedDate() != null ? fileSummary.getLastModifiedDate().toString() : "");
    }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.solutionbrowser.fileproperties.IFileModifier#init(org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto, com.google.gwt.xml.client.Document)
   */
  @Override
  public void init(RepositoryFile fileSummary, Document fileInfo) {
    // TODO Auto-generated method stub
    
  }
  
  /*
  private String getFileTypeDescription(SolutionFileInfo.Type type, String pluginTypeName){
    switch(type){
      case FOLDER:
        return Messages.getString("folder"); //$NON-NLS-1$
      case ANALYSIS_VIEW:
        return Messages.getString("analysisView"); //$NON-NLS-1$
      case XACTION:
        return Messages.getString("xaction"); //$NON-NLS-1$
      case URL:
        return "URL";   //$NON-NLS-1$
      case REPORT:
        return Messages.getString("report"); //$NON-NLS-1$
      case PLUGIN:
        return pluginTypeName;
      default:
        return ""; //$NON-NLS-1$
    }
  }
  */

//  public void populateUIFromServer() {
//    AbstractCommand getSolutionFileCmd = new AbstractCommand() {
//
//      private void getFileInfo() {
//        AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {
//
//          public void onFailure(Throwable caught) {
//            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
//            dialogBox.center();
//          }
//
//          public void onSuccess(SolutionFileInfo fileInfo) {
//            init(fileSummary, fileInfo);
//          }
//        };
//        MantleServiceCache.getService().getSolutionFileInfo(fileSummary.getSolution(), fileSummary.getPath(), fileSummary.getName(), callback);
//      }
//      
//      protected void performOperation() {
//        getFileInfo();
//      }
//
//      protected void performOperation(boolean feedback) {
//        getFileInfo();
//      }
//      
//    };
//    getSolutionFileCmd.execute();       
//  }

}
