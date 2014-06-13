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

package org.pentaho.platform.security.userroledao.jackrabbit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.collections.map.LRUMap;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authentication.CryptedSimpleCredentials;
import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
import org.apache.jackrabbit.core.security.user.UserManagerImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.ILockHelper;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileAclUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;
import org.pentaho.platform.security.userroledao.messages.Messages;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.dao.cache.NullUserCache;

public abstract class AbstractJcrBackedUserRoleDao implements IUserRoleDao {

  NameFactory NF = NameFactoryImpl.getInstance();

  Name P_PRINCIPAL_NAME = NF.create( Name.NS_REP_URI, "principalName" ); //$NON-NLS-1$

  protected ITenantedPrincipleNameResolver tenantedUserNameUtils;

  protected ITenantedPrincipleNameResolver tenantedRoleNameUtils;

  String pPrincipalName = "rep:principalName"; //$NON-NLS-1$

  IRepositoryFileAclDao repositoryFileAclDao;

  IRepositoryFileDao repositoryFileDao;

  String defaultTenant;

  String authenticatedRoleName;

  String tenantAdminRoleName;

  String repositoryAdminUsername;

  IPathConversionHelper pathConversionHelper;

  IRepositoryDefaultAclHandler defaultAclHandler;

  ILockHelper lockHelper;

  List<String> systemRoles;

  List<String> extraRoles;

  HashMap<String, UserManagerImpl> userMgrMap = new HashMap<String, UserManagerImpl>();

  private LRUMap userCache = new LRUMap( 4096 );

  private UserCache userDetailsCache = new NullUserCache();

  public AbstractJcrBackedUserRoleDao( ITenantedPrincipleNameResolver userNameUtils,
      ITenantedPrincipleNameResolver roleNameUtils, String authenticatedRoleName, String tenantAdminRoleName,
      String repositoryAdminUsername, IRepositoryFileAclDao repositoryFileAclDao, IRepositoryFileDao repositoryFileDao,
      final IPathConversionHelper pathConversionHelper, final ILockHelper lockHelper,
      final IRepositoryDefaultAclHandler defaultAclHandler, final List<String> systemRoles,
      final List<String> extraRoles, UserCache userDetailsCache ) throws NamespaceException {
    this.tenantedUserNameUtils = userNameUtils;
    this.tenantedRoleNameUtils = roleNameUtils;
    this.authenticatedRoleName = authenticatedRoleName;
    this.tenantAdminRoleName = tenantAdminRoleName;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.repositoryFileDao = repositoryFileDao;
    this.pathConversionHelper = pathConversionHelper;
    this.lockHelper = lockHelper;
    this.defaultAclHandler = defaultAclHandler;
    this.systemRoles = systemRoles;
    this.extraRoles = extraRoles;
    this.userDetailsCache = userDetailsCache;
  }

