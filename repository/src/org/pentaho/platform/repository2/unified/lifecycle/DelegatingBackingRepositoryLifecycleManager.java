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

import java.util.List;

import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.springframework.util.Assert;

/**
 * An {@link IBackingRepositoryLifecycleManager} that does nothing itself but instead delegates to an ordered collection 
 * of other {@link IBackingRepositoryLifecycleManager} instances.
 * 
 * @author mlowery
 */
public class DelegatingBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private List<IBackingRepositoryLifecycleManager> managers;

  // ~ Constructors ====================================================================================================

  public DelegatingBackingRepositoryLifecycleManager(final List<IBackingRepositoryLifecycleManager> managers) {
    super();
    Assert.notNull(managers);
    this.managers = managers;
  }

  // ~ Methods =========================================================================================================

  public synchronized void newTenant() {
    for (IBackingRepositoryLifecycleManager manager : managers) {
      manager.newTenant();
    }
  }

  public synchronized void newTenant(final String tenantId) {
    for (IBackingRepositoryLifecycleManager manager : managers) {
      manager.newTenant(tenantId);
    }
  }

  public synchronized void newUser() {
    for (IBackingRepositoryLifecycleManager manager : managers) {
      manager.newUser();
    }
  }

  public synchronized void newUser(final String tenantId, final String username) {
    for (IBackingRepositoryLifecycleManager manager : managers) {
      manager.newUser(tenantId, username);
    }
  }

  public synchronized void shutdown() {
    for (IBackingRepositoryLifecycleManager manager : managers) {
      manager.shutdown();
    }
  }

  public synchronized void startup() {
    for (IBackingRepositoryLifecycleManager manager : managers) {
      manager.startup();
    }
  }

}
