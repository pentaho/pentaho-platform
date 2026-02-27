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


package org.pentaho.platform.util.xml.dom4j;

import org.junit.Test;

import javax.xml.transform.TransformerFactoryConfigurationError;

import static org.mockito.Mockito.mock;

public class XmlDom4JHelperTest {

  @Test ( expected = TransformerFactoryConfigurationError.class )
  public void testConvertToDom4JDocTrowTransformerFactoryConfigurationErrorException() throws Exception {
    final org.w3c.dom.Document doc = mock( org.w3c.dom.Document.class );
    XmlDom4JHelper.convertToDom4JDoc( doc );
  }
}
