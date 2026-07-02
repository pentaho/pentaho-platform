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

import org.pentaho.platform.security.userroledao.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of UserDetailsService objects. Requests to load UserDetails will be delegated down to the
 * list in a round-robin fashion until a match is found. This first UserDetailsService to return a match will be used.
 * <p/>
 * Created by nbaker on 5/13/14.
 */
public class ChainedUserDetailsService implements UserDetailsService {

  private List<UserDetailsService> delegates = new ArrayList<UserDetailsService>();

  private static final Logger LOG = LoggerFactory.getLogger( ChainedUserDetailsService.class );

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
      } catch ( UsernameNotFoundException | CommunicationException exception ) {
        // ignore and continue;
        LOG.debug( "Exception in fetching username" + exception.getMessage() );
      }
    }
    throw new UsernameNotFoundException( Messages.getInstance().getString(
      "UserRoleDaoUserDetailsService.ERROR_0001_USER_NOT_FOUND" ) );
  }
}
