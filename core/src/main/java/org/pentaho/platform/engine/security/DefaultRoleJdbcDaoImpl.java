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


package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of {@link JdbcDaoImpl} that allows the addition of a default role to all authenticated users.
 * 
 * @author mlowery
 */
public class DefaultRoleJdbcDaoImpl extends JdbcDaoImpl {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  /**
   * A default role which will be assigned to all authenticated users if set
   */
  private GrantedAuthority defaultRole;

  private IAuthenticationRoleMapper roleMapper;

  // ~ Constructors
  // ====================================================================================================

  ITenantedPrincipleNameResolver userNameUtils;

  public DefaultRoleJdbcDaoImpl( ITenantedPrincipleNameResolver userNameUtils ) {
    super();
    this.userNameUtils = userNameUtils;
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
    return super.loadUserByUsername( userNameUtils.getPrincipleName( username ) );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected void addCustomAuthorities( final String username, final List authorities ) {
    if ( defaultRole != null && !authorities.contains( defaultRole ) ) {
      authorities.add( defaultRole );
    }

    // also add roles mapped to pentaho security roles if available
    if ( roleMapper != null ) {
      List<GrantedAuthority> currentAuthorities = new ArrayList<GrantedAuthority>();
      currentAuthorities.addAll( authorities );

      for ( GrantedAuthority role : currentAuthorities ) {
        GrantedAuthority mappedRole = new SimpleGrantedAuthority( roleMapper.toPentahoRole( role.getAuthority() ) );
        if ( !authorities.contains( mappedRole ) ) {
          authorities.add( mappedRole );
        }
      }
    }
  }

  /**
   * The default role which will be assigned to all users.
   * 
   * @param defaultRole
   *          the role name, including any desired prefix.
   */
  public void setDefaultRole( String defaultRole ) {
    Assert.notNull( defaultRole, "Default role must not be null" );
    this.defaultRole = new SimpleGrantedAuthority( defaultRole );
  }

  public void setRoleMapper( IAuthenticationRoleMapper roleMapper ) {
    this.roleMapper = roleMapper;
  }
}
