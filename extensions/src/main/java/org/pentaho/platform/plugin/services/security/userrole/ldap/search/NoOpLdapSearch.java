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


package org.pentaho.platform.plugin.services.security.userrole.ldap.search;

import java.util.Collections;
import java.util.List;

/**
 * Immediately returns an empty list without connecting to a server.
 * 
 * <p>
 * This is useful when you do not wish to implement one of the searches of <code>DefaultLdapUserRoleLIstService</code>
 * --most often the all usernames search.
 * </p>
 * 
 * @author mlowery
 */
public class NoOpLdapSearch implements LdapSearch {

  public List search( final Object[] filterArgs ) {
    return Collections.EMPTY_LIST;
  }

}
