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


package org.pentaho.platform.web.http.api.resources;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 11/10/15.
 */
public class ThemeTest {

  @Test
  public void testGettersAndSetters() throws Exception {
    assertThat( Theme.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testConstructors() throws Exception {
    assertThat( Theme.class, hasValidBeanConstructor() );

    Theme theme = new Theme( "myId", "myName" );
    assertEquals( "myId", theme.getId() );
    assertEquals( "myName", theme.getName() );
  }

}
