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
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IUIComponent;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
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

      String type = request.getParameter( "type" ); //$NON-NLS-1$
      if ( type == null ) {
        type = "text/html"; //$NON-NLS-1$
      }

      // find out which component is going to fulfill this request
      String componentName = request.getParameter( "component" ); //$NON-NLS-1$
      if ( componentName == null ) {
        response.setContentType( "text/html" ); //$NON-NLS-1$
        StringBuffer buffer = new StringBuffer();
        PentahoSystem
            .get( IMessageFormatter.class, userSession )
            .formatErrorMessage(
              "text/html", Messages.getInstance().getString( "UIServlet.ACTION_FAILED" ),
              Messages.getInstance().getErrorString( "UIServlet.ERROR_0001_COMPONENT_NOT_SPECIFIED" ), buffer ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
          PentahoSystem
              .get( IMessageFormatter.class, userSession )
              .formatErrorMessage(
                "text/html", Messages.getInstance().getString( "UIServlet.ACTION_FAILED" ),
                Messages.getInstance().getErrorString( "UIServlet.ERROR_0002_COMPONENT_INVALID" ), buffer ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

  private IUIComponent getComponent( final String componentName ) {
    return (IUIComponent) PentahoSystem.createObject( componentName, this );
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {

    doGet( request, response );

  }

}
