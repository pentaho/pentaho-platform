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
