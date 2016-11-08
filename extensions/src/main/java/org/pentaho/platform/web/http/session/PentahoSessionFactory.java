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

package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @deprecated obsolete
 */
@Deprecated
public class PentahoSessionFactory {

  public static IPentahoSession getSession( final String userName, final HttpSession session,
      final HttpServletRequest request ) {

    IPentahoSession userSession = (IPentahoSession) session.getAttribute( PentahoSystem.PENTAHO_SESSION_KEY );
    if ( userSession != null ) {
      return userSession;
    }
    userSession = new PentahoHttpSession( userName, session, request.getLocale(), userSession );
    ITempFileDeleter deleter = PentahoSystem.get( ITempFileDeleter.class, userSession );
    if ( deleter != null ) {
      userSession.setAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE, deleter );
    }
    return userSession;

  }

}
