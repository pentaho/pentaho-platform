package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * User: nbaker
 * Date: 1/17/13
 */
public class SpringScopeSessionHolder {
  public static final ThreadLocal<IPentahoSession> SESSION = new ThreadLocal<IPentahoSession>();
}
