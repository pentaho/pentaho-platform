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


package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
