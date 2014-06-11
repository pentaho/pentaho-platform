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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.UserSession;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetailsService;

import java.util.concurrent.Callable;

/**
 * A utility class with several methods that are used to either bind the <tt>Authentication</tt> to the
 * <tt>IPentahoSession</tt>, retrieve the <tt>Authentication</tt> from the <tt>IPentahoSession</tt>, and other various
 * helper functions.
 *
 * @author mbatchel
 */

public class SecurityHelper implements ISecurityHelper {

  private static final Log logger = LogFactory.getLog( SecurityHelper.class );

  /**
   * The default instance of this singleton
   */
  private static ISecurityHelper instance = new SecurityHelper();
  private static ISecurityHelper mockInstance;

  private ITenantedPrincipleNameResolver tenantedUserNameUtils;
  private IAclVoter aclVoter;
  private UserDetailsService userDetailsService;
  private IUserRoleListService userRoleListService;

  /**
   * Returns the default instance, if the test instance is not null return the test instance
   */
  public static ISecurityHelper getInstance() {
    if ( mockInstance != null ) {
      return mockInstance;
    }

    return instance;
  }

  /**
   * Set the mockInstance, this should only be used for testing
   *
   * @param mockInstanceValue the test implementation of SecurityHelper
   */
  public static void setMockInstance( ISecurityHelper mockInstanceValue ) {
    mockInstance = mockInstanceValue;
  }

  /**
   * Default constructor - protected so that it may be only constructed by a sub-class since this is a singleton
   */
  protected SecurityHelper() {

  }

  /**
   * Hi-jacks the system for the named user. <p/> <p> This will essentially create a session for this user, make that
   * session the current session, and add the Authentication objects to the session and Spring context holder. WARNING:
   * this method is irreversible!!! If you want execute a block of code as a surrogate user and have the orignal user
   * resume after it is complete, you want {@link #runAsUser(String, Callable)}. </p> <p/> <p> This is for unit tests
   * only. </p>
   *
   * @param principalName the user to become in the system
   */
  @Override
  public void becomeUser( final String principalName ) {
    becomeUser( principalName, null );
  }

  /**
   * Hi-jacks the system for the named user. <p/> <p> This is for unit tests only. </p>
   */
  @Override
  public void becomeUser( final String principalName, final IParameterProvider paramProvider ) {
    UserSession session = null;
    tenantedUserNameUtils = getTenantedUserNameUtils();
    if ( tenantedUserNameUtils != null ) {
      session = new UserSession( principalName, null, false, paramProvider );
      ITenant tenant = tenantedUserNameUtils.getTenant( principalName );
      session.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
      session.setAuthenticated( tenant.getId(), principalName );
    } else {
      session = new UserSession( principalName, null, false, paramProvider );
      session.setAuthenticated( principalName );
    }

    PentahoSessionHolder.setSession( session );

    Authentication auth = createAuthentication( principalName );
    // TODO We need to figure out how to inject this
    // Get the tenant id from the principle name and set it as an attribute of the pentaho session

    SecurityContextHolder.getContext().setAuthentication( auth );
    PentahoSystem.sessionStartup( PentahoSessionHolder.getSession(), paramProvider );
  }

  /**
   * Utility method that allows you to run a block of code as the given user. Regardless of success or exception
   * situation, the original session and authentication will be restored once your block of code is finished executing,
   * i.e. the given user will apply only to your {@link Callable}, then the system environment will return to the user
   * present prior to you calling this method.
   *
   * @param <T>           the return type of your operation, specify this type as <code>T</code>
   * @param principalName the user under whom you wish to run a section of code
   * @param callable      {@link Callable#call()} contains the code you wish to run as the given user
   * @return the value returned by your implementation of {@link Callable#call()}
   * @throws Exception
   * @see {@link Callable}
   */
  @Override
  public <T> T runAsUser( final String principalName, final Callable<T> callable ) throws Exception {
    return runAsUser( principalName, null, callable );
  }

