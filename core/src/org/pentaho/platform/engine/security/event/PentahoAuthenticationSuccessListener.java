package org.pentaho.platform.engine.security.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.Authentication;
import org.springframework.security.event.authentication.AbstractAuthenticationEvent;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.util.Assert;

/**
 * Synchronizes the Pentaho session's principal with the Spring Security {@code Authentication}. This listener fires 
 * either on interactive or non-interactive logins.
 * 
 * <p>Replaces functionality from SecurityStartupFilter.</p>
 * 
 * @author mlowery
 */
public class PentahoAuthenticationSuccessListener implements ApplicationListener, Ordered {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoAuthenticationSuccessListener.class);

  // ~ Instance fields =================================================================================================

  private int order = 100;

  // ~ Constructors ====================================================================================================

  public PentahoAuthenticationSuccessListener() {
    super();
  }

  // ~ Methods =========================================================================================================

  public void onApplicationEvent(final ApplicationEvent event) {
    if (event instanceof AuthenticationSuccessEvent) {
      logger.debug("received " + event.getClass().getSimpleName()); //$NON-NLS-1$
      logger.debug("synchronizing current IPentahoSession with SecurityContext"); //$NON-NLS-1$
      try {
        Authentication authentication = ((AbstractAuthenticationEvent) event).getAuthentication();
        IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
        Assert.notNull(pentahoSession, "PentahoSessionHolder doesn't have a session");
        pentahoSession.setAuthenticated(authentication.getName());
        // audit session creation
        AuditHelper.audit(pentahoSession.getId(), pentahoSession.getName(), pentahoSession.getActionName(), pentahoSession.getObjectName(), "", MessageTypes.SESSION_START, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage(), e);
      }
    }
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

}
