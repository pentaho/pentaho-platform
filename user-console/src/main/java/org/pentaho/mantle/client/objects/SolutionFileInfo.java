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


package org.pentaho.mantle.client.objects;

import org.pentaho.mantle.client.solutionbrowser.IFileSummary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class SolutionFileInfo implements Serializable, IFileSummary {
  private static final long serialVersionUID = 5180921684949664558L;
  public String path;
  public String name;
  public String localizedName;
  public Date lastModifiedDate;
  public long size;
  public byte[] data;
  public Type type;
  public String pluginTypeName;
  public boolean isDirectory = false;
  public boolean supportsAccessControls = true;
  public boolean canEffectiveUserManage = false;

  public enum Type {
    REPORT, XACTION, URL, ANALYSIS_VIEW, PLUGIN, FOLDER
  };

  public ArrayList<UserPermission> userPermissions;
  public ArrayList<RolePermission> rolePermissions;

  public SolutionFileInfo() {
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate( Date lastModifiedDate ) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public long getSize() {
    return size;
  }

  public void setSize( long size ) {
    this.size = size;
  }

  public byte[] getData() {
    return data;
  }

  public void setData( byte[] data ) {
    this.data = data;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory( boolean isDirectory ) {
    this.isDirectory = isDirectory;
  }

  public boolean isSupportsAccessControls() {
    return supportsAccessControls;
  }

  public void setSupportsAccessControls( boolean supportsAccessControls ) {
    this.supportsAccessControls = supportsAccessControls;
  }

  public ArrayList<UserPermission> getUserPermissions() {
    return userPermissions;
  }

  public void setUserPermissions( ArrayList<UserPermission> userPermissions ) {
    this.userPermissions = userPermissions;
  }

  public ArrayList<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions( ArrayList<RolePermission> rolePermissions ) {
    this.rolePermissions = rolePermissions;
  }

  public Type getType() {
    return type;
  }

  public void setType( Type type ) {
    this.type = type;
  }

  public boolean isCanEffectiveUserManage() {
    return canEffectiveUserManage;
  }

  public void setCanEffectiveUserManage( boolean canEffectiveUserManage ) {
    this.canEffectiveUserManage = canEffectiveUserManage;
  }

  public String getLocalizedName() {

    return localizedName;
  }

  public void setLocalizedName( String localizedName ) {

    this.localizedName = localizedName;
  }

}
