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

import org.junit.Before;
import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/26/15.
 */
public class DatabaseConnectionTest {

  DatabaseConnection conn;
  @Before
  public void setUp() throws Exception {
    conn = new DatabaseConnection();
  }

  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] {
      "partitioningInformation"
    };
    assertThat( DatabaseConnection.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testGetPartitioningInformation() throws Exception {
    assertNotNull( conn.getPartitioningInformation() );
    assertEquals( 0, conn.getPartitioningInformation().size() );
  }

  @Test
  public void testInnerEntryClasses() throws Exception {
    assertThat( DatabaseConnection.Attributes.Entry.class, hasValidGettersAndSetters() );
    assertThat( DatabaseConnection.ConnectionPoolingProperties.Entry.class, hasValidGettersAndSetters() );
    assertThat( DatabaseConnection.ExtraOptions.Entry.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testAttributes() throws Exception {
    DatabaseConnection.Attributes attributes = new DatabaseConnection.Attributes();
    assertNotNull( attributes.getEntry() );
    assertEquals( 0, attributes.getEntry().size() );
  }

  @Test
  public void testConnectionPoolingProperties() throws Exception {
    DatabaseConnection.ConnectionPoolingProperties attributes = new DatabaseConnection.ConnectionPoolingProperties();
    assertNotNull( attributes.getEntry() );
    assertEquals( 0, attributes.getEntry().size() );
  }

  @Test
  public void testExtraOptions() throws Exception {
    DatabaseConnection.ExtraOptions attributes = new DatabaseConnection.ExtraOptions();
    assertNotNull( attributes.getEntry() );
    assertEquals( 0, attributes.getEntry().size() );
  }

}
