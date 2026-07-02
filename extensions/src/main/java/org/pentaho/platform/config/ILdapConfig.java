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
