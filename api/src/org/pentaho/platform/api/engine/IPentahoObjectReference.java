package org.pentaho.platform.api.engine;

import java.util.Map;

/**
 * User: nbaker
 * Date: 1/16/13
 *
 * IPentahoObjectReference objects represent a pointer to an available object in the IPentahoObjectFactory
 * and contains all attributes associated with that object (priority, adhoc attributes).
 *
 * Implementations of this class should be lazy where possible, deferring retrieval of the Object
 * until the getObject() method is called
 *
 */
public interface IPentahoObjectReference<T> extends Comparable{
  Map<String, Object> getAttributes();
  T getObject();
}
