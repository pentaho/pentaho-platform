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

package org.pentaho.platform.repository2.unified.lifecycle;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;
import java.io.IOException;

/**
 * Default {@link IBackingRepositoryLifecycleManager} implementation.
 * <p/>
 * <p>
 * <strong> Note: You must be careful when changing, overriding, or substituting this class. The configuration of
 * {@code DefaultPentahoJackrabbitAccessControlHelper} depends on the behavior of this class. </strong>
 * </p>
 * 
 * @author mlowery
 */
public class DefaultBackingRepositoryLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================
  IUserRoleDao userRoleDao;

  ITenantManager tenantManager;

  protected String repositoryAdminUsername;

  protected String tenantAdminRoleName;

  protected String systemTenantAdminUserName;

  protected String systemTenantAdminPassword;

  protected String tenantAuthenticatedRoleName;

  protected String tenantAnonymousRoleName;

  protected IRepositoryFileDao repositoryFileDao;

  protected IRepositoryFileAclDao repositoryFileAclDao;

  private IPasswordService passwordService;

  // ~ Constructors
  // ====================================================================================================

  public DefaultBackingRepositoryLifecycleManager( final IRepositoryFileDao contentDao,
      final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
      final String repositoryAdminUsername, final String systemTenantAdminUserName,
      final String systemTenantAdminPassword, final String tenantAdminRoleName,
      final String tenantAuthenticatedRoleName, final String tenantAnonymousRoleName,
      final IPasswordService passwordService, final JcrTemplate adminJcrTemplate,
      final IPathConversionHelper pathConversionHelper ) {
    super( txnTemplate, adminJcrTemplate, pathConversionHelper );
    Assert.notNull( contentDao );
    Assert.notNull( repositoryFileAclDao );
    Assert.hasText( repositoryAdminUsername );
    Assert.hasText( tenantAuthenticatedRoleName );
    this.repositoryFileDao = contentDao;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.repositoryAdminUsername = repositoryAdminUsername;
    this.tenantAuthenticatedRoleName = tenantAuthenticatedRoleName;
    this.tenantAdminRoleName = tenantAdminRoleName;
    this.systemTenantAdminUserName = systemTenantAdminUserName;
    this.tenantAnonymousRoleName = tenantAnonymousRoleName;
    this.systemTenantAdminPassword = systemTenantAdminPassword;
    this.passwordService = passwordService;

  }

  // ~ Methods
  // =========================================================================================================

  protected void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior( TransactionDefinition.PROPAGATION_REQUIRES_NEW );
  }

  @Override
  public synchronized void newTenant( final ITenant tenant ) {
  }

  @Override
  public synchronized void newUser( final ITenant tenant, final String username ) {
    if ( getTenantManager().getUserHomeFolder( tenant, username ) == null ) {
      getTenantManager().createUserHomeFolder( tenant, username );
    }
  }

  @Override
  public void newTenant() {
    newTenant( JcrTenantUtils.getTenant() );
  }

  @Override
  public void newUser() {
  }

  @Override
  public synchronized void shutdown() {
  }

  @Override
  public synchronized void startup() {
    ITenant defaultTenant = null;
    loginAsRepositoryAdmin();
    createCustomPrivilege();
    ITenantManager tenantMgr = getTenantManager();
    ITenant systemTenant =
        tenantMgr.createTenant( null, ServerRepositoryPaths.getPentahoRootFolderName(), tenantAdminRoleName,
            tenantAuthenticatedRoleName, tenantAnonymousRoleName );
    if ( systemTenant != null ) {
      try {
        userRoleDao.createUser( systemTenant, systemTenantAdminUserName, passwordService
            .decrypt( systemTenantAdminPassword ), "System Tenant User", new String[] { tenantAdminRoleName,
              tenantAuthenticatedRoleName } );
        defaultTenant = tenantMgr.getTenant( JcrTenantUtils.getDefaultTenant().getId() );
        if ( defaultTenant == null ) {
          // We'll create the default tenant here... maybe this isn't the best place.
          defaultTenant =
              tenantMgr.createTenant( systemTenant, TenantUtils.TENANTID_SINGLE_TENANT, tenantAdminRoleName,
                  tenantAuthenticatedRoleName, tenantAnonymousRoleName );
        }
      } catch ( Throwable th ) {
        th.printStackTrace();
      }
    }
  }

  /**
   * @return the {@link IBackingRepositoryLifecycleManager} that this instance will use. If none has been
   *         specified, it will default to getting the information from {@link PentahoSystem.get()}
   */
  public ITenantManager getTenantManager() {
    // Check ... if we haven't been injected with a lifecycle manager, get one from PentahoSystem
    try {
      IPentahoObjectFactory objectFactory = PentahoSystem.getObjectFactory();
      IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
      return ( null != tenantManager ? tenantManager : objectFactory.get( ITenantManager.class, "tenantMgrProxy",
          pentahoSession ) );
    } catch ( ObjectFactoryException e ) {
      return null;
    }
  }

  private void createCustomPrivilege() {
    txnTemplate.execute( new TransactionCallbackWithoutResult() {
      public void doInTransactionWithoutResult( final TransactionStatus status ) {
        adminJcrTemplate.execute( new JcrCallback() {
          @Override
          public Object doInJcr( Session session ) throws IOException, RepositoryException {
            PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
            Workspace workspace = session.getWorkspace();
            PrivilegeManager privilegeManager = ( (JackrabbitWorkspace) workspace ).getPrivilegeManager();
            try {
              privilegeManager.getPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE() );
            } catch ( AccessControlException ace ) {
              privilegeManager.registerPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE(), false,
                  new String[0] );
            }
            session.save();
            return null;
          }
        } );
      }
    } );
  }

  /**
   * Sets the {@link IBackingRepositoryLifecycleManager} to be used by this instance
   * 
   * @param lifecycleManager
   *          the lifecycle manager to use (can not be null)
   */
  public void setTenantManager( final ITenantManager tenantManager ) {
    assert ( null != tenantManager );
    this.tenantManager = tenantManager;
  }

  public IUserRoleDao getUserRoleDao() {
    return userRoleDao;
  }

  public void setUserRoleDao( IUserRoleDao userRoleDao ) {
    this.userRoleDao = userRoleDao;
  }

  protected void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession( repositoryAdminUsername );
    pentahoSession.setAuthenticated( repositoryAdminUsername );
    final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[] {};
    final String password = "ignored";
    UserDetails repositoryAdminUserDetails =
        new User( repositoryAdminUsername, password, true, true, true, true, repositoryAdminAuthorities );
    Authentication repositoryAdminAuthentication =
        new UsernamePasswordAuthenticationToken( repositoryAdminUserDetails, password, repositoryAdminAuthorities );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( repositoryAdminAuthentication );
  }
}
