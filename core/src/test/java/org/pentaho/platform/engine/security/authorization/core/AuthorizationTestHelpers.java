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
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Helper class containing common helper methods for authorization tests.
 * This class centralizes test setup logic to reduce duplication across test classes.
 */
public class AuthorizationTestHelpers {

  private AuthorizationTestHelpers() {
    // Utility class - no instantiation
  }

  // region User Creation Helpers

  /**
   * Creates a test user with Administrator role for testing purposes.
   *
   * @return An AuthorizationUser with username "test-user" and Administrator role.
   */
  @NonNull
  public static AuthorizationUser createTestUser() {
    return createTestUser( "test-user", "Administrator" );
  }

  /**
   * Creates a test user with specified username and role for testing purposes.
   *
   * @param userName The username for the test user.
   * @param roleName The role name for the test user.
   * @return An AuthorizationUser with the specified username and role.
   */
  @NonNull
  public static AuthorizationUser createTestUser( @NonNull String userName, @NonNull String roleName ) {
    return new AuthorizationUser( userName, Set.of( new AuthorizationRole( roleName ) ) );
  }

  // endregion

  // region Action Creation Helpers

  private static class TestAuthorizationAction extends AbstractAuthorizationAction {
    private final String actionName;

    public TestAuthorizationAction( @NonNull String actionName ) {
      this.actionName = actionName;
    }

    @NonNull
    @Override
    public String getName() {
      return actionName;
    }

    @NonNull
    @Override
    public String getLocalizedDisplayName( @Nullable String locale ) {
      return actionName + "-display-name";
    }
  }

  private static class TestResourceAuthorizationAction extends TestAuthorizationAction
    implements IAuthorizationAction {

    private final Set<String> resourceTypes;

    public TestResourceAuthorizationAction( @NonNull String actionName, @NonNull Set<String> resourceTypes ) {
      super( actionName );
      this.resourceTypes = resourceTypes;
    }

    @NonNull @Override
    public Set<String> getResourceTypes() {
      return resourceTypes;
    }
  }

  /**
   * Creates a basic authorization action with the specified name.
   * Includes implementation of equals and hashCode to ensure proper comparison in tests.
   *
   * @param actionName The name for the mock action.
   * @return An IAuthorizationAction with the specified name.
   */
  @NonNull
  public static IAuthorizationAction createTestAction( @NonNull String actionName ) {
    return new TestAuthorizationAction( actionName );
  }

  /**
   * Creates a basic authorization resource action with the specified name and resource type.
   * Includes implementation of equals and hashCode to ensure proper comparison in tests.
   *
   * @param actionName   The name of the action.
   * @param resourceType The resource type this action applies to.
   * @return An IAuthorizationAction with the specified name and resource type.
   */
  @NonNull
  public static IAuthorizationAction createResourceAction( @NonNull String actionName, @NonNull String resourceType ) {
    return new TestResourceAuthorizationAction( actionName, Set.of( resourceType ) );
  }
  // endregion

  // region Rule Mock Creation Helpers

  /**
   * Creates a basic mock authorization rule with getRequestType() configured.
   *
   * @return A mock IAuthorizationRule with getRequestType() returning IAuthorizationRequest.class.
   */
  @NonNull
  public static IAuthorizationRule<IAuthorizationRequest> createMockRule() {
    return createMockRule( IAuthorizationRequest.class );
  }

  /**
   * Creates a basic mock authorization rule with getRequestType() configured for a specific request type.
   *
   * @param requestType The type of request this rule handles.
   * @param <T>         The type of authorization request this rule handles.
   * @return A mock IAuthorizationRule with getRequestType() returning the specified request type.
   */
  @SuppressWarnings( "unchecked" )
  @NonNull
  public static <T extends IAuthorizationRequest> IAuthorizationRule<T> createMockRule(
    @NonNull Class<T> requestType ) {
    IAuthorizationRule<T> rule = mock( IAuthorizationRule.class );
    when( rule.getRequestType() ).thenReturn( requestType );
    return rule;
  }
  // endregion

  // region Composite Decision Assertion Helpers

  /**
   * Asserts that the given composite decision contains exactly the expected decisions, in the same order and with the
   * same references.
   *
   * @param compositeDecision The composite decision to check.
   * @param expectedDecisions The expected decisions that should be contained in the composite decision.
   * @throws AssertionError If the composite decision does not contain exactly the expected decisions.
   */
  public static void assertCompositeDecisionContainsExactly( @NonNull ICompositeAuthorizationDecision compositeDecision,
                                                             IAuthorizationDecision... expectedDecisions ) {
    assertEquals( expectedDecisions.length, compositeDecision.getDecisions().size() );

    // Convert Set to List for index-based access
    var actualDecisionsList = List.copyOf( compositeDecision.getDecisions() );

    // Check index by index in a single loop
    for ( int i = 0; i < expectedDecisions.length; i++ ) {
      // Use assertSame to verify exact object reference at the same index
      assertSame( "Decision at index " + i + " should match expected decision",
        expectedDecisions[ i ], actualDecisionsList.get( i ) );
    }
  }

  /**
   * Asserts that the given authorization decision is a composite decision and contains exactly the expected decisions,
   * in the same order and with the same references.
   *
   * @param decision          The authorization decision to check.
   * @param expectedDecisions The expected decisions that should be contained in the composite decision.
   * @throws AssertionError If the decision is not a composite decision or does not contain exactly the expected
   *                        decisions.
   */
  public static void assertCompositeDecisionContainsExactly( @NonNull IAuthorizationDecision decision,
                                                             IAuthorizationDecision... expectedDecisions ) {
    assertTrue( "Decision should be a composite decision", decision instanceof ICompositeAuthorizationDecision );
    assertCompositeDecisionContainsExactly( (ICompositeAuthorizationDecision) decision, expectedDecisions );
  }
  // endregion

  // region Decision Mock Creation Helpers

  /**
   * Creates a mock IAuthorizationDecision for the given request and granted status.
   * <p>
   * Note: {@code isDenied()} does not need to be mocked explicitly because it has a default implementation in the
   * {@link IAuthorizationDecision} interface: {@code default boolean isDenied() { return !isGranted(); }}.
   * Mockito properly supports default interface methods, so {@code isDenied()} will automatically return the correct
   * value based on the mocked {@code isGranted()} value.
   *
   * @param request The authorization request.
   * @param granted Whether the decision is granted.
   * @return A mock IAuthorizationDecision with the specified request and granted status.
   */
  @NonNull
  public static IAuthorizationDecision createMockDecision( @NonNull IAuthorizationRequest request, boolean granted ) {
    IAuthorizationDecision decision = mock( IAuthorizationDecision.class );
    when( decision.getRequest() ).thenReturn( request );
    when( decision.isGranted() ).thenReturn( granted );
    // isDenied() is NOT mocked - the interface default method handles it automatically
    return decision;
  }
  // endregion
}
