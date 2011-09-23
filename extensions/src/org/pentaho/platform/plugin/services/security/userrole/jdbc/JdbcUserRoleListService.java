/*
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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.services.security.userrole.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.pentaho.platform.api.engine.IUserRoleListService;
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

public class JdbcUserRoleListService extends JdbcDaoSupport implements IUserRoleListService {

  // ~ Static fields/initializers
  // =============================================
  public static final String DEF_ALL_AUTHORITIES_QUERY = "SELECT distinct(authority) as authority FROM authorities"; //$NON-NLS-1$

  public static final String DEF_ALL_USERNAMES_QUERY = "SELECT distinct(username) as username FROM users"; //$NON-NLS-1$

  public static final String DEF_ALL_USERNAMES_IN_ROLE_QUERY = "SELECT distinct(username) as username FROM authorities where authority = ?"; //$NON-NLS-1$

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

  // ~ Constructors
  // ===========================================================

  public JdbcUserRoleListService(final UserDetailsService userDetailsService) {
    allAuthoritiesQuery = JdbcUserRoleListService.DEF_ALL_AUTHORITIES_QUERY;
    allUsernamesQuery = JdbcUserRoleListService.DEF_ALL_USERNAMES_QUERY;
    allUsernamesInRoleQuery = JdbcUserRoleListService.DEF_ALL_USERNAMES_IN_ROLE_QUERY;
    this.userDetailsService = userDetailsService;
  }

  // ~ Methods
  // ================================================================

  /**
   * Allows the default query string used to retrieve all authorities to be
   * overriden, if default table or column names need to be changed. The
   * default query is {@link #DEF_ALL_AUTHORITIES_QUERY}; when modifying this
   * query, ensure that all returned columns are mapped back to the same
   * column names as in the default query.
   *
   * @param queryString
   *            The query string to set
   */
  public void setAllAuthoritiesQuery(final String queryString) {
    allAuthoritiesQuery = queryString;
  }

  public String getAllAuthoritiesQuery() {
    return allAuthoritiesQuery;
  }

  /**
   * Allows the default query string used to retrieve all user names in a role
   * to be overriden, if default table or column names need to be changed. The
   * default query is {@link #DEF_ALL_USERS_QUERY}; when modifying this
   * query, ensure that all returned columns are mapped back to the same
   * column names as in the default query.
   *
   * @param queryString
   *            The query string to set
   */
  public void setAllUsernamesInRoleQuery(final String queryString) {
    allUsernamesInRoleQuery = queryString;
  }

  public String getAllUsernamesInRoleQuery() {
    return allUsernamesInRoleQuery;
  }

  /**
   * Allows the default query string used to retrieve all user names to be
   * overriden, if default table or column names need to be changed. The
   * default query is {@link #DEF_ALL_USERS_IN_ROLE_QUERY}; when modifying
   * this query, ensure that all returned columns are mapped back to the same
   * column names as in the default query.
   *
   * @param queryString
   *            The query string to set
   */
  public void setAllUsernamesQuery(final String queryString) {
    allUsernamesQuery = queryString;
  }

  public String getAllUsernamesQuery() {
    return allUsernamesQuery;
  }

  public List<String> getAllRoles() throws DataAccessException {
    List<GrantedAuthority> allAuths = allAuthoritiesMapping.execute();
    List<String> roles = new ArrayList<String>(allAuths.size());
    for (GrantedAuthority role : allAuths) {
      roles.add(role.getAuthority());
    }
    return roles;
  }

  public List<String> getAllUsers() throws DataAccessException {
    List<String> allUserNames = allUsernamesMapping.execute();
    return allUserNames;
  }

  public List<String> getUsersInRole(final String role) {
    List<String> allUserNamesInRole = allUsernamesInRoleMapping.execute(role);
    return allUserNamesInRole;
  }

  @Override
  protected void initDao() throws ApplicationContextException {
    initMappingSqlQueries();
  }

  /**
   * Extension point to allow other MappingSqlQuery objects to be substituted
   * in a subclass
   */
  protected void initMappingSqlQueries() {
    this.allAuthoritiesMapping = new AllAuthoritiesMapping(getDataSource());
    this.allUsernamesInRoleMapping = new AllUserNamesInRoleMapping(getDataSource());
    this.allUsernamesMapping = new AllUserNamesMapping(getDataSource());
  }

  // ~ Inner Classes
  // ==========================================================

  /**
   * Query object to look up all users.
   */
  protected class AllUserNamesMapping extends MappingSqlQuery {
    protected AllUserNamesMapping(final DataSource ds) {
      super(ds, allUsernamesQuery);
      compile();
    }

    @Override
    protected Object mapRow(final ResultSet rs, final int rownum) throws SQLException {
      return rs.getString(1);
    }
  }

  /**
   * Query object to look up users in a role.
   */
  protected class AllUserNamesInRoleMapping extends MappingSqlQuery {
    protected AllUserNamesInRoleMapping(final DataSource ds) {
      super(ds, allUsernamesInRoleQuery);
      declareParameter(new SqlParameter(Types.VARCHAR));
      compile();
    }

    @Override
    protected Object mapRow(final ResultSet rs, final int rownum) throws SQLException {
      return rs.getString(1);
    }
  }

  /**
   * Query object to look up all authorities.
   */
  protected class AllAuthoritiesMapping extends MappingSqlQuery {
    protected AllAuthoritiesMapping(final DataSource ds) {
      super(ds, allAuthoritiesQuery);
      compile();
    }

    @Override
    protected Object mapRow(final ResultSet rs, final int rownum) throws SQLException {
      return new GrantedAuthorityImpl(((null != rolePrefix) ? rolePrefix : "") + rs.getString(1)); //$NON-NLS-1$
    }
  }

  public List<String> getRolesForUser(final String username) throws UsernameNotFoundException,
      DataAccessException {
    UserDetails user = userDetailsService.loadUserByUsername(username);
    List<String> roles = new ArrayList<String>(user.getAuthorities().length);
    for (GrantedAuthority role : user.getAuthorities()) {
      roles.add(role.getAuthority());
    }
    return roles;
  }

  public void setRolePrefix(final String rolePrefix) {
    this.rolePrefix = rolePrefix;
  }

  public void setUserDetailsService(final UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

}
