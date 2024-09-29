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


package org.pentaho.test.platform.plugin.services.webservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;

public class StubTransportListener implements TransportListener {

  public void destroy() {
    System.out.println( "TestTransportListener.destroy" ); //$NON-NLS-1$
  }

  public EndpointReference getEPRForService( String serviceName, String ip ) throws AxisFault {
    System.out.println( "TestTransportListener.getEPRForService" ); //$NON-NLS-1$
    return null;
  }

  public EndpointReference[] getEPRsForService( String serviceName, String ip ) throws AxisFault {
    System.out.println( "TestTransportListener.getEPRsForService" ); //$NON-NLS-1$
    return null;
  }

  public SessionContext getSessionContext( MessageContext messageContext ) {
    System.out.println( "TestTransportListener.getSessionContext" ); //$NON-NLS-1$
    return null;
  }

  public void init( ConfigurationContext axisConf, TransportInDescription transprtIn ) throws AxisFault {
    System.out.println( "TestTransportListener.init" ); //$NON-NLS-1$
  }

  public void start() throws AxisFault {
    System.out.println( "TestTransportListener.start" ); //$NON-NLS-1$
  }

  public void stop() throws AxisFault {
    System.out.println( "TestTransportListener.stop" ); //$NON-NLS-1$
  }

}
