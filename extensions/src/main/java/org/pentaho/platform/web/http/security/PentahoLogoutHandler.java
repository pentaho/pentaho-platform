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


package org.pentaho.platform.web.http.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Hitachi Vantara behavior that should be invoked when a web user logs out.
 * 
 * @author mlowery
 * @see org.springframework.security.ui.logout.LogoutHandler
 * @see org.springframework.security.ui.logout.LogoutFilter
 */
public class PentahoLogoutHandler implements LogoutHandler {
  private static final Log logger = LogFactory.getLog( PentahoLogoutHandler.class );

  public void logout( final HttpServletRequest request, final HttpServletResponse response,
      final Authentication authentication ) {
    if ( PentahoLogoutHandler.logger.isDebugEnabled() ) {
      PentahoLogoutHandler.logger
          .debug( Messages.getInstance().getString( "PentahoLogoutHandler.DEBUG_HANDLE_LOGOUT" ) ); //$NON-NLS-1$
    }
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    PentahoSystem.invokeLogoutListeners( userSession );
    // removeSession call here is analogous to clearContext call in SecurityContextLogoutHandler
    PentahoSessionHolder.removeSession();
  }

}
