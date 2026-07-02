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


package org.pentaho.platform.config;

import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: nbaker Date: 4/2/13
 */
public class SystemConfig implements ISystemConfig {

  private static final Pattern pattern = Pattern.compile( "([^.]+)\\.(.+)" );
  private static final Logger logger = LoggerFactory.getLogger( SystemConfig.class );

  private final Map<String, IConfiguration> configs = new ConcurrentHashMap<>();

  public SystemConfig() {
  }

  public SystemConfig( List<IConfiguration> startingConfigs ) throws IOException {
    for ( IConfiguration startingConfig : startingConfigs ) {
      this.registerConfiguration( startingConfig );
    }
  }

  public String getProperty( String placeholder ) {
    // placeholder must be in the form of ID.PROP where
    if ( !pattern.matcher( placeholder ).matches() ) {
      throw new IllegalArgumentException( "property does not follow the pattern ID.PROP" );
    }
    return this.resolveValue( placeholder );
  }

  private String resolveValue( String placeholder ) {
    Matcher matcher = pattern.matcher( placeholder );
    matcher.find();
    String pid = matcher.group( 1 );
    String key = matcher.group( 2 );
    IConfiguration con = getConfiguration( pid );
    if ( con == null ) {
      logger.info( "Error resolving key replacement: " + placeholder );
      return null;
    }
    try {
      return con.getProperties().getProperty( key );
    } catch ( IOException e ) {
      logger.error( "Error getting properties for configuration: " + key );
      return null;
    }
  }

  @Override
  public IConfiguration getConfiguration( String configId ) {
    return configs.get( configId );
  }

  @Override
  public void registerConfiguration( IConfiguration configuration ) throws IOException {
    String configId = configuration.getId();
    if ( configId == null ) {
      throw new IllegalStateException( "Config id is null" );
    }

    try {
      configs.compute( configId, ( key, existingConfig ) -> {
        if ( existingConfig == null ) {
          // Creating the entry.
          return configuration;
        }

        try {
          // Private class, so that it is safe to modify its contents.
          if ( existingConfig instanceof CompositeConfiguration ) {
            ( (CompositeConfiguration) existingConfig ).addConfiguration( configuration );
            return existingConfig;
          }

          CompositeConfiguration composite = new CompositeConfiguration( key );
          composite.addConfiguration( existingConfig );
          composite.addConfiguration( configuration );
          return composite;
        } catch ( IOException e ) {
          // IOException is a checked exception, but the BiFunction of compute does not
          // admit any checked exceptions.
          // Wrap the IOException in an unchecked exception we control and
          // unwrap it in the outer catch.
          // Calling `addConfiguration` inside the compute function is important
          // to avoid racing conditions.
          throw new WrappedIOException( e );
        }
      } );
    } catch ( WrappedIOException wrapped ) {
      // Unwrap the original IOException and rethrow.
      throw wrapped.getCause();
    }
  }

  @Override
  public IConfiguration[] listConfigurations() {
    Collection<IConfiguration> entries = configs.values();
    return entries.toArray( new IConfiguration[ entries.size() ] );
  }

  // region Helper classes
  private static class WrappedIOException extends RuntimeException {
    public WrappedIOException( IOException cause ) {
      super( cause );
    }

    @Override
    public synchronized IOException getCause() {
      return (IOException) super.getCause();
    }
  }

  private static class CompositeConfiguration implements IConfiguration {
    private final String id;
    private final Properties properties;

    public CompositeConfiguration( String id ) {

      Objects.requireNonNull( id );

      this.id = id;
      this.properties = new Properties();
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public Properties getProperties() {
      return properties;
    }

    public void addConfiguration( IConfiguration configuration ) throws IOException {

      Objects.requireNonNull( configuration );

      this.properties.putAll( configuration.getProperties() );
    }

    @Override
    public void update( Properties addProperties ) throws IOException {
      throw new UnsupportedOperationException( "CompositeConfiguration does not support write-back" );
    }
  }
  // endregion
}
