/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import com.sun.xml.bind.StringInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.util.XmlTestConstants;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoSolutionSpringApplicationContextTest {

  PentahoSolutionSpringApplicationContext appContext;

  @Mock ServletContext context;

  @Before
  public void setUp() throws Exception {
    appContext = new PentahoSolutionSpringApplicationContext();
  }

  @Test
  public void testGetResourceByPath() throws Exception {
    appContext.setServletContext( context );
    File tempFile =
      File.createTempFile( "PentahoSolutionSpringApplicationContextTest", ".tmp" );
    tempFile.deleteOnExit();

    when( context.getRealPath( "" ) ).thenReturn( tempFile.getParent() );

    Resource resourceByPath = appContext.getResourceByPath( tempFile.getName() );
    assertNotNull( resourceByPath );
    assertEquals( tempFile.getName(), resourceByPath.getFilename() );
  }

  @Test( timeout = 2000, expected = SAXException.class )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven() throws IOException, ParserConfigurationException, SAXException {
    appContext.getResourceDocument( new StringInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";

    assertNotNull( appContext.getResourceDocument( new StringInputStream( xml ) ) );
  }
}
