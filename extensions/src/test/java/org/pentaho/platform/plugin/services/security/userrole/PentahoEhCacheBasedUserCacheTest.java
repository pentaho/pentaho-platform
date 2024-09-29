/*!
 *
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
 *
 * Copyright (c) 2020-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Before;
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
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache();
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );
    userCache.setCache( ehcache );

    assertEquals( null, userCache.getUserFromCache( username ) );
    verify( ehcache, times( 1 ) ).get( username );
  }

  @Test
  public void testGetUserFromCacheCaseInsensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache();
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );
    userCache.setCache( ehcache );

    assertEquals( null, userCache.getUserFromCache( username ) );
    verify( ehcache, times( 1 ) ).get( username.toLowerCase() );
  }

  @Test
  public void testPutUserInCacheCaseSensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache();
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );
    userCache.setCache( ehcache );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
    verify( ehcache, times( 1 ) ).put( new Element( username, userDetails ) );

  }

  @Test
  public void testPutUserInCacheCaseInsensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache();
    UserDetails userDetails = mock( UserDetails.class );
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );
    userCache.setCache( ehcache );

    when( userDetails.getUsername() ).thenReturn( username );

    userCache.putUserInCache( userDetails );
    verify( ehcache, times( 1 ) ).put( new Element( username.toLowerCase(), userDetails ) );
  }


  @Test
  public void testRemoveUserFromCacheCaseSensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache();
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( true );
    userCache.setCache( ehcache );

    userCache.removeUserFromCache( username );
    verify( ehcache, times( 1 ) ).remove( username );
  }

  @Test
  public void testRemoveUserFromCacheCaseInsensitive() {
    PentahoEhCacheBasedUserCache userCache = new PentahoEhCacheBasedUserCache();
    Ehcache ehcache = mock( Ehcache.class );

    String username = "USERNAME";
    userCache.setCaseSensitive( false );
    userCache.setCache( ehcache );

    userCache.removeUserFromCache( username );
    verify( ehcache, times( 1 ) ).remove( username.toLowerCase() );
  }

}
