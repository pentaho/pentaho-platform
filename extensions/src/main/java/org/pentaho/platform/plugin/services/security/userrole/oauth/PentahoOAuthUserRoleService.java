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

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class PentahoOAuthUserRoleService implements IUserRoleListService {

  public static final String AUTHENTICATED =
    PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null );

  private IUserRoleDao userRoleDao;

  private IAuthenticationRoleMapper roleMapper;

  private List<String> systemRoles;

  private List<String> extraRoles;

  @Autowired
  public PentahoOAuthUserRoleService() {
  }

  public PentahoOAuthUserRoleService( IUserRoleDao userRoleDao,
                                      IAuthenticationRoleMapper roleMapper,
                                      List<String> systemRoles,
                                      List<String> extraRoles ) {
    this.userRoleDao = userRoleDao;
    this.roleMapper = roleMapper;
    this.systemRoles = systemRoles;
    this.extraRoles = extraRoles;
  }

  /**
   * Add the roles nad extra roles to the list of roles if it does not already have it
   *
   * @param roles
   * @return roles union and unique list of roles
   */
  private List<String> getAllRoles( List<IPentahoRole> roles ) {
    Set<String> auths = new HashSet<>();

    for ( IPentahoRole role : roles ) {
      auths.add( role.getName() );
    }

    // Add extra roles to the list of roles if it does not already have it
    auths.addAll( extraRoles );

    return new ArrayList<>( auths );
  }

  @Override
  public List<String> getAllRoles() {
    return getAllRoles( userRoleDao.getRoles() );
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    return getAllRoles( userRoleDao.getRoles( tenant ) );
  }

  private List<String> getAllUsers( List<IPentahoUser> users ) {
    List<String> usernames = new ArrayList<>();

    for ( IPentahoUser user : users ) {
      usernames.add( user.getUsername() );
    }

    return usernames;
  }

  @Override
  public List<String> getAllUsers() {
    return getAllUsers( userRoleDao.getUsers() );
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    return getAllUsers( userRoleDao.getUsers( tenant ) );
  }

  /**
   * Get the roles for a user. This method is used to get the roles for a user when the user is logged in via OAuth.
   * The roles are retrieved from the authentication object and mapped to Pentaho roles.
   *
   * @param tenant   The tenant of the user
   * @param username The username of the user
   * @return A list of roles for the user
   */
  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if ( ( authentication instanceof OAuth2AuthenticationToken
      || authentication instanceof OAuth2LoginAuthenticationToken ) && authentication.isAuthenticated() ) {
      DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
      if ( Objects.nonNull( defaultOidcUser ) ) {
        Set<String> pentahoAuthorities = new HashSet<>();

        Collection<? extends GrantedAuthority> oauthAuthorities = authentication.getAuthorities();
        if ( CollectionUtils.isNotEmpty( oauthAuthorities ) ) {
          pentahoAuthorities.add( AUTHENTICATED );
          oauthAuthorities.forEach( authority ->
            pentahoAuthorities.add( roleMapper.toPentahoRole( authority.getAuthority() ) ) );
        }

        List<String> oauthRoles = defaultOidcUser.getAttribute( "roles" );
        if ( CollectionUtils.isNotEmpty( oauthRoles ) ) {
          pentahoAuthorities.add( AUTHENTICATED );
          oauthRoles.forEach( role -> pentahoAuthorities.add( roleMapper.toPentahoRole( role ) ) );
        }

        return new ArrayList<>( pentahoAuthorities );
      }
    }

    // After successful authentication, An application event is triggered by success listener which invokes
    // SecurityHelper.runAsSystem() and it needs to create Authentication object for Single tenant admin user
    if ( StringUtils.equals( username, PentahoSystem.get( String.class, "singleTenantAdminUserName", null ) ) ) {
      List<String> defaultAdminRoles = new ArrayList<>();
      defaultAdminRoles.add( PentahoSystem.getSystemSetting( "acl-voter/admin-role", "Administrator" ) );
      defaultAdminRoles.add( AUTHENTICATED );
      return defaultAdminRoles;
    }
    return Collections.emptyList();
  }

  public List<String> getUsersInRole( ITenant tenant, IPentahoRole role, String roleName ) {
    if ( role == null ) {
      return Collections.emptyList();
    }
    List<IPentahoUser> users = null;
    List<String> usernames = new ArrayList<>();
    if ( Objects.isNull( tenant ) ) {
      users = userRoleDao.getRoleMembers( null, roleName );
    } else {
      users = userRoleDao.getRoleMembers( tenant, roleName );
    }

    for ( IPentahoUser user : users ) {
      usernames.add( user.getUsername() );
    }

    return usernames;
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String roleName ) {
    return getUsersInRole( tenant, userRoleDao.getRole( tenant, roleName ), roleName );
  }

  public IUserRoleDao getUserRoleDao() {
    return userRoleDao;
  }

  public void setUserRoleDao( IUserRoleDao userRoleDao ) {
    this.userRoleDao = userRoleDao;
  }

  @Override
  public List<String> getSystemRoles() {
    return systemRoles;
  }

  /**
   * Creates the user in the system i.e., jackrabbit, if it does not exist.
   *
   * @param authentication This is sent from {@link PentahoOAuthUserCreationFilter}
   */
  public void createUser( OAuth2AuthenticationToken authentication ) {
    ITenant tenant = JcrTenantUtils.getTenant();
    String username = authentication.getName();

    List<String> roles = getRolesForUser( tenant, username );
    roles.remove( AUTHENTICATED );
    String[] userRoles = roles.toArray( new String[ 0 ] );

    String registrationId = authentication.getAuthorizedClientRegistrationId();
    String userId = authentication.getPrincipal().getAttribute( "oid" );

    if ( StringUtils.isBlank( userId ) ) {
      userId = authentication.getPrincipal().getAttribute( "user_id" );
    }

    if ( StringUtils.isBlank( userId ) ) {
      userId = authentication.getPrincipal().getAttribute( "sub" );
    }

    if ( StringUtils.isBlank( userId ) ) {
      userId = PentahoOAuthUtility.getUserNameAttribute( authentication.getAuthorizedClientRegistrationId() );
    }

    if ( isNewUser( tenant, username ) ) {
      userRoleDao.createOAuthUser( tenant, username, "password", "", userRoles, registrationId, userId );
    }
  }

  public boolean isNewUser( ITenant tenant, String name ) throws UncategorizedUserRoleDaoException {
    return Objects.isNull( userRoleDao.getUser( tenant, name ) );
  }

}
