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


package org.pentaho.platform.config;

import org.junit.Test;

import java.util.Properties;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by rfellows on 10/21/15.
 */
public class MondrianConfigPropertiesTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludeProperties = new String[] {
      "properties"
    };
    assertThat( MondrianConfigProperties.class, hasValidGettersAndSettersExcluding( excludeProperties ) );
  }

  @Test
  public void testConstructor() throws Exception {
    Properties properties = new Properties();

    MondrianConfigProperties config = new MondrianConfigProperties( properties );
    assertEquals( properties, config.getProperties() );
  }
}
