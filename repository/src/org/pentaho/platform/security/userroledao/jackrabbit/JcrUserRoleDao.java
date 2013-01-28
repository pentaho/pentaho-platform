package org.pentaho.platform.security.userroledao.jackrabbit;

import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl.Builder;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrSystemException;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

public class JcrUserRoleDao extends AbstractJcrBackedUserRoleDao  {
  
  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$
  JcrTemplate adminJcrTemplate;
  
  public JcrUserRoleDao(JcrTemplate adminJcrTemplate, ITenantedPrincipleNameResolver userNameUtils, ITenantedPrincipleNameResolver roleNameUtils, String authenticatedRoleName, String tenantAdminRoleName, String repositoryAdminUsername, IRepositoryFileAclDao repositoryFileAclDao, IRepositoryFileDao repositoryFileDao) throws NamespaceException {
    super(userNameUtils, roleNameUtils, authenticatedRoleName, tenantAdminRoleName, repositoryAdminUsername, repositoryFileAclDao, repositoryFileDao);
    this.adminJcrTemplate = adminJcrTemplate;
  }
  
  @Override
  public void setRoleMembers(final ITenant tenant, final String roleName, final String[] memberUserNames) {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setRoleMembers(session, tenant, roleName, memberUserNames);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    } 
  }
  
  @Override
  public void setUserRoles(final ITenant tenant, final String userName, final String[] roles) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setUserRoles(session, tenant, userName, roles);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    }
  }
  
  @Override
  public IPentahoRole createRole(final ITenant tenant, final String roleName, final String description, final String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole)adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return createRole(session, tenant, roleName, description, memberUserNames);
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException("Error creating role.", e);
    }
  }
  
  @Override
  public IPentahoUser createUser(final ITenant tenant, final String userName, final String password, final String description, final String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    final IPentahoUser user;
    try {
      user = (IPentahoUser)adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return createUser(session, tenant, userName, password, description, roles);
        }
      });
      createUserHomeFolder(tenant, userName);
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException("Error creating user.", e);
    }
    return user;
  }

  @Override
  public void deleteRole(final IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          deleteRole(session, role);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error deleting role. " + role.getName(), e);
    }
  }

  @Override
  public void deleteUser(final IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          deleteUser(session, user);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error deleting user. " + user.getUsername(), e);
    }
  }

  @Override
  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    return getRoles(getCurrentTenant());
  }

  @Override
  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return getUsers(getCurrentTenant());
  }

  @Override
  public void setRoleDescription(final ITenant tenant, final String roleName, final String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setRoleDescription(session, tenant, roleName, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    }
  }
  
  @Override
  public void setUserDescription(final ITenant tenant, final String userName, final String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setUserDescription(session, tenant, userName, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating user.", e);
    }
  }
  
  @Override
  public void setPassword(final ITenant tenant, final String userName, final String password) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setPassword(session, tenant, userName, password);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error setting user password.", e);
    }
  }
    
  @Override
  public List<IPentahoRole> getRoles(ITenant tenant) throws UncategorizedUserRoleDaoException {
    return getRoles(tenant, false);
  }

  @Override
  public List<IPentahoRole> getRoles(final ITenant tenant, final boolean includeSubtenants) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>)adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRoles(session, tenant, includeSubtenants);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing roles.", e);
    }   
  }
  
  @Override
  public List<IPentahoUser> getUsers(ITenant tenant) throws UncategorizedUserRoleDaoException {
    return getUsers(tenant, false);
  }

  @Override
  public List<IPentahoUser> getUsers(final ITenant tenant, final boolean includeSubtenants) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>)adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUsers(session, tenant, includeSubtenants);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing users.", e);
    }
  }
  
  @Override
  public IPentahoRole getRole(final ITenant tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole)adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRole(session, tenant, name);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error getting role.", e);
    }
  }
  
  @Override
  public IPentahoUser getUser(final ITenant tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoUser)adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUser(session, tenant, name);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error getting role.", e);
    }
  }
  
  @Override
  public List<IPentahoUser> getRoleMembers(final ITenant tenant, final String roleName) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>) adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRoleMembers(session, tenant, roleName);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing role members.", e);
    }
  }

  @Override
  public List<IPentahoRole> getUserRoles(final ITenant tenant, final String userName) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>) adminJcrTemplate.execute(new JcrCallback() {      
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUserRoles(session, tenant, userName);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing role members.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#createUserHomeFolder(java.lang.String, java.lang.String)
   */
  @Override
  public RepositoryFile createUserHomeFolder(ITenant theTenant, String username) {
    Builder aclsForUserHomeFolder = null;
    Builder aclsForTenantHomeFolder = null;
    
    if (theTenant == null) {
      theTenant = getTenant(username, true);
      username = getPrincipalName(username, true);
    }
    if (theTenant == null || theTenant.getId() == null) {
      theTenant = getCurrentTenant();
    }
    if(theTenant == null || theTenant.getId() == null) {
      theTenant = getDefaultTenant();
    }
    RepositoryFile userHomeFolder = null;
    String userId = tenantedUserNameUtils.getPrincipleId(theTenant, username);
    final RepositoryFileSid userSid = new RepositoryFileSid(userId);
      RepositoryFile tenantHomeFolder = null;
      RepositoryFile tenantRootFolder = null;
      // Get the Tenant Root folder. If the Tenant Root folder does not exist then exit.
      tenantRootFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
          .getTenantRootFolderPath(theTenant));
      if (tenantRootFolder != null) {
        // Try to see if Tenant Home folder exist
        tenantHomeFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths
            .getTenantHomeFolderPath(theTenant));
        if (tenantHomeFolder == null) {
          String ownerId = tenantedUserNameUtils.getPrincipleId(theTenant, username);
          RepositoryFileSid ownerSid = new RepositoryFileSid(ownerId, Type.USER);
          
          String tenantAuthenticatedRoleId = tenantedRoleNameUtils.getPrincipleId(theTenant, authenticatedRoleName);
          RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid(tenantAuthenticatedRoleId, Type.ROLE);
          
          aclsForTenantHomeFolder = new RepositoryFileAcl.Builder(userSid)
            .ace(tenantAuthenticatedRoleSid, EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL));

          aclsForUserHomeFolder = new RepositoryFileAcl.Builder(userSid).ace(ownerSid, EnumSet.of(RepositoryFilePermission.ALL));
          tenantHomeFolder = repositoryFileDao.createFolder(tenantRootFolder.getId(), new RepositoryFile.Builder(
                ServerRepositoryPaths.getTenantHomeFolderName()).folder(true).build(), aclsForTenantHomeFolder.build(), "tenant home folder");
        } else {
          String ownerId = tenantedUserNameUtils.getPrincipleId(theTenant, username);
          RepositoryFileSid ownerSid = new RepositoryFileSid(ownerId, Type.USER);
          aclsForUserHomeFolder = new RepositoryFileAcl.Builder(userSid).ace(ownerSid, EnumSet.of(RepositoryFilePermission.ALL));
        }
        
        // now check if user's home folder exist
        userHomeFolder = repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths.getUserHomeFolderPath(theTenant, username));
        if (userHomeFolder == null) {
          userHomeFolder = repositoryFileDao.createFolder(tenantHomeFolder.getId(),
              new RepositoryFile.Builder(username).folder(true).build(),
              aclsForUserHomeFolder.build(), "user home folder"); //$NON-NLS-1$
        }

      }
      return userHomeFolder;
 }
}