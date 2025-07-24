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

public class UserComparator implements Comparator<ProxyPentahoUser> {

  public int compare( ProxyPentahoUser user1, ProxyPentahoUser user2 ) {
    String userName1 = user1.getName().toLowerCase();
    String userName2 = user2.getName().toLowerCase();

    int result = userName1.compareTo( userName2 );
    if ( result == 0 ) {
      result = user1.getName().compareTo( user2.getName() );
    }

    return result;
  }

}
