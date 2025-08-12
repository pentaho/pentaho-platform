/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package com.pentaho.pdi.ws;

import org.junit.Before;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.Service;
import java.io.Serializable;
import java.net.URL;

@SuppressWarnings( "nls" )
public class RepositorySyncWebServiceClientIT extends RepositorySyncWebServiceIT implements Serializable {

  private static final long serialVersionUID = -6806897012063786589L; /* EESOURCE: UPDATE SERIALVERUID */

  @Before
  public void before() {
    System.out.println( "Starting server..." );
    String address = "http://localhost:9988/repo";
    Endpoint.publish( address, new RepositorySyncWebService() );
    System.out.println( "Server Started." );
  }

  @Override
  public IRepositorySyncWebService getRepositorySyncWebService() {
    try {
      Service service =
          Service.create( new URL( "http://localhost:9988/repo?wsdl" ), new QName( "http://www.pentaho.org/ws/1.0",
              "repositorySync" ) );

      return service.getPort( IRepositorySyncWebService.class );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }
}
