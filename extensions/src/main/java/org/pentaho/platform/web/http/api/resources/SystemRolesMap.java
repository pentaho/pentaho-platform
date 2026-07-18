/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
public class SystemRolesMap extends LogicalRoleAssignments {

  ArrayList<LocalizedLogicalRoleName> localizedRoleNames = new ArrayList<LocalizedLogicalRoleName>();

  public ArrayList<LocalizedLogicalRoleName> getLocalizedRoleNames() {
    return localizedRoleNames;
  }

  public void setLocalizedRoleNames( ArrayList<LocalizedLogicalRoleName> localizedRoleNames ) {
    if ( localizedRoleNames != this.localizedRoleNames ) {
      this.localizedRoleNames.clear();
      if ( localizedRoleNames != null ) {
        this.localizedRoleNames.addAll( localizedRoleNames );
      }
    }
  }

}
