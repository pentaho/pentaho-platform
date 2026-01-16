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

import jakarta.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Locale;

public class PentahoHttpSession extends BaseSession {

  private static final long serialVersionUID = 1500696455420691764L;

  private HttpSession session;

  private long authenticationTime = 0L;

  private static final Log logger = LogFactory.getLog( PentahoHttpSession.class );

  @Override
  public Log getLogger() {
    return PentahoHttpSession.logger;
  }

  public PentahoHttpSession( final String userName, final HttpSession session, final Locale locale,
      final IPentahoSession userSession ) {
    super( userName, session.getId(), locale );

    this.session = session;

    // run any session initialization actions
    IParameterProvider sessionParameters = new PentahoSessionParameterProvider( userSession );
    PentahoSystem.sessionStartup( this, sessionParameters );
  }

  @SuppressWarnings( "rawtypes" )
  public Iterator getAttributeNames() {
    return new EnumerationIterator( session.getAttributeNames() );
  }

  public Object getAttribute( final String attributeName ) {
    return session.getAttribute( attributeName );
  }

  public void setAttribute( final String attributeName, final Object value ) {
    session.setAttribute( attributeName, value );
  }

  public Object removeAttribute( final String attributeName ) {
    Object result = getAttribute( attributeName );
    session.removeAttribute( attributeName );
    return result;
  }

  @Override
  public void setAuthenticated( String name ) {
    super.setAuthenticated( name );
    authenticationTime = System.currentTimeMillis();
  }

  @Override
  public void destroy() {
    // audit session destruction
    if ( !"anonymousUser".equals( getName() ) ) {
      AuditHelper.audit( getId(), getName(), getActionName(), getObjectName(),
          "", MessageTypes.SESSION_END, "", "", ( ( System.currentTimeMillis() - authenticationTime ) / 1000F ), null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    super.destroy();
  }

}
