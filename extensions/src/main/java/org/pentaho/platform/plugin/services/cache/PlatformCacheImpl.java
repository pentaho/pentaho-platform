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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.plugin.services.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheProvider;
import org.pentaho.platform.api.cache.CacheRegionRequired;
import org.pentaho.platform.api.cache.CacheRegionsRequired;
import org.pentaho.platform.api.cache.ICacheExpirationRegistry;
import org.pentaho.platform.api.cache.IPlatformCache;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Sets;

@SuppressWarnings( "deprecation" )
public class PlatformCacheImpl implements ApplicationContextAware, IPlatformCache {

  static final Log LOG = LogFactory.getLog( PlatformCacheImpl.class );

  boolean isEnabled;

  ICacheExpirationRegistry cacheExpirationRegistry;
  CacheProvider cacheProvider;

  // Even though the map is not thread safe, we enforce
  // a read-write lock when accessing it at all times.
  Map<String, Cache> scopesCaches = new HashMap<String, Cache>();

  // Lock for cache access.
  ReentrantReadWriteLock lock = new ReentrantReadWriteLock( false );

  private ApplicationContext applicationContext;

  public PlatformCacheImpl() {
  }

  // This is invoked by the pentaho system listener
  public void start() {
    /*
     * The platform has finished initializing. We can start configuring things.
     */
    lock.writeLock().lock();

    try {
      if ( this.isEnabled ) {
        // Already configured and running.
        return;
      }

      this.cacheExpirationRegistry = PentahoSystem.get( ICacheExpirationRegistry.class );

      ISystemSettings settings = PentahoSystem.getSystemSettings();
      String s = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
      char c = s.charAt( s.length() - 1 );

      if ( ( c != '/' ) && ( c != '\\' ) ) {
        System.setProperty( "java.io.tmpdir", s + "/" ); //$NON-NLS-1$//$NON-NLS-2$
      }

      if ( settings != null ) {
        // We need to figure out which cache implementation to delegate
        // to. This is defined in pentaho.xml

        String cacheProviderClassName = settings.getSystemSetting( "cache-provider/class", null ); //$NON-NLS-1$

        Class<?> cacheClass = null;
        try {
          // Gotta make sure the class exists and is available.
          cacheClass = Class.forName( cacheProviderClassName );
        } catch ( Exception e ) {
          LOG.error( "Failed to load platform cache class.", e );
          cacheProviderClassName = null;
        }

        if ( cacheClass != null ) {
          Properties cacheProperties = getCacheProperties( settings );
          setupCacheProvider( cacheProviderClassName, cacheProperties );
          this.isEnabled = true;
          PentahoSystem.addLogoutListener( this );
        }
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  private final void setupCacheProvider( String cacheClass, Properties cacheProperties ) {

    Object obj = PentahoSystem.createObject( cacheClass );

    if ( null != obj ) {
      if ( obj instanceof CacheProvider ) {
        this.cacheProvider = (CacheProvider) obj;
        cacheProvider.start( cacheProperties );

        // First we need a space to store session caches.
        // This is a high level collection for all session data,
        // and not specific to a single session yet.
        // Sessions themselves will be maps of maps.
        Cache cache = buildCache( CacheScope.forSession( null ), cacheProperties );
        if ( cache == null ) {
          LOG.error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          scopesCaches.put( CacheScope.Scope.Session.name(), cache );
        }

        // Then we add a cache for global stuff. Nothing fancy.
        cache = buildCache( CacheScope.global(), cacheProperties );

        initAnnotations();
        if ( cache == null ) {
          LOG.error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          scopesCaches.put( CacheScope.global().getKey(), cache );
        }
      } else {
        LOG.error( Messages.getInstance().getString( "CacheManager.ERROR_0002_NOT_INSTANCE_OF_CACHE_PROVIDER" ) ); //$NON-NLS-1$
      }
    }
  }

  Cache buildCache( CacheScope scope, Properties cacheProperties ) {
    if ( this.cacheProvider != null ) {
      Cache cache = this.cacheProvider.buildCache( scope.getKey(), cacheProperties );
      LastModifiedCache lmCache = new LastModifiedCache( cache );
      if ( cacheExpirationRegistry != null ) {
        cacheExpirationRegistry.register( lmCache );
      } else {
        LOG.warn( Messages.getInstance().getErrorString( "CacheManager.WARN_0003_NO_CACHE_EXPIRATION_REGISTRY" ) );
      }
      return lmCache;
    } else {
      LOG.error( Messages.getInstance().getErrorString( "CacheManager.ERROR_0004_CACHE_PROVIDER_NOT_AVAILABLE" ) );
      return null;
    }
  }

  static Properties getCacheProperties( final ISystemSettings settings ) {
    Properties cacheProperties = new Properties();

    List propertySettings = settings.getSystemSettings( "cache-provider/properties/*" ); //$NON-NLS-1$

    for ( int i = 0; i < propertySettings.size(); i++ ) {
      Object obj = propertySettings.get( i );
      Element someProperty = (Element) obj;
      String propertyName = XmlDom4JHelper.getNodeText( "@name", someProperty, null ); //$NON-NLS-1$
      if ( propertyName != null ) {
        String propertyValue = someProperty.getTextTrim();
        if ( propertyValue != null ) {
          cacheProperties.put( propertyName, propertyValue );
        }
      }
    }
    return cacheProperties;
  }

  @Override public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    this.applicationContext = applicationContext;
  }

  void initAnnotations() {
    if ( applicationContext == null ) {
      // This is not a spring app. Bail out.
      // Some tests are heavy mocks and will not use
      // spring at all. We don't care about annotations then.
      return;
    }

    lock.writeLock().lock();
    try {
      List<CacheRegionRequired> regionsToLoad = new ArrayList<>();
      PentahoSystem.getApplicationContext();

      // Do singular first
      Map<String, Object> beans =
        applicationContext.getBeansWithAnnotation( CacheRegionRequired.class );
      for ( Entry<String, Object> entry : beans.entrySet() ) {
        CacheRegionRequired annotation =
          applicationContext.findAnnotationOnBean( entry.getKey(), CacheRegionRequired.class );
        regionsToLoad.add( annotation );
      }

      beans =
        applicationContext.getBeansWithAnnotation( CacheRegionsRequired.class );
      for ( Entry<String, Object> entry : beans.entrySet() ) {
        CacheRegionsRequired annotation =
          applicationContext.findAnnotationOnBean( entry.getKey(), CacheRegionsRequired.class );
        for ( CacheRegionRequired rr : annotation.value() ) {
          regionsToLoad.add( rr );
        }
      }

      Properties cacheProperties = getCacheProperties( PentahoSystem.getSystemSettings() );
      regionsToLoad.forEach( annotation -> {
        processAnnotation( annotation, cacheProperties );
      } );
    } finally {
      lock.writeLock().unlock();
    }
  }

  void processAnnotation( CacheRegionRequired annotation, Properties cacheProperties ) {
    CacheScope cacheScopeRegion = CacheScope.forRegion( annotation.region() );

    // Check if the region already exists.
    if ( !scopesCaches.containsKey( cacheScopeRegion.getKey() ) ) {
      // It does not. Create it.
      Cache cache = buildCache( cacheScopeRegion, cacheProperties );

      if ( cache == null ) {
        LOG.error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
      } else {
        scopesCaches.put( cacheScopeRegion.getKey(), cache );
      }
    }
  }

  public void onLogout( IPentahoSession session ) {
    clear( CacheScope.forSession( session ) );
  }

  public void put( CacheScope scope, Object key, Object value ) {
    if ( !isEnabled ) {
      return;
    }

    lock.writeLock().lock();

    try {
      switch ( scope.getScope() ) {
        case Global:
          scopesCaches.get( scope.getKey() ).put( key, value );
          break;
        case Region:
          Cache activeRegion = scopesCaches.get( scope.getKey() );

          if ( activeRegion == null ) {
            activeRegion = buildCache( scope, getCacheProperties( PentahoSystem.getSystemSettings() ) );
            scopesCaches.put( scope.getKey(), activeRegion );
          }
          activeRegion.put( key, value );
          break;

        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map<Object, Object> sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap == null ) {
            sessionMap = new HashMap<Object, Object>();
            cache.put( scope.getKey(), sessionMap );
          }
          sessionMap.put( key, value );
          break;
        default:
          throw new RuntimeException();
      }

    } finally {
      lock.writeLock().unlock();
    }
  }

  public Set entrySet( CacheScope scope ) {
    if ( !isEnabled ) {
      return Collections.emptySet();
    }
    lock.readLock().lock();
    try {
      switch ( scope.getScope() ) {
        case Global:
        case Region:
          return scopesCaches.get( scope.getKey() ).toMap().entrySet();
        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap != null ) {
            return sessionMap.entrySet();
          }
      }
      return Collections.emptySet();
    } finally {
      lock.readLock().unlock();
    }
  }

