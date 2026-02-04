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
 * A handle for a registered IPentahoObjectReference allowing the removal from the registered
 * IPentahoRegistrableObjectFactory
 * <p/>
 * Created by nbaker on 4/18/14.
 */
public interface IPentahoObjectRegistration {
  void remove();
}
