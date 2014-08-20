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

package org.pentaho.platform.web.http.api.resources;

import org.dom4j.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.services.SystemService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SystemUsersResourceTest {

  SystemUsersResource systemUsersResource;

  @Before
  public void setup() {
    systemUsersResource = spy( new SystemUsersResource() );
  }

  @After
  public void teardown() {
    systemUsersResource = null;
  }

  @Test
  public void testGetUsers() throws Exception {
    SystemService mockSystemService = mock( SystemService.class );
    doReturn( mockSystemService ).when( systemUsersResource ).getSystemService();

    Document mockDocument = mock( Document.class );
    doReturn( mockDocument ).when( mockSystemService ).getUsers();

    String xml = "xml";
    doReturn( xml ).when( mockDocument ).asXML();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( systemUsersResource ).buildOkResponse( xml, MediaType.APPLICATION_XML );

    Response testResponse = systemUsersResource.getUsers();
    assertEquals( mockResponse, testResponse );

    verify( systemUsersResource, times( 1 ) ).getSystemService();
    verify( mockSystemService, times( 1 ) ).getUsers();
    verify( mockDocument, times( 1 ) ).asXML();
    verify( systemUsersResource, times( 1 ) ).buildOkResponse( xml, MediaType.APPLICATION_XML );
  }

}
