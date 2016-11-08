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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;

import java.io.ByteArrayOutputStream;

public class StubTransportSender implements TransportSender {

  public static String transportOutStr = null;

  public void cleanup( MessageContext msgContext ) throws AxisFault {
    System.out.println( "TestTransportSender.cleanup 1 " ); //$NON-NLS-1$
  }

  public void init( ConfigurationContext confContext, TransportOutDescription transportOut ) throws AxisFault {
    System.out.println( "TestTransportSender.init 1 " ); //$NON-NLS-1$
  }

  public void stop() {
    System.out.println( "TestTransportSender.stop " ); //$NON-NLS-1$
  }

  public void cleanup() {
    System.out.println( "TestTransportSender.cleanup 2 " ); //$NON-NLS-1$
  }

  public void flowComplete( MessageContext msgContext ) {
    System.out.println( "TestTransportSender.flowComplete " ); //$NON-NLS-1$
  }

  public HandlerDescription getHandlerDesc() {
    System.out.println( "TestTransportSender.getHandlerDesc " ); //$NON-NLS-1$
    return null;
  }

  public String getName() {
    System.out.println( "TestTransportSender.getName " ); //$NON-NLS-1$
    return "testname"; //$NON-NLS-1$
  }

  public Parameter getParameter( String name ) {
    System.out.println( "TestTransportSender.getParameter " + name ); //$NON-NLS-1$
    return null;
  }

  public void init( HandlerDescription handlerDesc ) {
    System.out.println( "TestTransportSender.init 2 " ); //$NON-NLS-1$

  }

  public InvocationResponse invoke( MessageContext msgContext ) throws AxisFault {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TransportUtils.writeMessage( msgContext, out );
    StubTransportSender.transportOutStr = new String( out.toByteArray() );
    System.out.println( "TestTransportSender.invoke " ); //$NON-NLS-1$
    return null;
  }

}
