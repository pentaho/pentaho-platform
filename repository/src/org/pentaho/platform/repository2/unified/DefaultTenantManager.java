/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 13, 2012 
 * @author wseyler
 */

package org.pentaho.platform.repository2.unified;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.ITenantManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

/**
 * @author wseyler
 *
 */
public class DefaultTenantManager implements ITenantManager {
  // ~ Static fields/initializers ======================================================================================

  protected static final Log logger = LogFactory.getLog(DefaultTenantManager.class);

  // ~ Instance fields =================================================================================================

  /**
   * Repository super user.
   */
  protected String repositoryAdminUsername;

  /**
   * The role name pattern of role belonging to all authenticated users of a given tenant. {0} replaced with tenant ID.
   */
  protected String tenantAuthenticatedAuthorityNamePattern;

  /**
   * When not using multi-tenancy, this value is used as opposed to {@link tenantAuthenticatedAuthorityPattern}.
   */
//  protected String singleTenantAuthenticatedAuthorityName;

  protected JcrTemplate jcrTemplate;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;

  public DefaultTenantManager(final IRepositoryFileDao contentDao, final IRepositoryFileAclDao repositoryFileAclDao, final JcrTemplate jcrTemplate, final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern) {
    Assert.notNull(contentDao);
    Assert.notNull(repositoryFileAclDao);
    Assert.notNull(jcrTemplate);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(tenantAuthenticatedAuthorityNamePattern);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.jcrTemplate = jcrTemplate;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAuthenticatedAuthorityNamePattern = tenantAuthenticatedAuthorityNamePattern;
  }

