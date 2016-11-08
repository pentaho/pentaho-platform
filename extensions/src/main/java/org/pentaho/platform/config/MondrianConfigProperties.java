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

import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MondrianConfigProperties implements IMondrianConfig {
  Properties properties;

  public MondrianConfigProperties( File propertiesFile ) throws IOException {
    Properties props = new Properties();
    InputStream in = new FileInputStream( propertiesFile );
    props.load( in );
    in.close();
    properties = props;
  }

  public MondrianConfigProperties( Properties properties ) throws DocumentException {
    this.properties = properties;
  }

  public MondrianConfigProperties() {
    properties = new Properties();
  }

  public Properties getProperties() {
    return properties;
  }

  private String getProperty( String name ) {
    return properties.getProperty( name );
  }

  private void setProperty( String name, String value ) {
    if ( value == null ) {
      properties.remove( name );
    } else {
      properties.setProperty( name, value );
    }
  }

  public Integer getResultLimit() {

    Integer resultLimit = null;
    try {
      resultLimit = Integer.parseInt( getProperty( "mondrian.result.limit" ) );
    } catch ( Exception e ) {
      //ignore
    }
    return resultLimit;
  }

  public void setResultLimit( Integer limit ) {
    setProperty( "mondrian.result.limit", limit != null ? limit.toString() : null ); //$NON-NLS-1$
  }

  public Integer getTraceLevel() {
    Integer traceLevel = null;
    try {
      traceLevel = Integer.parseInt( getProperty( "mondrian.trace.level" ) );
    } catch ( Exception e ) {
      //ignore
    }
    return traceLevel;
  }

  public void setTraceLevel( Integer level ) {
    setProperty( "mondrian.trace.level", level != null ? level.toString() : null ); //$NON-NLS-1$
  }

  public String getLogFileLocation() {
    return getProperty( "mondrian.debug.out.file" ); //$NON-NLS-1$
  }

  public void setLogFileLocation( String location ) {
    setProperty( "mondrian.debug.out.file", location ); //$NON-NLS-1$
  }

  public Integer getQueryLimit() {
    Integer queryLimit = null;
    try {
      queryLimit = Integer.parseInt( getProperty( "mondrian.query.limit" ) );
    } catch ( Exception e ) {
      //ignore
    }
    return queryLimit;
  }

  public void setQueryLimit( Integer limit ) {
    setProperty( "mondrian.query.limit", limit != null ? limit.toString() : null ); //$NON-NLS-1$
  }

  public Integer getQueryTimeout() {
    Integer queryTimeout = null;
    try {
      queryTimeout = Integer.parseInt( getProperty( "mondrian.rolap.queryTimeout" ) );
    } catch ( Exception e ) {
      //ignore
    }
    return queryTimeout;
  }

  public void setQueryTimeout( Integer timeout ) {
    setProperty( "mondrian.rolap.queryTimeout", timeout != null ? timeout.toString() : null ); //$NON-NLS-1$
  }

  public boolean getIgnoreInvalidMembers() {
    return Boolean.parseBoolean( getProperty( "mondrian.rolap.ignoreInvalidMembers" ) ) && //$NON-NLS-1$
        Boolean.parseBoolean( getProperty( "mondrian.rolap.ignoreInvalidMembersDuringQuery" ) ); //$NON-NLS-1$
  }

  public void setIgnoreInvalidMembers( boolean ignore ) {
    setProperty( "mondrian.rolap.ignoreInvalidMembers", Boolean.toString( ignore ) ); //$NON-NLS-1$
    setProperty( "mondrian.rolap.ignoreInvalidMembersDuringQuery", Boolean.toString( ignore ) ); //$NON-NLS-1$
  }

  public boolean getCacheHitCounters() {
    return Boolean.parseBoolean( getProperty( "mondrian.rolap.agg.enableCacheHitCounters" ) ); //$NON-NLS-1$
  }

  public void setCacheHitCounters( boolean enabled ) {
    setProperty( "mondrian.rolap.agg.enableCacheHitCounters", Boolean.toString( enabled ) ); //$NON-NLS-1$
  }

}
