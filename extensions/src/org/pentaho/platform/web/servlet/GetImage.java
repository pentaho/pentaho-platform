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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GetImage extends ServletBase {
  private static final long serialVersionUID = 119698153917362988L;

  private static final Log logger = LogFactory.getLog( GetImage.class );

  public GetImage() {
  }

  @Override
  protected void doGet( final HttpServletRequest arg0, final HttpServletResponse arg1 ) throws ServletException,
    IOException {
    doPost( arg0, arg1 );
  }

  @Override
  public Log getLogger() {
    return GetImage.logger;
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    try {
      PentahoSystem.systemEntryPoint();

      final String image = request.getParameter( "image" ); //$NON-NLS-1$
      if ( image != null ) {
        if ( ServletBase.debug ) {
          debug( Messages.getInstance().getString( "IMAGE.DEBUG_IMAGE_PARAMETER" ) + image ); //$NON-NLS-1$
        }
      } else {
        error( Messages.getInstance().getErrorString( "IMAGE.ERROR_0001_IMAGE_PARAMETER_EMPTY" ) ); //$NON-NLS-1$
        return;
      }

      // some sanity checks ...
      if ( StringUtil.doesPathContainParentPathSegment( image ) ) {
        error( Messages.getInstance().getErrorString( "IMAGE.ERROR_0002_FILE_NOT_FOUND", image ) ); //$NON-NLS-1$
        // we don't give hints that we check the parameter. Just return not
        // found.
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );
        return;
      }

      String location = ""; //$NON-NLS-1$
      if ( image.startsWith( "/" ) || image.startsWith( "\\" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
        location = "system/tmp/" + image.substring( 1 ); //$NON-NLS-1$ 
      } else if ( image.startsWith( "tmp/" ) || image.startsWith( "tmp\\" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
        location = "system/" + image; //$NON-NLS-1$
      } else {
        location = "system/tmp/" + image; //$NON-NLS-1$
      }

      File tmpFile = new File( PentahoSystem.getApplicationContext().getSolutionPath( location ) );
      // if (image.charAt(0) != '/' && image.charAt(0) != '\\') {
      // file = new File(tempDirectory, image);
      // } else {
      // file = new File(tempDirectory, image.substring(1));
      // }

      // paranoia: Check whether the new file is contained in the temp
      // directory.
      // an evil user could simply use "//" as parameter and would therefore
      // circument the test above ...
      // IOUtils ioUtils = IOUtils.getInstance();
      // if (ioUtils.isSubDirectory(tempDirectory, file) == false) {
      //        error(Messages.getInstance().getErrorString("IMAGE.ERROR_0002_FILE_NOT_FOUND", image)); //$NON-NLS-1$
      // // we dont give hints that we check the parameter. Just return not
      // // found.
      // response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      // return;
      // }

      // Open the file and output streams
      InputStream in = new FileInputStream( tmpFile );

      if ( in == null ) {
        error( Messages.getInstance().getErrorString( "IMAGE.ERROR_0002_FILE_NOT_FOUND", image ) ); //$NON-NLS-1$
        response.setStatus( HttpServletResponse.SC_NOT_FOUND );
        return;
      }

      String mimeType = getServletContext().getMimeType( image );
      if ( ( null == mimeType ) || ( mimeType.length() <= 0 ) ) {
        // Hard coded to PNG because BIRT does not give us a mime type at
        // all...
        response.setContentType( "image/png" ); //$NON-NLS-1$
      } else {
        response.setContentType( mimeType );
      }
      OutputStream out = response.getOutputStream();
      try {
        byte[] buffer = new byte[2048];
        int n, length = 0;
        while ( ( n = in.read( buffer ) ) > 0 ) {
          out.write( buffer, 0, n );
          length += n;
        }
        response.setContentLength( length );
      } finally {
        in.close();
        out.close();
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }

  }

}