  @Override
  public <T> T
  runAsUser( final String principalName, final IParameterProvider paramProvider, final Callable<T> callable )
    throws Exception {
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      becomeUser( principalName );
      return callable.call();
    } finally {
      IPentahoSession sessionToDestroy = PentahoSessionHolder.getSession();
      if ( sessionToDestroy != null && sessionToDestroy != origSession ) {
        try {
          sessionToDestroy.destroy();
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }
      PentahoSessionHolder.setSession( origSession );
      SecurityContextHolder.getContext().setAuthentication( origAuth );
    }
  }

  /**
   * Utility method that allows you to run a block of code as the given user. Regardless of success or exception
   * situation, the original session and authentication will be restored once your block of code is finished executing,
   * i.e. the given user will apply only to your {@link Callable}, then the system environment will return to the user
   * present prior to you calling this method.
   *
   * @param <T>      the return type of your operation, specify this type as <code>T</code>
   * @param callable {@link Callable#call()} contains the code you wish to run as the given user
   * @return the value returned by your implementation of {@link Callable#call()}
   * @throws Exception
   * @see {@link Callable}
   */
  @Override
  public <T> T runAsAnonymous( final Callable<T> callable ) throws Exception {
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      PentahoSessionHolder.setSession( new StandaloneSession() );

      // get anonymous username/role defined in pentaho.xml
      String user = PentahoSystem
        .getSystemSetting( "anonymous-authentication/anonymous-user", "anonymousUser" ); //$NON-NLS-1$//$NON-NLS-2$
      String role = PentahoSystem
        .getSystemSetting( "anonymous-authentication/anonymous-role", "Anonymous" ); //$NON-NLS-1$//$NON-NLS-2$
      GrantedAuthority[] authorities = new GrantedAuthority[] { new GrantedAuthorityImpl( role ) };

      Authentication auth =
        new AnonymousAuthenticationToken( "anonymousUser", new User( user, "ignored", true, true, true, true,
          authorities ), authorities );

      SecurityContextHolder.getContext().setAuthentication( auth );
      return callable.call();
    } finally {
      PentahoSessionHolder.setSession( origSession );
      SecurityContextHolder.getContext().setAuthentication( origAuth );
    }
  }

  /**
   * Utility method that communicates with the installed ACLVoter to determine administrator status
   *
   * @param session The users IPentahoSession object
   * @return true if the user is considered a Pentaho administrator
   */
  @Override
  public boolean isPentahoAdministrator( final IPentahoSession session ) {
    return getAclVoter().isPentahoAdministrator( session );
  }

  /**
   * Utility method that communicates with the installed ACLVoter to determine whether a particular role is granted to
   * the specified user.
   *
   * @param session The users' IPentahoSession
   * @param role    The role to look for
   * @return true if the user is granted the specified role.
   */
  @Override
  public boolean isGranted( final IPentahoSession session, final GrantedAuthority role ) {
    return getAclVoter().isGranted( session, role );
  }

  /**
   * @param aFile
   * @return a boolean that indicates if this file can have ACLS placed on it.
   */
  @Override
  public boolean canHaveACLS( final ISolutionFile aFile ) {
    if ( aFile.isDirectory() ) { // All Directories can have ACLS
      return true;
    }

    // Otherwise anything in the PentahoSystem extension list.
    return PentahoSystem.getACLFileExtensionList().contains( aFile.getExtension() );
  }

  @Override
  public boolean hasAccess( final IAclHolder aHolder, final int actionOperation, final IPentahoSession session ) {
    int aclMask = -1;

    switch( actionOperation ) {
      case ( IAclHolder.ACCESS_TYPE_READ ): {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_WRITE:
      case IAclHolder.ACCESS_TYPE_UPDATE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_DELETE: {
        aclMask = IPentahoAclEntry.PERM_DELETE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_ADMIN: {
        aclMask = IPentahoAclEntry.PERM_ADMINISTRATION;
        break;
      }
      default: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }

    }
    return getAclVoter().hasAccess( session, aHolder, aclMask );
  }

  /**
   * Utility method for hydrating a Spring Authentication object (Principal) given just a user name. Note: The {@link
   * IUserRoleListService} will be consulted for the roles associated with this user.
   *
   * @param principalName the subject of this Authentication object
   * @return a Spring Authentication for the given user
   */
  @Override
  public Authentication createAuthentication( String principalName ) {

    //get 'anonymousUser' defined name from pentaho.xml's <anonymous-authentication> block
    String anonymousUser = PentahoSystem
      .getSystemSetting( "anonymous-authentication/anonymous-user", "anonymousUser" ); //$NON-NLS-1$//$NON-NLS-2$

    userDetailsService = getUserDetailsService();
    userRoleListService = getUserRoleListService();


    List<String> roles = new ArrayList<String>();

    // anonymousUser gets its roles from session
    if ( anonymousUser.equals( principalName ) ) {

      //get 'anonymous' defined role from pentaho.xml's <anonymous-authentication> block
      String anonymousRole = PentahoSystem
        .getSystemSetting( "anonymous-authentication/anonymous-role", "Anonymous" ); //$NON-NLS-1$//$NON-NLS-2$
      roles.add( anonymousRole );

    } else {

      // default (standard) role fetching via IUserRoleListService
      roles = userRoleListService.getRolesForUser( null, principalName );

    }

    if ( SecurityHelper.logger.isDebugEnabled() ) {
      SecurityHelper.logger.debug( "rolesForUser:" + roles ); //$NON-NLS-1$
    }

    GrantedAuthority[] grantedAuthorities = new GrantedAuthority[ roles.size() ];
    for ( int i = 0; i < roles.size(); i++ ) {
      grantedAuthorities[ i ] = new GrantedAuthorityImpl( roles.get( i ) );
    }

    User user = new User( principalName, "", true, true, true, true, grantedAuthorities );
    Authentication auth = new UsernamePasswordAuthenticationToken( user, null, grantedAuthorities );
    return auth;

  }

  @Override
  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * Remove this method when data-access is JCR-branched
   *
   * @param ignoredSession
   * @param ignoredAllowAnonymous
   * @return
   */
  @Override
  public Authentication getAuthentication( IPentahoSession ignoredSession, boolean ignoredAllowAnonymous ) {
    return getAuthentication();
  }

  /**
   * Runs code as system with full privileges.
   */
  public <T> T runAsSystem( final Callable<T> callable ) throws Exception {
    String singleTenantAdmin = PentahoSystem.get( String.class, "singleTenantAdminUserName", null );
    IPentahoSession origSession = PentahoSessionHolder.getSession();

    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();

    StandaloneSession session = null;
    try {
      session = new StandaloneSession( singleTenantAdmin );
      session.setAuthenticated( singleTenantAdmin );

      // Set the session first or else the call to
      // createAuthentication will fail
      PentahoSessionHolder.setSession( session );

      // Now create the authentication
      Authentication auth = createAuthentication( singleTenantAdmin ); //$NON-NLS-1$
      SecurityContextHolder.getContext().setAuthentication( auth );

      // Invoke the delta.
      return callable.call();
    } finally {
      // Make sure to destroy the system session so we don't leak anything.
      if ( session != null ) {
        try {
          session.destroy();
        } catch ( Exception e ) {
          // We can safely ignore this.
          e.printStackTrace();
        }
      }
      // Reset the original session.
      PentahoSessionHolder.setSession( origSession );
      SecurityContextHolder.getContext().setAuthentication( origAuth );
    }
  }

  public ITenantedPrincipleNameResolver getTenantedUserNameUtils() {
    if ( tenantedUserNameUtils == null ) {
      tenantedUserNameUtils = PentahoSystem.get( ITenantedPrincipleNameResolver.class, "tenantedUserNameUtils", null );
    }
    return tenantedUserNameUtils;
  }

  public IAclVoter getAclVoter() {
    if ( aclVoter == null ) {
      aclVoter = PentahoSystem.get( IAclVoter.class );
    }
    return aclVoter;
  }

  public UserDetailsService getUserDetailsService() {
    if ( userDetailsService == null ) {
      userDetailsService = PentahoSystem.get( UserDetailsService.class );
    }
    return userDetailsService;
  }

  public IUserRoleListService getUserRoleListService() {
    if ( userRoleListService == null ) {
      userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    }
    return userRoleListService;
  }
}
