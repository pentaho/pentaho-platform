/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.security.Authentication;
import org.springframework.security.ui.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Pentaho behavior that should be invoked when a web user logs out.
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
