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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IAuthorizationServiceTest {

  private IAuthorizationService service;
  private IAuthorizationRequest mockRequest;
  private IAuthorizationDecision mockDecision;

  @BeforeEach
  public void setUp() {
    mockRequest = mock( IAuthorizationRequest.class );
    mockDecision = mock( IAuthorizationDecision.class );

    service = spy( new TestAuthorizationService( mockDecision ) );
  }

  @Test
  void testAuthorizeWithDefaultOptions() {
    IAuthorizationDecision result = service.authorize( mockRequest );

    assertNotNull( result );
    assertSame( mockDecision, result );

    // Verify that the main authorize method was called with default options
    verify( service, times( 1 ) )
      .authorize( mockRequest, IAuthorizationOptions.getDefault() );
  }

  private static class TestAuthorizationService implements IAuthorizationService {
    private final IAuthorizationDecision decision;

    public TestAuthorizationService( IAuthorizationDecision decision ) {
      this.decision = decision;
    }

    @Override
    @NonNull
    public IAuthorizationDecision authorize( @NonNull IAuthorizationRequest request,
                                             @NonNull IAuthorizationOptions options ) {
      return decision;
    }

    @Override
    @NonNull
    public Optional<IAuthorizationDecision> authorizeRule( @NonNull IAuthorizationRequest request,
                                                           @NonNull
                                                           IAuthorizationRule<? extends IAuthorizationRequest> rule,
                                                           @NonNull IAuthorizationOptions options ) {
      throw new UnsupportedOperationException();
    }
  }
}
