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

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class EntityMetaDataTest extends Assert {
  @Test
  public void testGettersAndSetters() throws Exception {
    //@formatter:off
    final String[] excludes = new String[] {
      "isFolder",
      "hidden",
      "hiddenOrDefault",
      "schedulable",
      "schedulableOrDefault",
      "runAfterImport"
    };
    //@formatter:on
    assertThat( EntityMetaData.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testIsFolder() throws Exception {
    final EntityMetaData entityMetaData = new EntityMetaData();
    assertFalse( entityMetaData.isIsFolder() );

    entityMetaData.setIsFolder( true );
    assertTrue( entityMetaData.isIsFolder() );

    entityMetaData.setIsFolder( false );
    assertFalse( entityMetaData.isIsFolder() );
  }

  @Test
  public void testIsHidden() throws Exception {
    final EntityMetaData entityMetaData = new EntityMetaData();
    assertEquals( entityMetaData.isHiddenOrDefault(), RepositoryFile.HIDDEN_BY_DEFAULT );

    entityMetaData.setHidden( true );
    assertTrue( entityMetaData.isHidden() );

    entityMetaData.setHidden( false );
    assertFalse( entityMetaData.isHidden() );
  }

  @Test
  public void testIsRunAfterImport() throws Exception {
    final EntityMetaData entityMetaData = new EntityMetaData();
    assertFalse( entityMetaData.isRunAfterImport() );

    entityMetaData.setRunAfterImport( true );
    assertTrue( entityMetaData.isRunAfterImport() );

    entityMetaData.setRunAfterImport( false );
    assertFalse( entityMetaData.isRunAfterImport() );
  }
}
