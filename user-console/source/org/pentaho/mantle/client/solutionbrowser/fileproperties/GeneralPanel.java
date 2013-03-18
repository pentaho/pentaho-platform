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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.commands.RestoreFileCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 *
 */
public class GeneralPanel extends FlexTable implements IFileModifier {

  Label nameLabel = new Label();
  Label locationLabel = new Label();
  Label sourceLabel = new Label();
  Label typeLabel = new Label();
  Label sizeLabel = new Label();
  Label createdLabel = new Label();
  Label lastModifiedDateLabel = new Label();
  Label deletedDateLabel = new Label();
  Label originalLocationLabel = new Label();
  Label ownerLabel = new Label();
  
//  IFileSummary fileSummary;
  RepositoryFile fileSummary;

  boolean isInTrash;
  boolean dirty = false;

  ArrayList<JSONObject> metadataPerms = new ArrayList<JSONObject>();
  VerticalPanel metadataPermsPanel = new VerticalPanel();

  private static final String METADATA_PERM_PREFIX = "_PERM_"; //$NON-NLS-1$
  private static final String OWNER_NAME_ELEMENT_NAME = "owner"; //$NON-NLS-1$

  /**
   *
   * @param dialog
   * @param fileSummary
   */
  public GeneralPanel(final FilePropertiesDialog dialog, final RepositoryFile fileSummary) {
    super();
    this.fileSummary = fileSummary;
    isInTrash = this.fileSummary.getPath().contains("/.trash/pho:"); //$NON-NLS-1$
    setWidget(0, 0, new Label(Messages.getString("name")+":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(0, 1, nameLabel);

    setWidget(1, 0, new Label(Messages.getString("type") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(1, 1, typeLabel);

    addHr(2, 0, 2);

    setWidget(3, 0, new Label(Messages.getString("owner") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(3, 1, ownerLabel);

    setWidget(4, 0, new Label(Messages.getString("location") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(4, 1, locationLabel);

    setWidget(5, 0, new Label(Messages.getString("size") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(5, 1, sizeLabel);

    addHr(6, 0, 2);

    setWidget(7, 0, new Label(Messages.getString("created") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(7, 1, createdLabel);
    
    setWidget(8, 0, new Label(Messages.getString("lastModified") + ":")); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(8, 1, lastModifiedDateLabel);

    addHr(9, 0, 2);
    
    if (isInTrash) {
      setWidget(10, 0, new Label(Messages.getString("dateDeleted") + ":"));  //$NON-NLS-1$//$NON-NLS-2$
      setWidget(10, 1, deletedDateLabel);
      
    Label lbl = new Label(Messages.getString("originalLocation") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
    lbl.addStyleName("nowrap"); //$NON-NLS-1$     
      setWidget(11, 0, lbl);
      setWidget(11, 1, originalLocationLabel);
      
      Button restoreButton = new Button("Restore");
      restoreButton.setStylePrimaryName("pentaho-button");
      restoreButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          List<RepositoryFile> restoreList = new ArrayList<RepositoryFile>();
          restoreList.add(fileSummary);
          new RestoreFileCommand(restoreList).execute();
          dialog.hide();
        }
        
      });
      setWidget(12, 3, restoreButton);

      addHr(13, 0, 2);
    }

    setWidget(13, 0, metadataPermsPanel);

    setCellPadding(2);
    setCellSpacing(2);

    init();
  }

  /**
   *
   */
  public void apply() {
    // not used
  }

  public List<RequestBuilder> prepareRequests(){
    ArrayList<RequestBuilder> requestBuilders = new ArrayList<RequestBuilder>();
      String moduleBaseURL = GWT.getModuleBaseURL();
      String moduleName = GWT.getModuleName();
      String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
      String setMetadataUrl = contextURL + "api/repo/files/" + SolutionBrowserPanel.pathToId(fileSummary.getPath()) + "/metadata"; //$NON-NLS-1$//$NON-NLS-2$
      RequestBuilder setMetadataBuilder = new RequestBuilder(RequestBuilder.PUT, setMetadataUrl);
      setMetadataBuilder.setHeader("Content-Type", "application/json");

      // prepare request data
          JSONArray arr = new JSONArray();
          JSONObject metadata = new JSONObject();
          metadata.put("stringKeyStringValueDto", arr);
          for (int i = 0; i < metadataPerms.size(); i++) {
              Set<String> keys = metadataPerms.get(i).keySet();
              for (String key : keys) {
                  JSONObject obj = new JSONObject();
                  obj.put("key", new JSONString(key));
                  obj.put("value", metadataPerms.get(i).get(key).isString());
                  arr.set(i, obj);
              }
          }
          // setMetadataBuilder.sendRequest(metadata.toString(), setMetadataCallback);
       setMetadataBuilder.setRequestData(metadata.toString());
       requestBuilders.add(setMetadataBuilder);

    return requestBuilders;
  }

  /**
   *
   */
  public void init() {
    nameLabel.setText(fileSummary.getTitle());
    typeLabel.setText(fileSummary.isFolder() ? Messages.getString("folder") : fileSummary.getName().substring(fileSummary.getName().lastIndexOf(".")));  //$NON-NLS-1$//$NON-NLS-2$
    locationLabel.setText(isInTrash ? Messages.getString("recycleBin") : fileSummary.getPath().substring(0, fileSummary.getPath().lastIndexOf("/")));  //$NON-NLS-1$//$NON-NLS-2$
    sizeLabel.setText(NumberFormat.getDecimalFormat().format(fileSummary.getFileSize()/1000.00)+" " + Messages.getString("kiloBytes")); //$NON-NLS-1$ //$NON-NLS-2$
    createdLabel.setText(fileSummary.getCreatedDate().toString());
    lastModifiedDateLabel.setText(fileSummary.getLastModifiedDate() == null ? fileSummary.getCreatedDate().toString() : fileSummary.getLastModifiedDate().toString());
    deletedDateLabel.setText(fileSummary.getDeletedDate() == null ? "" : fileSummary.getDeletedDate().toString()); //$NON-NLS-1$
    originalLocationLabel.setText(fileSummary.getOriginalParentFolderPath());
  }

  /* (non-Javadoc)
   * @see org.pentaho.mantle.client.solutionbrowser.fileproperties.IFileModifier#init(org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto, com.google.gwt.xml.client.Document)
   */
  @Override
  public void init(RepositoryFile fileSummary, Document fileInfo) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Add an hr element with a specified colspan
   * @param row
   * @param col
   */
  @SuppressWarnings("serial")
  protected void addHr(int row, int col, int colspan){
    setHTML(row, col, new SafeHtml() {
      @Override
      public String asString() {
        return "<hr/>";
      }
    });
    getFlexCellFormatter().setColSpan(row, col, colspan);
  }

  /**
   * Accept metadata response object and parse for use in General panel
   * @param response
   */
  protected void setMetadataResponse(Response response){
    if (SolutionBrowserPanel.getInstance().isAdministrator() && !fileSummary.isFolder()) {
      JSONObject json = (JSONObject) JSONParser.parseLenient(response.getText());
      if (json != null) {
        JSONArray arr = (JSONArray) json.get("stringKeyStringValueDto");
        for (int i = 0; i < arr.size(); i++) {
          JSONValue arrVal = arr.get(i);
          String key = arrVal.isObject().get("key").isString().stringValue();
          String value = arrVal.isObject().get("value").isString().stringValue();
          if (key.startsWith(METADATA_PERM_PREFIX)) {
            JSONObject nv = new JSONObject();
            nv.put(key, new JSONString(value));
            metadataPerms.add(nv);
          }
        }
        for (final JSONObject nv : metadataPerms) {
          Set<String> keys = nv.keySet();
          for (final String key : keys) {
            final CheckBox cb = new CheckBox(Messages.getString(key.substring(METADATA_PERM_PREFIX.length()).toLowerCase()));
            cb.setValue(Boolean.parseBoolean(nv.get(key).isString().stringValue()));
            cb.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                dirty = true;
                nv.put(key, new JSONString(cb.getValue().toString()));
              }
            });
            metadataPermsPanel.add(cb);
          }
        }
      }
    }
  }

  /**
   * Get owner name from acl response
   * @param response
   */
  protected void setAclResponse(Response response){
    Document permissions = XMLParser.parse(response.getText());
    ownerLabel.setText(permissions.getElementsByTagName(OWNER_NAME_ELEMENT_NAME).item(0).getFirstChild().getNodeValue());
  }
}
