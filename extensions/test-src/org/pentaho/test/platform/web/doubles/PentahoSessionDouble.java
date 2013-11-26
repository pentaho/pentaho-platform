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

package org.pentaho.test.platform.web.doubles;

import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * A test double for IPentahoSession.
 * 
 * @author mlowery
 */
public class PentahoSessionDouble extends LoggerDouble implements IPentahoSession {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private Map<String, Object> attributes;

  private String name;

  private String id;

  private String processId;

  private String actionName;

  private Locale locale;

  private boolean authenticated;

  private boolean backgroundExecutionAlert;

  // ~ Constructors ====================================================================================================

  public PentahoSessionDouble( final String name ) {
    super();
    this.attributes = new HashMap<String, Object>();
    this.name = name;
    this.id = UUID.randomUUID().toString();
    this.locale = Locale.getDefault();
    this.actionName = ""; //$NON-NLS-1$
    this.authenticated = false;
    backgroundExecutionAlert = false;
  }

  // ~ Methods =========================================================================================================

  public void destroy() {
    attributes = null;
  }

  public Object getAttribute( final String attributeName ) {
    return attributes.get( attributeName );
  }

  public Iterator<String> getAttributeNames() {
    return attributes.keySet().iterator();
  }

  public boolean getBackgroundExecutionAlert() {
    return backgroundExecutionAlert;
  }

  public String getId() {
    return id;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getName() {
    return name;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public Object removeAttribute( final String attributeName ) {
    return attributes.remove( attributeName );
  }

  public void resetBackgroundExecutionAlert() {
    backgroundExecutionAlert = false;
  }

  public void setActionName( final String actionName ) {
    this.actionName = actionName;

  }

  public void setAttribute( final String attributeName, final Object value ) {
    attributes.put( attributeName, value );
  }

  public void setAuthenticated( final String name ) {
    if ( name != null ) {
      authenticated = true;
      this.name = name;
    }
  }

  public void setBackgroundExecutionAlert() {
    backgroundExecutionAlert = true;
  }

  public void setNotAuthenticated() {
    name = null;
    authenticated = false;
  }

  public String getActionName() {
    return actionName;
  }

  public String getObjectName() {
    return this.getClass().getName();
  }

  public void setProcessId( final String processId ) {
    this.processId = processId;
  }

  public String getProcessId() {
    return processId;
  }

}