  @Override
  public Serializable createSystemTenant(final String tenantName) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (Serializable) jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) throws RepositoryException, IOException {
          RepositoryFile systemTenantFolder = repositoryFileDao.getFileByAbsolutePath(RepositoryFile.SEPARATOR + tenantName);
          if (systemTenantFolder == null) {
            Serializable systemTenantFolderId = internalCreateFolder(session, null, new RepositoryFile.Builder(tenantName).folder(true).build(), false, null, tenantName);
            Map<String, Serializable> fileMeta = JcrRepositoryFileUtils.getFileMetadata(session, systemTenantFolderId);
            fileMeta.put(ITenantManager.TENANT_ROOT, true);
            fileMeta.put(ITenantManager.TENANT_ENABLED, true );
            JcrRepositoryFileUtils.setFileMetadata(session, systemTenantFolderId, fileMeta);
            createInitialTenantFolders(systemTenantFolderId);
            return systemTenantFolderId;
          } else {
            return systemTenantFolder.getId();
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#createTenant(java.lang.String, java.lang.String)
   */
  @Override
  public Serializable createTenant(final Serializable parentTenantId, final String tenantName) {
    Serializable tenantRootFolderId = null;
    try {
      tenantRootFolderId = createTenantRootFolder(parentTenantId, tenantName);
      createInitialTenantFolders(tenantRootFolderId);
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return tenantRootFolderId;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#createTenants(java.lang.String, java.util.List)
   */
  @Override
  public List<Serializable> createTenants(final Serializable parentTenantId, final List<String> tenantNames) {
    List<Serializable> newTenants = new ArrayList<Serializable>();
    for (String tenantName : tenantNames) {
      newTenants.add(createTenant(parentTenantId, tenantName));
    }
    return newTenants;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#deleteTenant(java.io.Serializable)
   */
  @Override
  public void deleteTenant(final Serializable tenantFolderId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          if (!isTenantRoot(tenantFolderId)) {
            throw new IllegalArgumentException();
          }
          repositoryFileDao.deleteFile(tenantFolderId, "tenant delete");
          return null;
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#deleteTenant(java.lang.String)
   */
  @Override
  public void deleteTenant(final String tenantPath) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    final Serializable tenantFolderId;
    try {
      tenantFolderId = (Serializable)jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          RepositoryFile tenantRootFolder = repositoryFileDao.getFile(tenantPath);
          return tenantRootFolder.getId();
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    deleteTenant(tenantFolderId);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#deleteTenants(java.util.List)
   */
  @Override
  public void deleteTenants(final List<String> tenantPaths) {
    for (String tenantPath : tenantPaths) {
      deleteTenant(tenantPath);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#disableTenant(java.io.Serializable)
   */
  @Override
  public void disableTenant(final Serializable tenantFolderId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          if (!isTenantRoot(tenantFolderId)) {
            throw new IllegalArgumentException();
          }
          try {
            Map<String, Serializable> fileMeta = JcrRepositoryFileUtils.getFileMetadata(session, tenantFolderId);
            fileMeta.put(ITenantManager.TENANT_ENABLED, false );
            JcrRepositoryFileUtils.setFileMetadata(session, tenantFolderId, fileMeta);
          } catch (RepositoryException e) {
            e.printStackTrace();
          }
          return null;
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#disableTenant(java.lang.String)
   */
  @Override
  public void disableTenant(final String tenantPath) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    final Serializable tenantFolderId;
    try {
      tenantFolderId = (Serializable) jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          //TODO Ensure we're working with a tenantRootFolder
          RepositoryFile tenantRootFolder = repositoryFileDao.getFile(tenantPath);
          return tenantRootFolder.getId();
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    disableTenant(tenantFolderId);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#disableTenants(java.util.List)
   */
  @Override
  public void disableTenants(final List<String> tenantPaths) {
    for (String tenantPath : tenantPaths) {
      disableTenant(tenantPath);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#getChildTenants(java.lang.String)
   */
  @Override
  public List<Serializable> getChildTenants(final String parentTenantPath) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    Serializable parentFolderId;
    try {
        parentFolderId = (Serializable) jcrTemplate.execute(new JcrCallback() {
          public Object doInJcr(final Session session) {
          RepositoryFile tenantParentFolder = repositoryFileDao.getFileByAbsolutePath(DefaultTenantManager.this.getParentPath(parentTenantPath));
          return tenantParentFolder.getId();
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    return getChildTenants(parentFolderId);
  }

  @Override
  public List<Serializable> getChildTenants(final Serializable parentTenantFolderId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (List<Serializable>) jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          if (!isTenantRoot(parentTenantFolderId)) {
            throw new IllegalArgumentException();
          }
          List<Serializable> children = new ArrayList<Serializable>();
          List<RepositoryFile> allChildren = null;
          try {
            allChildren = JcrRepositoryFileUtils.getChildren(session, new PentahoJcrConstants(session), new TenantPathConversionHelper(), null, parentTenantFolderId, null);
          } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          for (RepositoryFile repoFile : allChildren) {
            if (isTenantRoot(repoFile.getId())) { // Absolutely make sure that this is a tenanted folder before adding it
              children.add(repoFile.getId());
            }
          }
          return children;
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }    
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#updateTentant(java.lang.String, java.util.Map)
   */
  @Override
  public void updateTentant(String tenantPath, Map<String, Serializable> tenantInfo) {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#isTenantEnabled(java.io.Serializable)
   */
  @Override
  public boolean isTenantEnabled(final Serializable tenantFolderId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (Boolean) jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          Map<String, Serializable> metadata = repositoryFileDao.getFileMetadata(tenantFolderId);
          
          return isTenantRoot(tenantFolderId) && 
                 metadata.containsKey(ITenantManager.TENANT_ENABLED) && 
                 (Boolean)metadata.get(ITenantManager.TENANT_ENABLED);
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#isTenantEnabled(java.lang.String)
   */
  @Override
  public boolean isTenantEnabled(String tenantPath) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#isTenantRoot(java.io.Serializable)
   */
  @Override
  public boolean isTenantRoot(final Serializable tenantFolderId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    Object result = null;
    try {
      result = jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          Map<String, Serializable> metadata = null;
          try {
            metadata = JcrRepositoryFileUtils.getFileMetadata(session, tenantFolderId);
          } catch (RepositoryException e) {
            return false;
          }
          return metadata.containsKey(ITenantManager.TENANT_ROOT) && 
                 (Boolean)metadata.get(ITenantManager.TENANT_ROOT);
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    
    return (Boolean)result;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#isTenantRoot(java.lang.String)
   */
  @Override
  public boolean isTenantRoot(String tenantPath) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @param parentPath
   * @param tenantName
   * @throws RepositoryException 
   */
  protected void createInitialTenantFolders(final Serializable tenantRootFolderId) throws RepositoryException {
    if (!isTenantRoot(tenantRootFolderId)) {
      throw new IllegalArgumentException();
    }
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    Object result = null;
    try {
      result = jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          RepositoryFile tenantRootFolder;
          try {
            tenantRootFolder = JcrRepositoryFileUtils.getFileById(session, new PentahoJcrConstants(session), new TenantPathConversionHelper(), null, tenantRootFolderId);
            final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
            final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName(tenantRootFolder.getPath());
            if (repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths.getTenantPublicFolderPath(tenantRootFolder.getName())) == null) {
              internalCreateFolder(session, 
                tenantRootFolder.getId(), 
                new RepositoryFile.Builder(ServerRepositoryPaths.getTenantPublicFolderName()).folder(true).build(), 
                false, 
                repositoryAdminUserSid, 
                Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0003_TENANT_PUBLIC")); //$NON-NLS-1$
//              internalAddPermission(tenantPublicFolderId, new RepositoryFileSid(tenantAuthenticatedAuthorityName, RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL, RepositoryFilePermission.WRITE, RepositoryFilePermission.WRITE_ACL));
  
              // home folder used to inherit ACEs from parent ACL but instead now defines non-inherited ACEs since the 
              // user has a UI to modify it if it needs changing; if ACEs were inherited, the ACEs list would be empty in 
              // the UI; this is not desirable UI behavior
              internalCreateFolder(session, 
                tenantRootFolder.getId(), 
                new RepositoryFile.Builder(ServerRepositoryPaths.getTenantHomeFolderName()).folder(true).build(), 
                false, 
                repositoryAdminUserSid, 
                Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0004_TENANT_HOME")); //$NON-NLS-1$
//              internalAddPermission(tenantHomeFolderId, new RepositoryFileSid(tenantAuthenticatedAuthorityName, RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL));
  
              // etc folder inherits ACEs from parent ACL
              internalCreateFolder(session,
                tenantRootFolderId, 
                new RepositoryFile.Builder(ServerRepositoryPaths.getTenantEtcFolderName()).folder(true).build(), 
                true, 
                repositoryAdminUserSid, 
                Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0005_TENANT_ETC")); //$NON-NLS-1$
            }
          } catch (RepositoryException e) {
            return e;
          }
          return null;
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    if (result != null) {
      throw (RepositoryException) result;
    }
  }

  /**
   * @param tenantParentPath
   * @param tenantName
   * @return
   * @throws RepositoryException 
   */
  protected Serializable createTenantRootFolder(final Serializable tenantParentPathId, final String tenantName) throws RepositoryException {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    Object result = null;
    try {
      result = jcrTemplate.execute(new JcrCallback() {
        public Object doInJcr(final Session session) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          try {
            RepositoryFile parentTenant = JcrRepositoryFileUtils.getFileById(session, new PentahoJcrConstants(session), new TenantPathConversionHelper(), null, tenantParentPathId);
            RepositoryFile tenantRootFolder = repositoryFileDao.getFileByAbsolutePath(parentTenant.getPath() + tenantName);
            Serializable tenantRootFolderId = null;
            if (tenantRootFolder == null) {
              tenantRootFolderId = internalCreateFolder(session,
                parentTenant.getId(), 
                new RepositoryFile.Builder(tenantName).folder(true).build(), 
                false, 
                repositoryAdminUserSid, 
                Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0002_VER_COMMENT_TENANT_ROOT")); //$NON-NLS-1$
              Map<String, Serializable> fileMeta = JcrRepositoryFileUtils.getFileMetadata(session, tenantRootFolderId);
              fileMeta.put(ITenantManager.TENANT_ROOT, true);
              fileMeta.put(ITenantManager.TENANT_ENABLED, true );
              JcrRepositoryFileUtils.setFileMetadata(session, tenantRootFolderId, fileMeta);
            } else {
              tenantRootFolderId = tenantRootFolder.getId();
            }
            return tenantRootFolderId;
          } catch (RepositoryException e) {
            return e;
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    if (result instanceof RepositoryException) {
      throw (RepositoryException) result;
    }
    return (Serializable)result;
  }

  protected IPentahoSession createRepositoryAdminPentahoSession() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    return pentahoSession;
  }

  protected Serializable internalCreateFolder(final Session session, final Serializable parentFolderId, final RepositoryFile folder, final boolean inheritAces, final RepositoryFileSid ownerSid, final String versionMessage) throws RepositoryException {
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants(session);
    JcrRepositoryFileUtils.checkoutNearestVersionableFileIfNecessary(session, pentahoJcrConstants, parentFolderId);
    Node folderNode = JcrRepositoryFileUtils.createFolderNode(session, pentahoJcrConstants, parentFolderId, folder);
    final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
//    JcrRepositoryFileAclUtils.setAclMetadata(session, versionMessage, null, null)
    // we must create the acl during checkout
//    aclDao.createAcl(folderNode.getIdentifier(), acl);
    session.save();
    if (folder.isVersioned()) {
      JcrRepositoryFileUtils.checkinNearestVersionableNodeIfNecessary(session, pentahoJcrConstants, folderNode,
          versionMessage);
    }
    JcrRepositoryFileUtils
        .checkinNearestVersionableFileIfNecessary(
            session,
            pentahoJcrConstants,
            parentFolderId,
            Messages
                .getInstance()
                .getString(
                    "JcrRepositoryFileDao.USER_0001_VER_COMMENT_ADD_FOLDER", folder.getName(), (parentFolderId == null ? "root" : parentFolderId.toString()))); //$NON-NLS-1$ //$NON-NLS-2$
    return folderNode.getIdentifier();
  }

  protected RepositoryFileAcl makeAcl(final boolean inheritAces, final RepositoryFileSid ownerSid) {
    return new RepositoryFileAcl.Builder(ownerSid).entriesInheriting(inheritAces).build();
  }

  protected void internalAddPermission(final Serializable fileId, final RepositoryFileSid recipient, final EnumSet<RepositoryFilePermission> permissions) {
    Assert.notNull(fileId);
    Assert.notNull(recipient);
    Assert.notNull(permissions);
    Assert.notEmpty(permissions);

    repositoryFileAclDao.addAce(fileId, recipient, permissions);
  }

  protected String internalGetTenantAuthenticatedAuthorityName(final String tenantId) {
    return MessageFormat.format(tenantAuthenticatedAuthorityNamePattern, tenantId);
  }

  String getParentPath(String parentPath) {
    if (parentPath != null && parentPath.length() > 0) {
      return ServerRepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR + parentPath + RepositoryFile.SEPARATOR;
    } else {
      return ServerRepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR;
    }
  }
  
  String getTenantPath(String parentPath, String tenantName) {
    return getParentPath(parentPath) + tenantName;
  }

  
  /**
   * @author wseyler
   *
   * This class ensures that the DefaultTenantManager is ONLY working with absolute paths
   */
  public class TenantPathConversionHelper implements IPathConversionHelper {

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
    
  }

  private Serializable getIdFromPath(final String path) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    Serializable folderId;
    RepositoryFile folder;
    try {
        folderId = (Serializable) jcrTemplate.execute(new JcrCallback() {
          public Object doInJcr(final Session session) {
          RepositoryFile folder = repositoryFileDao.getFileByAbsolutePath(path);
          return folder.getId();
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    return folderId;
  }

  private boolean isSubTenant(Serializable parentFolderId, Serializable descendantFolderId, List<Serializable> childTenants) {
    for(Serializable tenantId: childTenants) {
      if(tenantId.equals(descendantFolderId)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSubTenant(Serializable parentFolderId, Serializable descendantFolderId) {
    List<Serializable> childTenants = getChildTenants(parentFolderId);
    if(childTenants != null && childTenants.size() > 0) {
      if(isSubTenant(parentFolderId, descendantFolderId, childTenants)) {
        return true;
      } else {
        for(Serializable childTenant: childTenants) {
          boolean done = isSubTenant(childTenant, descendantFolderId);
          if(done) {
            return done;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isSubTenant(String parentTenantPath, String descendantTenantPath) {
    Serializable descendantFolderId = getIdFromPath(descendantTenantPath);
    Serializable parentFolderId = getIdFromPath(parentTenantPath);
    return isSubTenant(parentFolderId, descendantFolderId);
  }
}
