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


import org.ehcache.core.Ehcache;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PentahoEhCacheBasedUserCacheTest {

  @Before
  public void setUp() {

  }

  @Test
  public void testGetUserFromCacheCaseSensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache( false );
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );
    userCache.setUserCache( ehcache );

    assertEquals( null, userCache.getUserFromCache( username ) );
    verify( ehcache, times( 1 ) ).get( username );
  }

  @Ignore
  @Test
  public void testGetUserFromCacheCaseInsensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache( false );
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );
    userCache.setUserCache( ehcache );

    assertEquals( null, userCache.getUserFromCache( username ) );
    verify( ehcache, times( 1 ) ).get( username.toLowerCase() );
  }

  @Ignore
  @Test
  public void testPutUserInCacheCaseSensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache( false );
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );
    userCache.setUserCache( ehcache );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
      verify( ehcache, times( 1 ) ).put( username.toLowerCase(), userDetails );

  }

  @Ignore
  @Test
  public void testPutUserInCacheCaseInsensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache( false);
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );
    userCache.setUserCache( ehcache );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
      verify( ehcache, times( 1 ) ).put( username.toLowerCase(), userDetails );
  }


  @Test
  public void testRemoveUserFromCacheCaseSensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache( false );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );
    userCache.setUserCache( ehcache );

    userCache.removeUserFromCache( username );
    verify( ehcache, times( 1 ) ).remove( username );
  }

  @Ignore
  @Test
  public void testRemoveUserFromCacheCaseInsensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache( true );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );
    userCache.setUserCache( ehcache );

    userCache.removeUserFromCache( username );
    verify( ehcache, times( 1 ) ).remove( username.toLowerCase() );
  }

}
