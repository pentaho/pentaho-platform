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


package org.pentaho.platform.util.web;

import org.pentaho.platform.api.engine.IPentahoUrl;

public class SimpleUrl implements IPentahoUrl {

  private StringBuffer url;

  public SimpleUrl( final String baseUrl ) {
    url = new StringBuffer( baseUrl );
  }

  public void setParameter( final String name, final String value ) {
    url.append( "&" ).append( name ).append( "=" ).append( value ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public String getUrl() {
    return url.toString();
  }

}
