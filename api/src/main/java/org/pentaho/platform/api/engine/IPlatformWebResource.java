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


package org.pentaho.platform.api.engine;

/**
 * Simple interface representing a Web Resource
 *
 * Created by nbaker on 9/8/16.
 */
public interface IPlatformWebResource {

  /**
   * Context associated with this resource.
   * @return context
   */
  String getContext();

  /**
   * Location relative to the root of the server.
   * @return location of the resource
   */
  String getLocation();
}
