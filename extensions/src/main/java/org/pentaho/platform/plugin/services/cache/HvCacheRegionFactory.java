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

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.jcache.ConfigSettings;
import org.hibernate.cache.jcache.MissingCacheStrategy;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import javax.cache.Cache;
import java.util.Map;
import static org.hibernate.cache.spi.SecondLevelCacheLogger.L2CACHE_LOGGER;

public class HvCacheRegionFactory extends JCacheRegionFactory {

  private static final String EHCACHE_XML_PATH = "ehcache.xml";

  private MissingCacheStrategy missingCacheStrategy;

  @Override
  protected void prepareForUse( SessionFactoryOptions settings, Map<String, Object> configValues ) {
    super.prepareForUse( settings, configValues );
    this.missingCacheStrategy = MissingCacheStrategy.interpretSetting( configValues != null ? configValues.get( ConfigSettings.MISSING_CACHE_STRATEGY ) : null );
  }

  @Override
  protected Cache<Object, Object> createCache( String regionName ) {
    // Parent's getOrCreateCache() already checked getCache() - this is only called when cache doesn't exist in ehcache.xml
    // EhCache3 removed support for default cache creation from ehcache.xml so explicitly handling the default cache creation here
    switch ( missingCacheStrategy ) {
      case CREATE_WARN:
        L2CACHE_LOGGER.missingCacheCreated( regionName, ConfigSettings.MISSING_CACHE_STRATEGY, MissingCacheStrategy.CREATE.getExternalRepresentation() );
      case CREATE:
        return getCacheManager().createCache( regionName,
                PentahoCacheUtil.getDefaultCacheConfiguration( regionName,
                        HvCacheRegionFactory.class.getClassLoader().getResource( EHCACHE_XML_PATH ) ) );
      case FAIL:
        throw new CacheException( "On-the-fly creation of JCache Cache objects is not supported [" + regionName + "]" );
      default:
        throw new IllegalStateException( "Unsupported missing cache strategy: " + missingCacheStrategy );
    }
  }
}
