/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
