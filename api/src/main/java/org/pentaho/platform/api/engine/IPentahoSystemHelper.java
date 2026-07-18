/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.engine;

public interface IPentahoSystemHelper {

  public IContentOutputHandler getOutputDestinationFromContentRef( final String contentTag,
      final IPentahoSession session );

  public String getSystemName();

  // use get(...) to retrieve pentaho system objects
  @Deprecated
  public Object createObject( final String className, final ILogger logger );

  // use get(...) to retrieve pentaho system objects
  @Deprecated
  public Object createObject( final String className );

  public void registerHostnameVerifier();

}
