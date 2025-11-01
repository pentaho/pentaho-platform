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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        response.sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        return;
      }
      String resLower = resource.toLowerCase();

      String resourcePath;
      if ( resLower.endsWith( ".xsl" ) ) { //$NON-NLS-1$
        resourcePath = "system/custom/xsl/" + resource; //$NON-NLS-1$
      } else if ( resLower.endsWith( ".mondrian.xml" ) ) { //$NON-NLS-1$
        // Ensure user is authenticated by checking the default role
        String defaultRole = PentahoSystem.get( String.class, "defaultRole", null ); // gets defaultRole from pentahoObjects-s-s.x
        if ( defaultRole != null ) {
          if ( !SecurityHelper.getInstance().isGranted( session, new SimpleGrantedAuthority( defaultRole ) ) ) {
            response.sendError( HttpServletResponse.SC_FORBIDDEN );
            return;
          }
        }
        // If no defaultRole is defined, then just continue action as per normal.
        resourcePath = resource;
      } else if ( resLower.endsWith( ".jpg" ) || resLower.endsWith( ".jpeg" )
        || resLower.endsWith( ".gif" ) || resLower.endsWith(
          ".png" ) || resLower.endsWith( ".bmp" ) ) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        resourcePath = resource;
      } else {
        error( Messages.getInstance().getErrorString( "GetResource.ERROR_0002_INVALID_FILE", resource ) ); //$NON-NLS-1$
        response.sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
        return;
      }

      IActionSequenceResource asqr =
          new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "", //$NON-NLS-1$ //$NON-NLS-2$
              resourcePath );
      InputStream in = asqr.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
      if ( in == null ) {
        error( Messages.getInstance().getErrorString( "GetResource.ERROR_0003_RESOURCE_MISSING", resourcePath ) ); //$NON-NLS-1$
        response.sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
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
