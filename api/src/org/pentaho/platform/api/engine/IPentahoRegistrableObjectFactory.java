package org.pentaho.platform.api.engine;

/**
 * Supports the registration of new implementations with the IPentahoObjectFactory at runtime.
 *
 * Created by nbaker on 4/15/14.
 */
public interface IPentahoRegistrableObjectFactory extends IPentahoObjectFactory {
  /**
   * The types that the Object instance or IPentahoObjectReference can be registered under. This controls how the
   * reference will be found later in the system.
   *
   * <p>
   *   <dl>
   *     <dt>CLASSES</dt>
   *     <dd>All superclasses and the class itself</dd>
   *     <dt>INTERFACES</dt>
   *     <dd>All implemented interfaces</dd>
   *     <dt>ALL</dt>
   *     <dd>A combination of the other options, all classes and interfaces.</dd>
   * </p>
   */
  static enum Types {
    INTERFACES, CLASSES, ALL
  }

  /**
   * Register an Object instance with the ObjectFactory. It will be registered implicitly under Types.ALL
   *
   * @param obj
   * @return a IPentahoObjectRegistration handle allowing de-registration later
   */
  <T> IPentahoObjectRegistration registerObject( T obj );

  /**
   * Register an Object instance with the ObjectFactory for the given types
   *
   * @param obj
   * @param types
   * @return a IPentahoObjectRegistration handle allowing de-registration later
   */
  <T> IPentahoObjectRegistration registerObject( T obj, Types types );

  /**
   * Register an Object instance with the ObjectFactory for the given classes
   *
   * @param obj
   * @param classes
   * @return a IPentahoObjectRegistration handle allowing de-registration later
   */
  <T> IPentahoObjectRegistration registerObject( T obj, Class<?> ... classes );

  /**
   * Register an IPentahoObjectReference with the factory under Types.ALL
   *
   * @param reference IPentahoObjectReference to be registered
   * @param <T> Type of the object described by this reference.
   * @return a IPentahoObjectRegistration handle allowing de-registration later
   */
  <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference );

  /**
   * Register an IPentahoObjectReference with the factory for the given Types.
   *
   * @param reference IPentahoObjectReference to be registered
   * @param types Types to be registered under.
   * @param <T> Type of the object described by this reference.
   */
  <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference, Types types );

  /**
   * Register an IPentahoObjectReference with the factory for the given Types.
   *
   * @param reference IPentahoObjectReference to be registered
   * @param classes Classes to be registered under.
   * @param <T> Type of the object described by this reference.
   * @return a IPentahoObjectRegistration handle allowing de-registration later
   */
  <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference, Class<?>... classes );
}
