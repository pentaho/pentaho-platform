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

import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.StandaloneTempFileDeleter;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;

public class SessionTempFileDeleter extends StandaloneTempFileDeleter implements HttpSessionBindingListener,
    Serializable, ITempFileDeleter, ILogoutListener {

  private static final long serialVersionUID = 1379936698516655051L;

  static {
    // This registers just one deleter to handle onLogout. The onLogout
    // will get the actual deleter from the session that's going away to call
    // cleanup on it. PRD-5900
    final SessionTempFileDeleter onLogoutHandlerDeleter = new SessionTempFileDeleter();
    PentahoSystem.addLogoutListener( onLogoutHandlerDeleter );
  }

  public SessionTempFileDeleter() {
    super();
  }

  public void valueBound( HttpSessionBindingEvent event ) {
  }

  public void valueUnbound( HttpSessionBindingEvent event ) {
    doTempFileCleanup();
  }

  public void onLogout( IPentahoSession session ) {
    // Implementation of onLogout to do the temp file cleanup as part of the 
    // logout for the user. PRD-5900
    if ( session != null ) {
      ITempFileDeleter realFileDeleter = (ITempFileDeleter) session.getAttribute( ITempFileDeleter.DELETER_SESSION_VARIABLE );
      if ( realFileDeleter != null ) {
        realFileDeleter.doTempFileCleanup();
      }
    }
  }

}
