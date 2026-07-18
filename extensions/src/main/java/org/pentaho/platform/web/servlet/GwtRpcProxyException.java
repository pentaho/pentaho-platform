/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.web.servlet;

import java.io.Serializable;

public class GwtRpcProxyException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = -5090524647540284482L;

  public GwtRpcProxyException() {
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException( String message ) {
    super( message );
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException( Throwable cause ) {
    super( cause );
    // TODO Auto-generated constructor stub
  }

  public GwtRpcProxyException( String message, Throwable cause ) {
    super( message, cause );
    // TODO Auto-generated constructor stub
  }

}
