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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;

import java.io.OutputStream;

/**
 * This class dispatches requests coming from GenericServlet and determines which AxisService should be invoked. It then
 * passes this information, along with all the Axis content, to a subclass to do the execution.
 * 
 * @author jamesdixon
 */
public abstract class AxisWebServiceRequestDispatcher extends AbstractAxisServiceContentGenerator {

  private static final long serialVersionUID = 8314157642653305277L;

  /**
   * Parses the path parameter to find the web service name, makes sure it is valid, and the calls the current subclass
   * to create the required content for the specified web service
   */
  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out )
    throws Exception {

    // make sure we have a 'path' parameters provider
    IParameterProvider pathParams = parameterProviders.get( "path" ); //$NON-NLS-1$
    if ( pathParams == null ) {
      // return an error
      String message =
          Messages.getInstance().getErrorString( "WebServiceContentGenerator.ERROR_0004_PATH_PARAMS_IS_MISSING" ); //$NON-NLS-1$
      getLogger().error( message );
      out.write( message.getBytes() );
      return;
    }

    // make sure we have a service name on the URL
    String serviceName = pathParams.getStringParameter( "path", null ); //$NON-NLS-1$
    if ( serviceName == null ) {
      // return an error
      String message =
          Messages.getInstance().getErrorString( "WebServiceContentGenerator.ERROR_0005_SERVICE_NAME_IS_MISSING" ); //$NON-NLS-1$
      getLogger().error( message );
      out.write( message.getBytes() );
      return;
    }

    // remove the leading '/'
    serviceName = serviceName.substring( 1 );

    // pull the service name off the URL
    String query = serviceName;
    String operationName = null;
    int idx = serviceName.indexOf( "/" ); //$NON-NLS-1$
    if ( idx != -1 ) {
      serviceName = serviceName.substring( 0, idx );
      query = query.substring( idx + 1 );
      idx = query.indexOf( "?" ); //$NON-NLS-1$
      if ( idx != -1 ) {
        operationName = query.substring( 0, idx );
      } else {
        operationName = query;
      }
    }

    // try to get the service using the name
    AxisService axisService = axisConfiguration.getService( serviceName );
    if ( axisService == null ) {
      // return an error
      String message =
          Messages.getInstance().getErrorString(
            "WebServiceContentGenerator.ERROR_0006_SERVICE_IS_INVALID", serviceName ); //$NON-NLS-1$
      getLogger().error( message );
      out.write( message.getBytes() );
      return;
    }

    // hand over to the subclass
    createServiceContent( axisService, operationName, axisConfiguration, context, out );

  }

  /**
   * Processes the current request for the provided Axis service
   * 
   * @param axisService
   *          The Axis web service
   * @param operationName
   *          The name of the operation to perform, if known
   * @param axisConfiguration
   *          The current configuration
   * @param context
   *          The current context
   * @param out
   *          The output stream for content to be written to
   * @throws Exception
   */
  protected abstract void createServiceContent( AxisService axisService, String operationName,
      AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception;

}
