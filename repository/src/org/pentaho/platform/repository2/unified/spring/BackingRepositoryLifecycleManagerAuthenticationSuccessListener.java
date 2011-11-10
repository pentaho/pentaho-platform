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
package org.pentaho.platform.repository2.unified.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.event.authentication.AbstractAuthenticationEvent;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;

import java.util.concurrent.Callable;

/**
 * {@link OrderedAuthenticationListener} that invokes {@link IBackingRepositoryLifecycleManager#newTenant()} and
 * {@link IBackingRepositoryLifecycleManager#newUser()}. This listener fires either on interactive or non-interactive 
 * logins.
 * 
 * @author mlowery
 */
public class BackingRepositoryLifecycleManagerAuthenticationSuccessListener implements ApplicationListener, Ordered {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory
      .getLog(BackingRepositoryLifecycleManagerAuthenticationSuccessListener.class);

  // ~ Instance fields =================================================================================================

  private int order = 200;

  private IBackingRepositoryLifecycleManager lifecycleManager;
  private ISecurityHelper securityHelper;

  // ~ Constructors ====================================================================================================

  public BackingRepositoryLifecycleManagerAuthenticationSuccessListener() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    if (event instanceof AuthenticationSuccessEvent || event instanceof InteractiveAuthenticationSuccessEvent) {
      logger.debug("received AbstractAuthenticationEvent"); //$NON-NLS-1$
      AbstractAuthenticationEvent aEvent = (AbstractAuthenticationEvent) event;

      // Get the lifecycle manager for this event
      final IBackingRepositoryLifecycleManager lifecycleManager = getLifecycleManager();

      try {
        // The newTenant() call should be executed as the system (or more correctly the tenantAdmin)
        SecurityHelper.getInstance().runAsSystem(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            lifecycleManager.newTenant();
            return null;
          }
        });
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage(), e);
      }

      try {
        // run as user to populate SecurityContextHolder and PentahoSessionHolder since Spring Security events are fired
        //   before SecurityContextHolder is set
        SecurityHelper.getInstance().runAsUser(aEvent.getAuthentication().getName(), new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            lifecycleManager.newUser();
            return null;  
          }
        });

      } catch (Exception e) {
        logger.error(e.getLocalizedMessage(), e);
      }
    }
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(final int order) {
    this.order = order;
  }

  public IBackingRepositoryLifecycleManager getLifecycleManager() {
    // Check ... if we haven't been injected with a lifecycle manager, get one from PentahoSystem
    return (null != lifecycleManager ? lifecycleManager : PentahoSystem.get(IBackingRepositoryLifecycleManager.class));
  }

  public void setLifecycleManager(final IBackingRepositoryLifecycleManager lifecycleManager) {
    assert(null != lifecycleManager);
    this.lifecycleManager = lifecycleManager;
  }

}
