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


package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/26/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class CustomPropertyTest {
  CustomProperty customProperty;
  @Mock Element element;

  @Before
  public void setUp() throws Exception {
    customProperty = new CustomProperty();
  }

  @Test
  public void testSetAny() throws Exception {
    customProperty.setAny( element );
    assertEquals( element, customProperty.getAny() );
  }

  @Test
  public void testGetOtherAttributes() throws Exception {
    assertNotNull( customProperty.getOtherAttributes() );
  }
}
