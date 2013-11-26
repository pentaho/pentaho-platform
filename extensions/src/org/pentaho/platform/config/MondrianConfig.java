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

import java.io.Serializable;

public class MondrianConfig implements IMondrianConfig, Serializable {

  boolean cacheHitCounters;
  boolean ignoreInvalidMembers;
  String logFileLocation;
  Integer queryLimit;
  Integer queryTimeout;
  Integer resultLimit;
  Integer traceLevel;

  public MondrianConfig() {

  }

  public MondrianConfig( IMondrianConfig mondrianConfig ) {
    setCacheHitCounters( mondrianConfig.getCacheHitCounters() );
    setIgnoreInvalidMembers( mondrianConfig.getIgnoreInvalidMembers() );
    setLogFileLocation( mondrianConfig.getLogFileLocation() );
    setQueryLimit( mondrianConfig.getQueryLimit() );
    setQueryTimeout( mondrianConfig.getQueryTimeout() );
    setResultLimit( mondrianConfig.getResultLimit() );
    setTraceLevel( mondrianConfig.getTraceLevel() );
  }

  public boolean getCacheHitCounters() {
    return cacheHitCounters;
  }

  public void setCacheHitCounters( boolean cacheHitCounters ) {
    this.cacheHitCounters = cacheHitCounters;
  }

  public boolean getIgnoreInvalidMembers() {
    return ignoreInvalidMembers;
  }

  public void setIgnoreInvalidMembers( boolean ignoreInvalidMembers ) {
    this.ignoreInvalidMembers = ignoreInvalidMembers;
  }

  public String getLogFileLocation() {
    return logFileLocation;
  }

  public void setLogFileLocation( String logFileLocation ) {
    this.logFileLocation = logFileLocation;
  }

  public Integer getQueryLimit() {
    return queryLimit;
  }

  public void setQueryLimit( Integer queryLimit ) {
    this.queryLimit = queryLimit;
  }

  public Integer getQueryTimeout() {
    return queryTimeout;
  }

  public void setQueryTimeout( Integer queryTimeout ) {
    this.queryTimeout = queryTimeout;
  }

  public Integer getResultLimit() {
    return resultLimit;
  }

  public void setResultLimit( Integer resultLimit ) {
    this.resultLimit = resultLimit;
  }

  public Integer getTraceLevel() {
    return traceLevel;
  }

  public void setTraceLevel( Integer traceLevel ) {
    this.traceLevel = traceLevel;
  }

}
