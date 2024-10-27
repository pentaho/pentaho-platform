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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.util.XmlTestConstants;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletContext;
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
