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


package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ISessionContainer;
import org.pentaho.platform.engine.core.messages.Messages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class StandaloneSession extends BaseSession {
  private static final long serialVersionUID = -1614831602086304014L;
  private static final Log logger = LogFactory.getLog( StandaloneSession.class );
  private static final String ERROR_0001 = "StandaloneSession.ERROR_0001_ACCESSING_DESTROYED_SESSION";

  private HashMap<String, Object> attributes;

  public StandaloneSession() {
    this( "unknown" );
  }

  public StandaloneSession( final String name ) {
    this( name, name );
  }

  public StandaloneSession( final String name, final String id ) {
    this( name, id, Locale.getDefault() );
  }

  public StandaloneSession( final String name, final String id, final Locale locale ) {
    super( name, id, locale );
    attributes = new HashMap<>();
  }

  @Override
  public Log getLogger() {
    return StandaloneSession.logger;
  }

  @SuppressWarnings( "rawtypes" ) // Treated as in IPentahoSession
  public Iterator getAttributeNames() {
    if ( attributes == null ) {
      throw new IllegalStateException(
        Messages.getInstance().getErrorString( ERROR_0001, String.valueOf( Thread.currentThread().getId() ) ) );
    }

    // TODO need to turn the set iterator into an enumeration...
    return attributes.keySet().iterator();
  }

  public Object getAttribute( final String attributeName ) {
    if ( attributes == null ) {
      throw new IllegalStateException(
        Messages.getInstance().getErrorString( ERROR_0001, String.valueOf( Thread.currentThread().getId() ) ) );
    }
    return attributes.get( attributeName );
  }

  public void setAttribute( final String attributeName, final Object value ) {
    if ( attributes == null ) {
      throw new IllegalStateException(
        Messages.getInstance().getErrorString( ERROR_0001, String.valueOf( Thread.currentThread().getId() ) ) );
    }

    // Make it semantically equivalent to HttpSession.setAttribute
    if ( value == null ) {
      attributes.remove( attributeName );
    } else {
      attributes.put( attributeName, value );
    }
  }

  public Object removeAttribute( final String attributeName ) {
    if ( attributes == null ) {
      throw new IllegalStateException(
        Messages.getInstance().getErrorString( ERROR_0001, String.valueOf( Thread.currentThread().getId() ) ) );
    }

    Object result = getAttribute( attributeName );
    attributes.remove( attributeName );
    return result;
  }

  public void destroy() {
    // Clear out references to this session in attributes.
    // See BISERVER-2639 for details
    if ( attributes != null ) {
      for ( Object o : attributes.values() ) {
        if ( o instanceof ISessionContainer ) {
          ISessionContainer c = ( (ISessionContainer) o );
          // XXX: should synchronized check if the session is actually /this/ session
          c.setSession( null );
        }
      }
      attributes = null;
    }
    super.destroy();
  }

}
