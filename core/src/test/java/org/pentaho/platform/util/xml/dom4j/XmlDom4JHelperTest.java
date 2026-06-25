/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.util.xml.dom4j;

import org.junit.Test;

import javax.xml.transform.TransformerFactoryConfigurationError;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlDom4JHelperTest {

  @Test ( expected = TransformerFactoryConfigurationError.class )
  public void testConvertToDom4JDocTrowTransformerFactoryConfigurationErrorException() throws Exception {
    // Create a mock with proper DOM node structure (DOCUMENT_NODE = 9)
    final org.w3c.dom.Document doc = mock( org.w3c.dom.Document.class );
    when( doc.getNodeType() ).thenReturn( org.w3c.dom.Document.DOCUMENT_NODE );  // nodeType = 9
    XmlDom4JHelper.convertToDom4JDoc( doc );
  }
}
