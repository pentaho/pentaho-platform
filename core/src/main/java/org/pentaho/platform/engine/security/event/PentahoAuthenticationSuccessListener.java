/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.util.Assert;

/**
 * Synchronizes the Hitachi Vantara session's principal with the Spring Security {@code Authentication}. This listener fires
 * either on interactive or non-interactive logins.
 * 
 * <p>
 * Replaces functionality from SecurityStartupFilter.
 * </p>
 * 
 * @author mlowery
 */
public class PentahoAuthenticationSuccessListener implements ApplicationListener, Ordered {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( PentahoAuthenticationSuccessListener.class );

  // ~ Instance fields
  // =================================================================================================

  private int order = 100;

  // ~ Constructors
  // ====================================================================================================

  public PentahoAuthenticationSuccessListener() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public void onApplicationEvent( final ApplicationEvent event ) {
    if ( event instanceof AuthenticationSuccessEvent ) {
      logger.debug( "received " + event.getClass().getSimpleName() ); //$NON-NLS-1$
      logger.debug( "synchronizing current IPentahoSession with SecurityContext" ); //$NON-NLS-1$
      try {
        Authentication authentication = ( (AbstractAuthenticationEvent) event ).getAuthentication();
        IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
        Assert.notNull( pentahoSession, "PentahoSessionHolder doesn't have a session" );
        setUserDetailsInPentahoSession( pentahoSession, authentication );
        // audit session creation
        AuditHelper.audit( pentahoSession.getId(), pentahoSession.getName(), pentahoSession.getActionName(),
            pentahoSession.getObjectName(), "", MessageTypes.SESSION_START, "", "", 0, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        logger.info( "The user \"" + pentahoSession.getName() + "\"" + " connected to server with session ID " + pentahoSession.getId() );
      } catch ( Exception e ) {
        logger.error( e.getLocalizedMessage(), e );
      }
    }
  }

  protected void setUserDetailsInPentahoSession( IPentahoSession pentahoSession, Authentication authentication ) {
    pentahoSession.setAuthenticated( authentication.getName() );
    pentahoSession.setAttribute( IPentahoSession.SESSION_ROLES, authentication.getAuthorities() );
  }

  public int getOrder() {
    return order;
  }

  public void setOrder( int order ) {
    this.order = order;
  }

}
