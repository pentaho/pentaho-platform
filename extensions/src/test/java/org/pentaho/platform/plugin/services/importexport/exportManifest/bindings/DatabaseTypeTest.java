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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/26/15.
 */
public class DatabaseTypeTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] {
      "supportedAccessTypes"
    };
    assertThat( DatabaseType.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testInnerEntryClasses() throws Exception {
    assertThat( DatabaseType.DefaultOptions.Entry.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testGetSupportedAccessTypes() throws Exception {
    DatabaseType dbType = new DatabaseType();
    assertNotNull( dbType.getSupportedAccessTypes() );
    assertEquals( 0, dbType.getSupportedAccessTypes().size() );
  }

  @Test
  public void testAttributes() throws Exception {
    DatabaseType.DefaultOptions defaultOptions = new DatabaseType.DefaultOptions();
    assertNotNull( defaultOptions.getEntry() );
    assertEquals( 0, defaultOptions.getEntry().size() );
  }
}
