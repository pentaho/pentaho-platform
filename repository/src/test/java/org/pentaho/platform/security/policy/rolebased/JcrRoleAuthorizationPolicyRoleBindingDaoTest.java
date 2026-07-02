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

package org.pentaho.platform.security.policy.rolebased;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCache;
import org.pentaho.platform.api.engine.security.authorization.caching.IAuthorizationDecisionCacheKey;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.engine.security.authorization.core.AuthorizationRole;
import org.springframework.extensions.jcr.JcrTemplate;

import javax.jcr.Session;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JcrRoleAuthorizationPolicyRoleBindingDao}, specifically focused on decision cache invalidation.
 */
public class JcrRoleAuthorizationPolicyRoleBindingDaoTest {

  /**
   * Testable subclass that overrides the JCR setRoleBindings method to bypass all JCR operations.
   */
  private static class TestableJcrRoleAuthorizationPolicyRoleBindingDao
    extends JcrRoleAuthorizationPolicyRoleBindingDao {

    public TestableJcrRoleAuthorizationPolicyRoleBindingDao(
      JcrTemplate jcrTemplate,
      Map<String, List<IAuthorizationAction>> immutableRoleBindings,
      Map<String, List<String>> bootstrapRoleBindings,
      String superAdminRoleName,
      ITenantedPrincipleNameResolver tenantedRoleNameUtils,
      List<IAuthorizationAction> authorizationActions,
      IAuthorizationDecisionCache decisionCache ) {
      super(
        jcrTemplate,
        immutableRoleBindings,
        bootstrapRoleBindings,
        superAdminRoleName,
        tenantedRoleNameUtils,
        authorizationActions,
        decisionCache );
    }

    /**
     * Override the protected method that does all the JCR work.
     * This is called by both public setRoleBindings methods.
     */
    @Override
    public void setRoleBindings(
      Session session,
      ITenant tenant,
      String runtimeRoleName,
      List<String> logicalRoleNames ) {
      // No-op: bypass all JCR operations in tests
      // The public methods will still call invalidateDecisionCacheForRole
    }
  }

  private JcrTemplate jcrTemplate;
  private IAuthorizationDecisionCache decisionCache;
  private Map<String, List<IAuthorizationAction>> immutableRoleBindings;
  private Map<String, List<String>> bootstrapRoleBindings;
  private ITenantedPrincipleNameResolver tenantedRoleNameUtils;
  private List<IAuthorizationAction> authorizationActions;
  private TestableJcrRoleAuthorizationPolicyRoleBindingDao dao;
  private MockedStatic<TenantUtils> tenantUtilsMock;

  @Before
  public void setUp() {
    jcrTemplate = mock( JcrTemplate.class );
    decisionCache = mock( IAuthorizationDecisionCache.class );
    tenantedRoleNameUtils = mock( ITenantedPrincipleNameResolver.class );

    immutableRoleBindings = new HashMap<>();
    bootstrapRoleBindings = new HashMap<>();
    authorizationActions = Collections.emptyList();

    // Mock TenantUtils.isAccessibleTenant to always return true
    tenantUtilsMock = mockStatic( TenantUtils.class );
    tenantUtilsMock.when( () -> TenantUtils.isAccessibleTenant( any() ) ).thenReturn( true );

    dao = new TestableJcrRoleAuthorizationPolicyRoleBindingDao(
      jcrTemplate,
      immutableRoleBindings,
      bootstrapRoleBindings,
      "Admin",
      tenantedRoleNameUtils,
      authorizationActions,
      decisionCache );
  }

  @After
  public void tearDown() {
    if ( tenantUtilsMock != null ) {
      tenantUtilsMock.close();
    }
  }

  // region Constructor Tests

  @Test
  public void testConstructor_WithDecisionCache() {
    assertNotNull( dao );
  }

