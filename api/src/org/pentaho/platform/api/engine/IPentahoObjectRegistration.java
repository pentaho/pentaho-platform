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
