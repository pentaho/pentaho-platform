/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class IPentahoDefinableObjectFactoryTest {

  @SuppressWarnings( "deprecation" )
  @Test
  public void testScopeEnum() {
    assertNotNull( IPentahoDefinableObjectFactory.Scope.valueOf( "GLOBAL" ) );
    assertNotNull( IPentahoDefinableObjectFactory.Scope.valueOf( "SESSION" ) );
    assertNotNull( IPentahoDefinableObjectFactory.Scope.valueOf( "REQUEST" ) );
    assertNotNull( IPentahoDefinableObjectFactory.Scope.valueOf( "THREAD" ) );
    assertNotNull( IPentahoDefinableObjectFactory.Scope.valueOf( "LOCAL" ) );
  }

}
