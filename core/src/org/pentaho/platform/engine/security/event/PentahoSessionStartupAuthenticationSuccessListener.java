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

package org.pentaho.platform.engine.security.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.util.Assert;

/**
 * Runs the session startup upon hearing an interactive login success.
 * 
 * <p>
 * Replaces functionality from SecurityStartupFilter.
 * </p>
 * 
 * @author mlowery
 */
public class PentahoSessionStartupAuthenticationSuccessListener implements ApplicationListener, Ordered {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( PentahoSessionStartupAuthenticationSuccessListener.class );

  // ~ Instance fields
  // =================================================================================================

  private int order = 150;

  // ~ Constructors
  // ====================================================================================================

  public PentahoSessionStartupAuthenticationSuccessListener() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public void onApplicationEvent( final ApplicationEvent event ) {
    if ( event instanceof InteractiveAuthenticationSuccessEvent || event instanceof AuthenticationSuccessEvent) {
      logger.debug( "received InteractiveAuthenticationSuccessEvent" ); //$NON-NLS-1$
      logger.debug( "calling PentahoSystem.sessionStartup" ); //$NON-NLS-1$
      try {
        IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
        Assert.notNull( pentahoSession, "PentahoSessionHolder doesn't have a session" );
        IParameterProvider sessionParameters = new PentahoSessionParameterProvider( pentahoSession );
        PentahoSystem.sessionStartup( pentahoSession, sessionParameters );
      } catch ( Exception e ) {
        logger.error( e.getLocalizedMessage(), e );
      }
    }
  }

  public int getOrder() {
    return order;
  }

  public void setOrder( int order ) {
    this.order = order;
  }

}
