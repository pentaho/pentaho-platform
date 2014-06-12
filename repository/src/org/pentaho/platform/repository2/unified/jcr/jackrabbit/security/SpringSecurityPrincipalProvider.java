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

package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.Session;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.core.security.principal.PrincipalIteratorAdapter;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.JcrAclMetadataStrategy.AclMetadataPrincipal;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.messages.Messages;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

/**
 * A Jackrabbit {@code PrincipalProvider} that delegates to a Pentaho {@link UserDetailsService}.
 * <p/>
 * <p> A {@code java.security.Principal} represents a user. A {@code java.security.acl.Group} represents a group. In
 * Spring Security, a group is called a role or authority or granted authority. Arguments to the method {@link
 * #providePrincipal(String)} can either be a Principal or Group. In other words, {@link #providePrincipal(String)}
 * might be called with an argument of a Spring Security granted authority. This happens when access control entries
 * (ACEs) grant access to roles and the system needs to verify the role is known. </p>
 * <p/>
 * <p> Jackrabbit assumes a unified space of all user and role names. The PrincipalProvider is responsible for
 * determining the type of a principal/group from its name. </p>
 * <p/>
 * <p> This implementation caches users and roles, but not passwords. Optionally, this implementation can take advantage
 * of a Spring Security UserCache. If available, it will use said cache for role membership lookups. Also note that the
 * removal of a role or user from the system will not be noticed by this implementation. (A restart of Jackrabbit is
 * required.) </p>
 * <p/>
 * <p> There are users and roles that are never expected to be in any backing store. By default, these are "everyone" (a
 * role), "anonymous" (a user), "administrators" (a role), and "admin" (a user). </p>
 * <p/>
 * <p> This implementation never returns null from {@link #getPrincipal(String)}. As a result, a {@code
 * NoSuchPrincipalException} is never thrown. See the method for details. </p>
 *
 * @author mlowery
 */
public class SpringSecurityPrincipalProvider implements PrincipalProvider {

  // ~ Static fields/initializers
  // ======================================================================================

  private Log logger = LogFactory.getLog( SpringSecurityPrincipalProvider.class );

  // ~ Instance fields
  // =================================================================================================

  private UserDetailsService userDetailsService;
  private IUserRoleListService userRoleListService;

  private String adminId;

  private AdminPrincipal adminPrincipal;

  private String anonymousId;

  private AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();

  final boolean ACCOUNT_NON_EXPIRED = true;

  final boolean CREDS_NON_EXPIRED = true;

  final boolean ACCOUNT_NON_LOCKED = true;

  /**
   * flag indicating if the instance has not been {@link #close() closed}
   */
  private final AtomicBoolean initialized = new AtomicBoolean( false );

  private final LRUMap userCache = new LRUMap( 4096 );

  private final LRUMap roleCache = new LRUMap( 512 );

  // ~ Constructors
  // ====================================================================================================

  public SpringSecurityPrincipalProvider() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public void init( final Properties options ) {
    synchronized ( initialized ) {
      if ( initialized.get() ) {
        throw new IllegalStateException( Messages.getInstance().getString(
          "SpringSecurityPrincipalProvider.ERROR_0001_ALREADY_INITIALIZED" ) ); //$NON-NLS-1$
      }
    }

    adminId = options.getProperty( LoginModuleConfig.PARAM_ADMIN_ID, SecurityConstants.ADMIN_ID );
    adminPrincipal = new AdminPrincipal( adminId );
    if ( logger.isTraceEnabled() ) {
      logger.trace( String.format( "using adminId [%s]", adminId ) ); //$NON-NLS-1$
    }
    anonymousId = options.getProperty( LoginModuleConfig.PARAM_ANONYMOUS_ID, SecurityConstants.ANONYMOUS_ID );
    if ( logger.isTraceEnabled() ) {
      logger.trace( String.format( "using anonymousId [%s]", anonymousId ) ); //$NON-NLS-1$
    }

    initialized.set( true );
  }

  public void close() {
    checkInitialized();
    clearCaches();
    initialized.set( false );
  }

  public synchronized void clearCaches() {
    synchronized ( userCache ) {
      userCache.clear();
    }
    synchronized ( roleCache ) {
      roleCache.clear();
    }
  }