  public void setRoleMembers( Session session, final ITenant theTenant, final String roleName,
      final String[] memberUserNames ) throws RepositoryException, NotFoundException {
    List<IPentahoUser> currentRoleMembers = getRoleMembers( session, theTenant, roleName );
    String[] usersToBeRemoved = findRemovedUsers( currentRoleMembers, memberUserNames );

    // If we are unassigning a user or users from the Administrator role, we need to check if this is a logged in user
    // or a user designated as a system user. If it is then we
    // will display a message to the user.
    if ( ( oneOfUserIsMySelf( usersToBeRemoved ) || oneOfUserIsDefaultAdminUser( usersToBeRemoved ) )
        && tenantAdminRoleName.equals( roleName ) ) {
      throw new RepositoryException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0009_USER_REMOVE_FAILED_YOURSELF_OR_DEFAULT_ADMIN_USER" ) );
    }

    // If this is the last user from the Administrator role, we will not let the user remove.
    if ( tenantAdminRoleName.equals( roleName ) && ( currentRoleMembers != null && currentRoleMembers.size() > 0 )
        && memberUserNames.length == 0 ) {
      throw new RepositoryException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0001_LAST_ADMIN_ROLE", tenantAdminRoleName ) );
    }
    Group jackrabbitGroup = getJackrabbitGroup( theTenant, roleName, session );

    if ( ( jackrabbitGroup == null )
        || !TenantUtils.isAccessibleTenant( theTenant == null ? tenantedRoleNameUtils.getTenant( jackrabbitGroup
            .getID() ) : theTenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0002_ROLE_NOT_FOUND" ) );
    }
    HashMap<String, User> currentlyAssignedUsers = new HashMap<String, User>();
    Iterator<Authorizable> currentMembers = jackrabbitGroup.getMembers();
    while ( currentMembers.hasNext() ) {
      Authorizable member = currentMembers.next();
      if ( member instanceof User ) {
        currentlyAssignedUsers.put( member.getID(), (User) member );
      }
    }

    HashMap<String, User> finalCollectionOfAssignedUsers = new HashMap<String, User>();
    if ( memberUserNames != null ) {
      ITenant tenant = theTenant == null ? JcrTenantUtils.getTenant( roleName, false ) : theTenant;
      for ( String user : memberUserNames ) {
        User jackrabbitUser = getJackrabbitUser( tenant, user, session );
        if ( jackrabbitUser != null ) {
          finalCollectionOfAssignedUsers.put(
            getTenantedUserNameUtils().getPrincipleId( tenant, user ), jackrabbitUser );
        }
      }
    }

    ArrayList<String> usersToRemove = new ArrayList<String>( currentlyAssignedUsers.keySet() );
    usersToRemove.removeAll( finalCollectionOfAssignedUsers.keySet() );

    ArrayList<String> usersToAdd = new ArrayList<String>( finalCollectionOfAssignedUsers.keySet() );
    usersToAdd.removeAll( currentlyAssignedUsers.keySet() );

    for ( String userId : usersToRemove ) {
      jackrabbitGroup.removeMember( currentlyAssignedUsers.get( userId ) );
      purgeUserFromCache( userId );
    }

    for ( String userId : usersToAdd ) {
      jackrabbitGroup.addMember( finalCollectionOfAssignedUsers.get( userId ) );

      // Purge the UserDetails cache
      purgeUserFromCache( userId );
    }
  }

  private void setUserRolesForNewUser( Session session, final ITenant theTenant, final String userName,
      final String[] roles ) throws RepositoryException, NotFoundException {
    Set<String> roleSet = new HashSet<String>();
    if ( roles != null ) {
      roleSet.addAll( Arrays.asList( roles ) );
    }
    roleSet.add( authenticatedRoleName );

    User jackrabbitUser = getJackrabbitUser( theTenant, userName, session );

    if ( ( jackrabbitUser == null )
        || !TenantUtils.isAccessibleTenant( theTenant == null ? tenantedUserNameUtils
            .getTenant( jackrabbitUser.getID() ) : theTenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0003_USER_NOT_FOUND" ) );
    }

    HashMap<String, Group> finalCollectionOfAssignedGroups = new HashMap<String, Group>();
    ITenant tenant = theTenant == null ? JcrTenantUtils.getTenant( userName, true ) : theTenant;
    for ( String role : roleSet ) {
      Group jackrabbitGroup = getJackrabbitGroup( tenant, role, session );
      if ( jackrabbitGroup != null ) {
        finalCollectionOfAssignedGroups.put( tenantedRoleNameUtils.getPrincipleId( tenant, role ), jackrabbitGroup );
      }
    }

    ArrayList<String> groupsToAdd = new ArrayList<String>( finalCollectionOfAssignedGroups.keySet() );

    for ( String groupId : groupsToAdd ) {
      finalCollectionOfAssignedGroups.get( groupId ).addMember( jackrabbitUser );
      // Purge the UserDetails cache
      purgeUserFromCache( userName );
    }
  }

  private void purgeUserFromCache( String userName ) {
    userDetailsCache.removeUserFromCache( getTenantedUserNameUtils().getPrincipleName( userName ) );
  }

  private boolean oneOfUserIsMySelf( String[] users ) {
    for ( int i = 0; i < users.length; i++ ) {
      if ( isMyself( users[i] ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean oneOfUserIsDefaultAdminUser( String[] users ) {
    for ( int i = 0; i < users.length; i++ ) {
      if ( isDefaultAdminUser( users[i] ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean isMyself( String userName ) {
    return PentahoSessionHolder.getSession().getName().equals( userName );
  }

  private boolean isDefaultAdminUser( String userName ) {
    String defaultAdminUser =
        PentahoSystem.get( String.class, "singleTenantAdminUserName", PentahoSessionHolder.getSession() );
    if ( defaultAdminUser != null ) {
      return defaultAdminUser.equals( userName );
    }
    return false;
  }

  private boolean adminRoleExist( String[] newRoles ) {
    return Arrays.asList( newRoles ).contains( tenantAdminRoleName );
  }

  public void setUserRoles( Session session, final ITenant theTenant, final String userName, final String[] roles )
    throws RepositoryException, NotFoundException {

    if ( ( isMyself( userName ) || isDefaultAdminUser( userName ) ) && !adminRoleExist( roles ) ) {
      throw new RepositoryException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0005_YOURSELF_OR_DEFAULT_ADMIN_USER" ) );
    }

    Set<String> roleSet = new HashSet<String>();
    if ( roles != null ) {
      roleSet.addAll( Arrays.asList( roles ) );
    }
    roleSet.add( authenticatedRoleName );

    User jackrabbitUser = getJackrabbitUser( theTenant, userName, session );

    if ( ( jackrabbitUser == null )
        || !TenantUtils.isAccessibleTenant( theTenant == null ? tenantedUserNameUtils
            .getTenant( jackrabbitUser.getID() ) : theTenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0003_USER_NOT_FOUND" ) );
    }
    HashMap<String, Group> currentlyAssignedGroups = new HashMap<String, Group>();
    Iterator<Group> currentGroups = jackrabbitUser.memberOf();
    while ( currentGroups.hasNext() ) {
      Group currentGroup = currentGroups.next();
      currentlyAssignedGroups.put( currentGroup.getID(), currentGroup );
    }

    HashMap<String, Group> finalCollectionOfAssignedGroups = new HashMap<String, Group>();
    ITenant tenant = theTenant == null ? JcrTenantUtils.getTenant( userName, true ) : theTenant;
    for ( String role : roleSet ) {
      Group jackrabbitGroup = getJackrabbitGroup( tenant, role, session );
      if ( jackrabbitGroup != null ) {
        finalCollectionOfAssignedGroups.put( tenantedRoleNameUtils.getPrincipleId( tenant, role ), jackrabbitGroup );
      }
    }

    ArrayList<String> groupsToRemove = new ArrayList<String>( currentlyAssignedGroups.keySet() );
    groupsToRemove.removeAll( finalCollectionOfAssignedGroups.keySet() );

    ArrayList<String> groupsToAdd = new ArrayList<String>( finalCollectionOfAssignedGroups.keySet() );
    groupsToAdd.removeAll( currentlyAssignedGroups.keySet() );

    for ( String groupId : groupsToRemove ) {
      currentlyAssignedGroups.get( groupId ).removeMember( jackrabbitUser );
    }

    for ( String groupId : groupsToAdd ) {
      finalCollectionOfAssignedGroups.get( groupId ).addMember( jackrabbitUser );
    }

    // Purge the UserDetails cache
    purgeUserFromCache( userName );
  }

  public IPentahoRole createRole( Session session, final ITenant theTenant, final String roleName,
      final String description, final String[] memberUserNames ) throws AuthorizableExistsException,
    RepositoryException {
    ITenant tenant = theTenant;
    String role = roleName;
    if ( tenant == null ) {
      tenant = JcrTenantUtils.getTenant( roleName, false );
      role = JcrTenantUtils.getPrincipalName( roleName, false );
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getCurrentTenant();
    }
    if ( !TenantUtils.isAccessibleTenant( tenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0006_TENANT_NOT_FOUND", theTenant.getId() ) );
    }
    String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, role );

    UserManager tenantUserMgr = getUserManager( tenant, session );
    // Intermediate path will always be an empty string. The path is already provided while creating a user manager
    tenantUserMgr.createGroup( new PrincipalImpl( roleId ), "" ); //$NON-NLS-1$
    setRoleMembers( session, tenant, role, memberUserNames );
    setRoleDescription( session, tenant, role, description );
    return getRole( session, theTenant, roleName );
  }

  public IPentahoUser createUser( Session session, final ITenant theTenant, final String userName,
      final String password, final String description, final String[] roles ) throws AuthorizableExistsException,
    RepositoryException {
    ITenant tenant = theTenant;
    String user = userName;
    if ( tenant == null ) {
      tenant = JcrTenantUtils.getTenant( userName, true );
      user = JcrTenantUtils.getPrincipalName( userName, true );
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getCurrentTenant();
    }
    if ( !TenantUtils.isAccessibleTenant( tenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0006_TENANT_NOT_FOUND", theTenant.getId() ) );
    }
    String userId = tenantedUserNameUtils.getPrincipleId( tenant, user );
    UserManager tenantUserMgr = getUserManager( tenant, session );
    tenantUserMgr.createUser( userId, password, new PrincipalImpl( userId ), "" ); //$NON-NLS-1$
    session.save();
    /**
     * This call is absolutely necessary. setUserRolesForNewUser will never inspect what roles this user is a part of.
     * Since this is a new user it will not be a part of new roles
     **/
    setUserRolesForNewUser( session, tenant, user, roles );
    setUserDescription( session, tenant, user, description );
    session.save();
    createUserHomeFolder( tenant, user, session );
    session.save();
    this.userDetailsCache.removeUserFromCache( userName );
    return getUser( session, tenant, userName );
  }

  public void deleteRole( Session session, final IPentahoRole role ) throws NotFoundException, RepositoryException {
    if ( canDeleteRole( session, role ) ) {
      final List<IPentahoUser> roleMembers = this.getRoleMembers( session, role.getTenant(), role.getName() );
      Group jackrabbitGroup = getJackrabbitGroup( role.getTenant(), role.getName(), session );
      if ( jackrabbitGroup != null
          && TenantUtils.isAccessibleTenant( tenantedRoleNameUtils.getTenant( jackrabbitGroup.getID() ) ) ) {
        jackrabbitGroup.remove();
      } else {
        throw new NotFoundException( "" ); //$NON-NLS-1$
      }
      for ( IPentahoUser roleMember : roleMembers ) {
        purgeUserFromCache( roleMember.getUsername() );
      }
    } else {
      throw new RepositoryException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0007_ATTEMPTED_SYSTEM_ROLE_DELETE" ) );
    }
  }

  public void deleteUser( Session session, final IPentahoUser user ) throws NotFoundException, RepositoryException {
    if ( canDeleteUser( session, user ) ) {
      User jackrabbitUser = getJackrabbitUser( user.getTenant(), user.getUsername(), session );
      if ( jackrabbitUser != null
          && TenantUtils.isAccessibleTenant( tenantedUserNameUtils.getTenant( jackrabbitUser.getID() ) ) ) {

        // [BISERVER-9215] Adding new user with same user name as a previously deleted user, defaults to all
        // previous
        // roles
        Iterator<Group> currentGroups = jackrabbitUser.memberOf();
        while ( currentGroups.hasNext() ) {
          currentGroups.next().removeMember( jackrabbitUser );
        }
        purgeUserFromCache( user.getUsername() );
        // [BISERVER-9215]
        jackrabbitUser.remove();
      } else {
        throw new NotFoundException( "" ); //$NON-NLS-1$
      }
    } else {
      throw new RepositoryException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0004_LAST_USER_NEEDED_IN_ROLE", tenantAdminRoleName ) );
    }
  }

  public List<IPentahoRole> getRoles( Session session ) throws RepositoryException {
    return getRoles( session, JcrTenantUtils.getCurrentTenant() );
  }

  private IPentahoUser convertToPentahoUser( User jackrabbitUser ) throws RepositoryException {
    if ( userCache.containsKey( jackrabbitUser.getID() ) ) {
      return (IPentahoUser) userCache.get( jackrabbitUser.getID() );
    }
    IPentahoUser pentahoUser = null;
    Value[] propertyValues = null;

    String description = null;
    try {
      propertyValues = jackrabbitUser.getProperty( "description" ); //$NON-NLS-1$
      description = propertyValues.length > 0 ? propertyValues[0].getString() : null;
    } catch ( Exception ex ) {
      // CHECKSTYLES IGNORE
    }

    Credentials credentials = jackrabbitUser.getCredentials();
    String password = null;
    if ( credentials instanceof CryptedSimpleCredentials ) {
      password = new String( ( (CryptedSimpleCredentials) credentials ).getPassword() );
    }

    pentahoUser =
        new PentahoUser( tenantedUserNameUtils.getTenant( jackrabbitUser.getID() ), tenantedUserNameUtils
            .getPrincipleName( jackrabbitUser.getID() ), password, description, !jackrabbitUser.isDisabled() );

    userCache.put( jackrabbitUser.getID(), pentahoUser );
    return pentahoUser;
  }

  private IPentahoRole convertToPentahoRole( Group jackrabbitGroup ) throws RepositoryException {
    IPentahoRole role = null;
    Value[] propertyValues = null;

    String description = null;
    try {
      propertyValues = jackrabbitGroup.getProperty( "description" ); //$NON-NLS-1$
      description = propertyValues.length > 0 ? propertyValues[0].getString() : null;
    } catch ( Exception ex ) {
      // CHECKSTYLES IGNORE
    }

    role =
        new PentahoRole( tenantedRoleNameUtils.getTenant( jackrabbitGroup.getID() ), tenantedRoleNameUtils
            .getPrincipleName( jackrabbitGroup.getID() ), description );
    return role;
  }

  public List<IPentahoUser> getUsers( Session session ) throws RepositoryException {
    return getUsers( session, JcrTenantUtils.getCurrentTenant() );
  }

  public void setRoleDescription( Session session, final ITenant theTenant, final String roleName,
      final String description ) throws NotFoundException, RepositoryException {
    Group jackrabbitGroup = getJackrabbitGroup( theTenant, roleName, session );
    if ( jackrabbitGroup != null
        && TenantUtils.isAccessibleTenant( theTenant == null ? tenantedRoleNameUtils
            .getTenant( jackrabbitGroup.getID() ) : theTenant ) ) {
      if ( description == null ) {
        jackrabbitGroup.removeProperty( "description" ); //$NON-NLS-1$
      } else {
        jackrabbitGroup.setProperty( "description", session.getValueFactory().createValue( description ) ); //$NON-NLS-1$
      }
    } else {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0002_ROLE_NOT_FOUND" ) );
    }
  }

  public void setUserDescription( Session session, final ITenant theTenant, final String userName,
      final String description ) throws NotFoundException, RepositoryException {
    User jackrabbitUser = getJackrabbitUser( theTenant, userName, session );
    if ( ( jackrabbitUser == null )
        || !TenantUtils.isAccessibleTenant( theTenant == null ? tenantedUserNameUtils
            .getTenant( jackrabbitUser.getID() ) : theTenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0003_USER_NOT_FOUND" ) );
    }
    if ( description == null ) {
      jackrabbitUser.removeProperty( "description" ); //$NON-NLS-1$
    } else {
      jackrabbitUser.setProperty( "description", session.getValueFactory().createValue( description ) ); //$NON-NLS-1$
    }
  }

  public void setPassword( Session session, final ITenant theTenant, final String userName, final String password )
    throws NotFoundException, RepositoryException {
    User jackrabbitUser = getJackrabbitUser( theTenant, userName, session );
    if ( ( jackrabbitUser == null )
        || !TenantUtils.isAccessibleTenant( theTenant == null ? tenantedUserNameUtils
            .getTenant( jackrabbitUser.getID() ) : theTenant ) ) {
      throw new NotFoundException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0003_USER_NOT_FOUND" ) );
    }
    jackrabbitUser.changePassword( password );

    /**
     * BISERVER-9906 Clear cache after changing password
     */
    purgeUserFromCache( userName );
    userCache.remove( jackrabbitUser.getID() );
  }

  public ITenantedPrincipleNameResolver getTenantedUserNameUtils() {
    return tenantedUserNameUtils;
  }

  public ITenantedPrincipleNameResolver getTenantedRoleNameUtils() {
    return tenantedRoleNameUtils;
  }

  public List<IPentahoRole> getRoles( Session session, ITenant tenant ) throws RepositoryException, NamespaceException {
    return getRoles( session, tenant, false );
  }

  public List<IPentahoRole> getRoles( Session session, ITenant theTenant, boolean includeSubtenants )
    throws RepositoryException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    if ( theTenant == null || theTenant.getId() == null ) {
      theTenant = JcrTenantUtils.getTenant();
    }

    if ( TenantUtils.isAccessibleTenant( theTenant ) ) {
      UserManager userMgr = getUserManager( theTenant, session );
      pPrincipalName = ( (SessionImpl) session ).getJCRName( P_PRINCIPAL_NAME );
      Iterator<Authorizable> it = userMgr.findAuthorizables( pPrincipalName, null, UserManager.SEARCH_TYPE_GROUP );
      while ( it.hasNext() ) {
        Group group = (Group) it.next();
        IPentahoRole pentahoRole = convertToPentahoRole( group );
        // Exclude the system role from the list of roles to be returned back
        if ( !extraRoles.contains( pentahoRole.getName() ) ) {
          if ( includeSubtenants ) {
            roles.add( pentahoRole );
          } else {
            if ( pentahoRole.getTenant() != null && pentahoRole.getTenant().equals( theTenant ) ) {
              roles.add( pentahoRole );
            }
          }
        }
      }
    }
    return roles;
  }

  public List<IPentahoUser> getUsers( Session session, ITenant tenant ) throws RepositoryException {
    return getUsers( session, tenant, false );
  }

  public List<IPentahoUser> getUsers( Session session, ITenant theTenant, boolean includeSubtenants )
    throws RepositoryException {
    ArrayList<IPentahoUser> users = new ArrayList<IPentahoUser>();
    if ( theTenant == null || theTenant.getId() == null ) {
      theTenant = JcrTenantUtils.getTenant();
    }
    if ( TenantUtils.isAccessibleTenant( theTenant ) ) {
      UserManager userMgr = getUserManager( theTenant, session );
      pPrincipalName = ( (SessionImpl) session ).getJCRName( P_PRINCIPAL_NAME );
      Iterator<Authorizable> it = userMgr.findAuthorizables( pPrincipalName, null, UserManager.SEARCH_TYPE_USER );
      while ( it.hasNext() ) {
        User user = (User) it.next();
        IPentahoUser pentahoUser = convertToPentahoUser( user );
        if ( includeSubtenants ) {
          users.add( pentahoUser );
        } else {
          if ( pentahoUser.getTenant() != null && pentahoUser.getTenant().equals( theTenant ) ) {
            users.add( pentahoUser );
          }
        }
      }
    }
    return users;
  }

  public IPentahoRole getRole( Session session, final ITenant tenant, final String name ) throws RepositoryException {
    Group jackrabbitGroup = getJackrabbitGroup( tenant, name, session );
    return jackrabbitGroup != null
        && TenantUtils.isAccessibleTenant( tenant == null ? tenantedRoleNameUtils.getTenant( jackrabbitGroup.getID() )
            : tenant ) ? convertToPentahoRole( jackrabbitGroup ) : null;
  }

  private UserManagerImpl getUserManager( ITenant theTenant, Session session ) throws RepositoryException {
    Properties tenantProperties = new Properties();
    tenantProperties.put( UserManagerImpl.PARAM_USERS_PATH, UserManagerImpl.USERS_PATH
        + theTenant.getRootFolderAbsolutePath() );
    tenantProperties.put( UserManagerImpl.PARAM_GROUPS_PATH, UserManagerImpl.GROUPS_PATH
        + theTenant.getRootFolderAbsolutePath() );
    return new UserManagerImpl( (SessionImpl) session, session.getUserID(), tenantProperties );
  }

  public IPentahoUser getUser( Session session, final ITenant tenant, final String name ) throws RepositoryException {
    User jackrabbitUser = getJackrabbitUser( tenant, name, session );
    return jackrabbitUser != null
        && TenantUtils.isAccessibleTenant( tenant == null ? tenantedUserNameUtils.getTenant( jackrabbitUser.getID() )
            : tenant ) ? convertToPentahoUser( jackrabbitUser ) : null;
  }

  private Group getJackrabbitGroup( ITenant theTenant, String name, Session session ) throws RepositoryException {
    Group jackrabbitGroup = null;
    String roleId = name;
    String roleName = name;
    ITenant tenant = theTenant;

    if ( tenant == null ) {
      tenant = JcrTenantUtils.getTenant( roleName, false );
      roleName = JcrTenantUtils.getPrincipalName( roleName, false );
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getCurrentTenant();
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getDefaultTenant();
    }
    roleId = tenantedRoleNameUtils.getPrincipleId( tenant, roleName );

    UserManager userMgr = getUserManager( tenant, session );
    Authorizable authorizable = userMgr.getAuthorizable( roleId );
    if ( authorizable instanceof Group ) {
      jackrabbitGroup = (Group) authorizable;
    }
    return jackrabbitGroup;
  }

  private User getJackrabbitUser( ITenant theTenant, String name, Session session ) throws RepositoryException {
    User jackrabbitUser = null;
    String userId = name;
    String userName = name;
    ITenant tenant = theTenant;
    if ( tenant == null ) {
      tenant = JcrTenantUtils.getTenant( userName, true );
      userName = JcrTenantUtils.getPrincipalName( userName, true );
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getCurrentTenant();
    }
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getDefaultTenant();
    }

    if ( tenant != null ) {
      userId = tenantedUserNameUtils.getPrincipleId( tenant, userName );

      UserManager userMgr = getUserManager( tenant, session );
      Authorizable authorizable = userMgr.getAuthorizable( userId );
      if ( authorizable instanceof User ) {
        jackrabbitUser = (User) authorizable;
      }
    }
    return jackrabbitUser;
  }

  protected boolean tenantExists( String tenantName ) {
    return tenantName != null && tenantName.trim().length() > 0;
  }

  public List<IPentahoUser> getRoleMembers( Session session, final ITenant theTenant, final String roleName )
    throws RepositoryException {
    List<IPentahoUser> users = new ArrayList<IPentahoUser>();
    Group jackrabbitGroup = getJackrabbitGroup( theTenant, roleName, session );
    if ( ( jackrabbitGroup != null )
        && TenantUtils.isAccessibleTenant( theTenant == null ? tenantedRoleNameUtils
            .getTenant( jackrabbitGroup.getID() ) : theTenant ) ) {
      Iterator<Authorizable> authorizables = jackrabbitGroup.getMembers();
      while ( authorizables.hasNext() ) {
        Authorizable authorizable = authorizables.next();
        if ( authorizable instanceof User ) {
          users.add( convertToPentahoUser( (User) authorizable ) );
        }
      }
    }
    return users;
  }

  public List<IPentahoRole> getUserRoles( Session session, final ITenant theTenant, final String userName )
    throws RepositoryException {
    ArrayList<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    User jackrabbitUser = getJackrabbitUser( theTenant, userName, session );
    if ( ( jackrabbitUser != null )
        && TenantUtils.isAccessibleTenant( theTenant == null ? tenantedUserNameUtils.getTenant( jackrabbitUser.getID() )
            : theTenant ) ) {
      Iterator<Group> groups = jackrabbitUser.memberOf();
      while ( groups.hasNext() ) {
        IPentahoRole role = convertToPentahoRole( groups.next() );
        // Exclude the extra role from the list of roles to be returned back
        if ( !extraRoles.contains( role.getName() ) ) {
          roles.add( role );
        }
      }
    }
    return roles;
  }

  private RepositoryFile createUserHomeFolder( ITenant theTenant, String username, Session session )
    throws RepositoryException {
    Builder aclsForUserHomeFolder = null;
    Builder aclsForTenantHomeFolder = null;

    if ( theTenant == null ) {
      theTenant = JcrTenantUtils.getTenant( username, true );
      username = JcrTenantUtils.getPrincipalName( username, true );
    }
    if ( theTenant == null || theTenant.getId() == null ) {
      theTenant = JcrTenantUtils.getCurrentTenant();
    }
    if ( theTenant == null || theTenant.getId() == null ) {
      theTenant = JcrTenantUtils.getDefaultTenant();
    }
    RepositoryFile userHomeFolder = null;
    String userId = tenantedUserNameUtils.getPrincipleId( theTenant, username );
    final RepositoryFileSid userSid = new RepositoryFileSid( userId );
    RepositoryFile tenantHomeFolder = null;
    RepositoryFile tenantRootFolder = null;
    RepositoryFileSid ownerSid = null;
    // Get the Tenant Root folder. If the Tenant Root folder does not exist then exit.
    tenantRootFolder =
        JcrRepositoryFileUtils.getFileByAbsolutePath( session, ServerRepositoryPaths
            .getTenantRootFolderPath( theTenant ), pathConversionHelper, lockHelper, false, null );
    if ( tenantRootFolder != null ) {
      // Try to see if Tenant Home folder exist
      tenantHomeFolder =
          JcrRepositoryFileUtils.getFileByAbsolutePath( session, ServerRepositoryPaths
              .getTenantHomeFolderPath( theTenant ), pathConversionHelper, lockHelper, false, null );

      if ( tenantHomeFolder == null ) {
        String ownerId = tenantedUserNameUtils.getPrincipleId( theTenant, username );
        ownerSid = new RepositoryFileSid( ownerId, Type.USER );

        String tenantAuthenticatedRoleId = tenantedRoleNameUtils.getPrincipleId( theTenant, authenticatedRoleName );
        RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid( tenantAuthenticatedRoleId, Type.ROLE );

        aclsForTenantHomeFolder =
            new RepositoryFileAcl.Builder( userSid ).ace( tenantAuthenticatedRoleSid, EnumSet
                .of( RepositoryFilePermission.READ ) );

        aclsForUserHomeFolder =
            new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
        tenantHomeFolder =
            internalCreateFolder( session, tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
                .getTenantHomeFolderName() ).folder( true ).title(
                Messages.getInstance().getString( "AbstractJcrBackedUserRoleDao.usersFolderDisplayName" ) ).build(),
                aclsForTenantHomeFolder.build(), "tenant home folder" ); //$NON-NLS-1$
      } else {
        String ownerId = tenantedUserNameUtils.getPrincipleId( theTenant, username );
        ownerSid = new RepositoryFileSid( ownerId, Type.USER );
        aclsForUserHomeFolder =
            new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
      }

      // now check if user's home folder exist
      userHomeFolder =
          JcrRepositoryFileUtils.getFileByAbsolutePath( session, ServerRepositoryPaths.getUserHomeFolderPath(
              theTenant, username ), pathConversionHelper, lockHelper, false, null );
      if ( userHomeFolder == null ) {
        userHomeFolder =
            internalCreateFolder( session, tenantHomeFolder.getId(), new RepositoryFile.Builder( username ).folder(
                true ).build(), aclsForUserHomeFolder.build(), "user home folder" ); //$NON-NLS-1$
      }

    }
    return userHomeFolder;
  }

  private RepositoryFile internalCreateFolder( final Session session, final Serializable parentFolderId,
      final RepositoryFile folder, final RepositoryFileAcl acl, final String versionMessage )
    throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId );
    Node folderNode = JcrRepositoryFileUtils.createFolderNode( session, pentahoJcrConstants, parentFolderId, folder );
    // we must create the acl during checkout
    JcrRepositoryFileAclUtils.createAcl( session, pentahoJcrConstants, folderNode.getIdentifier(), acl == null
        ? defaultAclHandler.createDefaultAcl( folder ) : acl );
    session.save();
    if ( folder.isVersioned() ) {
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary( session, pentahoJcrConstants, folderNode,
          versionMessage );
    }
    JcrRepositoryFileUtils.checkinNearestVersionableFileIfNecessary( session, pentahoJcrConstants, parentFolderId,
        Messages.getInstance().getString( "JcrRepositoryFileDao.USER_0001_VER_COMMENT_ADD_FOLDER", folder.getName(),
            ( parentFolderId == null ? "root" : parentFolderId.toString() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    return JcrRepositoryFileUtils.nodeToFile( session, pentahoJcrConstants, pathConversionHelper, lockHelper,
        folderNode );
  }

  /**
   * Checks to see if the removal of the received roles and users would cause the system to have no login associated
   * with the Admin role. This check is to be made before any changes take place
   *
   * @return Error message if invalid or null if ok
   * @throws RepositoryException
   */

  private boolean canDeleteUser( Session session, final IPentahoUser user ) throws RepositoryException {
    boolean userHasAdminRole = false;
    List<IPentahoRole> roles = getUserRoles( null, user.getUsername() );
    for ( IPentahoRole role : roles ) {
      if ( tenantAdminRoleName.equals( role.getName() ) ) {
        userHasAdminRole = true;
        break;
      }
    }

    if ( ( isMyself( user.getUsername() ) || isDefaultAdminUser( user.getUsername() ) ) && userHasAdminRole ) {
      throw new RepositoryException( Messages.getInstance().getString(
          "AbstractJcrBackedUserRoleDao.ERROR_0008_UNABLE_TO_DELETE_USER_IS_YOURSELF_OR_DEFAULT_ADMIN_USER" ) );
    }

    if ( userHasAdminRole ) {
      List<IPentahoUser> usersWithAdminRole = getRoleMembers( session, null, tenantAdminRoleName );
      if ( usersWithAdminRole == null ) {
        throw new RepositoryException( Messages.getInstance().getString(
            "AbstractJcrBackedUserRoleDao.ERROR_0004_LAST_USER_NEEDED_IN_ROLE", tenantAdminRoleName ) );
      }
      if ( usersWithAdminRole.size() > 1 ) {
        return true;
      } else if ( usersWithAdminRole.size() == 1 ) {
        return false;
      } else {
        throw new RepositoryException( Messages.getInstance().getString(
            "AbstractJcrBackedUserRoleDao.ERROR_0004_LAST_USER_NEEDED_IN_ROLE", tenantAdminRoleName ) );
      }
    }
    return true;
  }

  private boolean canDeleteRole( Session session, final IPentahoRole role ) {
    return !( role != null && systemRoles.contains( role.getName() ) );
  }

  private String[] findRemovedUsers( List<IPentahoUser> savedUsers, String[] toBeSaved ) {
    List<String> usersToBeRemoved = new ArrayList<String>();
    List<String> toBeSavedUsers = Arrays.asList( toBeSaved );
    for ( int i = 0; i < savedUsers.size(); i++ ) {
      if ( toBeSavedUsers != null && toBeSaved.length > 0 ) {
        if ( !toBeSavedUsers.contains( savedUsers.get( i ).getUsername() ) ) {
          usersToBeRemoved.add( savedUsers.get( i ).getUsername() );
        }
      } else {
        usersToBeRemoved.add( savedUsers.get( i ).getUsername() );
      }
    }
    return usersToBeRemoved.toArray( new String[0] );
  }
}
