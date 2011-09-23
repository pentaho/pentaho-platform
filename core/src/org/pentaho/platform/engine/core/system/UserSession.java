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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan 26, 2006 
 * @author James Dixon
 */

package org.pentaho.platform.engine.core.system;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;

public class UserSession extends StandaloneSession {

  private static final long serialVersionUID = -5078190319798278211L;

  private static final Log logger = LogFactory.getLog(UserSession.class);

  @Override
  public Log getLogger() {
    return UserSession.logger;
  }

  public UserSession(final String userName, final Locale locale, final boolean authenticated,
      final IParameterProvider sessionParameters) {
    super(userName, userName, locale);

    // audit session creation
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_START, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if (authenticated) {
      setAuthenticated(userName);
      PentahoSystem.sessionStartup(this, sessionParameters);
    }
  }

  public void doStartupActions(final IParameterProvider sessionParameters) {
    if (this.isAuthenticated()) {
      PentahoSystem.sessionStartup(this, sessionParameters);
    }
  }

  public UserSession(final String userName, final Locale locale, final IParameterProvider sessionParameters) {
    super(userName, userName, locale);

    // audit session creation
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_START, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    // run any session initialization actions
    PentahoSystem.sessionStartup(this, sessionParameters);

  }
  
  @Override
  public void destroy() {
    // audit session destruction
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_END, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    super.destroy();    
  }

}
