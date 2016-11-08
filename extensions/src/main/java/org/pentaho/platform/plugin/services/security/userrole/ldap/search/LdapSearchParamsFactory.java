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

/**
 * A factory for creating <code>LdapSearchParams</code> instances.
 * 
 * @author mlowery
 * 
 */
public interface LdapSearchParamsFactory {

  /**
   * Create a parameters object with the given arguments. The assumption is that the filter arguments will be the only
   * parameter not known until runtime.
   * 
   * @param filterArgs
   *          arguments that will be merged with the <code>filter</code> property of an <code>LdapSearchParams</code>
   *          instance
   * @return a new parameters object
   */
  LdapSearchParams createParams( Object[] filterArgs );

}
