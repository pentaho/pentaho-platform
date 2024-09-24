/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
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
