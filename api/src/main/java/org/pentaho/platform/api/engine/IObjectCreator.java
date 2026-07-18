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

/**
 * A simple creator interface. Used by several implementations of {@link org.pentaho.platform.api.engine
 * .IPentahoObjectReference}
 * to create an instance of class T
 * <p/>
 * Created by nbaker on 4/15/14.
 */
public interface IObjectCreator<T> {
  /**
   * Return an implementation for the Class T.
   *
   * @param session
   * @return T
   */
  T create( IPentahoSession session );
}