  @Test
  public void testConstructor_WithoutDecisionCache() {
    TestableJcrRoleAuthorizationPolicyRoleBindingDao daoWithoutCache =
      new TestableJcrRoleAuthorizationPolicyRoleBindingDao(
        jcrTemplate,
        immutableRoleBindings,
        bootstrapRoleBindings,
        "Admin",
        tenantedRoleNameUtils,
        authorizationActions,
        null );

    assertNotNull( daoWithoutCache );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testConstructor_NullJcrTemplate_ThrowsException() {
    new TestableJcrRoleAuthorizationPolicyRoleBindingDao(
      null,
      immutableRoleBindings,
      bootstrapRoleBindings,
      "Admin",
      tenantedRoleNameUtils,
      authorizationActions,
      decisionCache );
  }

  // endregion

  // region Cache Invalidation Tests

  @Test
  public void testSetRoleBindings_WithCache_InvalidatesCache() {
    String runtimeRoleName = "Business User";
    List<String> logicalRoleNames = Arrays.asList( "Read", "Write" );

    dao.setRoleBindings( runtimeRoleName, logicalRoleNames );

    // Verify cache invalidation was called
    verify( decisionCache, times( 1 ) ).invalidateAll( anyPredicate() );
  }

  @Test
  public void testSetRoleBindings_WithoutCache_DoesNotInvalidate() {
    TestableJcrRoleAuthorizationPolicyRoleBindingDao daoWithoutCache =
      new TestableJcrRoleAuthorizationPolicyRoleBindingDao(
        jcrTemplate,
        immutableRoleBindings,
        bootstrapRoleBindings,
        "Admin",
        tenantedRoleNameUtils,
        authorizationActions,
        null );

    String runtimeRoleName = "Business User";
    List<String> logicalRoleNames = Arrays.asList( "Read", "Write" );

    // Should not throw NPE when cache is null
    daoWithoutCache.setRoleBindings( runtimeRoleName, logicalRoleNames );

    // Verify no exception was thrown by asserting DAO still exists
    assertNotNull( daoWithoutCache );
  }

  @Test
  public void testSetRoleBindings_WithTenant_InvalidatesCache() {
    ITenant tenant = mock( ITenant.class );
    when( tenant.getId() ).thenReturn( "acme" );

    String runtimeRoleName = "Business User";
    List<String> logicalRoleNames = Arrays.asList( "Read", "Write" );

    dao.setRoleBindings( tenant, runtimeRoleName, logicalRoleNames );

    verify( decisionCache, times( 1 ) ).invalidateAll( anyPredicate() );
  }

  @Test
  public void testInvalidateDecisionCacheForRole_InvalidatesCorrectRole() {
    String runtimeRoleName = "Business User";
    AuthorizationRole role = new AuthorizationRole( runtimeRoleName );

    // Create a mock request that has the role
    IAuthorizationRequest requestWithRole = mock( IAuthorizationRequest.class );
    when( requestWithRole.getAllRoles() ).thenReturn( Set.of( role ) );

    // Create a mock request without the role
    IAuthorizationRequest requestWithoutRole = mock( IAuthorizationRequest.class );
    when( requestWithoutRole.getAllRoles() ).thenReturn( Set.of( new AuthorizationRole( "Other Role" ) ) );

    dao.invalidateDecisionCacheForRole( runtimeRoleName );

    // Verify invalidateAll was called with a predicate
    verify( decisionCache, times( 1 ) )
      .invalidateAll( argThat( predicate -> {
        // Test the predicate logic
        IAuthorizationDecisionCacheKey keyWithRole = mock( IAuthorizationDecisionCacheKey.class );
        when( keyWithRole.getRequest() ).thenReturn( requestWithRole );

        IAuthorizationDecisionCacheKey keyWithoutRole = mock( IAuthorizationDecisionCacheKey.class );
        when( keyWithoutRole.getRequest() ).thenReturn( requestWithoutRole );

        // Predicate should match keys with the role
        return predicate.test( keyWithRole ) && !predicate.test( keyWithoutRole );
      } ) );
  }

  // endregion

  // region Invalidation Predicate Tests

  @Test
  public void testInvalidationPredicate_MatchesRequestWithRole() {
    String runtimeRoleName = "Business User";
    AuthorizationRole role = new AuthorizationRole( runtimeRoleName );

    IAuthorizationRequest request = mock( IAuthorizationRequest.class );
    when( request.getAllRoles() ).thenReturn( Set.of( role ) );

    IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
    when( key.getRequest() ).thenReturn( request );

    dao.setRoleBindings( runtimeRoleName, List.of( "Read" ) );

    // Capture the predicate and verify it matches
    verify( decisionCache ).invalidateAll( argThat( predicate ->
      predicate.test( key )
    ) );
  }

  @Test
  public void testInvalidationPredicate_DoesNotMatchRequestWithoutRole() {
    String runtimeRoleName = "Business User";
    AuthorizationRole differentRole = new AuthorizationRole( "Power User" );

    IAuthorizationRequest request = mock( IAuthorizationRequest.class );
    when( request.getAllRoles() ).thenReturn( Set.of( differentRole ) );

    IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
    when( key.getRequest() ).thenReturn( request );

    dao.setRoleBindings( runtimeRoleName, List.of( "Read" ) );

    // Capture the predicate and verify it does NOT match
    verify( decisionCache ).invalidateAll( argThat( predicate ->
      !predicate.test( key )
    ) );
  }

  @Test
  public void testInvalidationPredicate_MatchesRequestWithMultipleRoles() {
    String runtimeRoleName = "Business User";
    AuthorizationRole role1 = new AuthorizationRole( runtimeRoleName );
    AuthorizationRole role2 = new AuthorizationRole( "Power User" );

    IAuthorizationRequest request = mock( IAuthorizationRequest.class );
    when( request.getAllRoles() ).thenReturn( Set.of( role1, role2 ) );

    IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
    when( key.getRequest() ).thenReturn( request );

    dao.setRoleBindings( runtimeRoleName, List.of( "Read" ) );

    // Should match because request contains the role among others
    verify( decisionCache ).invalidateAll( argThat( predicate ->
      predicate.test( key )
    ) );
  }

  @Test
  public void testInvalidationPredicate_MatchesRequestWithEmptyLogicalRoles() {
    String runtimeRoleName = "Business User";
    AuthorizationRole role = new AuthorizationRole( runtimeRoleName );

    IAuthorizationRequest request = mock( IAuthorizationRequest.class );
    when( request.getAllRoles() ).thenReturn( Set.of( role ) );

    IAuthorizationDecisionCacheKey key = mock( IAuthorizationDecisionCacheKey.class );
    when( key.getRequest() ).thenReturn( request );

    // Set with empty logical roles - should still invalidate based on runtime role
    dao.setRoleBindings( runtimeRoleName, List.of() );

    verify( decisionCache ).invalidateAll( argThat( predicate ->
      predicate.test( key )
    ) );
  }

  // endregion

  // region Edge Cases

  @Test
  public void testSetRoleBindings_EmptyLogicalRoleNames_InvalidatesCache() {
    String runtimeRoleName = "Business User";

    dao.setRoleBindings( runtimeRoleName, Collections.emptyList() );

    verify( decisionCache, times( 1 ) ).invalidateAll( anyPredicate() );
  }

  // endregion

  // region Helper Methods

  /**
   * Type-safe matcher for Predicate to avoid unchecked assignment warnings.
   */
  @SuppressWarnings( "unchecked" )
  private static Predicate<IAuthorizationDecisionCacheKey> anyPredicate() {
    return any( Predicate.class );
  }

  // endregion
}
