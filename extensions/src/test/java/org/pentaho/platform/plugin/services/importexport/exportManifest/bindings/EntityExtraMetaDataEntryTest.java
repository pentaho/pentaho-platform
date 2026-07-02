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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;

public class EntityExtraMetaDataEntryTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    final String[] excludes = new String[] {};
    Assert.assertThat( EntityExtraMetaDataEntry.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testConstructor() throws Exception {
    String name = "name";
    String value = "value";
    final EntityExtraMetaDataEntry entityExtraMetaDataEntry = new EntityExtraMetaDataEntry( name, value );

    Assert.assertEquals( name, entityExtraMetaDataEntry.getName() );
    Assert.assertEquals( value, entityExtraMetaDataEntry.getValue() );

  }
}
