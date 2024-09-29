/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.session;

import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.StandaloneTempFileDeleter;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
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
