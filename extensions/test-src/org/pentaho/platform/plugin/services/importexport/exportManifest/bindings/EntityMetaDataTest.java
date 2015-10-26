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

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by rfellows on 10/26/15.
 */
public class EntityMetaDataTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] {
      "isFolder",
      "isHidden",
      "runAfterImport"
    };
    assertThat( EntityMetaData.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testIsFolder() throws Exception {
    EntityMetaData entityMetaData = new EntityMetaData();
    assertFalse( entityMetaData.isIsFolder() );

    entityMetaData.setIsFolder( true );
    assertTrue( entityMetaData.isIsFolder() );

    entityMetaData.setIsFolder( false );
    assertFalse( entityMetaData.isIsFolder() );
  }

  @Test
  public void testIsHidden() throws Exception {
    EntityMetaData entityMetaData = new EntityMetaData();
    assertFalse( entityMetaData.isIsHidden() );

    entityMetaData.setIsHidden( true );
    assertTrue( entityMetaData.isIsHidden() );

    entityMetaData.setIsHidden( false );
    assertFalse( entityMetaData.isIsHidden() );
  }

  @Test
  public void testIsRunAfterImport() throws Exception {
    EntityMetaData entityMetaData = new EntityMetaData();
    assertFalse( entityMetaData.isRunAfterImport() );

    entityMetaData.setRunAfterImport( true );
    assertTrue( entityMetaData.isRunAfterImport() );

    entityMetaData.setRunAfterImport( false );
    assertFalse( entityMetaData.isRunAfterImport() );
  }
}