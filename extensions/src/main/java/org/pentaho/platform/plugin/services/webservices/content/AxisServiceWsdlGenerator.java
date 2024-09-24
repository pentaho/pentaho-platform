/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
