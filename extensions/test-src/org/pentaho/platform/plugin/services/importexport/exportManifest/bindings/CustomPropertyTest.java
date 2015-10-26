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

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
