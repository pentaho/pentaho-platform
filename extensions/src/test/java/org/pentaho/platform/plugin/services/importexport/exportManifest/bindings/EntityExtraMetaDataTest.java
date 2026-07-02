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

public class EntityExtraMetaDataTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    final String[] excludes = new String[] {};
    Assert.assertThat( EntityExtraMetaData.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testMetaData() throws Exception {
    final EntityExtraMetaData entityExtraMetaData = new EntityExtraMetaData();

    Assert.assertTrue( entityExtraMetaData.getMetadata().isEmpty() );

    EntityExtraMetaDataEntry entry = new EntityExtraMetaDataEntry( "key", "value");
    entityExtraMetaData.addMetadata( entry );
    Assert.assertTrue( entityExtraMetaData.getMetadata().size() == 1 );

    Assert.assertEquals( entry, entityExtraMetaData.getMetadata().get( 0 ));

    List<EntityExtraMetaDataEntry> metadata = new ArrayList<>();
    EntityExtraMetaDataEntry entry2 = new EntityExtraMetaDataEntry( "key2", "value2");

    entityExtraMetaData.setMetadata( metadata );
    Assert.assertEquals( metadata, entityExtraMetaData.getMetadata() );

  }
}
