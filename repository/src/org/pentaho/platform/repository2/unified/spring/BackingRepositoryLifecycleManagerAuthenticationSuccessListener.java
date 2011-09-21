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
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.util.Assert;

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

  // ~ Constructors ====================================================================================================

  public BackingRepositoryLifecycleManagerAuthenticationSuccessListener() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    if (event instanceof AuthenticationSuccessEvent || event instanceof InteractiveAuthenticationSuccessEvent) {
      logger.debug("received AbstractAuthenticationEvent"); //$NON-NLS-1$
      try {
        // by using PentahoSystem instead of dependency injection, this lookup is lazy, allowing PentahoSystem to get
        // initialized before this class uses Jackrabbit (which uses PentahoSystem on login)
        IBackingRepositoryLifecycleManager manager = PentahoSystem.get(IBackingRepositoryLifecycleManager.class);
        Assert.state(manager != null);
        manager.newTenant();
        manager.newUser();
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

}
