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
    try ( InputStream in = new FileInputStream( propertiesFile ) ) {
      props.load( in );
    }
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
