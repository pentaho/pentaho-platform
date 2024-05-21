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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.config.SystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.plugin.services.metadata.SessionCachingMetadataDomainRepository.DEFAULT_NUMBER_OF_THREADS;

public class SessionCachingMetadataDomainRepositoryTest {

  @Test
  public void testStoreAnnotationsXml() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    SessionCachingMetadataDomainRepository repo = spy( new SessionCachingMetadataDomainRepository( mock ) );
    PentahoMetadataDomainRepository delegate = mock( PentahoMetadataDomainRepository.class );

    String domainId = "myDomain";
    String annotationsXml = "<annotations/>";

    repo.storeAnnotationsXml( domainId, annotationsXml );
    verify( delegate, times( 0 ) ).storeAnnotationsXml( domainId, annotationsXml );

    repo = spy( new SessionCachingMetadataDomainRepository( delegate ) ); // use a valid delegate
    repo.storeAnnotationsXml( domainId, annotationsXml );
    verify( delegate, times( 1 ) ).storeAnnotationsXml( domainId, annotationsXml );
  }

  @Test
  public void testLoadAnnotationsXml() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    SessionCachingMetadataDomainRepository repo = spy( new SessionCachingMetadataDomainRepository( mock ) );
    PentahoMetadataDomainRepository delegate = mock( PentahoMetadataDomainRepository.class );

    String domainId = "myDomain";

    repo.loadAnnotationsXml( domainId );
    verify( delegate, times( 0 ) ).loadAnnotationsXml( domainId );

    repo = spy( new SessionCachingMetadataDomainRepository( delegate ) ); // use a valid delegate
    repo.loadAnnotationsXml( domainId );
    verify( delegate, times( 1 ) ).loadAnnotationsXml( domainId );
  }

  @Test
  public void shouldNotUseDomainIdsCacheIfDisabled() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    PentahoSessionHolder.setSession( new StandaloneSession( "session", "1" ) );
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    Domain domain = new Domain();
    domain.setId( "id" );
    mock.setPersistedDomains( domain );

    ICacheManager manager = mock( ICacheManager.class );
    repo.cacheManager = manager;
    repo.domainIdsCacheEnabled = false;

    Set<String> domainIds = repo.getDomainIds();

    assertEquals( 1, domainIds.size() );
    assertTrue( domainIds.contains( "id" ) );
    verify( mock, times( 1 ) ).getDomainIds();
    verify( mock, times( 1 ) ).reloadDomains();
    verify( manager, times( 0 ) ).getFromRegionCache( "metadata-domain-repository",
            "domain-id-cache-for-session:1" );
    verify( manager, times( 0 ) ).addCacheRegion( "domain-id-cache-for-session:1" );
  }

  @Test
  public void shouldUseDomainIdsCacheIfEnabled() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = spy( new MockSessionAwareMetadataDomainRepository() );
    PentahoSessionHolder.setSession( new StandaloneSession( "session", "1" ) );
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    Domain domain = new Domain();
    domain.setId( "id" );
    mock.setPersistedDomains( domain );

    ICacheManager manager = mock( ICacheManager.class );
    Set<String> ids = new HashSet<>( Arrays.asList( "domainId1", "domainId2" ) );
    when( manager.getFromRegionCache( "metadata-domain-repository", "domain-id-cache-for-session:1" ) )
            .thenReturn( ids );

    repo.cacheManager = manager;
    repo.domainIdsCacheEnabled = true;

    Set<String> domainIds = repo.getDomainIds();
    assertEquals( ids, domainIds );
    verify( mock, times( 0 ) ).reloadDomains();
    verify( manager, times( 1 ) ).getFromRegionCache( "metadata-domain-repository",
            "domain-id-cache-for-session:1" );
  }

  @Test
  public void testCreateCallablesGetDomain() throws Exception {
    MockSessionAwareMetadataDomainRepository mock = Mockito.mock( MockSessionAwareMetadataDomainRepository.class );
    SessionCachingMetadataDomainRepository repo = new SessionCachingMetadataDomainRepository( mock );
    String sessionId = "test-sessionId-1";

    Collection<String> domainIds = Arrays.asList(
            "test-domainId-1", "test-domainId-2", "test-domainId-3",
            "test-domainId-4", "test-domainId-5" );

    Collection<SessionCachingMetadataDomainRepository.GetDomainCallable> callables =
            repo.createCallablesGetDomain( repo, domainIds, sessionId );

    assertEquals( domainIds.size(), callables.size() );

    domainIds.stream().forEach( id -> assertTrue( "domain id '" + id + "' not found",
            callables.stream().anyMatch( c -> c.getDomainId() == id ) ) );
  }

  @Test
  public void testGetDomainCallable_Call_Success() throws Exception {
    String domainId = "test-domain-1";
    Domain domain = new Domain();
    domain.setId( domainId );
    String sessionId = "test-sessionId-1";
    Log logger = Mockito.mock( Log.class );
    SessionCachingMetadataDomainRepository repo = Mockito.mock( SessionCachingMetadataDomainRepository.class );
    when( repo.getDomain( domainId ) ).thenReturn( domain );

    SessionCachingMetadataDomainRepository.GetDomainCallable getDomainCallable =
            new SessionCachingMetadataDomainRepository.GetDomainCallable( repo, domainId, sessionId, logger );

    String actual = getDomainCallable.call();

    assertEquals( domainId, actual );

  }

  @Test
  public void testGetDomainCallable_Call_Null() throws Exception {
    String domainId = "test-domain-1";
    String sessionId = "test-sessionId-1";
    Log logger = Mockito.mock( Log.class );
    SessionCachingMetadataDomainRepository repo = Mockito.mock( SessionCachingMetadataDomainRepository.class );
    when( repo.getDomain( domainId ) ).thenReturn( null );

    SessionCachingMetadataDomainRepository.GetDomainCallable getDomainCallable =
            new SessionCachingMetadataDomainRepository.GetDomainCallable( repo, domainId, sessionId, logger );

    String actual = getDomainCallable.call();

    assertNull( actual );

  }

  @Test
  public void testGetDomainIds() throws Exception {
    // SETUP 1
    IDataSourceAwareMetadataDomainRepository mockDataSourceAwareMetadataDomainRepository = Mockito.mock(
            IDataSourceAwareMetadataDomainRepository.class );
    ICacheManager mockCacheManager = Mockito.mock( ICacheManager.class );
    boolean domainIdsCacheEnabled = false;
    int numberOfThreads = 1;
    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    Set<String> domainIds = new HashSet<>( Arrays.asList(
            "testDomainId-1", "testDomainId-2", "testDomainId-3", "testDomainId-4", "testDomainId-5" ) );
    when( mockDataSourceAwareMetadataDomainRepository.getDomainIds() ).thenReturn( domainIds );
    SessionCachingMetadataDomainRepository scmdr1 = new SessionCachingMetadataDomainRepository(
            mockDataSourceAwareMetadataDomainRepository,
            mockCacheManager, domainIdsCacheEnabled, numberOfThreads );

    // EXECUTE 1
    Set<String> actualDomainIds = scmdr1.getDomainIds( pentahoSession );

    // VERIFY 1
    domainIds.stream().forEach( domainId -> assertTrue( "Expected id:" + domainId,
            actualDomainIds.contains( domainId ) ) );
  }

  @Test
  public void testGetMetadataDomainIds() throws Exception {
    // SETUP 1
    IDataSourceAwareMetadataDomainRepository mockDataSourceAwareMetadataDomainRepository = Mockito.mock(
            IDataSourceAwareMetadataDomainRepository.class );
    ICacheManager mockCacheManager = Mockito.mock( ICacheManager.class );
    boolean domainIdsCacheEnabled = false;
    int numberOfThreads = 1;
    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    Set<String> domainIds = new HashSet<>( Arrays.asList(
            "testDomainId-1", "testDomainId-2", "testDomainId-3", "testDomainId-4" ) );
    when( mockDataSourceAwareMetadataDomainRepository.getMetadataDomainIds() ).thenReturn( domainIds );
    SessionCachingMetadataDomainRepository scmdr1 = new SessionCachingMetadataDomainRepository(
            mockDataSourceAwareMetadataDomainRepository,
            mockCacheManager, domainIdsCacheEnabled, numberOfThreads );

    // EXECUTE 1
    Set<String> actualDomainIds = scmdr1.getMetadataDomainIds( pentahoSession );

    // VERIFY 1
    domainIds.stream().forEach( domainId -> assertTrue( "Expected id:" + domainId,
            actualDomainIds.contains( domainId ) ) );
  }

  @Test
  public void testGetMetadataDomainIds_NotSupported() throws Exception {
    // SETUP
    ICacheManager mockCacheManager = Mockito.mock( ICacheManager.class );
    boolean domainIdsCacheEnabled = false;
    int numberOfThreads = 1;
    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );

    IMetadataDomainRepository mockMetadataDomainRepository = Mockito.mock(
            IMetadataDomainRepository.class );
    SessionCachingMetadataDomainRepository scmdr2 = new SessionCachingMetadataDomainRepository(
            mockMetadataDomainRepository,
            mockCacheManager, domainIdsCacheEnabled, numberOfThreads );

    // EXECUTE & VERIFY
    try {
      scmdr2.getMetadataDomainIds( pentahoSession );
      Assert.fail( "Expected an UnsupportedOperationException to be thrown" );
    } catch ( UnsupportedOperationException uoe ) {
      assertThat( uoe.getMessage(), containsString( "not supported" ) );
    }
  }

  @Test
  public void testGetDataSourceWizardDomainIds() throws Exception {
    // SETUP 1
    IDataSourceAwareMetadataDomainRepository mockDataSourceAwareMetadataDomainRepository = Mockito.mock(
            IDataSourceAwareMetadataDomainRepository.class );
    ICacheManager mockCacheManager = Mockito.mock( ICacheManager.class );
    boolean domainIdsCacheEnabled = false;
    int numberOfThreads = 1;
    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    Set<String> domainIds = new HashSet<>( Arrays.asList(
            "testDomainId-1", "testDomainId-2", "testDomainId-3" ) );
    when( mockDataSourceAwareMetadataDomainRepository.getDataSourceWizardDomainIds() ).thenReturn( domainIds );
    SessionCachingMetadataDomainRepository scmdr1 = new SessionCachingMetadataDomainRepository(
            mockDataSourceAwareMetadataDomainRepository,
            mockCacheManager, domainIdsCacheEnabled, numberOfThreads );

    // EXECUTE 1
    Set<String> actualDomainIds = scmdr1.getDataSourceWizardDomainIds( pentahoSession );

    // VERIFY 1
    domainIds.stream().forEach( domainId -> assertTrue( "Expected id:" + domainId,
            actualDomainIds.contains( domainId ) ) );
  }

  @Test
  public void testGetDataSourceWizardDomainIds_NotSupported() throws Exception {
    // SETUP
    ICacheManager mockCacheManager = Mockito.mock( ICacheManager.class );
    boolean domainIdsCacheEnabled = false;
    int numberOfThreads = 1;
    IPentahoSession pentahoSession = Mockito.mock( IPentahoSession.class );
    IMetadataDomainRepository mockMetadataDomainRepository = Mockito.mock(
            IMetadataDomainRepository.class );
    SessionCachingMetadataDomainRepository scmdr2 = new SessionCachingMetadataDomainRepository(
            mockMetadataDomainRepository,
            mockCacheManager, domainIdsCacheEnabled, numberOfThreads );

    // EXECUTE & VERIFY
    try {
      scmdr2.getDataSourceWizardDomainIds( pentahoSession );
      Assert.fail( "Expected an UnsupportedOperationException to be thrown" );
    } catch ( UnsupportedOperationException uoe ) {
      assertThat( uoe.getMessage(), containsString( "not supported" ) );
    }
  }

  @Test
  public void testGenerateDomainIdCacheKeyForSession() throws Exception {
    // SETUP 1
    boolean domainIdsCacheEnabled = false;
    int numberOfThreads = 1;
    IPentahoSession pentahoSession = new StandaloneSession( "test-session", "123" );
    SessionCachingMetadataDomainRepository scmdr1 = new SessionCachingMetadataDomainRepository(
            null, null, domainIdsCacheEnabled, numberOfThreads );

    // EXECUTE 1
    String domainKey1 = scmdr1.generateDomainIdCacheKeyForSession( pentahoSession );

    // VERIFY 1
    assertEquals( "domain-id-cache-for-session:123", domainKey1 );

    // EXECUTE 2
    String domainKey2 = scmdr1.generateDomainIdCacheKeyForSession( pentahoSession, "testType" );

    // VERIFY 2
    assertEquals( "domain-id-cache-for-session:testType:123", domainKey2 );

    // VERIFY 3
    /**  generateDomainIdCacheKeyForSession(session, type) should be different or else issues with cache retrieval for
     * SessionCachingMetadataDomainRepository#getDomainIds()
     * and SessionCachingMetadataDomainRepository#getDataSourceWizardDomainIds()
     */
    assertNotSame( "domainKeys are the same", domainKey1, domainKey2 );
  }

  @Test
  public void testGetNumberOfThreads() throws Exception {
    //  test various versions cases and versions of number
    // SETUP 1
    int highNumberOfThreads = 101; // random high number, not default value
    int lowNumberOfThreads = 1; // should be below default
    Properties properties = new Properties();
    properties.setProperty( "number-threads", "-2" );
    ISystemConfig systemConfig = createSystemConfigTestObject( properties );
    SessionCachingMetadataDomainRepository scmdr = new SessionCachingMetadataDomainRepository(
            null, null, true, 1 );

    // VERIFY 1
    // test negative numbers resolve to default
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 2
    properties.setProperty( "number-threads", "0" );

    // VERIFY 2
    // test 0 resolves to default
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 3
    assertTrue( 0 < lowNumberOfThreads && lowNumberOfThreads < DEFAULT_NUMBER_OF_THREADS );
    properties.setProperty( "number-threads", Integer.toString( lowNumberOfThreads ) );

    // VERIFY 3
    assertEquals( lowNumberOfThreads, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 4
    properties.setProperty( "number-threads", Integer.toString( DEFAULT_NUMBER_OF_THREADS ) );

    // VERIFY 4
    // sanity check that no side effects on specifying default
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 5
    assertTrue(  lowNumberOfThreads < DEFAULT_NUMBER_OF_THREADS );
    properties.setProperty( "number-threads", Integer.toString( highNumberOfThreads ) );

    // VERIFY 5
    assertEquals( highNumberOfThreads, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 6
    // verifying boolean value doesn't resolve to 1
    properties.setProperty( "number-threads",  "true" );

    // VERIFY 6
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 7
    properties.setProperty( "number-threads",  "1H" );

    // VERIFY 7
    // verifying non number value resolves to default
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 8
    properties.setProperty( "number-threads",  "" );

    // VERIFY 8
    // verifying emptyString resolves to default
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );

    // SETUP 9
    properties.clear();

    // VERIFY 9
    // verifying if property is not specified (ie null) resolves to default
    assertEquals( DEFAULT_NUMBER_OF_THREADS, scmdr.getNumberOfThreads( systemConfig ) );
  }

  @Test
  public void testAsyncRun() throws Exception {
    final ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue();
    // SETUP
    Collection<Integer> integers = Arrays.asList( 1, 2, 3, 4 );

    Collection<Callable<Integer>> tasks = new ArrayList<>();
    for ( Integer i : integers ) {
      // update queue to verify later tasks actually run
      tasks.add( () -> { queue.add( i ); return i; }  );
    }

    ExecutorService executorService = Executors.newFixedThreadPool( 1 );
    SessionCachingMetadataDomainRepository.PentahoAsyncThreadRunner patr =
            new SessionCachingMetadataDomainRepository.PentahoAsyncThreadRunner( 1, null );

    // EXECUTE
    Thread thread = patr.asyncRun( () -> executorService, tasks );

    // VERIFY
    assertNotNull( thread );
    assertTrue( "Timeout happened before threads stopped",
            executorService.awaitTermination( 5, TimeUnit.SECONDS ) ); // should complete way before this
    integers.stream().forEach( i -> assertTrue( "thread/task " + i + " not found", queue.contains( i ) ) );
  }

  @Test
  public void testExecuteTasks() throws Exception {
    // SETUP
    Callable<Integer> task1 = () -> { return 1; };
    Callable<Integer> task2 = () -> { return null; }; // null values won't be returned
    Callable<Integer> task3 = () -> { return 3; };
    // exceptions won't be returned
    Callable<Integer> task4 = () -> { throw new Exception( "error in callable, but ignore" ); };
    Callable<Integer> task5 = () -> { return 5; };
    Callable<Integer> task6 = () -> { return 6; };

    Collection<Callable<Integer>> tasks = Arrays.asList( task1, task2, task3, task4, task5, task6 );

    ExecutorService executorService = Executors.newFixedThreadPool( 1 );

    SessionCachingMetadataDomainRepository.PentahoAsyncThreadRunner patr =
            new SessionCachingMetadataDomainRepository.PentahoAsyncThreadRunner( 1, null );

    // EXECUTE
    // ignore errors in console thrown from task2 and task4
    Collection<Integer> actualExecuteTasks = patr.executeTasks( executorService, tasks );

    // VERIFY
    assertEquals( 4, actualExecuteTasks.size() );
    assertTrue( actualExecuteTasks.contains( 1 ) );
    assertTrue( actualExecuteTasks.contains( 3 ) );
    assertTrue( actualExecuteTasks.contains( 5 ) );
    assertTrue( actualExecuteTasks.contains( 6 ) );
  }

  public ISystemConfig createSystemConfigTestObject( Properties properties ) throws Exception {
    IConfiguration configuration = Mockito.mock( IConfiguration.class );
    Mockito.when( configuration.getId() ).thenReturn( "system" );
    Mockito.when( configuration.getProperties() ).thenReturn( properties );

    List<IConfiguration> configs = new ArrayList<>();
    configs.add( configuration );
    return new SystemConfig( configs );
  }
}
