/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jul 11, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.http.session;

import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.system.BaseSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PentahoHttpSession extends BaseSession {

  private static final long serialVersionUID = 1500696455420691764L;

  private HttpSession session;

  private static final Log logger = LogFactory.getLog(PentahoHttpSession.class);

  @Override
  public Log getLogger() {
    return PentahoHttpSession.logger;
  }

  public PentahoHttpSession(final String userName, final HttpSession session, final Locale locale, final IPentahoSession userSession) {
    super(userName, session.getId(), locale);

    this.session = session;
    
    // audit session creation
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_START, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // run any session initialization actions
    IParameterProvider sessionParameters = new PentahoSessionParameterProvider(userSession);
    PentahoSystem.sessionStartup(this, sessionParameters);
}

  public Iterator getAttributeNames() {

    return new EnumerationIterator(session.getAttributeNames());
  }

  public Object getAttribute(final String attributeName) {
    return session.getAttribute(attributeName);
  }

  public void setAttribute(final String attributeName, final Object value) {
    session.setAttribute(attributeName, value);
  }

  public Object removeAttribute(final String attributeName) {
    Object result = getAttribute(attributeName);
    session.removeAttribute(attributeName);
    return result;
  }
  
  @Override
  public void destroy() {
   
    // audit session destruction
    AuditHelper.audit(getId(), getName(), getActionName(), getObjectName(), "", MessageTypes.SESSION_END, "", "", 0, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    super.destroy();
  }

}
