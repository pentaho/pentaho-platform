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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.webservices.AxisUtil;

import java.io.OutputStream;

/**
 * Writes the WSDL for a Axis web service to the output stream provided
 * 
 * @author jamesdixon
 * 
 */
public class AxisServiceWsdlGenerator extends AxisWebServiceRequestDispatcher {

  private static final long serialVersionUID = -163750511475038584L;

  /**
   * Writes the WSDL to the output stream provided. The WSDL is prepared ahead of time when the web service is created.
   */
  @Override
  public void createServiceContent( AxisService axisService, String operationName, AxisConfiguration axisConfiguration,
      ConfigurationContext context, OutputStream out ) throws Exception {
    axisService.printWSDL( out, AxisUtil.getWebServiceExecuteUrl() );
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( AxisServiceWsdlGenerator.class );
  }

  @Override
  public String getMimeType() {
    return "text/xml"; //$NON-NLS-1$
  }

}
