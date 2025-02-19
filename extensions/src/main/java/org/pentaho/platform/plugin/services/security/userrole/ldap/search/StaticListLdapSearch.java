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
 * Returns the list configured via the <code>staticList</code> property. (Makes no connection to a directory.) One way
 * to use this class would be to use it along with <code>UnionizingLdapSearch</code> to return a list of additional
 * roles or usernames. This is useful when you want to use the Admin Permissions interface to assign ACLs but the role
 * or username that you wish to use is not actually present in the directory (e.g. <code>Anonymous</code>).
 * 
 * @deprecated Use org.pentaho.platform.plugin.services.security.userrole.ExtraRolesUserRoleListServiceDecorator
 * @author mlowery
 */
public class StaticListLdapSearch implements LdapSearch {

  private List staticList = Collections.EMPTY_LIST;

  public List search( final Object[] ignored ) {
    return staticList;
  }

  public void setStaticList( final List staticList ) {
    if ( null == staticList ) {
      this.staticList = Collections.EMPTY_LIST;
    } else {
      this.staticList = staticList;
    }
  }

  public List getStaticList() {
    return staticList;
  }

}
