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

package org.pentaho.platform.plugin.services.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.pentaho.platform.api.cache.ICacheExpirationRegistry;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class provides an access point for pluggable caching mechanisms. Right now, it only supports the caching
 * mechanisms implemented in <code>org.hibernate.cache</code>.
 * <p>
 * To use the cache manager, you need to include the following information in your <code>pentaho.xml</code>.
 * 
 * <pre>
 * 
 *  &lt;cache-provider&gt;
 *    &lt;class&gt;org.hibernate.cache.xxxxxxxx&lt;/class&gt;
 *    &lt;region&gt;pentahoCache&lt;/region&gt;
 *    &lt;properties&gt;
 *      &lt;property name=&quot;someProperty&quot;&gt;someValue&lt;/property&gt;
 *    &lt;/properties&gt;
 *  &lt;/cache-provider&gt;
 * </pre>
 * 
 * <p>
 * The specified class must implement the <code>org.hibernate.cache.CacheProvider</code> interface.
 * <p>
 * Each implementation of the <code>org.hibernate.cache.CacheProvider</code> has slightly different requirements with
 * respect to the required input parameters - so, please see the classes in that package for more information (available
 * from the Sourceforge Hibernate project). Also, some cache providers (notably the
 * <code>org.hibernate.cache.EhCacheProvider</code>) completely ignore the passed in properties, and only configure
 * themselves by locating a configuration file (e.g. ehcache.xml) on the classpath.
 * 
 * <p>
 * The cache manager supports session-based caching (that is, caching of data that is user-specific) as well as
 * global-based caching (that is, caching of data that is system-wide). To differentiate between session-based and
 * global-based caching, there are different methods that get called depending upon the storage type.
 * 
 * <p>
 * Data that is cached for user sessions require an <code>IPentahoSession</code> object to be passed in. The cache
 * manager uses the <code>IPentahoSession.getId()</code> to classify saved objects underneath a specific user session.
 * No information is actually stored in the user session object. For an example of this, see <code><br>
 * putInSessionCache(IPentahoSession session, String key, Object value)</code>
 * <p>
 * Data that is server-wide (i.e. global) uses different methods for storage/retrieval/management. For an example of
 * this, see <code><br> 
 * getFromGlobalCache(Object key)</code>
 * <p>
 * <b>Example Usage:</b>
 * <p>
 * 
 * <pre>
 * String globalCachable = &quot;String to cache&quot;;
 * String globalCacheKey = &quot;StringKey&quot;;
 * CacheManager cacheManager = PentahoSystem.getCacheManager();
 * cacheManager.putInGlobalCache( globalCacheKey, globalCachable );
 * </pre>
 * 
 * <p>
 * <b>Important Considerations</b>
 * <ul>
 * <li>Most caches require objects that go into the cache <i> as well as their respective object key</i> implement
 * Serializable. It is a safe assumption that both the Key and the Value should implement Serializable.</li>
 * <li>Some caches are read-only. Other caches are read-write. What does this mean? It means that once you put an object
 * in the cache, you can't put an object into the cache with the same key. You'd need to remove it first</li>
 * 
 * </ul>
 * 
 * <p>
 * 
 * @see org.hibernate.cache.CacheProvider
 * @see org.hibernate.cache.Cache
 * 
 * @author mbatchel
 * 
 */
public class CacheManager implements ICacheManager {

  protected static final Log logger = LogFactory.getLog( CacheManager.class );
  // ~ Instance Fields ======================================================
  private CacheProvider cacheProvider;

  private Map<String, Cache> regionCache;

  private String cacheProviderClassName;

  private boolean cacheEnabled;

  private final Properties cacheProperties = new Properties();

  private ICacheExpirationRegistry cacheExpirationRegistry;

  // ~ Constructors =========================================================

  /**
   * The constructor performs the following tasks:
   * <p>
   * <ul>
   * <li>Gets the Pentaho System Settings</li>
   * <li>Reads the <code>cache-provider/class</code> element.</li>
   * <li>Reads the <code>cache-provider/region</code> element.</li>
   * <li>Reads in any properties under <code>cache-provider/properties/*</li>
   * <li>Attempts to instance the cache provider classes specified</li>
   * <li>Starts the cache (see the <code>org.hibernate.cache.CacheProvider</code> interface)</li>
   * <li>Calls the buildCache method (see the <code>org.hibernate.cache.CacheProvider</code> interface)</li>
   * </ul>
   * <p>
   * 
   */
  public CacheManager() {
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    String s = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
    char c = s.charAt( s.length() - 1 );
    if ( ( c != '/' ) && ( c != '\\' ) ) {
      System.setProperty( "java.io.tmpdir", s + "/" ); //$NON-NLS-1$//$NON-NLS-2$
    }
    if ( settings != null ) {
      cacheProviderClassName = settings.getSystemSetting( "cache-provider/class", null ); //$NON-NLS-1$
      if ( cacheProviderClassName != null ) {
        Properties cacheProperties = getCacheProperties( settings );
        setupCacheProvider( cacheProperties );
        this.cacheEnabled = true;
      }
    }

    PentahoSystem.addLogoutListener( this );
  }

