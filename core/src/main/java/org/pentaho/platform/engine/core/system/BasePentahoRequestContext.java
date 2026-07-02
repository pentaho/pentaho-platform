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

import org.pentaho.platform.api.engine.IPentahoRequestContext;

public class BasePentahoRequestContext implements IPentahoRequestContext {

  public String contextPath;
  public static final String SLASH = "/";
  public static final String EMPTY = "";

  public BasePentahoRequestContext( String contextPath ) {
    super();
    if ( contextPath != null ) {
      String draftPath = contextPath + ( contextPath.endsWith( SLASH ) ? EMPTY : SLASH );
      this.contextPath = draftPath.replaceAll( "(?<!^http:|https:)(/){2,}", SLASH );
    } else {
      String path = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
      this.contextPath = path + ( path != null && path.endsWith( SLASH ) ? EMPTY : SLASH );
    }
  }

  public String getContextPath() {
    return contextPath;
  }
}
