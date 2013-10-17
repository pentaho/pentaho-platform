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

import java.util.List;

/**
 * Executes a search against a directory context using the given filter arguments plus other static search parameters
 * (in the form of instance variables) known at deploy time.
 * 
 * <p>
 * Can also be seen as a generalization of <code>org.springframework.security.ldap.LdapUserSearch</code>.
 * </p>
 * 
 * @author mlowery
 * @see javax.naming.directory.DirContext.search()
 */
public interface LdapSearch {

  /**
   * Executes a search against a directory context using the given filter arguments.
   * 
   * @param filterArgs
   *          the filter arguments
   * @return the result set as a list
   */
  List search( Object[] filterArgs );
}
