package org.pentaho.platform.repository2.mt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

public abstract class AbstractRepositoryTenantManager implements ITenantManager {

  public static final String FOLDER_NAME_AUTHZ = ".authz"; //$NON-NLS-1$

  public static final String FOLDER_NAME_ROLEBASED = "roleBased"; //$NON-NLS-1$

  public static final String FOLDER_NAME_RUNTIMEROLES = "runtimeRoles"; //$NON-NLS-1$
  
  protected IRepositoryFileAclDao repositoryFileAclDao;

  protected IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  protected IUserRoleDao userRoleDao;

  protected IRepositoryFileDao repositoryFileDao;

  protected ITenantedPrincipleNameResolver tenantedRoleNameResolver;

  protected String repositoryAdminUsername;

  protected String tenantAdminRoleName;
  
  protected String tenantAuthenticatedRoleName;

  protected IPathConversionHelper pathConversionHelper = new IPathConversionHelper() {

    /* (non-Javadoc)
     * @see org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper#absToRel(java.lang.String)
     */
    @Override
    public String absToRel(String absPath) {
      return absPath;
    }

    /* (non-Javadoc)
     * @see org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper#relToAbs(java.lang.String)
     */
    @Override
    public String relToAbs(String relPath) {
      return relPath;
    }

  };

  protected AbstractRepositoryTenantManager(final IRepositoryFileDao contentDao, final IUserRoleDao userRoleDao,
      final IRepositoryFileAclDao repositoryFileAclDao, IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
      final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern,
      final ITenantedPrincipleNameResolver tenantedRoleNameResolver,
      final String tenantAdminRoleName) {
    Assert.notNull(contentDao);
    Assert.notNull(repositoryFileAclDao);
    Assert.notNull(roleBindingDao);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(tenantAuthenticatedAuthorityNamePattern);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.userRoleDao = userRoleDao;
    this.roleBindingDao = roleBindingDao;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAdminRoleName = tenantAdminRoleName;
    this.tenantAuthenticatedRoleName = tenantAuthenticatedAuthorityNamePattern;
    this.tenantedRoleNameResolver = tenantedRoleNameResolver;
  }

  public void deleteTenants(Session session, final List<ITenant> tenants) throws RepositoryException {
    for (ITenant tenant : tenants) {
      deleteTenant(session, tenant);
    }
  }

  private void deleteUserRole(Session session, ITenant parentTenant, List<ITenant> tenants) throws RepositoryException {
      for (ITenant tenant : tenants) {
        deleteUserRole(session, tenant, getChildTenants(session, tenant));
      }
      for (IPentahoRole role : userRoleDao.getRoles(parentTenant)) {
        userRoleDao.deleteRole(role);
      }
      for (IPentahoUser user : userRoleDao.getUsers(parentTenant)) {
        userRoleDao.deleteUser(user);
      }
  }

  public void deleteTenant(Session jcrSession, final ITenant tenant) throws RepositoryException {
    deleteUserRole(jcrSession, tenant, getChildTenants(jcrSession, tenant));
    repositoryFileDao.permanentlyDeleteFile(getTenantRootFolder(jcrSession, tenant).getId(), "tenant delete");
  }

  public void enableTenant(Session session, final ITenant tenant, final boolean enable) throws ItemNotFoundException,
      RepositoryException {
    Map<String, Serializable> fileMeta = JcrRepositoryFileUtils.getFileMetadata(session,
        getTenantRootFolder(session, tenant).getId());
    fileMeta.put(ITenantManager.TENANT_ENABLED, enable);
    JcrRepositoryFileUtils.setFileMetadata(session, getTenantRootFolder(session, tenant).getId(), fileMeta);
  }

  @Override
  public RepositoryFile getTenantRootFolder(ITenant tenant) {
    RepositoryFile rootFolder = repositoryFileDao.getFileByAbsolutePath(tenant.getRootFolderAbsolutePath());
    if (rootFolder != null) {
      Map<String, Serializable> metadata = repositoryFileDao.getFileMetadata(rootFolder.getId());
      if (!metadata.containsKey(ITenantManager.TENANT_ROOT) || !(Boolean) metadata.get(ITenantManager.TENANT_ROOT)) {
        rootFolder = null;
      }
    }
    return rootFolder;
  }

  private RepositoryFile getTenantRootFolder(Session session, final ITenant tenant) throws RepositoryException {
    RepositoryFile rootFolder = JcrRepositoryFileUtils.getFileByAbsolutePath(session, tenant.getRootFolderAbsolutePath(),
        pathConversionHelper, null, false);
    if (rootFolder != null) {
      Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata(session, rootFolder.getId());
      if (!metadata.containsKey(ITenantManager.TENANT_ROOT) || !(Boolean) metadata.get(ITenantManager.TENANT_ROOT)) {
        rootFolder = null;
      }
    }
    return rootFolder;

  }

