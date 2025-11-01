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


package org.pentaho.platform.web.http.api.resources;

public class LocalizedLogicalRoleName {

  String roleName;
  String localizedName;

  public LocalizedLogicalRoleName() {

  }

  public LocalizedLogicalRoleName( String roleName, String localizedName ) {
    this.roleName = roleName;
    this.localizedName = localizedName;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName( String roleName ) {
    this.roleName = roleName;
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public void setLocalizedName( String localizedName ) {
    this.localizedName = localizedName;
  }

}
