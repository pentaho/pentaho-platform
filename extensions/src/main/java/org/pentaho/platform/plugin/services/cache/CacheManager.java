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


package org.pentaho.platform.plugin.services.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.hibernate.Cache;
import org.hibernate.SessionFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.jcache.internal.JCacheAccessImpl;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionImpl;
import org.pentaho.platform.api.cache.ICacheExpirationRegistry;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.repository.hibernate.HibernateLoadEventListener;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides an access point for pluggable caching mechanisms. Right now, it only supports the caching
 * mechanisms implemented in <code>org.hibernate.cache</code> for Timestamp Regions.
 * <p>
 * To use the cache manager, you need to include the following information in your <code>pentaho.xml</code>.
 * 
 * <pre>
 * 
 *  &lt;cache-provider&gt;
 *    &lt;class&gt;org.pentaho.platform.plugin.services.cache.HvCacheRegionFactory&lt;/class&gt;
 *    &lt;region&gt;pentahoCache&lt;/region&gt;
 *    &lt;properties&gt;
 *      &lt;property name=&quot;someProperty&quot;&gt;someValue&lt;/property&gt;
 *    &lt;/properties&gt;
 *  &lt;/cache-provider&gt;
 * </pre>
 * 
 * <p>
 * The specified class must extend <code>org.hibernate.cache.spi.AbstractRegionFactory</code> and/or implement
 * <code>org.hibernate.cache.spi.RegionFactory</code>.
 * <p>
 * Each implementation of the <code>org.hibernate.cache.spi.RegionFactory</code> has slightly
 * different requirements with respect to the required input parameters - so, please see the classes in that package
 * for more information (available from the Sourceforge Hibernate project). Also, some region factories (notably the
 * <code>org.hibernate.cache.EhCacheRegionFactory</code>) completely ignore the passed in properties, and only configure
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
 * @see org.hibernate.cache.RegionFactory
 * @see org.hibernate.Cache
 * 
 * @author mbatchel
 * 
 */
public class CacheManager implements ICacheManager {

  protected static final Log logger = LogFactory.getLog( CacheManager.class );
  // ~ Instance Fields ======================================================
  private RegionFactory regionFactory;

  private Map<String, Cache> regionCache;

  private String regionFactoryClassname;

  private boolean cacheEnabled;

  private final Properties cacheProperties = new Properties();

  private ICacheExpirationRegistry cacheExpirationRegistry;

  // ~ Constructors =========================================================

