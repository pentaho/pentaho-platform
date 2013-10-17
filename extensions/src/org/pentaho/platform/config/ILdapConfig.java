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

package org.pentaho.platform.config;

public interface ILdapConfig {
  public void setProviderUrl( String url );

  public String getProviderUrl();

  public void setUserDn( String userDn );

  public String getUserDn();

  public void setProviderPassword( String password );

  public String getProviderPassword();

  public boolean getConvertUserRolesToUpperCase();

  public void setConvertUserRolesToUpperCase( boolean convert );

  public String getUserRolesAttribute();

  public void setUserRolesAttribute( String attr );

  public String getUserRolesSearchBase();

  public void setUserRolesSearchBase( String base );

  public String getUserRolesSearchFilter();

  public void setUserRolesSearchFilter( String filter );

  public String getUserRolesPrefix();

  public void setUserRolesPrefix( String prefix );

  public boolean getSearchSubtreeForUserRoles();

  public void setSearchSubtreeForUserRoles( boolean searchSubtree );

  public String getAllRolesSearchBase();

  public void setAllRolesSearchBase( String base );

  public String getAllRolesSearchFilter();

  public void setAllRolesSearchFilter( String filter );

  public String getAllRolesAttribute();

  public void setAllRolesAttribute( String attr );

  public String getUserSearchBase();

  public void setUserSearchBase( String base );

  public String getUserSearchFilter();

  public void setUserSearchFilter( String filter );

}
