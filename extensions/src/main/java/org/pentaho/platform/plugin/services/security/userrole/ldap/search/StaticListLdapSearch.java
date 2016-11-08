/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
