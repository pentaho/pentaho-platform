/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
