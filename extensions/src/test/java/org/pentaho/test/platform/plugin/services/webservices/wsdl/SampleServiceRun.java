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


package org.pentaho.test.platform.plugin.services.webservices.wsdl;

import junit.framework.TestCase;

public class SampleServiceRun extends TestCase {

  public static void main( String[] args ) throws Exception {

    ServiceStub.ComplexType complex = new ServiceStub.ComplexType();
    complex.setName( "fred" ); //$NON-NLS-1$

    ServiceStub.GetDetails getDetails = new ServiceStub.GetDetails();

    getDetails.setObject( complex );

    ServiceStub stub = new ServiceStub();

    ServiceStub.GetDetailsResponse response = stub.getDetails( getDetails );

    ServiceStub.ComplexType returnValue = response.get_return();
    assertNotNull( returnValue );

  }

}
