/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.cache.CacheRegionsInitializer;
import org.pentaho.platform.engine.core.system.cache.SimpleMapCacheManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityPrincipalProvider.*;

@ActiveProfiles( "test" )
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = SpringSecurityPrincipalProvider_Caching_Test
  .SpringSecurityPrincipalProviderConfiguration.class )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
public class SpringSecurityPrincipalProvider_Caching_Test {

  @Profile( "test" )
  @Configuration
  public static class SpringSecurityPrincipalProviderConfiguration {
    @Autowired
    AbstractApplicationContext context;

    @Bean
    @Scope( ConfigurableBeanFactory.SCOPE_SINGLETON )
    public IPentahoSystemListener cacheRegionInitializer() {
      CacheRegionsInitializer cacheRegionsInitializer = new CacheRegionsInitializer();
      context.getBeanFactory().addBeanPostProcessor( cacheRegionsInitializer );
      //context.refresh();
      return cacheRegionsInitializer;
    }

    @Bean
    @Primary
    @Scope( ConfigurableBeanFactory.SCOPE_SINGLETON )
    public ICacheManager iCacheManager() {
      return spy( new SimpleMapCacheManager() );
    }

    @Bean
    @Primary
    public SpringSecurityPrincipalProvider springSecurityPrincipalProvider() {
      SpringSecurityPrincipalProvider provider = new SpringSecurityPrincipalProvider();
      provider.init( new Properties() );
      return provider;
    }

    @Bean
    @Primary
    public IPluginManager pluginManager() {
      return mock( IPluginManager.class );
    }
  }

  private static final String USER_PRINCIPLE = "user";
  private static final String ROLE_PRINCIPLE = "role";

  @Autowired
  private SpringSecurityPrincipalProvider provider;
  private MicroPlatform mp;

  private ITenantedPrincipleNameResolver userResolver;
  private ITenantedPrincipleNameResolver roleResolver;
  @Autowired
  private ICacheManager cacheManager;
  @Autowired
  private IPentahoSystemListener initializer;

  private ITenant userTenant;
  private ITenant roleTenant;
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  @Before
  public void setUp() throws Exception {
    userResolver = mock( ITenantedPrincipleNameResolver.class );
    roleResolver = mock( ITenantedPrincipleNameResolver.class );

    doReturn( false ).when( cacheManager ).cacheEnabled( USER_CACHE_REGION );
    doReturn( false ).when( cacheManager ).cacheEnabled( ROLE_CACHE_REGION );

    userTenant = new Tenant( USER_PRINCIPLE, true );
    roleTenant = new Tenant( ROLE_PRINCIPLE, true );

    mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( "tenantedUserNameUtils", userResolver );
    mp.defineInstance( "tenantedRoleNameUtils", roleResolver );
    mp.addLifecycleListener( initializer );
    mp.start();

    User user = new User( USER_PRINCIPLE, "", true, true, true, true, new ArrayList<GrantedAuthority>() );
    provider = spy( provider );
    doReturn( user ).when( provider ).internalGetUserDetails( USER_PRINCIPLE );
  }

  @After
  public void tearDown() throws Exception {
    mp.stop();
    userResolver = null;
    roleResolver = null;
    cacheManager = null;
    provider = null;
    mp = null;
  }

  @Test
  public void registersCacheRegion() {
    verify( cacheManager ).addCacheRegion( USER_CACHE_REGION );
    verify( cacheManager ).addCacheRegion( ROLE_CACHE_REGION );
  }

  @Test
  public void clearsCache() {
    initUserCacheConditions();
    initRoleCacheConditions();

    provider.getPrincipal( USER_PRINCIPLE );
    provider.getPrincipal( ROLE_PRINCIPLE );
    provider.clearCaches();

    assertNull( "Users' cache should be cleared", cacheManager.getFromRegionCache( USER_CACHE_REGION,
      USER_PRINCIPLE ) );
    assertNull( "Roles' cache should be cleared", cacheManager.getFromRegionCache( ROLE_CACHE_REGION,
      ROLE_PRINCIPLE ) );
  }

  @Test
  public void userIsCached() throws Exception {
    initUserCacheConditions();
    testPrincipleIsCached( USER_PRINCIPLE, USER_CACHE_REGION );
  }

  @Test
  public void roleIsCached() throws Exception {
    initRoleCacheConditions();
    testPrincipleIsCached( ROLE_PRINCIPLE, ROLE_CACHE_REGION );
  }

  private void testPrincipleIsCached( String principle, String cache ) throws Exception {
    Principal principal = assertPrincipalMatches( principle );

    Object shouldBePrincipal = cacheManager.getFromRegionCache( cache, principle );
    assertEquals( principal, shouldBePrincipal );

    Principal shouldBeCached = provider.getPrincipal( principle );
    assertTrue( "Second invocation should return the same object (cached)", principal == shouldBeCached );
  }

  private Principal assertPrincipalMatches( String principle ) {
    Principal principal = provider.getPrincipal( principle );
    assertNotNull( principal );
    assertEquals( principle, principal.getName() );
    return principal;
  }

  @Test
  public void cacheManagersAbsenceIsHandled_Users() throws Exception {
    initUserCacheConditions();
    testCacheManagersAbsenceIsHandled( USER_PRINCIPLE );
  }

  @Test
  public void cacheManagersAbsenceIsHandled_Roles() throws Exception {
    initRoleCacheConditions();
    testCacheManagersAbsenceIsHandled( ROLE_PRINCIPLE );
  }

  private void testCacheManagersAbsenceIsHandled( String principle ) {
    provider.setCacheManager( null );
    assertPrincipalMatches( principle );
  }

  public void initUserCacheConditions() {
    when( userResolver.isValid( USER_PRINCIPLE ) ).thenReturn( true );
    when( userResolver.getTenant( USER_PRINCIPLE ) ).thenReturn( userTenant );
  }

  public void initRoleCacheConditions() {
    when( roleResolver.isValid( ROLE_PRINCIPLE ) ).thenReturn( true );
    when( roleResolver.getTenant( ROLE_PRINCIPLE ) ).thenReturn( roleTenant );
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
  }
}
