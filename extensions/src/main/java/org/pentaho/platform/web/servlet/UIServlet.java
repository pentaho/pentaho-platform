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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IUIComponent;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.springframework.http.MediaType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */

/**
 * @deprecated Due to the PPP-3963
 */
@Deprecated
public class UIServlet extends ServletBase {

  /**
   * 
   */
  private static final long serialVersionUID = 7018489258697145705L;

  private static final Log logger = LogFactory.getLog( UIServlet.class );

  @Override
  public Log getLogger() {
    return UIServlet.logger;
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException,
    IOException {
    PentahoSystem.systemEntryPoint();
    try {
      OutputStream outputStream = response.getOutputStream();

      String path = request.getContextPath();

      IPentahoSession userSession = getPentahoSession( request );
      HttpSession session = request.getSession();

      String type = MediaType.valueOf( request.getParameter( "type" ) ).toString(); //$NON-NLS-1$
      if ( type == null ) {
        type = "text/html"; //$NON-NLS-1$
      }

      // find out which component is going to fulfill this request
      String componentName = StringEscapeUtils.escapeHtml( request.getParameter( "component" ) ); //$NON-NLS-1$
      if ( componentName == null ) {
        response.setContentType( "text/html" ); //$NON-NLS-1$
        StringBuffer buffer = new StringBuffer();
        formatErrorMessage( userSession, buffer, "UIServlet.ERROR_0001_COMPONENT_NOT_SPECIFIED" );
        outputStream.write( buffer.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
        return;

      }
      response.setCharacterEncoding( LocaleHelper.getSystemEncoding() );
      // TODO switch this to the interface once stable
      IUIComponent component = (IUIComponent) session.getAttribute( componentName );
      if ( component == null ) {
        component = getComponent( componentName );
        if ( component == null ) {
          response.setContentType( "text/html" ); //$NON-NLS-1$
          StringBuffer buffer = new StringBuffer();
          formatErrorMessage( userSession, buffer, "UIServlet.ERROR_0002_COMPONENT_INVALID" );
          outputStream.write( buffer.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
          return;
        }
        session.setAttribute( componentName, component );
      }

      if ( !component.validate() ) {
        // TODO need an error here
        return;
      }
      String baseUrl =
          request.getScheme()
              + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/content?type=" + type + "&component=" + componentName + "&"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

      response.setContentType( type );
      HttpOutputHandler outputHandler = new HttpOutputHandler( response, outputStream, true );

      SimpleUrlFactory urlFactory = new SimpleUrlFactory( baseUrl );

      HttpServletRequestHandler requestHandler =
          new HttpServletRequestHandler( userSession, null, request, outputHandler, urlFactory );

      requestHandler.handleUIRequest( component, type );
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  IUIComponent getComponent( final String componentName ) {
    return (IUIComponent) PentahoSystem.createObject( componentName, this );
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {

    doGet( request, response );

  }

  void formatErrorMessage( IPentahoSession userSession, StringBuffer buffer, String errorString ) {
    PentahoSystem.get( IMessageFormatter.class, userSession ).formatErrorMessage( "text/html",
            Messages.getInstance().getString( "UIServlet.ACTION_FAILED" ),
            Messages.getInstance().getErrorString( errorString ), buffer );
  }

}
