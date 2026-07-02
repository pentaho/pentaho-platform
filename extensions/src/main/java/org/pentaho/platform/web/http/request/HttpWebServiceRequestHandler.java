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


package org.pentaho.platform.web.http.request;

import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.services.BaseRequestHandler;

public class HttpWebServiceRequestHandler extends BaseRequestHandler {

  public HttpWebServiceRequestHandler( final IPentahoSession session, final String instanceId,
      final IOutputHandler outputHandler, final IParameterProvider parameterProvider,
      final IPentahoUrlFactory urlFactory ) {
    super( session, instanceId, outputHandler, parameterProvider, urlFactory );
  }

}
