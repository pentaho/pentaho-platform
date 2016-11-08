/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
