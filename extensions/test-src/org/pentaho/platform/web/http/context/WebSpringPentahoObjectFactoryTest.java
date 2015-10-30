/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static org.junit.Assert.*;
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
