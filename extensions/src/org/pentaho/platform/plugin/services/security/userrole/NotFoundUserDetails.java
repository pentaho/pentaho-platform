/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

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
  public GrantedAuthority[] getAuthorities() {
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
