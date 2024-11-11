/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IPentahoSession;

public class PentahoSessionParameterProvider extends SimpleParameterProvider {

  private IPentahoSession session;

  public PentahoSessionParameterProvider( final IPentahoSession session ) {
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
