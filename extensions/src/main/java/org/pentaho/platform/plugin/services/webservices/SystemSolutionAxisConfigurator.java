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

package org.pentaho.platform.plugin.services.webservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides axis with its configuration information from the file axis/axis2_config.xml in the pentaho
 * system solutions folder. In particular, it: 1) Provides an input stream for the axis configuration XML which is
 * located in the root of the plugin folder (getConfigXml) 2) Defines the web services that are available
 * (getWebServiceDefinitions) 3) Provides persistence for enabled/disabled status of each web service
 * 
 * @author jamesdixon
 * 
 */
public class SystemSolutionAxisConfigurator extends AbstractAxisConfigurator {

  private static final long serialVersionUID = -4219285702722007821L;

  private static final Log logger = LogFactory.getLog( SystemSolutionAxisConfigurator.class );

  protected List<IServiceConfig> wsDfns = new ArrayList<IServiceConfig>();

  public SystemSolutionAxisConfigurator() {
    super();
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  public void init() {
  }

  public static String getAxisConfigPath() {
    return "system/axis/axis2_config.xml"; //$NON-NLS-1$
  }

  @Override
  public InputStream getConfigXml() {

    try {
      byte[] configBytes = IOUtils.toByteArray( ActionSequenceResource.getInputStream( getAxisConfigPath(), null ) );
      // FIXME: specify an encoding when getting bytes
      ByteArrayInputStream in = new ByteArrayInputStream( configBytes );
      return in;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString(
        "SystemSolutionAxisConfigurator.ERROR_0001_BAD_CONFIG_FILE", getAxisConfigPath() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  public boolean setEnabled( String name, boolean enabled ) throws AxisFault {
    return true;
  }

  public void addService( IServiceConfig ws ) {
    wsDfns.add( ws );
  }

  @Override
  protected List<IServiceConfig> getWebServiceDefinitions() {

    return wsDfns;
  }

  @Override
  protected void addTransports( AxisService axisService ) {
    // the defaults include http so we are good to go
  }

  @Override
  protected void addServiceEndPoints( AxisService axisService ) {
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    String endPoint1 = requestContext.getContextPath() + "content/ws-run/" + axisService.getName(); //$NON-NLS-1$
    axisService.setEPRs( new String[] { endPoint1 } );
  }

}
