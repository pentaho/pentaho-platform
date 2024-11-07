/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.config;

import org.dom4j.DocumentException;
import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
/**
 * Created by rfellows on 10/21/15.
 */
public class ConsoleConfigXmlTest {

  @Test
  public void testHasValidGettersAndSetters() {
    String[] excludeProperties = new String[] {
      "document"
    };
    assertThat( ConsoleConfigXml.class, hasValidGettersAndSettersExcluding( excludeProperties ) );
  }

  @Test ( expected = DocumentException.class )
  public void testConstructor_String() throws Exception {
    ConsoleConfigXml xml = new ConsoleConfigXml( "<xml></xml>" );
  }

  @Test
  public void testGetDocument() throws Exception {
    ConsoleConfigXml xml = new ConsoleConfigXml();
    assertNotNull( xml.getDocument() );
  }
}
