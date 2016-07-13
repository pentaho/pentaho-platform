/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/**
 * Created by rfellows on 10/26/15.
 */
public class EntityMetaDataTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] {
      "isFolder",
      "isHidden",
      "schedulable",
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
