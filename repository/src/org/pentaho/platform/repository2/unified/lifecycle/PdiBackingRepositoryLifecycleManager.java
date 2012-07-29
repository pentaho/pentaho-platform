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
 */
package org.pentaho.platform.repository2.unified.lifecycle;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Initializes folders used by Pentaho Data Integration.
 *
 * @author mlowery
 */
public class PdiBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  private ITenantedPrincipleNameResolver nameUtils = new DefaultTenantedPrincipleNameResolver();
  
  public PdiBackingRepositoryLifecycleManager(final IRepositoryFileDao contentDao,
                                              final IRepositoryFileAclDao repositoryFileAclDao, final TransactionTemplate txnTemplate,
                                              final String repositoryAdminUsername, final String tenantAuthenticatedAuthorityNamePattern) {
  }

  // ~ Methods =========================================================================================================
  @Override
  public void startup() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void newTenant(String tenantId) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void newTenant() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void newUser(String tenantId, String username) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void newUser() {
    // TODO Auto-generated method stub
    
  }
}
