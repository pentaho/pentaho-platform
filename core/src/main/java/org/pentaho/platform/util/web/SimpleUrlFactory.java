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
import org.pentaho.platform.api.engine.IPentahoUrlFactory;

public class SimpleUrlFactory implements IPentahoUrlFactory {

  private String baseUrl;

  public SimpleUrlFactory( final String baseUrl ) {
    this.baseUrl = baseUrl;
  }

  public IPentahoUrl getActionUrlBuilder() {
    return new SimpleUrl( baseUrl );
  }

  public IPentahoUrl getDisplayUrlBuilder() {
    return new SimpleUrl( baseUrl );
  }

}
