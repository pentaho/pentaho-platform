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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
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
  protected String singleTenantAuthenticatedAuthorityName;

  /**
   * When not using multi-tenancy, this value is used as opposed to {@link tenantAdminAuthorityPattern}.
   */
  protected String singleTenantAdminAuthorityName;

  protected TransactionTemplate txnTemplate;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;

  public DefaultTenantManager(final IRepositoryFileDao contentDao, final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate, final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern, final String singleTenantAuthenticatedAuthorityName) {
    Assert.notNull(contentDao);
    Assert.notNull(repositoryFileAclDao);
    Assert.notNull(txnTemplate);
    Assert.hasText(repositoryAdminUsername);
    Assert.hasText(tenantAuthenticatedAuthorityNamePattern);
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.txnTemplate = txnTemplate;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAuthenticatedAuthorityNamePattern = tenantAuthenticatedAuthorityNamePattern;
    this.singleTenantAuthenticatedAuthorityName = singleTenantAuthenticatedAuthorityName;
    initTransactionTemplate();
  }

  @Override
  public RepositoryFile createSystemTenant(final String tenantName) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (RepositoryFile) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile rootFolder = repositoryFileDao.getFileByAbsolutePath(tenantName);
          if (rootFolder == null) {
            // because this is running as the repo admin, the owner of this folder is the repo admin who also has full
            // control (no need to do a setOwner call); also, inherit from parent to let everyone see this folder
            rootFolder = internalCreateFolder(null, new RepositoryFile.Builder(ServerRepositoryPaths
                .getPentahoRootFolderName()).folder(true).build(), true, repositoryAdminUserSid, Messages.getInstance()
                .getString("DefaultRepositoryLifecycleManager.USER_0001_VER_COMMENT_PENTAHO_ROOT")); //$NON-NLS-1$
            // no aces added here; access to tenant root is governed by DefaultPentahoJackrabbitAccessControlHelper
            // Since the root folder is the root of all tenants it needs to have the metadata of a tenant... but not the extended structure
            Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata(rootFolder.getId());
            fileMeta.put(ITenantManager.TENANT_ROOT, true);
            fileMeta.put(ITenantManager.TENANT_ENABLED, true );
            repositoryFileDao.setFileMetadata(rootFolder.getId(), fileMeta);
          }
          return rootFolder;
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
  public RepositoryFile createTenant(String parentPath, String tenantName) {
    RepositoryFile tenantRootFolder = createTenantRootFolder(parentPath, tenantName);
    createInitialTenantFolders(tenantRootFolder);
    return tenantRootFolder;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#createTenants(java.lang.String, java.util.List)
   */
  @Override
  public List<RepositoryFile> createTenants(String parentPath, List<String> tenantNames) {
    List<RepositoryFile> newTenants = new ArrayList<RepositoryFile>();
    for (String tenantName : tenantNames) {
      newTenants.add(createTenant(parentPath, tenantName));
    }
    return newTenants;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#deleteTenant(java.io.Serializable)
   */
  @Override
  public void deleteTenant(final Serializable tenantId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          // TODO ensure that we're working with a tenant directory
          repositoryFileDao.deleteFile(tenantId, "tenant delete");
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
      tenantFolderId = (Serializable) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          //TODO Ensure we're working with a tenantRootFolder
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
  public void deleteTenants(List<String> tenantPaths) {
    for (String tenantPath : tenantPaths) {
      deleteTenant(tenantPath);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#disableTenant(java.io.Serializable)
   */
  @Override
  public void disableTenant(final Serializable tenantId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          //TODO Ensure we're working with a tenantRootFolder
          Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata(tenantId);
          fileMeta.put(ITenantManager.TENANT_ENABLED, false);
          repositoryFileDao.setFileMetadata(tenantId, fileMeta);
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
      tenantFolderId = (Serializable) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
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
  public void disableTenants(List<String> tenantPaths) {
    for (String tenantPath : tenantPaths) {
      disableTenant(tenantPath);
    }
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#getChildTenants(java.lang.String)
   */
  @Override
  public List<RepositoryFile> getChildTenants(final String parentPath) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    Serializable parentFolderId;
    try {
        parentFolderId = (Serializable) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          RepositoryFile tenantParentFolder = repositoryFileDao.getFileByAbsolutePath(DefaultTenantManager.this.getParentPath(parentPath));
          return tenantParentFolder.getId();
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
    return getChildTenants(parentFolderId);
  }

  public List<RepositoryFile> getChildTenants(final Serializable parentFolderId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (List<RepositoryFile>) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          RepositoryFile tenantParentFolder = repositoryFileDao.getFileById(parentFolderId);
          assert(isTenantRoot(tenantParentFolder.getId()));
          List<RepositoryFile> children = new ArrayList<RepositoryFile>();
          List<RepositoryFile> allChildren = repositoryFileDao.getChildren(tenantParentFolder.getId(), null);
          for (RepositoryFile repoFile : allChildren) {
            if (isTenantRoot(repoFile.getId())) {
              children.add(repoFile);
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

  protected void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.repository2.unified.ITenantManager#isTenantEnabled(java.io.Serializable)
   */
  @Override
  public boolean isTenantEnabled(final Serializable tenantRootfileId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (Boolean) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          Map<String, Serializable> metadata = repositoryFileDao.getFileMetadata(tenantRootfileId);
          
          return isTenantRoot(tenantRootfileId) && 
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
  public boolean isTenantRoot(final Serializable tenantRootfileId) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (Boolean) txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          Map<String, Serializable> metadata = repositoryFileDao.getFileMetadata(tenantRootfileId);
          
          return metadata.containsKey(ITenantManager.TENANT_ROOT) && 
                 (Boolean)metadata.get(ITenantManager.TENANT_ROOT);
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
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
   */
  protected void createInitialTenantFolders(final RepositoryFile tenantRootFolder) {
    final String tenantAuthenticatedAuthorityName = internalGetTenantAuthenticatedAuthorityName(tenantRootFolder.getPath());
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      txnTemplate.execute(new TransactionCallbackWithoutResult() {
        public void doInTransactionWithoutResult(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          Assert.notNull(tenantRootFolder);
          if (repositoryFileDao.getFileByAbsolutePath(ServerRepositoryPaths.getTenantPublicFolderPath(tenantRootFolder.getName())) == null) {
            RepositoryFile tenantPublicFolder = internalCreateFolder(tenantRootFolder.getId(), new RepositoryFile.Builder(ServerRepositoryPaths.getTenantPublicFolderName()).folder(true).build(), false, repositoryAdminUserSid, Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0003_TENANT_PUBLIC")); //$NON-NLS-1$
            internalAddPermission(tenantPublicFolder.getId(), new RepositoryFileSid(tenantAuthenticatedAuthorityName, RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL, RepositoryFilePermission.WRITE, RepositoryFilePermission.WRITE_ACL));

            // home folder used to inherit ACEs from parent ACL but instead now defines non-inherited ACEs since the 
            // user has a UI to modify it if it needs changing; if ACEs were inherited, the ACEs list would be empty in 
            // the UI; this is not desirable UI behavior
            RepositoryFile tenantHomeFolder = internalCreateFolder(tenantRootFolder.getId(), new RepositoryFile.Builder(ServerRepositoryPaths.getTenantHomeFolderName()).folder(true).build(), false, repositoryAdminUserSid, Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0004_TENANT_HOME")); //$NON-NLS-1$
            internalAddPermission(tenantHomeFolder.getId(), new RepositoryFileSid(tenantAuthenticatedAuthorityName, RepositoryFileSid.Type.ROLE), EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL));

            // etc folder inherits ACEs from parent ACL
            internalCreateFolder(tenantRootFolder.getId(), new RepositoryFile.Builder(ServerRepositoryPaths.getTenantEtcFolderName()).folder(true).build(), true, repositoryAdminUserSid, Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0005_TENANT_ETC")); //$NON-NLS-1$
          }
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  /**
   * @param parentPath
   * @param tenantName
   * @return
   */
  protected RepositoryFile createTenantRootFolder(final String parentPath, final String tenantName) {
    IPentahoSession origPentahoSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession(createRepositoryAdminPentahoSession());
    try {
      return (RepositoryFile)txnTemplate.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus status) {
          final RepositoryFileSid repositoryAdminUserSid = new RepositoryFileSid(repositoryAdminUsername);
          RepositoryFile parentTenant = repositoryFileDao.getFileByAbsolutePath(DefaultTenantManager.this.getParentPath(parentPath));
          RepositoryFile tenantRootFolder = repositoryFileDao.getFileByAbsolutePath(DefaultTenantManager.this.getTenantPath(parentPath, tenantName));
          if (tenantRootFolder == null) {
            tenantRootFolder = internalCreateFolder(parentTenant.getId(), new RepositoryFile.Builder(tenantName).folder(true).build(), false, repositoryAdminUserSid, Messages.getInstance().getString("DefaultRepositoryLifecycleManager.USER_0002_VER_COMMENT_TENANT_ROOT")); //$NON-NLS-1$
            // no aces added here; access to tenant root is governed by DefaultPentahoJackrabbitAccessControlHelper
            // Here is where we tell the system that we're a tenant
            Map<String, Serializable> fileMeta = repositoryFileDao.getFileMetadata(tenantRootFolder.getId());
            fileMeta.put(ITenantManager.TENANT_ROOT, true);
            fileMeta.put(ITenantManager.TENANT_ENABLED, true);
            repositoryFileDao.setFileMetadata(tenantRootFolder.getId(), fileMeta);
          }
          return tenantRootFolder;
        }
      });
    } finally {
      PentahoSessionHolder.setSession(origPentahoSession);
    }
  }

  protected IPentahoSession createRepositoryAdminPentahoSession() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    return pentahoSession;
  }

  protected RepositoryFile internalCreateFolder(final Serializable parentFolderId, final RepositoryFile file, final boolean inheritAces, final RepositoryFileSid ownerSid, final String versionMessage) {
    Assert.notNull(file);

    return repositoryFileDao.createFolder(parentFolderId, file, makeAcl(inheritAces, ownerSid), versionMessage);
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
    if (!TenantUtils.TENANTID_SINGLE_TENANT.equals(tenantId)) {
      return MessageFormat.format(tenantAuthenticatedAuthorityNamePattern, tenantId);
    } else {
      return singleTenantAuthenticatedAuthorityName;
    }
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

}
