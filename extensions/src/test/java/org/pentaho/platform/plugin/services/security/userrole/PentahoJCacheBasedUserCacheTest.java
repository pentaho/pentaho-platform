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


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.security.core.userdetails.UserDetails;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
@Ignore
public class PentahoJCacheBasedUserCacheTest {

  @Mock
  private Cache jcache;
  private PentahoJCacheBasedUserCache userCache;

  @Before
  public void setUp() {
    try (MockedStatic<Caching> mockedCaching = mockStatic(Caching.class)) {
      CachingProvider provider = mock(CachingProvider.class);
      CacheManager cacheManager = mock(CacheManager.class);

      mockedCaching.when(Caching::getCachingProvider).thenReturn(provider);
      when(provider.getCacheManager()).thenReturn(cacheManager);
      when(cacheManager.getCache(anyString(), any(), any())).thenReturn(jcache);
      userCache = new PentahoJCacheBasedUserCache(true);
    }
  }

  @Test
  public void testGetUserFromCacheCaseSensitive() {
    String username = "USERNAME";
    userCache.setCaseSensitive( true );

    assertNull(userCache.getUserFromCache(username));
    verify( jcache, times( 1 ) ).get( username );
  }

  @Test
  public void testGetUserFromCacheCaseInsensitive() {
    String username = "USERNAME";
    userCache.setCaseSensitive( false );

    assertNull(userCache.getUserFromCache(username));
    verify( jcache, times( 1 ) ).get( username.toLowerCase() );
  }

  @Test
  public void testPutUserInCacheCaseSensitive() {
    UserDetails userDetails = mock( UserDetails.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
      verify( jcache, times( 1 ) ).put( username.toLowerCase(), userDetails );
  }

  @Test
  public void testPutUserInCacheCaseInsensitive() {
    UserDetails userDetails = mock( UserDetails.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
      verify( jcache, times( 1 ) ).put( username.toLowerCase(), userDetails );
  }

  @Test
  public void testRemoveUserFromCacheCaseSensitive() {

    String username = "USERNAME";
    userCache.setCaseSensitive( true );

    userCache.removeUserFromCache( username );
    verify( jcache, times( 1 ) ).remove( username );
  }

  @Ignore
  @Test
  public void testRemoveUserFromCacheCaseInsensitive() {
    String username = "USERNAME";
    userCache.setCaseSensitive( false );

    userCache.removeUserFromCache( username );
    verify( jcache, times( 1 ) ).remove( username.toLowerCase() );
  }

}
