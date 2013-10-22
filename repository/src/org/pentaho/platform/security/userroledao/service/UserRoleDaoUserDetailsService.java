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

package org.pentaho.platform.security.userroledao.service;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.userroledao.messages.Messages;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A <code>UserDetailsService</code> that delegates to an {@link IUserRoleDao} to load users by username.
 * 
 * @author mlowery
 */
public class UserRoleDaoUserDetailsService implements UserDetailsService {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private String rolePrefix = "ROLE_"; //$NON-NLS-1$

  private IUserRoleDao userRoleDao;

  /**
   * A default role which will be assigned to all authenticated users if set
   */
  private GrantedAuthority defaultRole;

  private String defaultRoleString;

  // ~ Constructors
  // ====================================================================================================

  // ~ Methods
  // =========================================================================================================

  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
    final boolean ACCOUNT_NON_EXPIRED = true;
    final boolean CREDS_NON_EXPIRED = true;
    final boolean ACCOUNT_NON_LOCKED = true;

    IPentahoUser user;
    try {
      if ( userRoleDao == null ) {
        userRoleDao = PentahoSystem.get( IUserRoleDao.class, "userRoleDaoProxy", PentahoSessionHolder.getSession() );
      }
      user = userRoleDao.getUser( null, username );
    } catch ( UncategorizedUserRoleDaoException e ) {
      throw new UserRoleDaoUserDetailsServiceException( Messages.getInstance().getString(
          "UserRoleDaoUserDetailsService.ERROR_0003_DATA_ACCESS_EXCEPTION" ), e ); //$NON-NLS-1$
    }

    if ( user == null ) {
      throw new UsernameNotFoundException( Messages.getInstance().getString(
          "UserRoleDaoUserDetailsService.ERROR_0001_USER_NOT_FOUND" ) ); //$NON-NLS-1$
    }
    // convert IPentahoUser to a UserDetails instance
    List<IPentahoRole> userRoles = userRoleDao.getUserRoles( null, username );
    int authsSize = userRoles != null ? userRoles.size() : 0;
    GrantedAuthority[] auths = new GrantedAuthority[authsSize];
    int i = 0;
    for ( IPentahoRole role : userRoles ) {
      auths[i++] = new GrantedAuthorityImpl( role.getName() );
    }

    List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>( Arrays.asList( auths ) );
    addCustomAuthorities( user.getUsername(), dbAuths );

    // Store the Tenant ID in the session
    IPentahoSession session = PentahoSessionHolder.getSession();
    String tenantId = (String) session.getAttribute( IPentahoSession.TENANT_ID_KEY );
    if ( tenantId == null ) {
      ITenant tenant = JcrTenantUtils.getTenant( username, true );
      session.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
    }

    if ( !StringUtils.isEmpty( defaultRoleString ) ) {
      defaultRole = new GrantedAuthorityImpl( defaultRoleString );
    }

    if ( defaultRole != null && !dbAuths.contains( defaultRole ) ) {
      dbAuths.add( defaultRole );
    }

    if ( dbAuths.size() == 0 ) {
      throw new UsernameNotFoundException( Messages.getInstance().getString(
          "UserRoleDaoUserDetailsService.ERROR_0002_NO_AUTHORITIES" ) ); //$NON-NLS-1$
    }

    GrantedAuthority[] arrayAuths = dbAuths.toArray( new GrantedAuthority[dbAuths.size()] );

    return new User( user.getUsername(), user.getPassword(), user.isEnabled(), ACCOUNT_NON_EXPIRED, CREDS_NON_EXPIRED,
        ACCOUNT_NON_LOCKED, arrayAuths );
  }

  /**
   * Allows subclasses to add their own granted authorities to the list to be returned in the <code>User</code>.
   * 
   * @param username
   *          the username, for use by finder methods
   * @param authorities
   *          the current granted authorities, as populated from the <code>authoritiesByUsername</code> mapping
   */
  protected void addCustomAuthorities( String username, List authorities ) {
  }

  /**
   * Allows a default role prefix to be specified. If this is set to a non-empty value, then it is automatically
   * prepended to any roles read in from the db. This may for example be used to add the <code>ROLE_</code> prefix
   * expected to exist in role names (by default) by some other Spring Security framework classes, in the case that
   * the prefix is not already present in the db.
   * 
   * @param rolePrefix
   *          the new prefix
   */
  public void setRolePrefix( String rolePrefix ) {
    if ( rolePrefix == null ) {
      this.rolePrefix = ""; //$NON-NLS-1$
    } else {
      this.rolePrefix = rolePrefix;
    }
  }

  public String getRolePrefix() {
    return rolePrefix;
  }

  /**
   * A data access exception specific to a <code>IUserRoleDao</code>-based <code>UserDetailsService</code>.
   */
  protected class UserRoleDaoUserDetailsServiceException extends DataAccessException {

    private static final long serialVersionUID = -3598806635515478946L;

    public UserRoleDaoUserDetailsServiceException( String msg ) {
      super( msg );
    }

    public UserRoleDaoUserDetailsServiceException( String msg, Throwable cause ) {
      super( msg, cause );
    }

  }

  public void setUserRoleDao( IUserRoleDao userRoleDao ) {
    this.userRoleDao = userRoleDao;
  }

  /**
   * The default role which will be assigned to all users.
   * 
   * @param defaultRole
   *          the role name, including any desired prefix.
   */
  public void setDefaultRole( String defaultRole ) {
    Assert.notNull( defaultRole );
    this.defaultRoleString = defaultRole;
    this.defaultRole = new GrantedAuthorityImpl( defaultRole );
  }
}
