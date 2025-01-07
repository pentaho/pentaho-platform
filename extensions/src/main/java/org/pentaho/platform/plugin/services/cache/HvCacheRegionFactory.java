/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024-2025 by Hitachi Vantara, LLC : http://www.pentaho.com
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
import javax.cache.configuration.MutableConfiguration;
import java.util.Map;
import static org.hibernate.cache.spi.SecondLevelCacheLogger.L2CACHE_LOGGER;

public class HvCacheRegionFactory extends JCacheRegionFactory {
  private MissingCacheStrategy missingCacheStrategy;

  @Override
  protected void prepareForUse( SessionFactoryOptions settings, Map<String, Object> configValues ) {
    super.prepareForUse( settings, configValues );
    this.missingCacheStrategy = MissingCacheStrategy.interpretSetting( configValues != null ? configValues.get( ConfigSettings.MISSING_CACHE_STRATEGY ) : null );
  }

  @Override
  protected Cache<Object, Object> createCache( String regionName ) {
    MutableConfiguration<Object, Object> configuration = new MutableConfiguration<>();
    configuration.setStoreByValue( false );
    switch ( missingCacheStrategy ) {
      case CREATE_WARN:
        L2CACHE_LOGGER.missingCacheCreated( regionName, ConfigSettings.MISSING_CACHE_STRATEGY, MissingCacheStrategy.CREATE.getExternalRepresentation() );
        return getCacheManager().createCache( regionName, configuration );
      case CREATE:
        return getCacheManager().createCache( regionName, configuration );
      case FAIL:
        throw new CacheException( "On-the-fly creation of JCache Cache objects is not supported [" + regionName + "]" );
      default:
        throw new IllegalStateException( "Unsupported missing cache strategy: " + missingCacheStrategy );
    }
  }
}
