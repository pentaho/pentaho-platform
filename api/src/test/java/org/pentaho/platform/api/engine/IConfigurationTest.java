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


package org.pentaho.platform.api.engine;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IConfigurationTest {

  @Test
  void reloadDefaultImplementationIsNoOp() throws IOException {
    Properties properties = new Properties();
    properties.setProperty( "one", "1" );

    IConfiguration configuration = new IConfiguration() {
      @Override
      public String getId() {
        return "test";
      }

      @Override
      public Properties getProperties() {
        return properties;
      }

      @Override
      public void update( Properties updatedProperties ) {
        properties.clear();
        properties.putAll( updatedProperties );
      }
    };

    assertDoesNotThrow( configuration::reload );
    assertEquals( "test", configuration.getId() );
    assertSame( properties, configuration.getProperties() );
    assertEquals( "1", configuration.getProperties().getProperty( "one" ) );
  }
}
