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

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by rfellows on 10/26/15.
 */
public class ExportManifestMetaStoreTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    assertThat( ExportManifestMetaStore.class, hasValidGettersAndSetters() );
  }
  @Test
  public void testConstructors() throws Exception {
    assertThat( ExportManifestMetaStore.class, hasValidBeanConstructor() );
    ExportManifestMetaStore metaStore = new ExportManifestMetaStore( "file", "name", "description" );
    assertEquals( "file", metaStore.getFile() );
    assertEquals( "name", metaStore.getName() );
    assertEquals( "description", metaStore.getDescription() );
  }
}
