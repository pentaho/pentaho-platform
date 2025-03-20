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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.security.userrole.oauth.PentahoOAuthUserSync;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.pentaho.platform.security.userroledao.PentahoOAuthUser;
import org.pentaho.platform.security.userroledao.messages.Messages;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        performOAuthUserSync( s, delegate );
        UserDetails details = delegate.loadUserByUsername( s );
        if ( details != null ) {
          return details;
        }
      } catch ( UsernameNotFoundException | CommunicationException exception ) {
        // ignore and continue;
        LOG.debug( "Exception in fetching username" + exception.getMessage() );
      } catch ( ResourceAccessException resourceAccessException ) {
        LOG.error( "Exception Occurred ", resourceAccessException );
      }
    }
    throw new UsernameNotFoundException( Messages.getInstance().getString(
      "UserRoleDaoUserDetailsService.ERROR_0001_USER_NOT_FOUND" ) );
  }

  /**
   * This method is used to perform the OAuth user sync for the given username and delegate.
   * It checks if the delegate is an instance of UserRoleDaoUserDetailsService and if OAuth is enabled with dual auth.
   * If so, it retrieves the PentahoOAuthUser and performs the sync.
   * <p>
   * We are creating user objects in jackrabbit for every logged-in user via OAuth. And for each login via OAuth or
   * via username and password
   * the roles of the user are synced with the OAuth provider. This is done to ensure that the roles of the user are
   * always up to date.
   *
   * @param username The username to sync.
   * @param delegate The UserDetailsService delegate.
   */
  private static void performOAuthUserSync( String username, UserDetailsService delegate ) {
    if ( delegate instanceof UserRoleDaoUserDetailsService
            && PentahoOAuthUtility.isOAuthEnabledWithDualAuth()
            && PentahoOAuthUtility.shouldPerformLiveUpdate()) {
      PentahoOAuthUserSync pentahoOAuthUserSync =
              PentahoSystem.get( PentahoOAuthUserSync.class, "pentahoOAuthUserSync", PentahoSessionHolder.getSession() );
      if ( Objects.nonNull( pentahoOAuthUserSync ) ) {
        PentahoOAuthUser pentahoOAuthUser = (PentahoOAuthUser) ( (UserRoleDaoUserDetailsService) delegate).getPentahoOAuthUser( null, username );
        if ( StringUtils.isNotBlank( pentahoOAuthUser.getUserId() )
                && StringUtils.isNotBlank( pentahoOAuthUser.getRegistrationId() ) ) {
          pentahoOAuthUserSync.performSyncForUser(  pentahoOAuthUser );
        }
      }
    }
  }

}
