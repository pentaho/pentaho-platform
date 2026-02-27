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

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;

public class HttpSessionParameterProvider extends SimpleParameterProvider {

  private IPentahoSession session;

  public HttpSessionParameterProvider( final IPentahoSession session ) {
    this.session = session;
  }

  @Override
  public Object getParameter( final String name ) {
    if ( "name".equals( name ) ) { //$NON-NLS-1$
      return session.getName();
    }
    return session.getAttribute( name );
  }

  @Override
  public String getStringParameter( final String name, final String defaultValue ) {
    Object value = getParameter( name );
    if ( value != null ) {
      return value.toString();
    }
    return defaultValue;
  }

}