  /**
   * {@inheritDoc}
   */
  public synchronized boolean canReadPrincipal( final Session session, final Principal principalToRead ) {
    checkInitialized();
    return true;
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p> Attempts to load user using given {@code principalName} using a Pentaho {@code UserDetailsService}. If it fails
   * to find user, it returns a {@link Group} which will be caught by {@code SpringSecurityLoginModule}. </p>
   */
  public synchronized Principal getPrincipal( final String principalName ) {

    if ( logger.isDebugEnabled() ) {
      logger.debug( "principalName: [" + principalName + "]" );
    }

    checkInitialized();
    Assert.notNull( principalName );
    // first handle AclMetadataPrincipal, admin, anonymous, and everyone
    // specially
    if ( AclMetadataPrincipal.isAclMetadataPrincipal( principalName ) ) {
      return new AclMetadataPrincipal( principalName );
    } else if ( adminId.equals( principalName ) ) {
      return adminPrincipal;
    } else if ( anonymousId.equals( principalName ) ) {
      return anonymousPrincipal;
    } else if ( EveryonePrincipal.getInstance().getName().equals( principalName ) ) {
      return EveryonePrincipal.getInstance();
    } else {

      if ( JcrTenantUtils.isTenantedUser( principalName ) ) {
        // 1. then try the user cache
        Principal userFromUserCache;
        synchronized ( userCache ) {
          userFromUserCache = (Principal) userCache.get( JcrTenantUtils.getTenantedUser( principalName ) );
        }
        if ( userFromUserCache != null ) {
          if ( logger.isTraceEnabled() ) {
            logger.trace( "user " + principalName + " found in cache" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
          return userFromUserCache;
        } else {
          if ( logger.isTraceEnabled() ) {
            logger.trace( "user " + principalName + " not found in cache" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }

        // 2. then try the springSecurityUserCache and, failing that, actual
        // back-end user lookup
        final UserDetails userDetails = internalGetUserDetails( principalName );
        if ( userDetails != null ) {
          final Principal user = new UserPrincipal( principalName );
          synchronized ( userCache ) {
            userCache.put( principalName, user );
          }
          return user;
        }

      } else if ( JcrTenantUtils.isTenatedRole( principalName ) ) {

        // 1. first try the role cache
        final Principal roleFromCache = (Principal) roleCache.get( JcrTenantUtils.getTenantedRole( principalName ) );
        if ( roleFromCache != null ) {
          if ( logger.isTraceEnabled() ) {
            logger.trace( "role " + principalName + " found in cache" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
          return roleFromCache;
        } else {
          if ( logger.isTraceEnabled() ) {
            logger.trace( "role " + principalName + " not found in cache" ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }

        // 2. finally just assume role; this assumption serves two purposes:
        // (1) avoid any role search config by the user
        // and (2) performance (if we don't care that a role is not
        // present--why look it up); finally, a Group returned
        // by this class will be caught in
        // SpringSecurityLoginModule.getPrincipal and the login will fail
        final Principal roleToCache = createSpringSecurityRolePrincipal( principalName );
        roleCache.put( principalName, roleToCache );
        if ( logger.isTraceEnabled() ) {
          logger.trace( "assuming " + principalName + " is a role" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return roleToCache;

      }

      return null;

    }
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p> Called from {@code AbstractLoginModule.getPrincipals()} </p>
   */
  public PrincipalIterator getGroupMembership( final Principal principal ) {
    checkInitialized();
    Assert.notNull( principal );
    // first handle anonymous and everyone specially
    Set<Principal> groups = new HashSet<Principal>();
    if ( principal instanceof AnonymousPrincipal ) {
      return PrincipalIteratorAdapter.EMPTY;
    } else if ( principal instanceof EveryonePrincipal ) {
      return PrincipalIteratorAdapter.EMPTY;
    }

    // make sure it's a user; also, repo admins are never in back-end--no
    // need to attempt to look them up; also acl
    // metadata principals never have group membership
    if ( !( principal instanceof Group ) && !( principal instanceof AdminPrincipal )
      && !( principal instanceof AclMetadataPrincipal ) ) {
      UserDetails user = internalGetUserDetails( principal.getName() );
      if ( user == null ) {
        return new PrincipalIteratorAdapter( groups );
      }
      for ( final GrantedAuthority role : user.getAuthorities() ) {

        final String roleAuthority = role.getAuthority();
        Principal fromCache;
        synchronized ( roleCache ) {
          fromCache = (Principal) roleCache.get( roleAuthority );
        }
        if ( fromCache != null ) {
          groups.add( fromCache );
        } else {
          groups.add( createSpringSecurityRolePrincipal( roleAuthority ) );
        }
      }
    }
    groups.add( EveryonePrincipal.getInstance() );
    if ( logger.isTraceEnabled() ) {
      logger.trace( "group membership for principal=" + principal + " is " + groups ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return new PrincipalIteratorAdapter( groups );
  }

  /**
   * Gets user details. Checks cache first.
   */
  protected UserDetails internalGetUserDetails( final String username ) {

    if ( username != null && username.equals( "administrators" ) ) {
      return null;
    }
    // optimization for when running in pre-authenticated mode (i.e. Spring Security filters have setup holder with
    // current user meaning we don't have to hit the back-end again)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if ( auth != null ) {
      Object ssPrincipal = auth.getPrincipal();
      if ( ssPrincipal instanceof UserDetails ) {
        if ( username.equals( ( (UserDetails) ssPrincipal ).getUsername() ) ) {
          return (UserDetails) ssPrincipal;
        }
      }
    }

    UserDetails user = null;
    // user cache not available or user not in cache; do lookup
    GrantedAuthority[] auths = null;
    GrantedAuthority[] authorities = null;
    UserDetails newUser = null;
    if ( getUserDetailsService() != null ) {
      try {
        user = getUserDetailsService().loadUserByUsername( username );
        // We will use the authorities from the Authentication object of SecurityContextHolder. 
        //Authentication object is null then we will get it from IUserRoleListService
        if ( auth == null || auth.getAuthorities() == null || auth.getAuthorities().length == 0 ) {
          if ( logger.isTraceEnabled() ) {
            logger.trace( "Authentication object from SecurityContextHolder is null,"
              + " so getting the roles for [ " + user.getUsername() + " ]  from IUserRoleListService " ); //$NON-NLS-1$
          }

          List<String> roles = getUserRoleListService().getRolesForUser( JcrTenantUtils.getCurrentTenant(), username );
          authorities = new GrantedAuthority[ roles.size() ];
          for ( int i = 0; i < roles.size(); i++ ) {
            authorities[ i ] = new GrantedAuthorityImpl( roles.get( i ) );
          }
        } else {
          authorities = auth.getAuthorities();
        }

        auths = new GrantedAuthority[ authorities.length ];
        // cache the roles while we're here
        for ( int i = 0; i < authorities.length; i++ ) {
          String role = authorities[ i ].getAuthority();
          final String tenatedRoleString = JcrTenantUtils.getTenantedRole( role );
          synchronized ( roleCache ) {
            if ( !roleCache.containsKey( role ) ) {
              final SpringSecurityRolePrincipal ssRolePrincipal = new SpringSecurityRolePrincipal( tenatedRoleString );
              roleCache.put( role, ssRolePrincipal );
            }
          }
          auths[ i ] = new GrantedAuthorityImpl( tenatedRoleString );
        }
        if ( logger.isTraceEnabled() ) {
          logger.trace( "found user in back-end " + user.getUsername() ); //$NON-NLS-1$
        }
      } catch ( UsernameNotFoundException e ) {
        if ( logger.isTraceEnabled() ) {
          logger
            .trace( "username " + username + " not in cache or back-end; returning null" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }

      if ( user != null ) {
        if ( auths == null || auths.length <= 0 ) {
          logger.trace( "Authorities are null, so creating an empty Auth array ==  " + user.getUsername() );
          // auth is null so we are going to pass an empty auths array
          auths = new GrantedAuthority[ 0 ];
        }
        String password = user.getPassword() != null ? user.getPassword() : "";
        newUser =
          new User( user.getUsername(), password, user.isEnabled(), ACCOUNT_NON_EXPIRED, CREDS_NON_EXPIRED,
            ACCOUNT_NON_LOCKED, auths );
      }

    }

    return newUser;
  }

  protected void checkInitialized() {
    synchronized ( initialized ) {
      if ( !initialized.get() ) {
        throw new IllegalStateException( Messages.getInstance().getString(
          "SpringSecurityPrincipalProvider.ERROR_0003_NOT_INITIALIZED" ) ); //$NON-NLS-1$
      }
    }
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p> Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is
   * never called. </p>
   */
  public PrincipalIterator findPrincipals( final String simpleFilter ) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p> Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is
   * never called. </p>
   */
  public PrincipalIterator findPrincipals( final String simpleFilter, final int searchType ) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <p> Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is
   * never called. </p>
   */
  public PrincipalIterator getPrincipals( final int searchType ) {
    throw new UnsupportedOperationException();
  }

  protected UserDetailsService getUserDetailsService() {
    if ( null != userDetailsService ) {
      return userDetailsService;
    } else {
      if ( PentahoSystem.getInitializedOK() ) {
        userDetailsService = PentahoSystem.get( UserDetailsService.class );
        return userDetailsService;
      } else {
        return null;
      }
    }
  }

  protected IUserRoleListService getUserRoleListService() {
    if ( null != userRoleListService ) {
      return userRoleListService;
    } else {
      if ( PentahoSystem.getInitializedOK() ) {
        userRoleListService = PentahoSystem.get( IUserRoleListService.class );
        return userRoleListService;
      } else {
        return null;
      }
    }
  }

  private SpringSecurityRolePrincipal createSpringSecurityRolePrincipal( String principal ) {
    return new SpringSecurityRolePrincipal( JcrTenantUtils.getTenantedRole( principal ) );
  }

}
