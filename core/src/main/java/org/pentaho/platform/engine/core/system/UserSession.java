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
import org.pentaho.platform.api.engine.IParameterProvider;

import java.util.Locale;

public class UserSession extends StandaloneSession {

  private static final long serialVersionUID = -5078190319798278211L;

  private static final Log logger = LogFactory.getLog( UserSession.class );

  @Override
  public Log getLogger() {
    return UserSession.logger;
  }

  public UserSession( final String userName, final String userId, final Locale locale, final boolean authenticated,
      final IParameterProvider sessionParameters ) {
    super( userName, userId, locale );

    if ( authenticated ) {
      setAuthenticated( userName );
      PentahoSystem.sessionStartup( this, sessionParameters );
    }
  }

  public UserSession( final String userName, final Locale locale, final boolean authenticated,
      final IParameterProvider sessionParameters ) {
    this( userName, userName, locale, authenticated, sessionParameters );
  }

  public void doStartupActions( final IParameterProvider sessionParameters ) {
    if ( this.isAuthenticated() ) {
      PentahoSystem.sessionStartup( this, sessionParameters );
    }
  }

  public UserSession( final String userName, final Locale locale, final IParameterProvider sessionParameters ) {
    this( userName, locale, true, sessionParameters );
  }
}
