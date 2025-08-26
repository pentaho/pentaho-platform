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
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.pentaho.platform.engine.security.authorization.core.AuthorizationTestHelpers.createMockRule;

public class AbstractCompositeAuthorizationRuleTest {

  /**
   * Simple test subclass of AbstractCompositeAuthorizationRule for testing base functionality.
   */
  private static class TestCompositeAuthorizationRule extends AbstractCompositeAuthorizationRule {

    public TestCompositeAuthorizationRule( @NonNull List<IAuthorizationRule<IAuthorizationRequest>> rules ) {
      super( rules );
    }

    @NonNull
    @Override
    protected AbstractCompositeResultBuilder createResultBuilder( @NonNull IAuthorizationContext context ) {
      // Return a simple builder that doesn't affect the core functionality we're testing
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void testGetRequestTypeIsIAuthorizationRequest() {
    var mockRule1 = createMockRule();
    var mockRule2 = createMockRule();

    var testRule = new TestCompositeAuthorizationRule( List.of( mockRule1, mockRule2 ) );

    assertEquals( IAuthorizationRequest.class, testRule.getRequestType() );
  }

  @Test( expected = NullPointerException.class )
  public void testConstructorWithNullRulesThrows() {
    //noinspection DataFlowIssue
    new TestCompositeAuthorizationRule( null );
  }

  @Test
  public void testGetRulesReturnsUnmodifiableList() {
    var mockRule1 = createMockRule();
    var mockRule2 = createMockRule();
    var mockRule3 = createMockRule();
    var rules = List.of( mockRule1, mockRule2 );

    var testRule = new TestCompositeAuthorizationRule( rules );

    var returnedRules = testRule.getRules();
    assertEquals( 2, returnedRules.size() );
    assertEquals( mockRule1, returnedRules.get( 0 ) );
    assertEquals( mockRule2, returnedRules.get( 1 ) );

    // Verify list is unmodifiable
    try {
      returnedRules.add( mockRule3 );
      throw new AssertionError( "Expected UnsupportedOperationException" );
    } catch ( UnsupportedOperationException e ) {
      // Expected
    }
  }

  @Test
  public void testConstructorWithEmptyRulesListSucceeds() {
    var testRule = new TestCompositeAuthorizationRule( Collections.emptyList() );

    // Verify that the rule can be created with empty lists
    assertEquals( 0, testRule.getRules().size() );
  }
}
