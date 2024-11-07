/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.sql.DataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class JdbcUserRoleListService extends JdbcDaoSupport implements IUserRoleListService {

  // ~ Static fields/initializers
  // =============================================
  public static final String DEF_ALL_AUTHORITIES_QUERY = "SELECT distinct(authority) as authority FROM authorities ORDER BY authority"; //$NON-NLS-1$

  public static final String DEF_ALL_USERNAMES_QUERY = "SELECT distinct(username) as username FROM users ORDER BY username"; //$NON-NLS-1$

  public static final String DEF_ALL_USERNAMES_IN_ROLE_QUERY =
      "SELECT distinct(username) as username FROM authorities WHERE authority = ?"; //$NON-NLS-1$

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

  private List<String> extraRoles;

  private IAuthenticationRoleMapper roleMapper;

  // ~ Constructors
  // ===========================================================

  public JdbcUserRoleListService( final UserDetailsService userDetailsService, final List<String> systemRoles ) {
    allAuthoritiesQuery = JdbcUserRoleListService.DEF_ALL_AUTHORITIES_QUERY;
    allUsernamesQuery = JdbcUserRoleListService.DEF_ALL_USERNAMES_QUERY;
    allUsernamesInRoleQuery = JdbcUserRoleListService.DEF_ALL_USERNAMES_IN_ROLE_QUERY;
    this.userDetailsService = userDetailsService;
    this.systemRoles = systemRoles;
    this.extraRoles = PentahoSystem.get( ArrayList.class, "extraSystemAuthorities", PentahoSessionHolder.getSession() );
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
   * column names need to be changed. The default query is {@link #DEF_ALL_USERNAMES_QUERY}; when modifying this query,
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
   * need to be changed. The default query is {@link #DEF_ALL_USERNAMES_IN_ROLE_QUERY}; when modifying this query,
   * ensure that all returned columns are mapped back to the same column names as in the default query.
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
    LinkedHashSet<String> roles = new LinkedHashSet<String>( allAuths.size() );
    for ( GrantedAuthority role : allAuths ) {
      if ( roleMapper != null ) {
        roles.add( roleMapper.toPentahoRole( role.getAuthority() ) );
      } else {
        roles.add( role.getAuthority() );
      }
    }
    return new ArrayList<>( addExtraRoles( roles ) );
  }

  private LinkedHashSet<String> addExtraRoles( LinkedHashSet<String> roles ) {
    if ( extraRoles == null ) {
      return roles;
    }
    // Now add extra role if it does not exist in the list
    for ( String extraRole : extraRoles ) {
      roles.add( extraRole );
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
      return new SimpleGrantedAuthority( ( ( null != rolePrefix ) ? rolePrefix : "" ) + rs.getString( 1 ) ); //$NON-NLS-1$
    }
  }

  public List<String> getRolesForUser( final String username ) throws UsernameNotFoundException, DataAccessException {
    UserDetails user = userDetailsService.loadUserByUsername( username );
    LinkedHashSet<String> roles = new LinkedHashSet<String>( user.getAuthorities().size() );
    for ( GrantedAuthority role : user.getAuthorities() ) {
      if ( roleMapper != null ) {
        roles.add( roleMapper.toPentahoRole( role.getAuthority() ) );
      } else {
        roles.add( role.getAuthority() );
      }
    }

    return new ArrayList<>( addExtraRoles( roles ) );
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