  public void enableTenants(Session session, final List<ITenant> tenants, final boolean enable)
      throws ItemNotFoundException, RepositoryException {
    for (ITenant tenant : tenants) {
      enableTenant(session, tenant, enable);
    }
  }

  public List<ITenant> getChildTenants(Session session, final ITenant parentTenant, final boolean includeDisabledTenants)
      throws RepositoryException {
    List<ITenant> children = new ArrayList<ITenant>();
    List<RepositoryFile> allChildren = JcrRepositoryFileUtils.getChildren(session, new PentahoJcrConstants(session),
        pathConversionHelper, null, getTenantRootFolder(session, parentTenant).getId(), null);
    for (RepositoryFile repoFile : allChildren) {
      Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata(session, repoFile.getId());
      if (metadata.containsKey(ITenantManager.TENANT_ROOT) && (Boolean) metadata.get(ITenantManager.TENANT_ROOT)) {
        Tenant tenant = new Tenant(repoFile.getPath(), isTenantEnabled(session, repoFile.getId()));
        if (includeDisabledTenants || tenant.isEnabled()) {
          children.add(new Tenant(repoFile.getPath(), isTenantEnabled(session, repoFile.getId())));
        }

      }
    }
    return children;
  }

  public List<ITenant> getChildTenants(Session session, final ITenant parentTenant) throws RepositoryException {
    return getChildTenants(session, parentTenant, false);
  }

  public void updateTentant(Session jcrSession, String arg0, Map<String, Serializable> arg1) {
  }

  protected void createInitialTenantFolders(Session session, final RepositoryFile tenantRootFolder,
      final RepositoryFileSid fileOwnerSid, final RepositoryFileSid authenticatedRoleSid) throws RepositoryException {
    // We create a tenant's home folder while creating a user
    repositoryFileDao.createFolder(tenantRootFolder.getId(),
        new RepositoryFile.Builder(ServerRepositoryPaths.getTenantPublicFolderName()).folder(true).build(),
        new RepositoryFileAcl.Builder(fileOwnerSid).build(), null);
    repositoryFileDao.createFolder(tenantRootFolder.getId(),
        new RepositoryFile.Builder(ServerRepositoryPaths.getTenantEtcFolderName()).folder(true).build(),
        new RepositoryFileAcl.Builder(fileOwnerSid).build(), null);
  }

  protected void setAsSystemFolder(Serializable fileId) {
    Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata(fileId);
    fileMeta.put(IUnifiedRepository.SYSTEM_FOLDER, true);
    repositoryFileDao.setFileMetadata(fileId, fileMeta);

  }


  
  private boolean isTenantEnabled(Session session, final Serializable tenantFolderId) throws ItemNotFoundException,
      RepositoryException {
    Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata(session, tenantFolderId);

    return metadata.containsKey(ITenantManager.TENANT_ENABLED) && (Boolean) metadata.get(ITenantManager.TENANT_ENABLED);
  }

  public boolean isSubTenant(Session jcrSession, ITenant parentTenant, ITenant descendantTenant) {
    return internalIsSubTenant(parentTenant, descendantTenant);
  }

  private boolean internalIsSubTenant(ITenant descendantTenant, List<ITenant> childTenants) {
    for (ITenant tenant : childTenants) {
      if (tenant != null) {
        if (tenant.equals(childTenants)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean internalIsSubTenant(ITenant parentTenant, ITenant descendantTenant) {
    if (parentTenant.equals(descendantTenant)) {
      return true;
    } else {
      List<ITenant> childTenants = getChildTenants(parentTenant);
      if (childTenants != null && childTenants.size() > 0) {
        if (internalIsSubTenant(descendantTenant, childTenants)) {
          return true;
        } else {
          for (ITenant childTenant : childTenants) {
            boolean done = internalIsSubTenant(childTenant, descendantTenant);
            if (done) {
              return done;
            }
          }
        }
      } else {
        return false;
      }
    }
    return false;
  }

  public ITenant getTenant(Session session, String tenantId) throws RepositoryException {
    ITenant tenant = null;
    RepositoryFile tenantRootFolder = JcrRepositoryFileUtils.getFileByAbsolutePath(session, tenantId,
        pathConversionHelper, null, false);
    if ((tenantRootFolder != null) && isTenantRoot(session, tenantRootFolder.getId())) {
      tenant = new Tenant(tenantId, isTenantEnabled(session, tenantRootFolder.getId()));
    }
    return tenant;
  }

  private boolean isTenantRoot(Session session, final Serializable tenantFolderId) throws ItemNotFoundException,
      RepositoryException {
    Map<String, Serializable> metadata = JcrRepositoryFileUtils.getFileMetadata(session, tenantFolderId);

    return metadata.containsKey(ITenantManager.TENANT_ROOT) && (Boolean) metadata.get(ITenantManager.TENANT_ROOT);
  }
}