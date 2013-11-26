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
