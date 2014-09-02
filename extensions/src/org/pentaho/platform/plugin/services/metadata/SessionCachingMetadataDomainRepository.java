/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

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
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is the platform implementation which provides session-based caching for an existing
 * {@link IMetadataDomainRepository}.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public class SessionCachingMetadataDomainRepository implements IMetadataDomainRepository,
    org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter, ILogoutListener {

  private static final Log logger = LogFactory.getLog( SessionCachingMetadataDomainRepository.class );

  /**
   * Public so others know where we're caching our objects
   */
  public static String CACHE_REGION = "metadata-domain-repository"; //$NON-NLS-1$

  private ICacheManager cacheManager;

  private IMetadataDomainRepository delegate;
  private static final String DOMAIN_CACHE_KEY_PREDICATE = "domain-id-cache-for-session:";

  /**
   * this as a public class so that if necessary someone can get access to a session key and clear the cache in their
   * own way via javascript rule / etc
   */
  public static class CacheKey implements Serializable {
    private static final long serialVersionUID = 1737869846319540136L;
    public String sessionId;
    public String domainId;

    protected CacheKey() { }

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
      throw new IllegalStateException( getClass().getSimpleName() + " (" + CACHE_REGION + ") cannot be initialized" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    PentahoSystem.addLogoutListener( this ); // So you can remove a users' region when their session disappears
  }

  /**
   * Simple callback interface to facilitate iterating over cache keys
   */
  protected static interface CacheIteratorCallback {
    /**
     * Will be called for each cache key found
     * 
     * @param cacheManager
     *          The cache manager we're iterating through
     * @param key
     *          Key from cache manager
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
   * @param callback
   *          {@see CacheCallback}
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
   * @param session
   *          Session to use for matching keys
   * @param callback
   *          {@see CacheCallback}
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
      return domain;
    }
    domain = delegate.getDomain( id );
    if ( domain != null ) {
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
   * @param domainId
   *          Domain id to remove from cache
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

  protected String generateDomainIdCacheKeyForSession( IPentahoSession session ) {
    return DOMAIN_CACHE_KEY_PREDICATE + session.getId();
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
    final IPentahoSession session = PentahoSessionHolder.getSession();

    final String domainKey = generateDomainIdCacheKeyForSession( session );
    Set<String> domainIds = (Set<String>) cacheManager.getFromRegionCache( CACHE_REGION, domainKey );
    if ( domainIds != null ) {
      // We've previously cached domainIds available for this session
      return domainIds;
    }
    // Domains are accessible by anyone. What they contain may be different so rely on the lookup to be
    // session-specific.
    domainIds = delegate.getDomainIds();
    cacheManager.putInRegionCache( CACHE_REGION, domainKey, new HashSet<String>( domainIds ) );
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
}
