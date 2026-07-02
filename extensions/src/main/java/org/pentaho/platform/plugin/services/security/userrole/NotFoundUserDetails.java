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


package org.pentaho.platform.plugin.services.security.userrole;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;

public class NotFoundUserDetails implements UserDetails {
  private static final long serialVersionUID = 2330317789852451431L;
  private static final String ERROR_MSG = NotFoundUserDetails.class.getCanonicalName()
      + " is intended to serve as a placeholder in the user cache and never be used outside of it.";
  private final String username;
  private final UsernameNotFoundException originalException;

  public NotFoundUserDetails( String username, UsernameNotFoundException originalException ) {
    this.username = username;
    this.originalException = originalException;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    throw new IllegalStateException( ERROR_MSG );
  }

  @Override
  public String getPassword() {
    throw new IllegalStateException( ERROR_MSG );
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    throw new IllegalStateException( ERROR_MSG );
  }

  @Override
  public boolean isAccountNonLocked() {
    throw new IllegalStateException( ERROR_MSG );
  }

  @Override
  public boolean isCredentialsNonExpired() {
    throw new IllegalStateException( ERROR_MSG );
  }

  @Override
  public boolean isEnabled() {
    throw new IllegalStateException( ERROR_MSG );
  }

  public UsernameNotFoundException getOriginalException() {
    return originalException;
  }

}
