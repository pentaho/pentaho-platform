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


package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * User: nbaker Date: 1/17/13
 */
public class SpringScopeSessionHolder {
  public static final ThreadLocal<IPentahoSession> SESSION = new ThreadLocal<IPentahoSession>();
}
