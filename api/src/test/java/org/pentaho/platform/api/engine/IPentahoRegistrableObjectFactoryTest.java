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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class IPentahoRegistrableObjectFactoryTest {

  @Test
  public void testTypesEnum() {
    assertNotNull( IPentahoRegistrableObjectFactory.Types.valueOf( "ALL" ) );
    assertNotNull( IPentahoRegistrableObjectFactory.Types.valueOf( "CLASSES" ) );
    assertNotNull( IPentahoRegistrableObjectFactory.Types.valueOf( "INTERFACES" ) );
  }

}
