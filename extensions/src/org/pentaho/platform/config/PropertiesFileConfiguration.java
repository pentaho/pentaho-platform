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

  private void loadProperties() throws IOException {
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
}
