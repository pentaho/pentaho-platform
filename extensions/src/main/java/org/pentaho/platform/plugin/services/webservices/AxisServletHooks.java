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

package org.pentaho.platform.plugin.services.webservices;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.AxisServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class subclasses AxisServlet to expose protected methods and variables
 * 
 * @author jamesdixon
 * 
 */

public class AxisServletHooks extends AxisServlet {

  private static final long serialVersionUID = 3169157817280586159L;

  protected AxisService axisService;

  protected AxisOperation axisOperation;

  protected OperationContext operationContext;

  protected ServiceContext serviceContext;

  protected MessageContext messageContext;

  /**
   * Returns the current Axis MessageContext object.
   * 
   * @return message context
   */
  public MessageContext getMessageContext() {
    return messageContext;
  }

  /**
   * Sets the Axis configuration context object
   * 
   * @param configContext
   */
  public void setContext( ConfigurationContext configContext ) {
    this.configContext = configContext;
  }

  /**
   * Sets the Axis configuration object
   * 
   * @param axisConfiguration
   */
  public void setConfiguration( AxisConfiguration axisConfiguration ) {
    this.axisConfiguration = axisConfiguration;
  }

  /**
   * Sets the Axis service context for this request
   * 
   * @param serviceContext
   */
  public void setServiceContext( ServiceContext serviceContext ) {
    this.serviceContext = serviceContext;
  }

  /**
   * Sets the servlet config object. This is mainly used for standalone and testing purposes
   * 
   * @param servletConfig
   * @throws ServletException
   */
  public void setServletConfig( ServletConfig servletConfig ) throws ServletException {
    if ( servletConfig != null ) {
      init( servletConfig );
    }
  }

  /**
   * Creates an Axis message context object for this request. If the AxisOperation is null (this will be the case during
   * a POST operation) the operation is determined by examining the contentType
   */
  @Override
  public MessageContext createMessageContext( HttpServletRequest request, HttpServletResponse response,
      boolean invocationType ) throws IOException {
    messageContext = super.createMessageContext( request, response, invocationType );

    if ( axisOperation == null ) {
      // we don't know the operation yet so pull it from the requests's contentType
      String contentType = request.getContentType();
      // parse it looking for action="operation"
      int idx = contentType.indexOf( "action=" ); //$NON-NLS-1$
      if ( idx != -1 ) {
        char delim = contentType.charAt( idx + 7 );
        int idx2 = contentType.indexOf( delim, idx + 8 );
        if ( idx2 != -1 ) {
          String actionStr = contentType.substring( idx + 8, idx2 );
          String operationName = actionStr.substring( 4 );
          axisOperation = axisService.getOperationByAction( operationName );
        }
      }
      operationContext = serviceContext.createOperationContext( axisOperation );
    }
    // setup the objects the message context needs to execute
    messageContext.setAxisService( axisService );
    messageContext.setOperationContext( operationContext );
    messageContext.setAxisOperation( axisOperation );

    return messageContext;
  }

  /**
   * Handle and HTTP PUT request
   * 
   * @param httpMethodString
   *          "PUT"
   * @param request
   *          HTTP request
   * @param response
   *          HTTP response
   * @throws ServletException
   * @throws IOException
   */
  public void handlePut( String httpMethodString, HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {

    new RestRequestProcessor( httpMethodString, request, response ).processXMLRequest();
  }

  /**
   * Handle and HTTP GET request
   * 
   * @param httpMethodString
   *          "GET"
   * @param request
   *          HTTP request
   * @param response
   *          HTTP response
   * @throws ServletException
   * @throws IOException
   */
  public void handleGet( String httpMethodString, HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {

    RestRequestProcessor processor = new RestRequestProcessor( httpMethodString, request, response );
    processor.processURLRequest();
  }

  /**
   * Handle and HTTP POST request
   * 
   * @param httpMethodString
   *          "POST"
   * @param request
   *          HTTP request
   * @param response
   *          HTTP response
   * @throws ServletException
   * @throws IOException
   */
  public void handlePost( String httpMethodString, HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {

    super.doPost( request, response );

  }

  /**
   * Sets the Axis service object for this request
   * 
   * @param axisService
   */
  public void setAxisService( AxisService axisService ) {
    this.axisService = axisService;
  }

  /**
   * Sets the Axis opertation for the current request
   * 
   * @param axisOperation
   */
  public void setAxisOperation( AxisOperation axisOperation ) {
    this.axisOperation = axisOperation;
  }

  /**
   * Sets the Axis operation context for the current request
   * 
   * @param operationContext
   */
  public void setOperationContext( OperationContext operationContext ) {
    this.operationContext = operationContext;
  }

}
