/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.util.XmlTestConstants;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;

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
    appContext.getResourceDocument( new StringBufferInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";

    assertNotNull( appContext.getResourceDocument( new StringBufferInputStream( xml ) ) );
  }
}
