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
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.impl.copy.IdentityCopier;
import org.ehcache.jsr107.Eh107Configuration;
import org.pentaho.platform.engine.services.messages.Messages;

import javax.cache.configuration.Configuration;
import java.time.Duration;

/**
 * Utility class for Pentaho cache configuration operations.
 */
public class PentahoCacheUtil {

  private static final String DEFAULT_CACHE_ALIAS = "defaultCache";

  private static final Log logger = LogFactory.getLog( PentahoCacheUtil.class );

  private PentahoCacheUtil() {
    // Private Constructor to prevent instantiation
  }

  /**
   * Loads cache configuration from ehcache.xml.
   * First tries to find a cache-specific configuration, then falls back to the "defaultCache" configuration.
   * If not found in XML, creates a simple default programmatic configuration.
   *
   * @param regionName the name of the cache region to load configuration for
   * @param ehcacheUrl the URL of the ehcache.xml configuration file
   * @return the cache configuration to use
   */
  public static Configuration<Object, Object> getDefaultCacheConfiguration( String regionName, java.net.URL ehcacheUrl ) {
    try {
      if ( ehcacheUrl != null ) {
        org.ehcache.xml.XmlConfiguration xmlConfig = new org.ehcache.xml.XmlConfiguration( ehcacheUrl );

        // Try to find cache-specific configuration first
        org.ehcache.config.CacheConfiguration<?, ?> cacheConfig = xmlConfig.getCacheConfigurations().get( regionName );

        // If not found, try the "defaultCache" configuration
        if ( cacheConfig == null ) {
          cacheConfig = xmlConfig.getCacheConfigurations().get( DEFAULT_CACHE_ALIAS );
        }

        if ( cacheConfig != null ) {
          @SuppressWarnings( "unchecked" )
          org.ehcache.config.CacheConfiguration<Object, Object> typedConfig =
              (org.ehcache.config.CacheConfiguration<Object, Object>) cacheConfig;
          return Eh107Configuration.fromEhcacheCacheConfiguration( typedConfig );
        }
      }
    } catch ( Exception e ) {
      logger.warn(  Messages.getInstance().getString( "PentahoCacheUtil.WARN_CANNOT_LOAD_FROM_EHCACHE_XML", regionName ), e );
    }

    // Fallback: Create default programmatic configuration
    return createProgrammaticDefaultConfiguration();
  }

  /**
   * Creates a default programmatic cache configuration.
   * This is used as a fallback when ehcache.xml is not available or doesn't contain default configuration.
   *
   * @return a default cache configuration with TTL of 600 seconds and heap size of 1000 entries
   */
  public static Configuration<Object, Object> createProgrammaticDefaultConfiguration() {
    org.ehcache.config.CacheConfiguration<Object, Object> defaultConfig =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, Object.class,
            ResourcePoolsBuilder.heap( 1000 ) )
        .withExpiry( ExpiryPolicyBuilder.timeToLiveExpiration( Duration.ofSeconds( 600 ) ) )
        .withKeyCopier( new IdentityCopier<>() )
        .withValueCopier( new IdentityCopier<>() )
        .build();

    return Eh107Configuration.fromEhcacheCacheConfiguration( defaultConfig );
  }
}

