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

import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LdapConfigProperties implements ILdapConfig {
  Properties properties;

  public static final String PROVIDER_URL_KEY = "contextSource.providerUrl";
  public static final String USER_DN_KEY = "contextSource.userDn";
  public static final String PROVIDER_PASSWORD_KEY = "contextSource.password";
  public static final String USER_SEARCH_BASE_KEY = "userSearch.searchBase";
  public static final String USER_SEARCH_FILTER_KEY = "userSearch.searchFilter";
  public static final String CONVERT_USER_ROLES_TO_UPPERCASE_KEY = "populator.convertToUpperCase";
  public static final String USER_ROLE_ATTRIBUTE_KEY = "populator.groupRoleAttribute";
  public static final String USER_ROLE_SEARCH_BASE_KEY = "populator.groupSearchBase";
  public static final String USER_ROLE_SEARCH_FILTER_KEY = "populator.groupSearchFilter";
  public static final String USER_ROLE_PREFIX_KEY = "populator.rolePrefix";
  public static final String SEARCH_SUBTREE_FOR_USER_ROLES_KEY = "populator.searchSubtree";
  public static final String ALL_ROLES_ATTRIBUTE_KEY = "allAuthoritiesSearch.roleAttribute";
  public static final String ALL_ROLES_SEARCH_BASE_KEY = "allAuthoritiesSearch.searchBase";
  public static final String ALL_ROLES_SEARCH_FILTER_KEY = "allAuthoritiesSearch.searchFilter";

  public LdapConfigProperties( File propertiesFile ) throws IOException {
    Properties props = new Properties();
    InputStream in = new FileInputStream( propertiesFile );
    props.load( in );
    in.close();
    properties = props;
  }

  public LdapConfigProperties( Properties properties ) throws DocumentException {
    this.properties = properties;
  }

  public LdapConfigProperties() {
    properties = new Properties();
  }

  public Properties getProperties() {
    return properties;
  }

  private String getProperty( String name ) {
    return properties.getProperty( name );
  }

  private void setProperty( String name, String value ) {
    if ( value == null ) {
      properties.remove( name );
    } else {
      properties.setProperty( name, value );
    }
  }

  public String getAllRolesAttribute() {
    return getProperty( ALL_ROLES_ATTRIBUTE_KEY ); //$NON-NLS-1$
  }

  public String getAllRolesSearchBase() {
    return getProperty( ALL_ROLES_SEARCH_BASE_KEY ); //$NON-NLS-1$
  }

  public String getAllRolesSearchFilter() {
    return getProperty( ALL_ROLES_SEARCH_FILTER_KEY ); //$NON-NLS-1$
  }

  public boolean getConvertUserRolesToUpperCase() {
    return Boolean.parseBoolean( getProperty( CONVERT_USER_ROLES_TO_UPPERCASE_KEY ) ); //$NON-NLS-1$
  }

  public String getProviderPassword() {
    return getProperty( PROVIDER_PASSWORD_KEY ); //$NON-NLS-1$
  }

  public String getProviderUrl() {
    return getProperty( PROVIDER_URL_KEY ); //$NON-NLS-1$
  }

  public boolean getSearchSubtreeForUserRoles() {
    return Boolean.parseBoolean( getProperty( SEARCH_SUBTREE_FOR_USER_ROLES_KEY ) ); //$NON-NLS-1$
  }

  public String getUserDn() {
    return getProperty( USER_DN_KEY ); //$NON-NLS-1$
  }

  public String getUserRolesAttribute() {
    return getProperty( USER_ROLE_ATTRIBUTE_KEY ); //$NON-NLS-1$
  }

  public String getUserRolesPrefix() {
    return getProperty( USER_ROLE_PREFIX_KEY ); //$NON-NLS-1$
  }

  public String getUserRolesSearchBase() {
    return getProperty( USER_ROLE_SEARCH_BASE_KEY ); //$NON-NLS-1$
  }

  public String getUserRolesSearchFilter() {
    return getProperty( USER_ROLE_SEARCH_FILTER_KEY ); //$NON-NLS-1$
  }

  public String getUserSearchBase() {
    return getProperty( USER_SEARCH_BASE_KEY ); //$NON-NLS-1$
  }

  public String getUserSearchFilter() {
    return getProperty( USER_SEARCH_FILTER_KEY ); //$NON-NLS-1$
  }

  public void setAllRolesAttribute( String attr ) {
    setProperty( ALL_ROLES_ATTRIBUTE_KEY, attr ); //$NON-NLS-1$
  }

  public void setAllRolesSearchBase( String base ) {
    setProperty( ALL_ROLES_SEARCH_BASE_KEY, base ); //$NON-NLS-1$
  }

  public void setAllRolesSearchFilter( String filter ) {
    setProperty( ALL_ROLES_SEARCH_FILTER_KEY, filter ); //$NON-NLS-1$
  }

  public void setConvertUserRolesToUpperCase( boolean convert ) {
    setProperty( CONVERT_USER_ROLES_TO_UPPERCASE_KEY, Boolean.toString( convert ) ); //$NON-NLS-1$
  }

  public void setProviderPassword( String password ) {
    setProperty( PROVIDER_PASSWORD_KEY, password ); //$NON-NLS-1$
  }

  public void setProviderUrl( String url ) {
    setProperty( PROVIDER_URL_KEY, url ); //$NON-NLS-1$
  }

  public void setSearchSubtreeForUserRoles( boolean searchSubtree ) {
    setProperty( SEARCH_SUBTREE_FOR_USER_ROLES_KEY, Boolean.toString( searchSubtree ) ); //$NON-NLS-1$
  }

  public void setUserDn( String userDn ) {
    setProperty( USER_DN_KEY, userDn ); //$NON-NLS-1$
  }

  public void setUserRolesAttribute( String attr ) {
    setProperty( USER_ROLE_ATTRIBUTE_KEY, attr ); //$NON-NLS-1$
  }

  public void setUserRolesPrefix( String prefix ) {
    setProperty( USER_ROLE_PREFIX_KEY, prefix ); //$NON-NLS-1$
  }

  public void setUserRolesSearchBase( String base ) {
    setProperty( USER_ROLE_SEARCH_BASE_KEY, base ); //$NON-NLS-1$
  }

  public void setUserRolesSearchFilter( String filter ) {
    setProperty( USER_ROLE_SEARCH_FILTER_KEY, filter ); //$NON-NLS-1$
  }

  public void setUserSearchBase( String base ) {
    setProperty( USER_SEARCH_BASE_KEY, base ); //$NON-NLS-1$
  }

  public void setUserSearchFilter( String filter ) {
    setProperty( USER_SEARCH_FILTER_KEY, filter ); //$NON-NLS-1$
  }
}
