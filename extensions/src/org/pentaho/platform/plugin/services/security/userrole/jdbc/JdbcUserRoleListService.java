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

package org.pentaho.platform.plugin.services.security.userrole.jdbc;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserRoleListService extends JdbcDaoSupport implements IUserRoleListService {

  // ~ Static fields/initializers
  // =============================================
  public static final String DEF_ALL_AUTHORITIES_QUERY = "SELECT distinct(authority) as authority FROM authorities"; //$NON-NLS-1$

  public static final String DEF_ALL_USERNAMES_QUERY = "SELECT distinct(username) as username FROM users"; //$NON-NLS-1$

  public static final String DEF_ALL_USERNAMES_IN_ROLE_QUERY =
      "SELECT distinct(username) as username FROM authorities where authority = ?"; //$NON-NLS-1$

  // ~ Instance fields
  // ========================================================

  protected MappingSqlQuery allAuthoritiesMapping;

  protected MappingSqlQuery allUsernamesMapping;

  protected MappingSqlQuery allUsernamesInRoleMapping;

  private String allAuthoritiesQuery;

  private String allUsernamesQuery;

  private String allUsernamesInRoleQuery;

  private UserDetailsService userDetailsService;

  private String rolePrefix;

  private List<String> systemRoles;

  private IAuthenticationRoleMapper roleMapper;

  // ~ Constructors
  // ===========================================================

  public JdbcUserRoleListService( final UserDetailsService userDetailsService, final List<String> systemRoles ) {
    allAuthoritiesQuery = JdbcUserRoleListService.DEF_ALL_AUTHORITIES_QUERY;
    allUsernamesQuery = JdbcUserRoleListService.DEF_ALL_USERNAMES_QUERY;
    allUsernamesInRoleQuery = JdbcUserRoleListService.DEF_ALL_USERNAMES_IN_ROLE_QUERY;
    this.userDetailsService = userDetailsService;
    this.systemRoles = systemRoles;
  }

  // ~ Methods
  // ================================================================

  /**
   * Allows the default query string used to retrieve all authorities to be overriden, if default table or column names
   * need to be changed. The default query is {@link #DEF_ALL_AUTHORITIES_QUERY}; when modifying this query, ensure that
   * all returned columns are mapped back to the same column names as in the default query.
   * 
   * @param queryString
   *          The query string to set
   */
  public void setAllAuthoritiesQuery( final String queryString ) {
    allAuthoritiesQuery = queryString;
  }

  public String getAllAuthoritiesQuery() {
    return allAuthoritiesQuery;
  }

  /**
   * Allows the default query string used to retrieve all user names in a role to be overriden, if default table or
   * column names need to be changed. The default query is {@link #DEF_ALL_USERS_QUERY}; when modifying this query,
   * ensure that all returned columns are mapped back to the same column names as in the default query.
   * 
   * @param queryString
   *          The query string to set
   */
  public void setAllUsernamesInRoleQuery( final String queryString ) {
    allUsernamesInRoleQuery = queryString;
  }

  public String getAllUsernamesInRoleQuery() {
    return allUsernamesInRoleQuery;
  }

  /**
   * Allows the default query string used to retrieve all user names to be overriden, if default table or column names
   * need to be changed. The default query is {@link #DEF_ALL_USERS_IN_ROLE_QUERY}; when modifying this query, ensure
   * that all returned columns are mapped back to the same column names as in the default query.
   * 
   * @param queryString
   *          The query string to set
   */
  public void setAllUsernamesQuery( final String queryString ) {
    allUsernamesQuery = queryString;
  }

  public String getAllUsernamesQuery() {
    return allUsernamesQuery;
  }

  public List<String> getAllRoles() throws DataAccessException {
    List<GrantedAuthority> allAuths = allAuthoritiesMapping.execute();
    List<String> roles = new ArrayList<String>( allAuths.size() );
    for ( GrantedAuthority role : allAuths ) {
      if ( roleMapper != null ) {
        roles.add( roleMapper.toPentahoRole( role.getAuthority() ) );
      } else {
        roles.add( role.getAuthority() );
      }

    }
    return roles;
  }

  public List<String> getAllUsers() throws DataAccessException {
    List<String> allUserNames = allUsernamesMapping.execute();
    return allUserNames;
  }

  public List<String> getUsersInRole( final String role ) {

    String roleToTest = role;

    if ( roleMapper != null ) {
      roleToTest = roleMapper.fromPentahoRole( role );
    }

    List<String> allUserNamesInRole = allUsernamesInRoleMapping.execute( roleToTest );

    return allUserNamesInRole;
  }

  @Override
  protected void initDao() throws ApplicationContextException {
    initMappingSqlQueries();
  }

  /**
   * Extension point to allow other MappingSqlQuery objects to be substituted in a subclass
   */
  protected void initMappingSqlQueries() {
    this.allAuthoritiesMapping = new AllAuthoritiesMapping( getDataSource() );
    this.allUsernamesInRoleMapping = new AllUserNamesInRoleMapping( getDataSource() );
    this.allUsernamesMapping = new AllUserNamesMapping( getDataSource() );
  }

  // ~ Inner Classes
  // ==========================================================

  /**
   * Query object to look up all users.
   */
  protected class AllUserNamesMapping extends MappingSqlQuery {
    protected AllUserNamesMapping( final DataSource ds ) {
      super( ds, allUsernamesQuery );
      compile();
    }

    @Override
    protected Object mapRow( final ResultSet rs, final int rownum ) throws SQLException {
      return rs.getString( 1 );
    }
  }

  /**
   * Query object to look up users in a role.
   */
  protected class AllUserNamesInRoleMapping extends MappingSqlQuery {
    protected AllUserNamesInRoleMapping( final DataSource ds ) {
      super( ds, allUsernamesInRoleQuery );
      declareParameter( new SqlParameter( Types.VARCHAR ) );
      compile();
    }

    @Override
    protected Object mapRow( final ResultSet rs, final int rownum ) throws SQLException {
      return rs.getString( 1 );
    }
  }

  /**
   * Query object to look up all authorities.
   */
  protected class AllAuthoritiesMapping extends MappingSqlQuery {
    protected AllAuthoritiesMapping( final DataSource ds ) {
      super( ds, allAuthoritiesQuery );
      compile();
    }

    @Override
    protected Object mapRow( final ResultSet rs, final int rownum ) throws SQLException {
      return new GrantedAuthorityImpl( ( ( null != rolePrefix ) ? rolePrefix : "" ) + rs.getString( 1 ) ); //$NON-NLS-1$
    }
  }

  public List<String> getRolesForUser( final String username ) throws UsernameNotFoundException, DataAccessException {
    UserDetails user = userDetailsService.loadUserByUsername( username );
    List<String> roles = new ArrayList<String>( user.getAuthorities().length );
    for ( GrantedAuthority role : user.getAuthorities() ) {
      if ( roleMapper != null ) {
        roles.add( roleMapper.toPentahoRole( role.getAuthority() ) );
      } else {
        roles.add( role.getAuthority() );
      }
    }

    return roles;
  }

  public void setRolePrefix( final String rolePrefix ) {
    this.rolePrefix = rolePrefix;
  }

  public void setUserDetailsService( final UserDetailsService userDetailsService ) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    return getAllRoles();
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    return getAllUsers();
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String role ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    return getUsersInRole( role );
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    if ( tenant != null && !tenant.equals( JcrTenantUtils.getDefaultTenant() ) ) {
      throw new UnsupportedOperationException( "only allowed to access to default tenant" );
    }
    return getRolesForUser( username );
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }

  public void setRoleMapper( IAuthenticationRoleMapper roleMapper ) {
    this.roleMapper = roleMapper;
  }
}
