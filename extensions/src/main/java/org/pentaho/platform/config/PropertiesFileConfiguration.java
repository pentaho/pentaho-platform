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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User: nbaker Date: 4/2/13
 */
public class PropertiesFileConfiguration implements IConfiguration {

  private Properties properties;
  private String id;
  private File propFile;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public PropertiesFileConfiguration( String id, Properties properties ) {
    if ( id == null ) {
      throw new IllegalArgumentException( "id cannot be null" );
    }
    if ( properties == null ) {
      throw new IllegalArgumentException( "properties cannot be null" );
    }
    this.id = id;
    this.properties = properties;

  }

  public PropertiesFileConfiguration( String id, File propFile ) {
    if ( id == null ) {
      throw new IllegalArgumentException( "id cannot be null" );
    }
    if ( propFile == null ) {
      throw new IllegalArgumentException( "properties cannot be null" );
    }
    if ( propFile.exists() == false ) {
      throw new IllegalArgumentException( "properties file does not exist" );
    }
    this.id = id;
    this.propFile = propFile;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Properties getProperties() throws IOException {
    if ( properties == null ) {
      loadProperties();
    }
    Properties p = new Properties();
    synchronized ( properties ) {
      p.putAll( properties );
    }
    return p;
  }

  protected void loadProperties() throws IOException {
    properties = new Properties();
    synchronized ( propFile ) {
      properties.load( new FileInputStream( propFile ) );
    }

  }

  /**
   * 20140121/PM - Updated to back up original file with timestamp for historical
   * rollback purposes
   *
   * @param newProperties
   * @throws IOException
   */
  @Override
  public void update( Properties newProperties ) throws IOException {

    if ( properties == null ) {
      loadProperties();
    }

    synchronized ( properties ) {
      // first back up original property file
      // use unix time for timestamp and append to filename
      long unixTime = System.currentTimeMillis() / 1000L;
      properties.store( new FileOutputStream( propFile + "." + String.valueOf( unixTime ) ), "" );

      properties.clear();
      properties.putAll( newProperties );
    }
    synchronized ( propFile ) {
      properties.store( new FileOutputStream( propFile ), "" );
    }

  }

  protected void setPropFile( File propFile ) {
    this.propFile = propFile;
  }
}
