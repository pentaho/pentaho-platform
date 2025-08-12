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

package org.pentaho.platform.api.engine.security.authorization.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IAuthorizationDecisionTest {

  // region isDenied() Default Method Tests
  @Test
  void testIsDeniedWhenGrantedIsTrue() {
    IAuthorizationDecision grantedDecision = new TestAuthorizationDecision( true );

    assertTrue( grantedDecision.isGranted() );
    assertFalse( grantedDecision.isDenied() );
  }

  @Test
  void testIsDeniedWhenGrantedIsFalse() {
    IAuthorizationDecision deniedDecision = new TestAuthorizationDecision( false );

    assertFalse( deniedDecision.isGranted() );
    assertTrue( deniedDecision.isDenied() );
  }
  // endregion

  // region getBaseType() Default Method Tests
  @Test
  void testGetBaseTypeForSimpleDecision() {
    IAuthorizationDecision simpleDecision = new TestAuthorizationDecision( true );

    Class<? extends IAuthorizationDecision> baseType = simpleDecision.getBaseType();

    assertEquals( IAuthorizationDecision.class, baseType );
  }

  @Test
  void testGetBaseTypeForOpposedDecision() {
    IAuthorizationDecision opposedDecision = new TestOpposedAuthorizationDecision();

    Class<? extends IAuthorizationDecision> baseType = opposedDecision.getBaseType();

    assertEquals( IOpposedAuthorizationDecision.class, baseType );
  }

  @Test
  void testGetBaseTypeForAllDecision() {
    IAuthorizationDecision allDecision = new TestAllAuthorizationDecision();

    Class<? extends IAuthorizationDecision> baseType = allDecision.getBaseType();

    assertEquals( IAllAuthorizationDecision.class, baseType );
  }

  @Test
  void testGetBaseTypeForAnyDecision() {
    IAuthorizationDecision anyDecision = new TestAnyAuthorizationDecision();

    Class<? extends IAuthorizationDecision> baseType = anyDecision.getBaseType();

    assertEquals( IAnyAuthorizationDecision.class, baseType );
  }
  // endregion

  /**
   * Simple test implementation of IAuthorizationDecision.
   */
  private static class TestAuthorizationDecision implements IAuthorizationDecision {
    private final boolean granted;

    public TestAuthorizationDecision( boolean granted ) {
      this.granted = granted;
    }

    @Override
    @NonNull
    public IAuthorizationRequest getRequest() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGranted() {
      return granted;
    }

    @Override
    @NonNull
    public String getShortJustification() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Test implementation of IOpposedAuthorizationDecision.
   */
  private static class TestOpposedAuthorizationDecision implements IOpposedAuthorizationDecision {

    public TestOpposedAuthorizationDecision() {
    }

    @Override
    @NonNull
    public IAuthorizationRequest getRequest() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGranted() {
      throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    public String getShortJustification() {
      throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public IAuthorizationDecision getOpposedToDecision() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Test implementation of IAllAuthorizationDecision.
   */
  private static class TestAllAuthorizationDecision implements IAllAuthorizationDecision {

    public TestAllAuthorizationDecision() {
    }

    @Override
    @NonNull
    public IAuthorizationRequest getRequest() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGranted() {
      throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    public String getShortJustification() {
      throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Set<IAuthorizationDecision> getDecisions() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Test implementation of IAnyAuthorizationDecision.
   */
  private static class TestAnyAuthorizationDecision implements IAnyAuthorizationDecision {

    public TestAnyAuthorizationDecision() {
    }

    @Override
    @NonNull
    public IAuthorizationRequest getRequest() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGranted() {
      throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    public String getShortJustification() {
      throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Set<IAuthorizationDecision> getDecisions() {
      throw new UnsupportedOperationException();
    }
  }
}
