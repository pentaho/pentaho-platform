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

package org.pentaho.platform.engine.security.authorization.core.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractAuthorizationRuleTest {

  private TestAuthorizationRule testRule;

  // Concrete test implementation of AbstractAuthorizationRule
  private static class TestAuthorizationRule extends AbstractAuthorizationRule<IAuthorizationRequest> {
    public TestAuthorizationRule() {
    }

    @NonNull
    @Override
    public Class<IAuthorizationRequest> getRequestType() {
      return IAuthorizationRequest.class;
    }

    @NonNull
    @Override
    public Optional<IAuthorizationDecision> authorize(
      @NonNull IAuthorizationRequest request,
      @NonNull IAuthorizationContext context ) {
      return abstain();
    }
  }

  @Before
  public void setUp() {
    testRule = new TestAuthorizationRule();
  }

  @Test
  public void testAbstainMethod() {
    var abstainResult = testRule.abstain();

    assertTrue( abstainResult.isEmpty() );
  }

  @Test
  public void testToStringReturnsSimpleClassName() {
    assertEquals( "TestAuthorizationRule", testRule.toString() );
  }
}
