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

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/26/15.
 */
public class ExportManifestPropertyTest {

  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] { "customProperty" };
    assertThat( ExportManifestProperty.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testGetCustomProperty() throws Exception {
    ExportManifestProperty property = new ExportManifestProperty();
    assertNotNull( property.getCustomProperty() );
    assertEquals( 0, property.getCustomProperty().size() );
  }
}
