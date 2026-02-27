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
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class PentahoJCacheBasedUserCacheTest {
  private Cache jcache;
  private PentahoJCacheBasedUserCache userCache;

  @BeforeClass
  public static void setup() {
    mockStatic( Caching.class );
  }

  @Before
  public void setUp() {

    jcache = mock( Cache.class );
    CachingProvider provider = mock( CachingProvider.class );
    CacheManager cacheManager = mock( CacheManager.class );

    when( Caching.getCachingProvider() ).thenReturn( provider );
    when( provider.getCacheManager(any(), any()) ).thenReturn( cacheManager );
    when( cacheManager.getCache( anyString() ) ).thenReturn( jcache );
    userCache = new PentahoJCacheBasedUserCache( true );
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
    UserDetails userDetails = mock( UserDetails.class );
    Cache.Entry<String, UserDetails> cacheEntry = new Cache.Entry<String, UserDetails>() {
      @Override
      public String getKey() {
        return username;
      }

      @Override
      public UserDetails getValue() {
        return userDetails;
      }

      @Override
      public <T> T unwrap(Class<T> aClass) {
        return null;
      }
    };
    List<Cache.Entry<String, UserDetails>> cacheEntries = new ArrayList<>();
    cacheEntries.add( cacheEntry );

    userCache.setCaseSensitive( false );
    when( jcache.iterator() ).thenReturn( cacheEntries.iterator() );

    assertEquals( userDetails, userCache.getUserFromCache( username ) );
  }

  @Test
  public void testPutUserInCacheCaseSensitive() {
    UserDetails userDetails = mock( UserDetails.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
    verify( jcache, times( 1 ) ).put( username, userDetails );
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

  @Test
  public void testRemoveUserFromCacheCaseInsensitive() {
    String username = "USERNAME";
    userCache.setCaseSensitive( false );

    userCache.removeUserFromCache( username );
    verify( jcache, times( 1 ) ).remove( username.toLowerCase() );
  }

}
