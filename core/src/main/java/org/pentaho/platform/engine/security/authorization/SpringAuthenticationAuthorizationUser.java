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

package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@code SpringAuthenticationAuthorizationUser} class implements the {@link IAuthorizationUser} interface
 * from data lazily computed from a given Spring Security {@link Authentication} object.
 * <p>
 * This implementation exposes the username and roles, but does not expose any user attributes.
 */
public class SpringAuthenticationAuthorizationUser implements IAuthorizationUser {
  @NonNull
  private final Authentication authentication;

  // Lazily initialized from authentication.
  @Nullable
  private Set<String> roles = null;

  public SpringAuthenticationAuthorizationUser( @NonNull Authentication authentication ) {
    Assert.notNull( authentication, "Argument `authentication` must not be null" );

    this.authentication = authentication;
  }

  @NonNull
  @Override
  public String getName() {
    Object principal = authentication.getPrincipal();

    assert principal != null;

    return principal instanceof UserDetails
      ? ( (UserDetails) principal ).getUsername()
      : principal.toString();
  }

  @NonNull
  @Override
  public Map<String, Object> getAttributes() {
    return Map.of();
  }

  @NonNull
  @Override
  public Set<String> getRoles() {
    // Thread safe: some initialization concurrency and duplicate work acceptable.
    if ( roles == null ) {
      roles = buildRoles();
    }

    return roles;
  }

  @NonNull
  protected Set<String> buildRoles() {
    Set<String> resultRoles = new LinkedHashSet<>();

    for ( GrantedAuthority authority : authentication.getAuthorities() ) {
      resultRoles.add( authority.getAuthority() );
    }

    return Collections.unmodifiableSet( resultRoles );
  }
}