  protected void setupCacheProvider( Properties cacheProperties ) {
    Object obj = PentahoSystem.createObject( cacheProviderClassName );
    cacheExpirationRegistry = PentahoSystem.get( ICacheExpirationRegistry.class, null );

    if ( null != obj ) {
      if ( obj instanceof CacheProvider ) {
        this.cacheProvider = (CacheProvider) obj;
        cacheProvider.start( cacheProperties );
        regionCache = new HashMap<String, Cache>();
        Cache cache = buildCache( SESSION, cacheProperties );
        if ( cache == null ) {
          CacheManager.logger
              .error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          regionCache.put( SESSION, cache );
        }
        cache = buildCache( GLOBAL, cacheProperties );
        if ( cache == null ) {
          CacheManager.logger
              .error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          regionCache.put( GLOBAL, cache );
        }
      } else {
        CacheManager.logger.error( Messages.getInstance().getString(
            "CacheManager.ERROR_0002_NOT_INSTANCE_OF_CACHE_PROVIDER" ) ); //$NON-NLS-1$
      }
    }
  }

  public void cacheStop() {
    if ( cacheEnabled ) {
      regionCache.clear();
      cacheProvider.stop();
    }
  }

  /**
   * Returns the underlying cache provider (implements <code>org.hibernate.cache.CacheProvider</code>
   * 
   * @return cacheProvider.
   */
  protected CacheProvider getCacheProvider() {
    return cacheProvider;
  }

