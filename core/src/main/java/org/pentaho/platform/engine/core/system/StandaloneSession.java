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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ISessionContainer;
import org.pentaho.platform.engine.core.messages.Messages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class StandaloneSession extends BaseSession {

  /**
   * 
   */
  private static final long serialVersionUID = -1614831602086304014L;

  private static final Log logger = LogFactory.getLog( StandaloneSession.class );

  @Override
  public Log getLogger() {
    return StandaloneSession.logger;
  }

  private HashMap attributes;

  public StandaloneSession() {
    this( "unknown" ); //$NON-NLS-1$
  }

  public StandaloneSession( final String name ) {
    this( name, name );
  }

  public StandaloneSession( final String name, final String id ) {
    this( name, id, Locale.getDefault() );
  }

  public StandaloneSession( final String name, final String id, final Locale locale ) {
    super( name, id, locale );
    attributes = new HashMap();
  }

  public Iterator getAttributeNames() {
    if ( attributes == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "StandaloneSession.ERROR_0001_ACCESSING_DESTROYED_SESSION", String.valueOf( Thread.currentThread().getId() ) ) ); //$NON-NLS-1$
    }

    // TODO need to turn the set iterator into an enumeration...
    return attributes.keySet().iterator();
  }

  public Object getAttribute( final String attributeName ) {
    if ( attributes == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "StandaloneSession.ERROR_0001_ACCESSING_DESTROYED_SESSION", String.valueOf( Thread.currentThread().getId() ) ) ); //$NON-NLS-1$
    }
    return attributes.get( attributeName );
  }

  public void setAttribute( final String attributeName, final Object value ) {
    if ( attributes == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "StandaloneSession.ERROR_0001_ACCESSING_DESTROYED_SESSION", String.valueOf( Thread.currentThread().getId() ) ) ); //$NON-NLS-1$
    }

    attributes.put( attributeName, value );
  }

  public Object removeAttribute( final String attributeName ) {
    if ( attributes == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "StandaloneSession.ERROR_0001_ACCESSING_DESTROYED_SESSION", String.valueOf( Thread.currentThread().getId() ) ) ); //$NON-NLS-1$
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
