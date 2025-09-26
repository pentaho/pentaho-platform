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


package org.pentaho.platform.web.http.api.resources;

import org.dom4j.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.services.SystemService;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
