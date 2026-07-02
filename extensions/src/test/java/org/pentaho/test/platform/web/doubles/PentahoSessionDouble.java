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
