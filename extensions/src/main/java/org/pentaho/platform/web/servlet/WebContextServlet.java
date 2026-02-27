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


package org.pentaho.platform.web.servlet;

import org.pentaho.platform.engine.core.system.PentahoSystem;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class WebContextServlet extends HttpServlet {

  protected void doGet( final HttpServletRequest arg0, final HttpServletResponse arg1 ) throws ServletException,
    IOException {
    doPost( arg0, arg1 );
  }

  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    response.setContentType( "text/javascript" );
    OutputStream out = response.getOutputStream();
    // split out a base url, guaranteed to have a trailing slash
    String baseURL = PentahoSystem.getApplicationContext().getBaseUrl();
    if ( !baseURL.endsWith( "/" ) ) {
      baseURL += "/";
    }
    String webContext = "var WEB_CONTEXT_BASE = '" + baseURL + "';";
    out.write( webContext.getBytes() );
    out.close();
  }
}
