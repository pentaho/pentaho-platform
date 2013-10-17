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
