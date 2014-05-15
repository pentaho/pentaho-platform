/*
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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.security.userroledao.messages.Messages;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of UserDetailsService objects. Requests to load UserDetails will be delegated down to the
 * list in a round-robbin fashion until a match is found. This first UserDetailsService to return a match will be used.
 * <p/>
 * Created by nbaker on 5/13/14.
 */
public class ChainedUserDetailsService implements UserDetailsService {

  private List<UserDetailsService> delegates = new ArrayList<UserDetailsService>();

  public ChainedUserDetailsService( List<UserDetailsService> delegates ) {
    this.delegates.addAll( delegates );
  }


  @Override public UserDetails loadUserByUsername( String s ) throws UsernameNotFoundException, DataAccessException {
    for ( UserDetailsService delegate : delegates ) {
      try {
        UserDetails details = delegate.loadUserByUsername( s );
        if ( details != null ) {
          return details;
        }
      } catch ( UsernameNotFoundException ignored ) {
        // ignore and continue;
      }
    }
    throw new UsernameNotFoundException( Messages.getInstance().getString(
      "UserRoleDaoUserDetailsService.ERROR_0001_USER_NOT_FOUND" ) );
  }
}