  /**
   * Populates the properties object from the pentaho.xml
   * 
   * @param settings
   *          The Pentaho ISystemSettings object
   */
  private Properties getCacheProperties( final ISystemSettings settings ) {
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

  public boolean cacheEnabled( String region ) {
    Cache cache = regionCache.get( region );
    if ( cache == null ) {
      return false;
    }
    return true;
  }

  public void onLogout( final IPentahoSession session ) {
    removeRegionCache( session.getName() );
  }

  public boolean addCacheRegion( String region, Properties cacheProperties ) {
    boolean returnValue = false;
    if ( cacheEnabled ) {
      if ( !cacheEnabled( region ) ) {
        Cache cache = buildCache( region, cacheProperties );
        if ( cache == null ) {
          CacheManager.logger
              .error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          regionCache.put( region, cache );
          returnValue = true;
        }
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
            "CacheManager.WARN_0002_REGION_ALREADY_EXIST", region ) ); //$NON-NLS-1$
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
    return returnValue;
  }

  public boolean addCacheRegion( String region ) {
    boolean returnValue = false;
    if ( cacheEnabled ) {
      if ( !cacheEnabled( region ) ) {
        Cache cache = buildCache( region, null );
        if ( cache == null ) {
          CacheManager.logger
              .error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          regionCache.put( region, cache );
          returnValue = true;
        }
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
            "CacheManager.WARN_0002_REGION_ALREADY_EXIST", region ) ); //$NON-NLS-1$
        returnValue = true;
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
    return returnValue;
  }

  public void clearRegionCache( String region ) {
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cache != null ) {
        try {
          cache.clear();
        } catch ( CacheException e ) {
          CacheManager.logger.error( Messages.getInstance().getString(
            "CacheManager.ERROR_0006_CACHE_EXCEPTION", e.getLocalizedMessage() ) ); //$NON-NLS-1$
        }
      } else {
        CacheManager.logger.info( Messages.getInstance().getString(
            "CacheManager.INFO_0001_CACHE_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
  }

  public void removeRegionCache( String region ) {
    if ( cacheEnabled ) {
      if ( cacheEnabled( region ) ) {
        clearRegionCache( region );
      } else {
        CacheManager.logger.info( Messages.getInstance().getString(
            "CacheManager.INFO_0001_CACHE_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
  }

  public void putInRegionCache( String region, Object key, Object value ) {
    if ( cacheEnabled ) {
      if ( cacheEnabled( region ) ) {
        Cache cache = regionCache.get( region );
        cache.put( key, value );
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
            "CacheManager.WARN_0003_REGION_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
  }

  public Object getFromRegionCache( String region, Object key ) {
    Object returnValue = null;
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cacheEnabled( region ) ) {
        returnValue = cache.get( key );
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
            "CacheManager.WARN_0003_REGION_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }

    return returnValue;
  }

  public List getAllValuesFromRegionCache( String region ) {
    List list = new ArrayList<Object>();
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cacheEnabled( region ) ) {
        Map cacheMap = cache.toMap();
        if ( cacheMap != null ) {
          Iterator it = cacheMap.entrySet().iterator();
          while ( it.hasNext() ) {
            Map.Entry entry = (Map.Entry) it.next();
            list.add( entry.getValue() );
          }
        }
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
    return list;
  }

  public Set getAllKeysFromRegionCache( String region ) {
    Set set = null;
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cacheEnabled( region ) ) {
        Map cacheMap = cache.toMap();
        if ( cacheMap != null ) {
          set = cacheMap.keySet();
        }
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
    return set;
  }

  public Set getAllEntriesFromRegionCache( String region ) {
    Set set = null;
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cacheEnabled( region ) ) {
        Map cacheMap = cache.toMap();
        if ( cacheMap != null ) {
          set = cacheMap.entrySet();
        }
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
    return set;
  }

  public void removeFromRegionCache( String region, Object key ) {
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cacheEnabled( region ) ) {
        cache.remove( key );
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
            "CacheManager.WARN_0003_REGION_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
      }
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
  }

  public boolean cacheEnabled() {
    return cacheEnabled;
  }

  public void clearCache() {
    if ( cacheEnabled ) {
      Iterator it = regionCache.entrySet().iterator();
      while ( it.hasNext() ) {
        Map.Entry entry = (Map.Entry) it.next();
        String key = ( entry.getKey() != null ) ? entry.getKey().toString() : ""; //$NON-NLS-1$
        if ( key != null ) {
          Cache cache = regionCache.get( key );
          cache.clear();
        }
      }
    }
  }

  public Object getFromGlobalCache( Object key ) {
    return getFromRegionCache( GLOBAL, key );
  }

  public Object getFromSessionCache( IPentahoSession session, String key ) {
    return getFromRegionCache( SESSION, getCorrectedKey( session, key ) );
  }

  public void killSessionCache( IPentahoSession session ) {
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( SESSION );
      if ( cache != null ) {
        Map cacheMap = cache.toMap();
        if ( cacheMap != null ) {
          Set set = cacheMap.keySet();
          Iterator it = set.iterator();
          while ( it.hasNext() ) {
            String key = (String) it.next();
            if ( key.indexOf( session.getId() ) >= 0 ) {
              cache.remove( key );
            }
          }
        }
      }
    }
  }

  public void killSessionCaches() {
    removeRegionCache( SESSION );
  }

  public void putInGlobalCache( Object key, Object value ) {
    putInRegionCache( GLOBAL, key, value );
  }

  public void putInSessionCache( IPentahoSession session, String key, Object value ) {
    putInRegionCache( SESSION, getCorrectedKey( session, key ), value );
  }

  public void removeFromGlobalCache( Object key ) {
    removeFromRegionCache( GLOBAL, key );
  }

  public void removeFromSessionCache( IPentahoSession session, String key ) {
    removeFromRegionCache( SESSION, getCorrectedKey( session, key ) );
  }

  private String getCorrectedKey( final IPentahoSession session, final String key ) {
    String sessionId = session.getId();
    if ( sessionId != null ) {
      String newKey = sessionId + "\t" + key; //$NON-NLS-1$
      return newKey;
    } else {
      throw new CacheException( Messages.getInstance().getErrorString( "CacheManager.ERROR_0001_NOSESSION" ) ); //$NON-NLS-1$
    }
  }

  private LastModifiedCache buildCache( String key, Properties cacheProperties ) {
    if ( getCacheProvider() != null ) {
      Cache cache = getCacheProvider().buildCache( key, cacheProperties );
      LastModifiedCache lmCache = new LastModifiedCache( cache );
      if ( cacheExpirationRegistry != null ) {
        cacheExpirationRegistry.register( lmCache );
      } else {
        logger.warn( Messages.getInstance().getErrorString( "CacheManager.WARN_0003_NO_CACHE_EXPIRATION_REGISTRY" ) );
      }
      return lmCache;
    } else {
      logger.error( Messages.getInstance().getErrorString( "CacheManager.ERROR_0004_CACHE_PROVIDER_NOT_AVAILABLE" ) );
      return null;
    }
  }

  @Override
  public long getElementCountInRegionCache( String region ) {
    if ( cacheEnabled ) {
      Cache cache = regionCache.get( region );
      if ( cache != null ) {
        try {
          long memCnt = cache.getElementCountInMemory();
          long discCnt = cache.getElementCountOnDisk();
          return memCnt + discCnt;
        } catch ( Exception ignored ) {
          return -1;
        }
      } else {
        return -1;
      }
    } else {
      return -1;
    }
  }

  @Override
  public long getElementCountInSessionCache() {
    return getElementCountInRegionCache( SESSION );
  }

  @Override
  public long getElementCountInGlobalCache() {
    return getElementCountInRegionCache( GLOBAL );
  }
}
