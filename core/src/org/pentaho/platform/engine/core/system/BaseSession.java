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

package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;

import java.util.Locale;

public abstract class BaseSession extends PentahoBase implements IPentahoSession {

  private static final long serialVersionUID = -8559249546126139228L;

  /**
   * key into the server provided session's attributes to retrieve the IPentahoSession
   */

  private String name;

  private String id;

  private String processId;

  private String actionName;

  private Locale locale;

  private boolean authenticated;

  private volatile boolean backgroundExecutionAlert;

  public BaseSession( final String name, final String id, final Locale locale ) {
    this.name = name;
    this.id = id;
    this.locale = locale;
    actionName = ""; //$NON-NLS-1$
    setLogId( Messages.getInstance().getString( "BaseSession.CODE_LOG_ID", id, ILogger.SESSION_LOG, name ) ); //$NON-NLS-1$
    authenticated = false;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated( final String name ) {
    setAuthenticated( null, name );
  }

  public void setAuthenticated( final String tenantId, final String name ) {
    if ( name != null ) {
      authenticated = true;
      this.name = name;
      if ( tenantId != null ) {
        setAttribute( TENANT_ID_KEY, tenantId );
      } else {
        removeAttribute( TENANT_ID_KEY );
      }
    }
  }

  public void setNotAuthenticated() {
    name = null;
    authenticated = false;
  }

  public Locale getLocale() {
    return locale;
  }

  public void destroy() {
  }

  public void setActionName( final String actionName ) {
    this.actionName = actionName;
  }

  public void setProcessId( final String processId ) {
    this.processId = processId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.session.IPentahoSession#getName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.audit.IAuditable#getId()
   */
  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.audit.IAuditable#getObjectName()
   */
  @Override
  public String getObjectName() {
    return this.getClass().getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.audit.IAuditable#getProcessId()
   */
  public String getProcessId() {
    return processId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.audit.IAuditable#getActionName()
   */
  public String getActionName() {
    return actionName;
  }

  public synchronized void setBackgroundExecutionAlert() {
    this.backgroundExecutionAlert = true;
  }

  public synchronized boolean getBackgroundExecutionAlert() {
    return this.backgroundExecutionAlert;
  }

  public synchronized void resetBackgroundExecutionAlert() {
    this.backgroundExecutionAlert = false;
  }

}
