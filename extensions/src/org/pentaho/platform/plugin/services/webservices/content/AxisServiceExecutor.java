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

package org.pentaho.platform.plugin.services.webservices.content;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.services.webservices.AxisServletHooks;
import org.pentaho.platform.plugin.services.webservices.AxisUtil;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Executes an operation of a web service. This class requires that a HttpServletRequest, HttpServletResponse, and
 * ServletConfig object are provided. Mock objects can be used instead of real HTTP objects.
 * 
 * @author jamesdixon
 * 
 */
public class AxisServiceExecutor extends AxisWebServiceRequestDispatcher implements OutTransportInfo {

  private static final long serialVersionUID = -8815968682881342687L;

  @Override
  public void createServiceContent( AxisService axisService, String operationName, AxisConfiguration axisConfiguration,
      ConfigurationContext context, OutputStream out ) throws Exception {

    IParameterProvider pathParams = parameterProviders.get( "path" ); //$NON-NLS-1$

    // get the HTTP objects from the 'path' parameter provider
    HttpServletRequest request = (HttpServletRequest) pathParams.getParameter( "httprequest" ); //$NON-NLS-1$

    @SuppressWarnings( "unchecked" )
    Enumeration names = request.getParameterNames();
    while ( names.hasMoreElements() ) {
      String name = (String) names.nextElement();
      if ( name.equalsIgnoreCase( "wsdl" ) ) { //$NON-NLS-1$
        axisService.printWSDL( out, AxisUtil.getWebServiceExecuteUrl() );
        return;
      }
    }

    HttpServletResponse response = (HttpServletResponse) pathParams.getParameter( "httpresponse" ); //$NON-NLS-1$
    ServletConfig servletConfig = (ServletConfig) pathParams.getParameter( "servletconfig" ); //$NON-NLS-1$

    // create a service group and group context for this service
    AxisServiceGroup axisServiceGroup = new AxisServiceGroup( context.getAxisConfiguration() );
    axisServiceGroup.addService( axisService );
    ServiceGroupContext serviceGroupContext = new ServiceGroupContext( context, axisServiceGroup );
    // create a service context
    ServiceContext serviceContext = serviceGroupContext.getServiceContext( axisService );
    // get an operation by name, if possible
    AxisOperation axisOperation = axisService.getOperationByAction( operationName );
    OperationContext operationContext = serviceContext.createOperationContext( axisOperation );

    // create an object to hook into Axis and give it everything we have
    AxisServletHooks hooks = new AxisServletHooks();
    hooks.setContext( context );
    hooks.setServletConfig( servletConfig );
    hooks.setConfiguration( axisConfiguration );
    hooks.initContextRoot( request );
    hooks.setAxisService( axisService );
    hooks.setAxisOperation( axisOperation );
    hooks.setOperationContext( operationContext );
    hooks.setServiceContext( serviceContext );
    hooks.setAxisOperation( axisOperation );
    hooks.setOperationContext( operationContext );
    // now execute the operation
    if ( request != null && response != null ) {
      try {
        PentahoSessionHolder.setSession( userSession );
        String method = request.getMethod();
        if ( "GET".equalsIgnoreCase( method ) ) { //$NON-NLS-1$
          hooks.handleGet( method, request, response );
        } else if ( "POST".equalsIgnoreCase( request.getMethod() ) ) { //$NON-NLS-1$
          hooks.handlePost( method, request, response );
        } else if ( "PUT".equalsIgnoreCase( request.getMethod() ) ) { //$NON-NLS-1$
          hooks.handlePut( method, request, response );
        }
      } catch ( Exception e ) {
        processAxisFault( hooks.getMessageContext(), out, e );
        error( Messages.getInstance().getErrorString( "RunService.ERROR_0001_ERROR_DURING_EXECUTION" ), e ); //$NON-NLS-1$
      }
    }

  }

  @Override
  public String getMimeType() {
    return "text/xml"; //$NON-NLS-1$
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( AxisServiceExecutor.class );
  }

  public void setContentType( String contentType ) {
    IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", instanceId, getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    contentItem.setMimeType( contentType );
  }

}
