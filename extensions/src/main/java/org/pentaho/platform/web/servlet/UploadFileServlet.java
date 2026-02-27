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

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.FileNotFoundException;
import java.io.IOException;

// Activate request multi-part processing; enable use of Request#getPart API.
@MultipartConfig
public class UploadFileServlet extends HttpServlet implements Servlet {

  private static final long serialVersionUID = 8305367618713715640L;

  protected void doPost( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    try {
      IPentahoSession session = PentahoSessionHolder.getSession();
      if ( !hasManageDataAccessPermission( session ) ) {
        response.sendError( 403, Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0009_UNAUTHORIZED" ) );
        return;
      }

      response.setContentType( "text/plain" );

      Part uploadPart = request.getPart( "uploadFormElement" );
      if ( uploadPart == null ) {
        String error = Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0001_NO_FILE_TO_UPLOAD" );
        response.getWriter().write( error );
        return;
      }

      String fileName = request.getParameter( "file_name" );
      if ( StringUtils.isEmpty( fileName ) ) {
        throw new ServletException(
          Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0010_FILE_NAME_INVALID" ) );
      }


      boolean isTemporary = false;
      String temporary = request.getParameter( "mark_temporary" );
      if ( temporary != null ) {
        isTemporary = Boolean.parseBoolean( temporary );
      }

      boolean shouldUnzip = false;
      String unzip = request.getParameter( "unzip" );
      if ( unzip != null ) {
        shouldUnzip = Boolean.parseBoolean( unzip );
      }

      UploadFileUtils utils = new UploadFileUtils( session );
      utils.setShouldUnzip( shouldUnzip );
      utils.setTemporary( isTemporary );
      utils.setFileName( fileName );
      utils.setWriter( response.getWriter() );
      utils.setUploadedPart( uploadPart );

      // Do nothing with success value - the output should already have been written to the servlet response.
      utils.process();

    } catch ( FileNotFoundException e ) {
      response.getWriter().write( Messages.getInstance().getErrorString( "UploadFileServlet.ERROR_0013_NO_SUCH_FILE_OR_DIRECTORY" ) );
    } catch ( Exception e ) {
      String error = Messages.getInstance().getErrorString(
        "UploadFileServlet.ERROR_0005_UNKNOWN_ERROR",
        e.getLocalizedMessage() );
      response.getWriter().write( error );
    }
  }

  /**
   * Returns true if the current user has Manage Data Source Security. Otherwise returns false.
   *
   * @param session
   * @return
   */
  protected boolean hasManageDataAccessPermission( IPentahoSession session ) {
    // If this breaks an OEM's plugin, provide a get-out-of-jail card with an entry in the pentaho.xml.
    String override = PentahoSystem.getSystemSetting( "data-access-override", "false" );
    if ( Boolean.parseBoolean( override ) ) {
      // Override the security policy with the entry in the pentaho.xml.
      return true;
    }

    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    if ( policy != null ) {
      return policy.isAllowed( "org.pentaho.platform.dataaccess.datasource.security.manage" );
    }

    return false;
  }
}
