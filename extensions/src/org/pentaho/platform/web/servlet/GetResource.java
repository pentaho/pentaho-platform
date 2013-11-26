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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GetResource extends ServletBase {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet( final HttpServletRequest arg0, final HttpServletResponse arg1 ) throws ServletException,
    IOException {
    doPost( arg0, arg1 );
  }

  private static final Log logger = LogFactory.getLog( GetResource.class );

  @Override
  public Log getLogger() {
    return GetResource.logger;
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    // TODO perform any authorization here...
    // TODO support caching
    PentahoSystem.systemEntryPoint();
    try {
      IPentahoSession session = getPentahoSession( request );
      String resource = request.getParameter( "resource" ); //$NON-NLS-1$

      if ( ( resource == null ) || StringUtil.doesPathContainParentPathSegment( resource ) ) {
        error( Messages.getInstance().getErrorString( "GetResource.ERROR_0001_RESOURCE_PARAMETER_MISSING" ) ); //$NON-NLS-1$
        response.setStatus( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        return;
      }
      String resLower = resource.toLowerCase();

      String resourcePath;
      if ( resLower.endsWith( ".xsl" ) ) { //$NON-NLS-1$
        resourcePath = "system/custom/xsl/" + resource; //$NON-NLS-1$
      } else if ( resLower.endsWith( ".mondrian.xml" ) ) { //$NON-NLS-1$
        resourcePath = resource;
      } else if ( resLower.endsWith( ".jpg" ) || resLower.endsWith( ".jpeg" )
        || resLower.endsWith( ".gif" ) || resLower.endsWith(
          ".png" ) || resLower.endsWith( ".bmp" ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        resourcePath = resource;
      } else if ( resLower.endsWith( ".properties" ) ) { //$NON-NLS-1$
        resourcePath = resource;
      } else if ( resLower.endsWith( ".jar" ) ) { //$NON-NLS-1$
        resourcePath = resource;
      } else {
        error( Messages.getInstance().getErrorString( "GetResource.ERROR_0002_INVALID_FILE", resource ) ); //$NON-NLS-1$
        response.setStatus( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        return;
      }

      IActionSequenceResource asqr =
          new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "", //$NON-NLS-1$ //$NON-NLS-2$
              resourcePath );
      InputStream in = asqr.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
      if ( in == null ) {
        error( Messages.getInstance().getErrorString( "GetResource.ERROR_0003_RESOURCE_MISSING", resourcePath ) ); //$NON-NLS-1$
        response.setStatus( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        return;
      }
      String mimeType = getServletContext().getMimeType( resourcePath );
      String resourceName = resourcePath;
      if ( resourcePath.indexOf( "/" ) != -1 ) { //$NON-NLS-1$
        resourceName = resourcePath.substring( resourcePath.lastIndexOf( "/" ) + 1 ); //$NON-NLS-1$
      }
      response.setHeader( "content-disposition", "attachment;filename=" + resourceName ); //$NON-NLS-1$ //$NON-NLS-2$
      if ( ( null == mimeType ) || ( mimeType.length() <= 0 ) ) {
        // Hard coded to PNG because BIRT does not give us a mime type at
        // all...
        response.setContentType( "image/png" ); //$NON-NLS-1$
      } else {
        response.setContentType( mimeType );
      }
      response.setCharacterEncoding( LocaleHelper.getSystemEncoding() );
      response.setHeader( "expires", "0" ); //$NON-NLS-1$ //$NON-NLS-2$
      // Open the input and output streams
      OutputStream out = response.getOutputStream();
      try {
        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        int totalBytes = 0;
        while ( ( count = in.read( buf ) ) >= 0 ) {
          out.write( buf, 0, count );
          totalBytes += count;
        }
        response.setContentLength( totalBytes );
      } finally {
        in.close();
        out.close();
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }
}