  public Set keySet( CacheScope scope ) {
    if ( !isEnabled ) {
      return Collections.emptySet();
    }
    lock.readLock().lock();
    try {
      switch ( scope.getScope() ) {
        case Global:
          return scopesCaches.get( scope.getKey() ).toMap().keySet();
        case Region:
          Cache activeRegion = scopesCaches.get( scope.getKey() );
          if ( activeRegion == null ) {
            return Collections.emptySet();
          } else {
            return activeRegion.toMap().keySet();
          }
        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap != null ) {
            return sessionMap.keySet();
          }
      }
      return Collections.emptySet();
    } finally {
      lock.readLock().unlock();
    }
  }

  public Set values( CacheScope scope ) {
    if ( !isEnabled ) {
      return Collections.emptySet();
    }
    lock.readLock().lock();
    try {
      switch ( scope.getScope() ) {
        case Global:
        case Region:
          return Sets.newHashSet( scopesCaches.get( scope.getKey() ).toMap().values() );
        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap != null ) {
            return Sets.newHashSet( sessionMap.values() );
          }
      }
      return Collections.emptySet();
    } finally {
      lock.readLock().unlock();
    }
  }

  public int size( CacheScope scope ) {
    if ( !isEnabled ) {
      return -1;
    }
    lock.readLock().lock();
    try {
      return keySet( scope ).size();
    } finally {
      lock.readLock().unlock();
    }
  }

  public Object get( CacheScope scope, Object key ) {
    if ( !isEnabled ) {
      return null;
    }
    lock.readLock().lock();
    try {
      switch ( scope.getScope() ) {
        case Global:
          return scopesCaches.get( scope.getKey() ).get( key );
        case Region:
          Cache activeRegion = scopesCaches.get( scope.getKey() );
          if ( activeRegion == null ) {
            return null;
          } else {
            return activeRegion.get( key );
          }
        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap != null ) {
            return sessionMap.get( key );
          }
        default:
          // no op.
      }
      return null;
    } finally {
      lock.readLock().unlock();
    }
  }

  public void remove( CacheScope scope, Object key ) {
    if ( !isEnabled ) {
      return;
    }

    lock.readLock().lock();

    try {
      switch ( scope.getScope() ) {
        case Global:
        case Region:
          scopesCaches.get( scope.getKey() ).remove( key );
          break;

        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map<Object, Object> sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap != null ) {
            sessionMap.remove( key );
          }
          break;
        default:
          throw new RuntimeException();
      }

    } finally {
      lock.readLock().unlock();
    }
  }

  public void clear() {
    if ( !isEnabled ) {
      return;
    }
    lock.writeLock().lock();
    try {
      // Clear the caches contents
      scopesCaches.entrySet().forEach( entry -> {
        entry.getValue().clear();
      } );
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void clear( CacheScope scope ) {
    clear( scope, false );
  }

  public void clear( CacheScope scope, boolean delete ) {
    if ( !isEnabled ) {
      return;
    }

    lock.writeLock().lock();

    try {
      switch ( scope.getScope() ) {
        case Global:
          scopesCaches.get( scope.getKey() ).clear();
          break;
        case Region:
          Cache activeRegion = scopesCaches.get( scope.getKey() );
          if ( activeRegion != null ) {
            activeRegion.clear();
            if ( delete ) {
              if ( cacheExpirationRegistry != null ) {
                // This is a bad API. The expiration registry
                // asks for the implementation instead of the cache
                // interface. Let's not boil the ocean here and cast
                // to the class we know was used above.
                cacheExpirationRegistry.unRegister( (LastModifiedCache) activeRegion );
              }
              activeRegion.destroy();
              scopesCaches.remove( scope.getKey() );
            }
          }
          break;

        case Session:
          Cache cache = scopesCaches.get( CacheScope.Scope.Session.name() );
          Map<Object, Object> sessionMap = (Map) cache.get( scope.getKey() );
          if ( sessionMap != null ) {
            sessionMap.clear();
          }
          // Also cleanup the map for this session
          cache.remove( scope.getKey() );
          break;
        default:
          throw new RuntimeException();
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void stop() {
    if ( !isEnabled ) {
      return;
    }
    lock.writeLock().lock();
    try {
      if ( isEnabled ) {
        scopesCaches.entrySet().forEach( entry -> {
          entry.getValue().clear();
          entry.getValue().destroy();
        } );
        scopesCaches.clear();
        cacheProvider.stop();
        isEnabled = false;
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public boolean isEnabled() {
    return isEnabled;
  }
}
