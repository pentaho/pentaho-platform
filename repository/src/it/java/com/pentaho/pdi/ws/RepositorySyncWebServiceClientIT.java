/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package com.pentaho.pdi.ws;

import org.junit.Before;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
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
