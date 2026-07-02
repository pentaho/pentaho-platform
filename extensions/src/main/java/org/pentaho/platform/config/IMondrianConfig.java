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

public interface IMondrianConfig {
  public Integer getResultLimit();

  public void setResultLimit( Integer limit );

  public Integer getTraceLevel();

  public void setTraceLevel( Integer level );

  public String getLogFileLocation();

  public void setLogFileLocation( String location );

  public Integer getQueryLimit();

  public void setQueryLimit( Integer limit );

  public Integer getQueryTimeout();

  public void setQueryTimeout( Integer timeout );

  public boolean getIgnoreInvalidMembers();

  public void setIgnoreInvalidMembers( boolean ignore );

  public boolean getCacheHitCounters();
}
