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
