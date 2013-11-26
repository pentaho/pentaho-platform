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

import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.apache.jackrabbit.core.security.principal.PrincipalIteratorAdapter;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.repository2.unified.jcr.JcrAclMetadataStrategy.AclMetadataPrincipal;
import org.pentaho.platform.repository2.unified.jcr.sejcr.ConstantCredentialsStrategy;
import org.pentaho.platform.repository2.unified.jcr.sejcr.CredentialsStrategy;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * PrincipalProvider for unit test purposes. Has admin and the other Pentaho users. In addition, it has the
 * Jackrabbit principals "everyone", "admin", and "anonymous".
 * 
 * <p>
 * Some parts copied from SimplePrincipalProvider.
 * </p>
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class TestPrincipalProvider implements PrincipalProvider {

  // ~ Instance fields
  // =================================================================================================

  private Map<String, Principal> principals = new HashMap<String, Principal>();

  private Map<String, List<SpringSecurityRolePrincipal>> roleAssignments =
      new HashMap<String, List<SpringSecurityRolePrincipal>>();

  private String adminId;

  private AdminPrincipal adminPrincipal;

  private String anonymousId;

  private AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();

  private String adminRole;

  private SpringSecurityRolePrincipal adminRolePrincipal;

  private static final String KEY_ADMIN_ID = "adminId"; //$NON-NLS-1$

  private static final String KEY_ANONYMOUS_ID = "anonymousId"; //$NON-NLS-1$

  private static final String KEY_ADMIN_ROLE = "adminRole"; //$NON-NLS-1$

  private ITenantedPrincipleNameResolver tenantedUserNameUtils = new DefaultTenantedPrincipleNameResolver();

  private ITenantedPrincipleNameResolver tenantedRoleNameUtils = new DefaultTenantedPrincipleNameResolver(
      DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );

  private boolean primeWithSampleUsers;

  public static IUserRoleDao userRoleDao;
  public static CredentialsStrategy adminCredentialsStrategy = new ConstantCredentialsStrategy();
  public static Repository repository;
  Session session;

  // ~ Constructors
  // ====================================================================================================

  public TestPrincipalProvider() {
    this( true );
  }

  public TestPrincipalProvider( boolean primeWithSampleUsers ) {
    super();
    this.primeWithSampleUsers = primeWithSampleUsers;
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  @Override
  public void init( Properties options ) {
    adminId = options.getProperty( KEY_ADMIN_ID, SecurityConstants.ADMIN_ID );
    adminPrincipal = new AdminPrincipal( adminId );
    adminRole = options.getProperty( KEY_ADMIN_ROLE, SecurityConstants.ADMINISTRATORS_NAME );
    adminRolePrincipal = new SpringSecurityRolePrincipal( adminRole );

    anonymousId = options.getProperty( KEY_ANONYMOUS_ID, SecurityConstants.ANONYMOUS_ID );

    principals.put( adminId, adminPrincipal );
    principals.put( adminRole, adminRolePrincipal );
    ArrayList<SpringSecurityRolePrincipal> assignedAdminRoles = new ArrayList<SpringSecurityRolePrincipal>();
    assignedAdminRoles.add( adminRolePrincipal );
    roleAssignments.put( adminId, assignedAdminRoles );

    principals.put( anonymousId, anonymousPrincipal );

    EveryonePrincipal everyone = EveryonePrincipal.getInstance();
    principals.put( everyone.getName(), everyone );

  }

  Session getAdminSession() {
    try {
      if ( session == null ) {
        session = repository.login( adminCredentialsStrategy.getCredentials(), null );
      }
    } catch ( LoginException e ) {
      e.printStackTrace();
    } catch ( NoSuchWorkspaceException e ) {
      e.printStackTrace();
    } catch ( RepositoryException e ) {
      e.printStackTrace();
    }
    return session;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    // nothing to do
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canReadPrincipal( Session session, Principal principal ) {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Principal getPrincipal( String principalName ) {
    if ( AclMetadataPrincipal.isAclMetadataPrincipal( principalName ) ) {
      return new AclMetadataPrincipal( principalName );
    }
    if ( principals.containsKey( principalName ) ) {
      return principals.get( principalName );
    } else {
      if ( userRoleDao != null ) {
        try {
          if ( userRoleDao.getUser( null, principalName ) != null ) {
            return new UserPrincipal( principalName );
          } else if ( userRoleDao.getRole( null, principalName ) != null ) {
            return new SpringSecurityRolePrincipal( principalName );
          } else {
            /*
             * if(principalName.startsWith("super")) { return new UserPrincipal(principalName); }
             */
            if ( principalName.startsWith( "super" ) ) {
              return new SpringSecurityRolePrincipal( principalName );
            }

          }
        } catch ( Exception e ) {
          // CHECKSTYLES IGNORE
        }
      }
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Called from {@code AbstractLoginModule.getPrincipals()}
   * </p>
   */
  @Override
  public PrincipalIterator getGroupMembership( Principal principal ) {
    if ( principal instanceof EveryonePrincipal ) {
      return PrincipalIteratorAdapter.EMPTY;
    }
    if ( principal instanceof AclMetadataPrincipal ) {
      return PrincipalIteratorAdapter.EMPTY;
    }
    Set<Principal> principals =
        new HashSet<Principal>( roleAssignments.containsKey( principal.getName() ) ? roleAssignments.get( principal
            .getName() ) : new HashSet<Principal>() );
    principals.add( EveryonePrincipal.getInstance() );
    if ( principal instanceof AdminPrincipal ) {
      principals.add( adminRolePrincipal );
    } else if ( principal instanceof UserPrincipal ) {
      if ( userRoleDao != null ) {
        List<IPentahoRole> roles;
        try {
          roles = userRoleDao.getUserRoles( null, principal.getName() );
          for ( IPentahoRole role : roles ) {
            principals.add( new SpringSecurityRolePrincipal( tenantedRoleNameUtils.getPrincipleId( role.getTenant(),
                role.getName() ) ) );
          }
        } catch ( Exception e ) {
          roles = userRoleDao.getUserRoles( null, principal.getName() );
          for ( IPentahoRole role : roles ) {
            principals.add( new SpringSecurityRolePrincipal( tenantedRoleNameUtils.getPrincipleId( role.getTenant(),
                role.getName() ) ) );
          }
        }
      } else {
        if ( principal.getName() != null
            && ( principal.getName().startsWith( "admin" ) || principal.getName().startsWith( "suzy" ) || principal
                .getName().startsWith( "tiffany" ) ) ) {
          ITenant tenant = tenantedUserNameUtils.getTenant( principal.getName() );
          principals.add( new SpringSecurityRolePrincipal( tenantedRoleNameUtils.getPrincipleId( tenant,
              "Authenticated" ) ) );
        }
        if ( principal.getName() != null && principal.getName().startsWith( "admin" ) ) {
          ITenant tenant = tenantedUserNameUtils.getTenant( principal.getName() );
          principals
              .add( new SpringSecurityRolePrincipal( tenantedRoleNameUtils.getPrincipleId( tenant, "TenantAdmin" ) ) );
        }
        if ( principal.getName() != null && principal.getName().startsWith( "super" ) ) {
          ITenant tenant = tenantedUserNameUtils.getTenant( principal.getName() );
          principals
              .add( new SpringSecurityRolePrincipal( tenantedRoleNameUtils.getPrincipleId( tenant, "SysAdmin" ) ) );
        }
      }
    }
    return new PrincipalIteratorAdapter( principals );
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is
   * never called.
   * </p>
   */
  @Override
  public PrincipalIterator findPrincipals( String simpleFilter ) {
    throw new UnsupportedOperationException( "not implemented" );
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is
   * never called.
   * </p>
   */
  @Override
  public PrincipalIterator findPrincipals( String simpleFilter, int searchType ) {
    throw new UnsupportedOperationException( "not implemented" );
  }

  /**
   * {@inheritDoc}
   * 
   * <p>
   * Not implemented. This method only ever called from method in {@code PrincipalManagerImpl} and that method is
   * never called.
   * </p>
   */
  @Override
  public PrincipalIterator getPrincipals( int searchType ) {
    throw new UnsupportedOperationException( "not implemented" );
  }
}
