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


package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginOperation;

import java.util.ArrayList;
import java.util.List;

public class ContentInfo implements IContentInfo {

  private String description;

  private String extension;

  private String mimeType;

  private String title;

  private List<IPluginOperation> operations = new ArrayList<IPluginOperation>();

  private String iconUrl;

  private boolean canImport;

  private boolean canExport;

  public void setIconUrl( String iconUrl ) {
    this.iconUrl = iconUrl;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension( String extension ) {
    this.extension = extension;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public List<IPluginOperation> getOperations() {
    return operations;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public void addOperation( IPluginOperation operation ) {
    operations.add( operation );
  }

  public boolean isCanImport() {
    return canImport;
  }

  public void setCanImport( boolean canImport ) {
    this.canImport = canImport;
  }

  public boolean isCanExport() {
    return canExport;
  }

  public void setCanExport( boolean canExport ) {
    this.canExport = canExport;
  }

  @Override
  public boolean canExport() {
    return canExport;
  }

  @Override
  public boolean canImport() {
    return canImport;
  }

}
