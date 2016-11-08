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
 * This class provides axis with it's configuration information from the file axis/axis2_config.xml in the pentaho
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