  /**
   * The constructor performs the following tasks:
   * <p>
   * <ul>
   * <li>Gets the Hitachi Vantara System Settings</li>
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
      regionFactoryClassname = settings.getSystemSetting( "cache-provider/class", null ); //$NON-NLS-1$
      if ( regionFactoryClassname != null ) {
        Properties cacheProperties = getCacheProperties( settings );
        setupRegionProvider( cacheProperties );
        this.cacheEnabled = true;
      }
    }

    PentahoSystem.addLogoutListener( this );
  }

  protected void setupRegionProvider( Properties cacheProperties ) {
    Object obj = PentahoSystem.createObject( regionFactoryClassname );  //Should be an HvCacheRegionFactory
    cacheExpirationRegistry = PentahoSystem.get( ICacheExpirationRegistry.class );

    if ( null != obj ) {
      if ( obj instanceof RegionFactory ) {
        this.regionFactory = ( RegionFactory ) obj;
        Map<String, Object> cachePropertiesMap = cacheProperties.entrySet().stream()
          .collect( Collectors.toMap( e -> e.getKey().toString(), Map.Entry::getValue) );
        regionFactory.start( HibernateUtil.getSessionFactory().getSessionFactoryOptions(), cachePropertiesMap );
        regionCache = new HashMap<String, Cache>();
        ( (SessionFactoryImplementor) HibernateUtil.getSessionFactory() ).getServiceRegistry()
          .getService( EventListenerRegistry.class ).prependListeners(
            EventType.LOAD, new HibernateLoadEventListener() );
        Cache cache = buildCache( SESSION, HibernateUtil.getSessionFactory(), cacheProperties );
        if ( cache == null ) {
          CacheManager.logger
              .error( Messages.getInstance().getString( "CacheManager.ERROR_0005_UNABLE_TO_BUILD_CACHE" ) ); //$NON-NLS-1$
        } else {
          regionCache.put( SESSION, cache );
        }
        cache = buildCache( GLOBAL, HibernateUtil.getSessionFactory(), cacheProperties );
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
      regionFactory.stop();
    }
  }

  /**
   * Returns the underlying regionFactory (implements <code>org.hibernate.cache.RegionFactory</code>)
   * 
   * @return regionFactory.
   */
  protected RegionFactory getRegionFactory() {
    return regionFactory;
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
    for (Object obj : propertySettings) {
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
    return regionCache.get( region ) != null;
  }

  public void onLogout( final IPentahoSession session ) {
    killSessionCache( session );
  }

  public boolean addCacheRegion( String region, Properties cacheProperties ) {
    boolean returnValue = false;
    if ( checkCacheEnabled() ) {
      if ( !cacheEnabled( region ) ) {
        Cache cache = buildCache( region, HibernateUtil.getSessionFactory(), cacheProperties );
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
    }
    return returnValue;
  }

  public boolean addCacheRegion( String region ) {
    boolean returnValue = false;
    if ( checkCacheEnabled() ) {
      if ( !cacheEnabled( region ) ) {
        Cache cache = buildCache( region, HibernateUtil.getSessionFactory(), null );
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
    }
    return returnValue;
  }

  public boolean addCacheRegion( String region, Cache cache ) {
    if ( checkCacheEnabled() ) {
      if ( !cacheEnabled( region ) ) {
        regionCache.put( region, cache );
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
          "CacheManager.WARN_0002_REGION_ALREADY_EXIST", region ) );
      }
    } else {
      return false;
    }
    return true;
  }

  public void clearRegionCache( String region ) {
    if ( checkCacheEnabled() ) {
       HvCache cache = (HvCache) regionCache.get( region );
      if ( cache != null ) {
        try {
          try ( SessionImpl session = ( SessionImpl ) cache.getSessionFactory().openSession() ) {
            cache.getStorageAccess().clearCache( session );
          }
        } catch ( CacheException e ) {
          CacheManager.logger.error( Messages.getInstance().getString(
            "CacheManager.ERROR_0006_CACHE_EXCEPTION", e.getLocalizedMessage() ) ); //$NON-NLS-1$
        }
      } else {
        CacheManager.logger.info( Messages.getInstance().getString(
            "CacheManager.INFO_0001_CACHE_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
      }
    }
  }

  public void removeRegionCache( String region ) {
    if ( checkRegionEnabled( region ) ) {
      clearRegionCache( region );
    }
  }

  public void putInRegionCache( String region, Object key, Object value ) {
    if ( checkRegionEnabled( region ) ) {
      if ( key == null || value == null ) {
        return;
      }
      HvCache hvcache = ( HvCache ) regionCache.get( region );  //This is our LastModifiedCache or CarteStatusCache
      hvcache.getDirectAccessRegion().putIntoCache( key, value, null );
    }
  }

  public Object getFromRegionCache( String region, Object key ) {
    if ( checkRegionEnabled( region ) ) {
      HvCache hvcache = (HvCache) regionCache.get( region );  //This is our LastModifiedCache or CarteStatusCache
      return hvcache.getDirectAccessRegion().getFromCache( key, null );
    }
    return null;
  }

  public List<Object> getAllValuesFromRegionCache( String region ) {
    List<Object> list = new ArrayList<>();
    if ( checkRegionEnabled( region ) ) {
      HvCache hvcache = (HvCache) regionCache.get( region );  //This is our LastModifiedCache or CarteStatusCache
      javax.cache.Cache<Object, Object> cache = ( ( JCacheAccessImpl ) hvcache.getStorageAccess() ).getUnderlyingCache();
      cache.forEach( entry -> list.add( entry.getValue() ) );
    }
    return list;
  }

  public Set getAllKeysFromRegionCache( String region ) {
    if ( checkRegionEnabled( region ) ) {
      HvCache hvcache = (HvCache) regionCache.get( region );  //This is our LastModifiedCache or CarteStatusCache
      return hvcache.getAllKeys();
    }
    return null;
  }

  public Set getAllEntriesFromRegionCache( String region ) {
    if ( checkRegionEnabled( region ) ) {
      HvCache hvcache = (HvCache) regionCache.get( region );  //This is our LastModifiedCache or CarteStatusCache
      javax.cache.Cache<Object, Object> cache = ( ( JCacheAccessImpl ) hvcache.getStorageAccess() ).getUnderlyingCache();
      Set cacheValues = new HashSet<>();
      cache.forEach( entry -> cacheValues.add( entry.getValue() ) );
      return cacheValues;
    }
    return null;
  }

  public void removeFromRegionCache( String region, Object key ) {
    if ( checkRegionEnabled( region ) ) {
      HvCache hvcache = (HvCache) regionCache.get( region );
      hvcache.getStorageAccess().evictData( key );
    } else {
      CacheManager.logger.warn( Messages.getInstance().getString(
        "CacheManager.WARN_0003_REGION_DOES_NOT_EXIST", region ) ); //$NON-NLS-1$
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
          removeRegionCache( key );
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
      HvCache hvcache = (HvCache) regionCache.get( SESSION );
      if ( hvcache != null ) {
        javax.cache.Cache<Object, Object> cache = ( ( JCacheAccessImpl ) hvcache.getStorageAccess() ).getUnderlyingCache();
        if ( cache != null ) {
          Iterator<javax.cache.Cache.Entry<Object, Object>> cacheIterator = cache.iterator();
          while ( cacheIterator.hasNext() ) {
            javax.cache.Cache.Entry<Object, Object> entry = cacheIterator.next();
            String key = ( String ) entry.getKey();
            if ( key.contains( session.getId() ) ) {
              hvcache.getStorageAccess().evictData( key );
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
      return sessionId + "\t" + key;
    } else {
      throw new CacheException( Messages.getInstance().getErrorString( "CacheManager.ERROR_0001_NOSESSION" ) ); //$NON-NLS-1$
    }
  }

  private LastModifiedCache buildCache( String key, SessionFactory sessionFactory, Properties cacheProperties ) {
    if ( getRegionFactory() != null ) {
      TimestampsRegion timestampsRegion = getRegionFactory().buildTimestampsRegion( key, ( SessionFactoryImplementor ) sessionFactory );
      LastModifiedCache lmCache = new LastModifiedCache( timestampsRegion, sessionFactory );
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

  private boolean checkRegionEnabled( String region ) {
    if ( checkCacheEnabled() ) {
      if ( cacheEnabled( region ) ) {
        return true;
      } else {
        CacheManager.logger.warn( Messages.getInstance().getString(
          "CacheManager.WARN_0003_REGION_DOES_NOT_EXIST", region ) );
      }
    }
    return false;
  }

  private boolean checkCacheEnabled() {
    if ( cacheEnabled ) {
      return true;
    } else {
      CacheManager.logger.warn(
        Messages.getInstance().getString( "CacheManager.WARN_0001_CACHE_NOT_ENABLED" ) ); //$NON-NLS-1$
    }
    return false;
  }

  private boolean checkRegionNonExistent( String region ) {
    if ( checkCacheEnabled() ) {
      if ( cacheEnabled( region ) ) {
        CacheManager.logger.warn( Messages.getInstance().getString(
          "CacheManager.WARN_0002_REGION_ALREADY_EXIST", region ) ); //$NON-NLS-1$
      } else {
        return true;
      }
    }
    return false;
  }
}
