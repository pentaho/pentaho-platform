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

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * User: nbaker Date: 8/1/13
 */
public class TenantAwareUserDetailsServiceDecorator implements UserDetailsService {

  private final UserDetailsService delegate;
  private final ITenantedPrincipleNameResolver nameResolver;

  public TenantAwareUserDetailsServiceDecorator( UserDetailsService delegate,
      ITenantedPrincipleNameResolver nameResolver ) {
    this.delegate = delegate;
    this.nameResolver = nameResolver;
  }

  @Override
  public UserDetails loadUserByUsername( final String s ) throws UsernameNotFoundException, DataAccessException {
    return this.delegate.loadUserByUsername( extractUsername( s ) );
  }

  private String extractUsername( final String composite ) {
    return nameResolver.getPrincipleName( composite );
  }
}
