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



package org.pentaho.platform.web.http.api.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class UnauthorizedException extends WebApplicationException {

  private static final long serialVersionUID = 3078386410564732410L;

  public UnauthorizedException() {
    super( Response.status( Response.Status.UNAUTHORIZED ).build() );
  }
}
