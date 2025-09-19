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


package org.pentaho.platform.web.http.context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import jakarta.servlet.ServletContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class WebSpringPentahoObjectFactoryTest {

  @Mock ServletContext context;

  @Test
  public void testInit() throws Exception {
    WebSpringPentahoObjectFactory factory = new WebSpringPentahoObjectFactory();
    XmlWebApplicationContext webAppContext = mock( XmlWebApplicationContext.class );
    when( context.getAttribute( WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE ) ).thenReturn( webAppContext );
    factory.init( null, context );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testInit_noServletContext() throws Exception {
    WebSpringPentahoObjectFactory factory = new WebSpringPentahoObjectFactory();

    factory.init( null, new Object() );
  }

}
