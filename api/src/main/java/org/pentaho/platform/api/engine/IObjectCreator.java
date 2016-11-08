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
