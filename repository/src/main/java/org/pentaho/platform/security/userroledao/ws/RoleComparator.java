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


package org.pentaho.platform.security.userroledao.ws;

import java.util.Comparator;

public class RoleComparator implements Comparator<ProxyPentahoRole> {

  public int compare( ProxyPentahoRole role1, ProxyPentahoRole role2 ) {

    String userName1 = role1.getName().toLowerCase();
    String userName2 = role2.getName().toLowerCase();

    int result = userName1.compareTo( userName2 );
    if ( result == 0 ) {
      result = role1.getName().compareTo( role2.getName() );
    }

    return result;
  }

}
