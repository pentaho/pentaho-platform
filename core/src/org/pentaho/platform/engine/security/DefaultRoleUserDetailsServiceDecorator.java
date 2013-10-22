/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Decorates another {@link UserDetailsService} and returns a proxy during
 * {@link UserDetailsService#loadUserByUsername(String)}. The proxy an extra role when
 * {@link UserDetails#getAuthorities()} is called.
 * 
 * <p>
 * This class is only necessary for {@link UserDetailsService} implementations that don't allow you to supply a
 * default role (e.g. {@code LdapUserDetailsService}).
 * </p>
 * 
 * Use with {@code ExtraRolesUserRoleListServiceDecorator}.
 * 
 * @author mlowery
 */
public class DefaultRoleUserDetailsServiceDecorator implements UserDetailsService {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( DefaultRoleUserDetailsServiceDecorator.class );

  // ~ Instance fields
  // =================================================================================================

  private UserDetailsService userDetailsService;

  private GrantedAuthority defaultRole;

  private IAuthenticationRoleMapper roleMapper;

  // ~ Constructors
  // ====================================================================================================

  public DefaultRoleUserDetailsServiceDecorator() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public UserDetails loadUserByUsername( final String username ) throws UsernameNotFoundException, DataAccessException {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "injecting proxy" ); //$NON-NLS-1$
    }
    UserDetails userDetails = userDetailsService.loadUserByUsername( username );

    return getUserDetailsWithDefaultRole( userDetails );
  }

  protected UserDetails getUserDetailsWithDefaultRole( final UserDetails userDetails ) {
    if ( defaultRole != null ) {
      return new DefaultRoleUserDetailsProxy( userDetails, defaultRole, roleMapper );
    } else {
      return userDetails;
    }
  }

  public void setUserDetailsService( final UserDetailsService userDetailsService ) {
    Assert.notNull( userDetailsService );
    this.userDetailsService = userDetailsService;
  }

  public void setDefaultRole( final String defaultRole ) {
    Assert.notNull( defaultRole );
    this.defaultRole = new GrantedAuthorityImpl( defaultRole );
  }

  public void setRoleMapper( final IAuthenticationRoleMapper roleMapper ) {
    this.roleMapper = roleMapper;
  }

  /**
   * A {@link UserDetails} that has an extra role. The extra role is added to the end of the original role list and
   * only if it is not already in the original role list.
   * 
   * @author mlowery
   */
  public static class DefaultRoleUserDetailsProxy implements UserDetails {

    // ~ Static fields/initializers
    // ====================================================================================

    // ~ Instance fields
    // ===============================================================================================

    private static final long serialVersionUID = -4262518338443465424L;

    private UserDetails userDetails;

    private GrantedAuthority[] newRoles;

    private IAuthenticationRoleMapper roleMapper;

    // ~ Constructors
    // ==================================================================================================

    public DefaultRoleUserDetailsProxy( final UserDetails userDetails, final GrantedAuthority defaultRole ) {
      this( userDetails, defaultRole, null );
    }

    public DefaultRoleUserDetailsProxy( final UserDetails userDetails, final GrantedAuthority defaultRole,
        final IAuthenticationRoleMapper roleMapper ) {
      super();
      Assert.notNull( userDetails );
      Assert.notNull( defaultRole );
      this.userDetails = userDetails;
      this.roleMapper = roleMapper;
      newRoles = getNewRoles( defaultRole );
    }

    // ~ Methods
    // =======================================================================================================

    /**
     * Since UserDetails is immutable, we can safely pre-calculate the new roles.
     */
    protected GrantedAuthority[] getNewRoles( final GrantedAuthority defaultRole ) {
      List<GrantedAuthority> origRoles = Arrays.asList( userDetails.getAuthorities() );
      List<GrantedAuthority> newRoles1 = new ArrayList<GrantedAuthority>();
      // Map Non-pentaho roles to pentaho roles. This mapping is defined in the
      // applicationContext-spring-security-ldap.xml
      if ( roleMapper != null ) {
        for ( GrantedAuthority authority : origRoles ) {
          newRoles1.add( new GrantedAuthorityImpl( roleMapper.toPentahoRole( authority.getAuthority() ) ) );
        }
      }

      if ( !origRoles.contains( defaultRole ) ) {
        if ( logger.isDebugEnabled() ) {
          logger
              .debug( "adding defaultRole=" + defaultRole + " to list of roles for username=" + userDetails.getUsername() ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        newRoles1.add( defaultRole );
      }
      return newRoles1.toArray( new GrantedAuthority[0] );
    }

    public GrantedAuthority[] getAuthorities() {
      return newRoles;
    }

    public String getPassword() {
      return userDetails.getPassword();
    }

    public String getUsername() {
      return userDetails.getUsername();
    }

    public boolean isAccountNonExpired() {
      return userDetails.isAccountNonExpired();
    }

    public boolean isAccountNonLocked() {
      return userDetails.isAccountNonLocked();
    }

    public boolean isCredentialsNonExpired() {
      return userDetails.isCredentialsNonExpired();
    }

    public boolean isEnabled() {
      return userDetails.isEnabled();
    }

  }

}
