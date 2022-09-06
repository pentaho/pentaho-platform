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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.SecurityHelper;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * This is the platform implementation which provides session-based caching for an existing {@link
 * IMetadataDomainRepository}.
 *
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public class SessionCachingMetadataDomainRepository implements IMetadataDomainRepository,
    IPentahoMetadataDomainRepositoryExporter, ILogoutListener,
    IAclAwarePentahoMetadataDomainRepositoryImporter, IModelAnnotationsAwareMetadataDomainRepositoryImporter,
    IDataSourceAwareMetadataDomainRepository {

  private static final Log logger = LogFactory.getLog( SessionCachingMetadataDomainRepository.class );

  /**
   * Public so others know where we're caching our objects
   */
  public static String CACHE_REGION = "metadata-domain-repository"; //$NON-NLS-1$

  // default number for threads
  static final int DEFAULT_NUMBER_OF_THREADS = 3;

  ICacheManager cacheManager;
  boolean domainIdsCacheEnabled = true;

  int numberOfThreads = DEFAULT_NUMBER_OF_THREADS;

  private final IMetadataDomainRepository delegate;
  private static final String DOMAIN_CACHE_KEY_PREDICATE = "domain-id-cache-for-session:";

  /**
   * this as a public class so that if necessary someone can get access to a session key and clear the cache in their
   * own way via javascript rule / etc
   */
  public static class CacheKey implements Serializable {
    private static final long serialVersionUID = 1737869846319540136L;
    public String sessionId;
    public String domainId;

    protected CacheKey() {
    }

    public CacheKey( String sessionId, String domainId ) {
      this.sessionId = sessionId;
      this.domainId = domainId;
    }

    @Override
    public boolean equals( final Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( o == null || getClass() != o.getClass() ) {
        return false;
      }

      CacheKey cacheKey = (CacheKey) o;

      if ( domainId != null ? !domainId.equals( cacheKey.domainId ) : cacheKey.domainId != null ) {
        return false;
      }
      if ( sessionId != null ? !sessionId.equals( cacheKey.sessionId ) : cacheKey.sessionId != null ) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = sessionId != null ? sessionId.hashCode() : 0;
      result = 31 * result + ( domainId != null ? domainId.hashCode() : 0 );
      return result;
    }

    @Override
    public String toString() {
      return "(" + sessionId + "," + domainId + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  /**
   * Wraps the provided domain repository to provide session-based caching of domains.
   */
  public SessionCachingMetadataDomainRepository( final IMetadataDomainRepository delegate ) {
    if ( delegate == null ) {
      throw new NullPointerException();
    }
    this.delegate = delegate;

    cacheManager = PentahoSystem.getCacheManager( null ); // cache manager gets loaded just once...
    if ( cacheManager != null ) {
      if ( !cacheManager.cacheEnabled( CACHE_REGION ) ) {
        if ( !cacheManager.addCacheRegion( CACHE_REGION ) ) {
          cacheManager = null;
        }
      }
    }
    if ( cacheManager == null ) {
      throw new IllegalStateException(
        getClass().getSimpleName() + " (" + CACHE_REGION + ") cannot be initialized" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    PentahoSystem.addLogoutListener( this ); // So you can remove a users' region when their session disappears
    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    if ( systemConfig != null ) {
      String enableDomainIdCache = systemConfig.getProperty( "system.enableDomainIdCache" );
      domainIdsCacheEnabled = ( enableDomainIdCache == null ) || Boolean.valueOf( enableDomainIdCache );
      numberOfThreads = getNumberOfThreads( systemConfig );
    }
  }

  public SessionCachingMetadataDomainRepository( final IMetadataDomainRepository delegate,
                                                 ICacheManager cacheManager, boolean domainIdsCacheEnabled,
                                                 int numberOfThreads  ) {
    this.delegate = delegate;
    this.cacheManager = cacheManager;
    this.domainIdsCacheEnabled = domainIdsCacheEnabled;
    this.numberOfThreads = numberOfThreads;
  }

  /**
   * Retrieve positive number of threads from system configuration. If parsing error occurs or non-positive number is
   * supplied then {@link #DEFAULT_NUMBER_OF_THREADS} will be returned.
   * @param systemConfig
   * @return positive number else {@link #DEFAULT_NUMBER_OF_THREADS}
   */
  int getNumberOfThreads( ISystemConfig systemConfig ) {
    int convertedNumber = NumberUtils.toInt( systemConfig.getProperty( "system.number-threads" ) );
    // 0 if conversion fails, only accept positive numbers
    return ( convertedNumber < 1 ) ? DEFAULT_NUMBER_OF_THREADS : convertedNumber;
  }

  /**
   * Simple callback interface to facilitate iterating over cache keys
   */
  protected static interface CacheIteratorCallback {
    /**
     * Will be called for each cache key found
     *
     * @param cacheManager The cache manager we're iterating through
     * @param key          Key from cache manager
     * @return Returning false will cause the look that is calling this callback to break
     */
    public Boolean call( final ICacheManager cacheManager, final CacheKey key );
  }

  /**
   * Removes all keys it encounters from the known cache region
   */
  protected final CacheIteratorCallback REMOVE_ALL_CALLBACK = new CacheIteratorCallback() {
    @Override
    public Boolean call( final ICacheManager cacheManager, final CacheKey key ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Removing domain from cache: " + key ); //$NON-NLS-1$
      }
      cacheManager.removeFromRegionCache( CACHE_REGION, key );
      return true; // continue
    }
  };

  /**
   * Calls the callback for every key in the cache region
   *
   * @param callback {@see CacheCallback}
   */
  protected void forAllKeys( final CacheIteratorCallback callback ) {
    try {
      Set<?> cachedObjects = cacheManager.getAllKeysFromRegionCache( CACHE_REGION );
      if ( cachedObjects != null ) {
        for ( Object k : cachedObjects ) {
          if ( k instanceof CacheKey ) {
            CacheKey key = (CacheKey) k;
            if ( Boolean.FALSE.equals( callback.call( cacheManager, key ) ) ) {
              break;
            }
          }
        }
      }
    } catch ( Throwable e ) {
      // due to a known issue in hibernate cache
      // the getAll* methods of ICacheManager throw a NullPointerException if
      // cache values are null (this can happen due to cache object timeouts)
      // please see: http://opensource.atlassian.com/projects/hibernate/browse/HHH-3248
      if ( logger.isDebugEnabled() ) {
        logger.debug( "", e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Calls the callback for every key in the cache region whose session id matches the provided session's id.
   *
   * @param session  Session to use for matching keys
   * @param callback {@see CacheCallback}
   */
  protected void forAllKeysInSession( final IPentahoSession session, final CacheIteratorCallback callback ) {
    forAllKeys( new CacheIteratorCallback() {
      @Override
      public Boolean call( final ICacheManager cacheManager, final CacheKey key ) {
        if ( session.getId() == null ? key.sessionId == null : session.getId().equals( key.sessionId ) ) {
          if ( Boolean.FALSE.equals( callback.call( cacheManager, key ) ) ) {
            return false; // break
          }
        }
        return true; // continue
      }
    } );
  }

  @Override
  public Domain getDomain( final String id ) {
    final IPentahoSession session = PentahoSessionHolder.getSession();
    final CacheKey key = new CacheKey( session.getId(), id );
    Domain domain = (Domain) cacheManager.getFromRegionCache( CACHE_REGION, key );
    if ( domain != null ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Found domain in cache: " + key ); //$NON-NLS-1$
      }
      if ( delegate instanceof IAclAwarePentahoMetadataDomainRepositoryImporter && !( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).hasAccessFor( id ) ) {

        if ( logger.isDebugEnabled() ) {
          logger.debug( "User no longer has access to Domain, purging from session cache: " + key );
        }
        purgeDomain( domain.getId() );
        domain = null;
      }
      return domain;
    }
    domain = delegate.getDomain( id );
    if ( domain != null ) {

      if ( logger.isDebugEnabled() ) {
        logger.debug( "Requested Domain wasn't in Session Cache, but was found in the delegating repository: " + id );
      }

      SecurityHelper helper = new SecurityHelper();
      domain = helper.createSecureDomain( this, domain );
      // cache domain with the key we used to look it up, not whatever new id it might have now
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Caching domain by session: " + key ); //$NON-NLS-1$
      }
      cacheManager.putInRegionCache( CACHE_REGION, key, domain );
    }
    return domain;
  }

  /**
   * Remove all cache entries whose domain's id is equal to {@code domainId}.
   *
   * @param domainId Domain id to remove from cache
   */
  private void purgeDomain( final String domainId ) {
    forAllKeys( new CacheIteratorCallback() {
      @Override
      public Boolean call( ICacheManager cacheManager, CacheKey key ) {
        if ( domainId == null ? key.domainId == null : domainId.equals( key.domainId ) ) {
          cacheManager.removeFromRegionCache( CACHE_REGION, key );
        }
        return true; // continue
      }
    } );
    removeDomainFromIDCache( domainId );
  }

  @Override
  public void reloadDomains() {
    forAllKeys( REMOVE_ALL_CALLBACK );
    clearDomainIdsFromCache();
    delegate.reloadDomains();
  }

  @Override
  public void flushDomains() {
    forAllKeys( REMOVE_ALL_CALLBACK );
    clearDomainIdsFromCache();
    delegate.flushDomains();
  }

  protected void flushDomains( final IPentahoSession session ) {
    forAllKeysInSession( session, REMOVE_ALL_CALLBACK );
    clearDomainIdsFromCache( session );
  }

  /**
   * Remove domain ID cache for all sessions
   */
  protected void clearDomainIdsFromCache() {
    try {
      Set<?> cachedObjects = cacheManager.getAllKeysFromRegionCache( CACHE_REGION );
      if ( cachedObjects != null ) {
        for ( Object k : cachedObjects ) {
          if ( k instanceof String ) {
            String key = (String) k;
            if ( key != null && key.startsWith( DOMAIN_CACHE_KEY_PREDICATE ) ) {
              cacheManager.removeFromRegionCache( CACHE_REGION, key );
            }
          }
        }
      }
    } catch ( Throwable e ) {
      // due to a known issue in hibernate cache
      // the getAll* methods of ICacheManager throw a NullPointerException if
      // cache values are null (this can happen due to cache object timeouts)
      // please see: http://opensource.atlassian.com/projects/hibernate/browse/HHH-3248
      if ( logger.isDebugEnabled() ) {
        logger.debug( "", e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Remove domain ID cache for a given session
   *
   * @param session
   */
  protected void clearDomainIdsFromCache( IPentahoSession session ) {
    final String key = generateDomainIdCacheKeyForSession( session );
    if ( cacheManager.getFromRegionCache( CACHE_REGION, key ) != null ) {
      cacheManager.removeFromRegionCache( CACHE_REGION, key );
    }
  }

  /**
   * Determine if a domain Id is non-accessible. Then Remove all non-accessible domain ids from <code>domainIds</code>
   * and from all session domain ID caches.
   * @param domainIds
   * @return true if <code>domainIds</code> has change of membership, false otherwise
   */
  private boolean removeDomainFromIDCache( Set<String> domainIds ) {
    boolean dirtyCache = false;
    for ( String domainId : domainIds ) {
      if ( delegate instanceof IAclAwarePentahoMetadataDomainRepositoryImporter
              && !( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).hasAccessFor( domainId ) ) {
        domainIds.remove( domainId );
        removeDomainFromIDCache( domainId );
        dirtyCache = true;
      }
    }
    return dirtyCache;
  }

  /**
   * Remove a single domain ID from all session domain ID caches
   *
   * @param domainId
   */
  private void removeDomainFromIDCache( String domainId ) {
    try {
      Set<?> cachedObjects = cacheManager.getAllKeysFromRegionCache( CACHE_REGION );
      if ( cachedObjects != null ) {
        for ( Object k : cachedObjects ) {
          if ( k instanceof String ) {
            String key = (String) k;
            if ( key != null && key.startsWith( DOMAIN_CACHE_KEY_PREDICATE ) ) {
              Set<String> domainIds = (Set<String>) cacheManager.getFromRegionCache( CACHE_REGION, key );
              domainIds.remove( domainId );
              cacheManager.putInRegionCache( CACHE_REGION, key, domainIds );
            }
          }
        }
      }
    } catch ( Throwable e ) {
      // due to a known issue in hibernate cache
      // the getAll* methods of ICacheManager throw a NullPointerException if
      // cache values are null (this can happen due to cache object timeouts)
      // please see: http://opensource.atlassian.com/projects/hibernate/browse/HHH-3248
      if ( logger.isDebugEnabled() ) {
        logger.debug( "", e ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Create caching key String using <code>session</code>.
   * {@link #DOMAIN_CACHE_KEY_PREDICATE} and {@link IPentahoSession#getId()} are used in key creation.
   * @param session
   * @return
   */
  protected String generateDomainIdCacheKeyForSession( IPentahoSession session ) {
    return DOMAIN_CACHE_KEY_PREDICATE + session.getId();
  }

  /**
   * Create caching key String using <code>session</code> and <code>type</code>.
   * {@link #DOMAIN_CACHE_KEY_PREDICATE}, {@link IPentahoSession#getId()} and <code>type</code>
   * are used in key creation.
   * @param session
   * @param type
   * @return
   */
  protected String generateDomainIdCacheKeyForSession( IPentahoSession session, String type ) {
    return DOMAIN_CACHE_KEY_PREDICATE + type + ":" + session.getId();
  }

  @Override
  public void removeDomain( final String domainId ) {
    delegate.removeDomain( domainId );
    purgeDomain( domainId );
    removeDomainFromIDCache( domainId );
  }

  @Override
  public void storeDomain( final Domain domain, final boolean overwrite ) throws DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException {
    delegate.storeDomain( domain, overwrite );
    purgeDomain( domain.getId() );
    clearDomainIdsFromCache();
  }

  @Override
  public void removeModel( final String domainId, final String modelId ) throws DomainIdNullException,
    DomainStorageException {
    delegate.removeModel( domainId, modelId );
    purgeDomain( domainId );
  }

  @Override
  public Set<String> getDomainIds() {
    return getDomainIds( PentahoSessionHolder.getSession() );
  }

  Set<String> getDomainIds( IPentahoSession pentahoSession ) {
    final String domainKey = generateDomainIdCacheKeyForSession( pentahoSession );
    return getDomainIdsHelper( pentahoSession, domainKey, delegate::getDomainIds );
  }

  @Override
  public Set<String> getMetadataDomainIds() {
    return getMetadataDomainIds( PentahoSessionHolder.getSession() );
  }

  Set<String> getMetadataDomainIds( IPentahoSession pentahoSession ) {
    if ( delegate instanceof IDataSourceAwareMetadataDomainRepository ) {
      final String domainKey = generateDomainIdCacheKeyForSession( pentahoSession,
              PentahoDataSourceType.METADATA.toString() );
      return getDomainIdsHelper( pentahoSession,
              domainKey,
              ( (IDataSourceAwareMetadataDomainRepository) delegate )::getMetadataDomainIds );
    } else {
      throw new UnsupportedOperationException( "not supported" );
    }
  }

  @Override
  public Set<String> getDataSourceWizardDomainIds() {
    return getDataSourceWizardDomainIds( PentahoSessionHolder.getSession() );
  }

  Set<String> getDataSourceWizardDomainIds( IPentahoSession pentahoSession ) {
    if ( delegate instanceof IDataSourceAwareMetadataDomainRepository ) {
      final String domainKey = generateDomainIdCacheKeyForSession( pentahoSession,
              PentahoDataSourceType.DATA_SOURCE_WIZARD.toString() );
      return getDomainIdsHelper( pentahoSession,
              domainKey,
              ( (IDataSourceAwareMetadataDomainRepository) delegate )::getDataSourceWizardDomainIds );
    } else {
      throw new UnsupportedOperationException( "not supported" );
    }
  }

  /**
   * Wrapper to retrieve domains domain identifiers checking ACL. Will return results from cache, else it will
   * query delegate. Before return, will synchronously populate the cache for the specified domain identifiers.
   * @param session pentaho session
   * @param domainKey key used for caching
   * @param delegateGetDomainIds logic to retrieve domains from delegate if not in cache
   * @return domain identifiers
   */
  Set<String> getDomainIdsHelper( final IPentahoSession session, String domainKey,
                                  Supplier<Set<String>> delegateGetDomainIds ) {
    Set<String> domainIds = getDomainIdsFromCache( domainKey, delegateGetDomainIds );
    asyncPopulateCacheDomain( domainIds, session );
    return domainIds;
  }

  /**
   * Wrapper to retrieve domain identifiers while checking ACL. Will return results from cache, else it will
   * query delegate.
   * @param domainKey key used for caching
   * @param delegateGetDomainIds logic to retrieve domains from delegate if not in cache
   * @return domain identifiers
   */
  Set<String> getDomainIdsFromCache( String domainKey, Supplier<Set<String>> delegateGetDomainIds ) {
    Set<String> domainIds;
    if ( domainIdsCacheEnabled ) {
      domainIds = (Set<String>) cacheManager.getFromRegionCache( CACHE_REGION, domainKey );
      if ( domainIds != null ) {
        boolean dirtyCache = removeDomainFromIDCache( domainIds );
        if ( dirtyCache ) {
          cacheManager.putInRegionCache( CACHE_REGION, domainKey, new HashSet<>( domainIds ) );
        }
        // We've previously cached domainIds available for this session
        return domainIds;
      }
    } else {
      delegate.reloadDomains();
    }
    // Domains are accessible by anyone. What they contain may be different so rely on the lookup to be
    // session-specific.
    domainIds = delegateGetDomainIds.get();
    if ( domainIdsCacheEnabled ) {
      cacheManager.putInRegionCache( CACHE_REGION, domainKey, new HashSet<>( domainIds ) );
    }
    return domainIds;
  }

  @Override
  public String generateRowLevelSecurityConstraint( final LogicalModel model ) {
    return delegate.generateRowLevelSecurityConstraint( model );
  }

  @Override
  public boolean hasAccess( final int accessType, final IConcept aclHolder ) {
    return delegate.hasAccess( accessType, aclHolder );
  }

  @Override
  public void onLogout( final IPentahoSession session ) {
    flushDomains( session );
  }

  public Map<String, InputStream> getDomainFilesData( final String domainId ) {
    if ( delegate instanceof org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter ) {
      return ( (org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter) delegate )
        .getDomainFilesData( domainId );
    } else {
      throw new UnsupportedOperationException( "Exporting is not supported by this Metadata Domain Repository" );
    }
  }

  @Override
  public void storeDomain( InputStream inputStream, String domainId, boolean overwrite, RepositoryFileAcl acl )
    throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    if ( delegate instanceof IAclAwarePentahoMetadataDomainRepositoryImporter ) {
      ( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).storeDomain( inputStream, domainId, overwrite, acl );
    }
  }

  @Override
  public void setAclFor( String domainId, RepositoryFileAcl acl ) {
    if ( delegate instanceof IAclAwarePentahoMetadataDomainRepositoryImporter ) {
      ( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).setAclFor( domainId, acl );
    }
  }

  @Override
  public RepositoryFileAcl getAclFor( String domainId ) {
    if ( delegate instanceof IAclAwarePentahoMetadataDomainRepositoryImporter ) {
      return ( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).getAclFor( domainId );
    } else {
      return null;
    }
  }

  @Override
  public boolean hasAccessFor( String domainId ) {
    return !( delegate instanceof IAclAwarePentahoMetadataDomainRepositoryImporter )
      || ( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).hasAccessFor( domainId );
  }

  @Override
  public void storeDomain( InputStream inputStream, String domainId, boolean overwrite ) throws DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException {
    if ( delegate instanceof IPentahoMetadataDomainRepositoryImporter ) {
      ( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).storeDomain( inputStream, domainId, overwrite );
    }
  }

  @Override
  public void addLocalizationFile( String domainId, String locale, InputStream inputStream, boolean overwrite )
    throws DomainStorageException {
    if ( delegate instanceof IPentahoMetadataDomainRepositoryImporter ) {
      ( (IAclAwarePentahoMetadataDomainRepositoryImporter) delegate ).addLocalizationFile( domainId, locale,
          inputStream, overwrite );
    }
  }

  @Override public String loadAnnotationsXml( String domainId ) {
    if ( delegate instanceof IModelAnnotationsAwareMetadataDomainRepositoryImporter ) {
      return ( (IModelAnnotationsAwareMetadataDomainRepositoryImporter) delegate ).loadAnnotationsXml( domainId );
    }

    return null;
  }

  @Override public void storeAnnotationsXml( String domainId, String annotationsXml ) {
    if ( delegate instanceof IModelAnnotationsAwareMetadataDomainRepositoryImporter ) {
      ( (IModelAnnotationsAwareMetadataDomainRepositoryImporter) delegate )
          .storeAnnotationsXml( domainId, annotationsXml );
    }
  }

  /**
   * Asynchronously populate the cache for the specified domain identifiers.
   * @param domainIds collection of domain identifiers.
   * @param pentahoSession session used to set in spawned threads
   * @return asynchronously running thread
   */
  Thread asyncPopulateCacheDomain( Collection<String> domainIds, IPentahoSession pentahoSession ) {
    PentahoAsyncThreadRunner patr = new PentahoAsyncThreadRunner( numberOfThreads, pentahoSession );
    //create threads to individually populate the cache for each domain id
    return asyncPopulateCacheDomain( patr,
            createCallablesGetDomain( this, domainIds, pentahoSession.getId() ) );
  }

  /**
   * Asynchronously populate the cache for the specified domain identifiers.
   * @param pentahoAsyncThreadRunner
   * @param callables
   * @return asynchronously running thread
   */
  Thread asyncPopulateCacheDomain( PentahoAsyncThreadRunner pentahoAsyncThreadRunner,
                                  Collection<GetDomainCallable> callables ) {
    return pentahoAsyncThreadRunner.asyncRun( callables );
  }

  /**
   * Instantiate tasks (ie threads) to call {@link IMetadataDomainRepository#getDomain(String)}.
   * @param repository
   * @param domainIds
   * @param sessionId
   * @return
   */
  Collection<GetDomainCallable> createCallablesGetDomain( final IMetadataDomainRepository repository,
                                                         final Collection<String> domainIds,
                                                         String sessionId ) {
    return ofNullable( domainIds ).orElseGet( Collections::emptyList )
            .stream().map( id -> new GetDomainCallable( repository, id, sessionId, logger ) )
            .collect( Collectors.toList() );
  }

  /**
   * Callable to call {@link IMetadataDomainRepository#getDomain(String)} with some logging statement to help in
   * debug.
   */
  public static class GetDomainCallable implements Callable<String> {

    IMetadataDomainRepository repository;
    String domainId;
    String sessionId;
    Log log;

    public GetDomainCallable( IMetadataDomainRepository repository, String domainId, String sessionId, Log log ) {
      this.repository = repository;
      this.domainId = domainId;
      this.sessionId = sessionId;
      this.log = log;
    }

    @Override
    public String call() throws Exception {
      log.debug( String.format( "start thread #getDomain(domainId=%s) with sessionId=%s", domainId, sessionId ) );
      Domain domain = repository.getDomain( domainId );
      if ( domain == null ) {
        log.error( String.format( "Error #getDomain(domainId=%s) with sessionId=%s"
                + " -> null in GetDomainCallable#call", domainId, sessionId ) );
      }
      log.debug( String.format( "finished thread #getDomain(domainId=%s) with sessionId=%s", domainId, sessionId ) );
      return ( domain != null ) ? domain.getId() : null;
    }

    public String getDomainId() {
      return domainId;
    }
  }

  /**
   * Asynchronously executes a collection of tasks (ie {@link Callable} ). Contains logic that correctly
   * sets the {@link IPentahoSession} for each spawned thread. Wrapper around {@link ExecutorService}.
   */
  public static class PentahoAsyncThreadRunner {

    private static final Log logger = LogFactory.getLog( PentahoAsyncThreadRunner.class );

    // maximum number of threads in the thread pool.
    int numberOfThreads;

    // Pentaho session to be set in threads
    IPentahoSession pentahoSession;

    /** naming pattern used to creating executor threads that run the tasks/callables */
    private final String threadExecutorNamingPattern;

    /** name of thread that creates the executor service and calls {@link #executeTasks(ExecutorService, Collection)} */
    private final String threadMasterName;

    private static final String DEFAULT_THREAD_NAMING_PATTERN = "pentaho-runner-exec-%d";

    private static final String DEFAULT_THREAD_MASTER_NAME = "pentaho-runner-master";

    /**
     * Constructor to set necessary information for underlying {@link ExecutorService} instance.
     * @param numberOfThreads
     * @param pentahoSession
     * @param threadMasterName
     * @param threadExecutorNamingPattern
     */
    public PentahoAsyncThreadRunner( int numberOfThreads, IPentahoSession pentahoSession, String threadMasterName,
                                     String threadExecutorNamingPattern ) {
      this.numberOfThreads = numberOfThreads;
      this.pentahoSession = pentahoSession;
      this.threadMasterName = threadMasterName;
      this.threadExecutorNamingPattern = threadExecutorNamingPattern;
    }

    /**
     * Constructor to set necessary information for underlying {@link ExecutorService} instance.
     * @param numberOfThreads
     * @param pentahoSession
     */
    public PentahoAsyncThreadRunner( int numberOfThreads, IPentahoSession pentahoSession ) {
      this( numberOfThreads, pentahoSession, DEFAULT_THREAD_MASTER_NAME, DEFAULT_THREAD_NAMING_PATTERN );
    }

    ExecutorService createExecutorService() {

      BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
              .namingPattern( threadExecutorNamingPattern )
              .wrappedFactory( r -> new PentahoSessionThread( r, pentahoSession ) )
              .build();

      /** new threads have to use the same PentahoSession from parent thread that is servicing the REST request.
       * Otherwise, a new PentahoSession instance will be created without the correct credentials
       * and the calls to code that rely on the original PentahoSession instance will fail.
       * */
      ExecutorService executorService = Executors.newFixedThreadPool( numberOfThreads, threadFactory );


      logger.debug( String.format( "creating executorService with number of threads: %d and Pentaho sessionId: %s",
              numberOfThreads, pentahoSession.getId() ) );
      return executorService;
    }

    /**
     * Asynchronously execute all <code>tasks</code>. Setup overhead for initialization
     * of threading infrastructure will have minimal or no effect on runtime execution of calling class.
     * Wrapper around {@link ExecutorService}.
     *
     * @param tasks Collection of tasks that will be executed. For each callable,
     *                  Return non-empty string for success, null otherwise.
     * @return thread used to execute the running of the <code>tasks</code>. The function {@link Thread#start()}
     * will have already been called.
     */
    public <T> Thread asyncRun( Collection<? extends Callable<T>> tasks ) {

      return asyncRun( this::createExecutorService, tasks );
    }

    /**
     * Asynchronously execute all <code>tasks</code>. Setup overhead for initialization
     * of threading infrastructure will have minimal or no effect on runtime execution of calling class.
     * Wrapper around {@link ExecutorService}.
     *
     * @param fnCreateExecutorService lambda expression to create executor Service.
     *                                Goal is to create ExecutorService inside newly thread that will be returned.
     * @param tasks Collection of tasks that will be executed. For each callable,
     *                  Return non-empty string for success, null otherwise.
     * @return thread used to execute the running of the <code>tasks</code>. The function {@link Thread#start()}
     * will have already been called.
     */
    <T> Thread asyncRun( Supplier<? extends ExecutorService> fnCreateExecutorService, Collection<? extends Callable<T>> tasks ) {

      if ( tasks == null ) {
        return null;
      }
      /**
       *  Creating a thread here to initialize all the threading related objects and classes.
       *
       *  This approach minimizes the execution time from a calling class.
       *  The execution time of {@link #executeTasks(ExecutorService, Collection)} will not count towards the
       *  execution time of the calling class.
       */
      Thread thread = new Thread( () -> {
        ExecutorService executorService = fnCreateExecutorService.get();
        executeTasks( executorService, tasks );
        // shutdown properly, or else orphan process won't allow normal exit of program
        executorService.shutdown();
      }, threadMasterName );
      thread.start();
      return thread;
    }

    /**
     * Execute the tasks via {@link ExecutorService#invokeAll(Collection)}.
     * @param executorService
     * @param tasks
     * @return list of non-null results
     */
    <T> Collection<T> executeTasks( ExecutorService executorService, Collection<? extends Callable<T>> tasks ) {
      Set<T> successfulTaskReturns = new HashSet<>(); // keeping return values for debugging purposes
      try {
        logger.debug( String.format( "start executeTasks(executorService: %s, size(tasks): %d)",
                executorService.hashCode(), tasks.size() ) );
        List<Future<T>> futures = executorService.invokeAll( tasks );

        for ( Future<T> future : futures ) {
          T returnValue = futureGet( future );
          if ( returnValue != null ) {
            successfulTaskReturns.add( returnValue );
            logger.debug( String.format( "successful callable executeTasks(executorService: %s, size(tasks): %d)"
                            + " return value: %s", executorService.hashCode(), tasks.size(), returnValue ) );
          }
        }
        logger.debug( String.format( "end executeTasks(executorService: %s, size(tasks): %d) "
                        + " with size(successfulTaskReturns): %d ",
                executorService.hashCode(), tasks.size(), successfulTaskReturns.size() ) );
      } catch ( InterruptedException ie ) {
        logger.error( "Error in invokeAll tasks", ie );
        Thread.currentThread().interrupt();
      }
      return successfulTaskReturns;
    }

    /**
     * Wrapper around {@link Future#get()}. Handles the thrown exceptions.
     * @param future
     * @param <T>
     * @return future value or null otherwise
     */
    <T> T futureGet( Future<T> future ) {
      T returnValue = null;
      try {
        returnValue = future.get(); //blocking in submitted order
      } catch ( InterruptedException ie ) {
        logger.error( "Error - InterruptedException in executing task", ie );
        Thread.currentThread().interrupt();
      } catch ( ExecutionException ee ) {
        logger.error( "Error - ExecutionException in executing task", ee );
      }
      return returnValue;
    }
  }

  /**
   * Thread that sets the {@link IPentahoSession } for the thread see {@link PentahoSessionHolder}.
   */
  public static class PentahoSessionThread extends Thread {

    public PentahoSessionThread( Runnable target, IPentahoSession pentahoSession ) {
      super( target );
      PentahoSessionHolder.setSession( pentahoSession );
    }

  }

}
