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

package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AbstractAuthorizationPrincipalTest {

  private static class TestPrincipal extends AbstractAuthorizationPrincipal {
    private final String name;

    public TestPrincipal( String name ) {
      this.name = name;
    }

    @NonNull
    @Override
    public String getName() {
      return name;
    }
  }

  @Test
  public void testGetAttributesReturnsEmptyMap() {
    var principal = new TestPrincipal( "user1" );
    assertTrue( principal.getAttributes().isEmpty() );
  }

  @Test
  public void testToStringFormat() {
    var principal = new TestPrincipal( "user1" );
    assertTrue( principal.toString().contains( "user1" ) );
  }
}
