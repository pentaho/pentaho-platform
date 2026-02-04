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
 * This class subclasses IPentahoObjectFactory and adds methods that enables the implementation to allow objects to be
 * defined through public classes. The main use case for this is a system where the objects created by the factory are
 * defined in code via an API. Examples are when the Pentaho system is fully embedded into an application, and unit
 * tests.
 *
 * @author jamesdixon
 * @deprecated Use the new {@link IPentahoRegistrableObjectFactory} facilities available in PentahoSystem
 */
public interface IPentahoDefinableObjectFactory extends IPentahoObjectFactory {

  /**
   * The different object scopes that are supported
   *
   * @author jamesdixon
   */
  public static enum Scope {
    GLOBAL, SESSION, REQUEST, THREAD, LOCAL
  }

  /**
   * Defines a new object.
   *
   * @param key       - typically the interface name
   * @param className - the name of the class to instatiate
   * @param scope     - the scope of the object
   */
  public void defineObject( String key, String className, Scope scope );

  /**
   * Defines a new object that must be loaded with a specific classloader
   *
   * @param key       - typically the interface name
   * @param className - the name of the class to instatiate
   * @param scope     - the scope of the object
   * @param loader    - the loader to be used to create the class
   */
  public void defineObject( String key, String className, Scope scope, ClassLoader loader );

  /**
   * Defines a new object instance.
   *
   * @param key      - typically the interface name
   * @param instance - the object instance to return when asked for by key
   */
  public void defineInstance( String key, Object instance );
}
