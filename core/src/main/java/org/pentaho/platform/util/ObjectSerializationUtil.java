/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A specialized utility class that converts a complex {@link Object} into a {@link Map} that can be safely
 * serialized. Many serialization mechanisms do not handle complex objects well and even throw exceptions. This
 * utility can be used to "normalize" any Object, turning into a {@link Map} of easily serializable values, where
 * they map keys correspond to the Object's declared fields and methods, and the value is either a String, primitive
 * or another "normalized" {@link Map}.<br><br>
 * <p>
 * This utility class also provides convenience methods for reading object "properties", where given a "propertyName",
 * an attempt is made to read that value directly via reflection, or call one of the most likely corresponding getter
 * methods, or simply use the property name as a key, if the object being read is a {@link Map}.<br><br>
 * <p>
 * Example: Let's assume we have the following classes:<br>
 * <pre>
 *  public class Company {
 *    private String name;
 *    private Map<String, Employee> employees = new HashMap<String, Employee>();
 *    public Company( final String name ) {
 *      this.name = name;
 *    }
 *    public void addEmployee( final String firstName, final String lastName, final boolean isManager ) {
 *      employees.put( firstName, new Employee ( firstName, lastName, isManager ) );
 *    }
 *    public String getName() {
 *      return this.name;
 *    }
 *  }
 *
 *  public class Employee {
 *    private boolean imAManager;
 *    private String firstName;
 *    private String lastName;
 *
 *    public Employee( final String firstName, final String lastName, final imAManager ) {
 *      this.firstName = firstName;
 *      this.lastName = lastName;
 *      this.imAManager = imAManager;
 *    }
 *
 *    public boolean isManager() {
 *      return this.imAManager;
 *    }
 *  }
 * </pre>
 * And let's assume that we instantiate the {@code Company} object as follows:
 * <pre>
 *   final Company company = new Company( "Pentaho" );
 *   company.addEmployee( "John", "Smith", false );
 *   company.addEmployee( "Jane", "Doe", true );
 * </pre>
 * When we normalize this company object:
 * <pre>
 *    final Map normalizedCompany = ObjectSerializationUtil.normalize( company );
 * </pre>
 * We will get a {@link Map} that looks as follows:
 * <pre>
 *    "name" -> "Pentaho"
 *    "employees" ->
 *        "john" ->
 *            "imAManager" -> false
 *            "isManager" -> false
 *            "firstName" -> "John"
 *            "lastName" -> "Smith"
 * </pre>
 * <p>
 * The {@link #getPropertyValue(Object, String)} method can be used on the normalized object to fetch values from
 * witin the object. Examples:
 * <li>- {@code ObjectSerializationUtil.getPropertyValue( normalizedCompany, "name" } -> "Pentaho"</li>
 * <li>- {@code ObjectSerializationUtil.getPropertyValue( normalizedCompany, "getName" } -> "Pentaho"</li>
 * <li>- {@code ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.john" } -> Employee "John
 * Smith"</li>
 * <li>- {@code ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.john.manager" } -> false</li>
 * <li>- {@code ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.john.imAManager" } -> false</li>
 * <li>- {@code ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.john.isManager" } -> false</li>
 */
public class ObjectSerializationUtil {

  private static final Log logger = LogFactory.getLog( ObjectSerializationUtil.class );

  /**
   * The default value for the max depth of traversal when normalizing the object - prevents unexpected stack overflows.
   */
  private static int DEFAULT_MAX_DEPTH = 3;

  /**
   * Converts an {@link Object} into an easily serializable form. See javadoc for details. Calls the
   * {@link #normalize(Object, int)} with {@code maxDepth} set to {@link #DEFAULT_MAX_DEPTH}.
   *
   * @param object the {@link Object} being normalized
   * @return an {@link Object} that has been normalized for serialization.
   */
  public static Object normalize( final Object object ) {
    return normalize( object, DEFAULT_MAX_DEPTH );
  }

  /**
   * Converts an {@link Object} into an easily serializable form. See javadoc for details.
   *
   * @param object   the {@link Object} being normalized
   * @param maxDepth the max depth of traversal when normalizing the object - prevents unexpected stack overflows
   * @return an {@link Object} that has been normalized for serialization.
   */
  public static Object normalize( final Object object, final int maxDepth ) {
    return normalize( object, maxDepth, new HashSet<Object>(), 0 );
  }

  /**
   * Converts an {@link Object} into an easily serializable form. See javadoc for details.
   *
   * @param object                  the {@link Object} being normalized
   * @param touchedObjectReferences a {@link Set} that keeps track of "touched" objects, to avoid running into circular
   *                                references, which woudl in turn cause a stack overflow
   * @param level                   the current level of depth relative to the top level object being traversed - a
   *                                check inside this method to ensure that we don't go deeper than the max number of
   *                                levels prevents unexpected stack overflow
   * @return an {@link Object} that has been normalized for serialization.
   */
  private static Object normalize( final Object object, final int maxDepth, final Set<Object> touchedObjectReferences,
                                   final int level ) {

    if ( object == null || level > maxDepth ) {
      return null;
    }
    final Map<String, Object> normalizedMap = new HashMap<String, Object>();

    if ( object instanceof String || ClassUtils.isPrimitiveOrWrapper( object.getClass() ) ) {
      // if the object is a String or a primitive, return is as is
      return object;
    } else if ( object instanceof List ) {
      // List support does not go beyond turning it into a String
      return object.toString();
    } else if ( object instanceof Map ) {
      // if the object is already a map, convert all keys and values to string
      final Iterator<Map.Entry> iter = ( (Map) object ).entrySet().iterator();
      while ( iter.hasNext() ) {
        final Map.Entry entry = iter.next();
        final Object key = entry.getKey();
        final Object value = entry.getValue();
        normalizedMap.put( key == null ? null : key.toString(), normalize( value, maxDepth, touchedObjectReferences,
          level + 1 ) );
      }
    } else {
      // we have an object, get it's declared fields, and turn into a map
      for ( final Field field : object.getClass().getDeclaredFields() ) {
        field.setAccessible( true );
        // we're only interested in fields that aren't some sort of selt-reference
        if ( field.getName().equals( "this" ) || field.getName().startsWith( "this$" ) ) {
          continue;
        }
        try {
          Object value = field.get( object );
          // keep track of objects we have already "touched" to avoid circular dependencies
          if ( touchedObjectReferences.contains( value ) ) {
            continue;
          }
          if ( value != null && !ClassUtils.isPrimitiveOrWrapper( value.getClass() ) ) {
            touchedObjectReferences.add( value );
          }

          // check is value is null or same as the original workItemDetails to prevent looping forever
          if ( value != null && value != object ) {
            normalizedMap.put( field.getName(), normalize( value, maxDepth, touchedObjectReferences, level + 1 ) );
          }
        } catch ( final IllegalAccessException iae ) {
          // the field it inaccessible, nothing we can do
        }
      }
      // we have an object, get it's declared methods, and turn into a map
      for ( final Method method : object.getClass().getDeclaredMethods() ) {
        method.setAccessible( true );
        // we are only interested in methods with no arguments that return non-void
        if ( method.getParameterTypes().length > 0 || method.getReturnType().equals( Void.TYPE ) ) {
          continue;
        }
        try {
          Object value = method.invoke( object );
          // keep track of non-primitive objects we have already "touched" to avoid circular dependencies
          if ( touchedObjectReferences.contains( value ) ) {
            continue;
          }
          if ( value != null && !ClassUtils.isPrimitiveOrWrapper( value.getClass() ) ) {
            touchedObjectReferences.add( value );
          }

          // check is value is null or same as the original workItemDetails to prevent looping forever
          if ( value != null && value != object ) {
            normalizedMap.put( method.getName(), normalize( value, maxDepth, touchedObjectReferences, level + 1 ) );
          }
        } catch ( final Exception iae ) {
          // there may have been a problem invoking the method, nothing we can do
        }
      }
    }
    return normalizedMap.isEmpty() ? null : normalizedMap;
  }

  /**
   * Given a {@code propertyName}, which might be a specific field name, method name, map key, or a chained list of
   * them, returns the for responding value. See class javadoc for details examples of usage.
   *
   * @param object       the {@link Object} whose property is being looked up
   * @param propertyName a {@link String} containing the field,  method name or map key
   * @return the {@link Object} value corresponding to a field, method or map key with the name {@code propertyName} or
   * null, if no such field, method or key exists in the {@code object}s class.
   */
  public static Object getPropertyValue( final Object object, final String propertyName ) {
    if ( StringUtil.isEmpty( propertyName ) || object == null ) {
      return null;
    }

    Object content = null;
    try {
      // is there a period in the propertyName? If not, we have a single property name
      final int firstDotIndex = propertyName.indexOf( '.' );
      if ( firstDotIndex == -1 ) {
        final List<String> methodNames = getGetterMethodNames( propertyName );
        for ( final String methodName : methodNames ) {
          if ( object instanceof Map ) {
            content = ( (Map) object ).get( methodName );
          } else {
            content = getPropertyValueByReflection( object, methodName );
          }
          // we found it!
          if ( content != null ) {
            break;
          }
        }
      } else {
        // is there a period in the propertyName? If so, we have multiple property names, we want to get the root object
        // and the remaining properties
        final String rootFieldName = propertyName.substring( 0, firstDotIndex );
        final Object rootObject = getPropertyValue( object, rootFieldName );
        final String remainingFieldNames = propertyName.substring( firstDotIndex + 1 );
        content = getPropertyValue( rootObject, remainingFieldNames );
      }
    } catch ( final Exception e ) {
      logger.error( e.getLocalizedMessage() );
    }
    return content;
  }

  /**
   * Given a {@code propertyName}, returns all potential getter method. For instance, given {@code propertyName}
   * {@code manager}, this method will return a {@link List} containing the following values:
   * <li>- manager</li>
   * <li>- getManager</li>
   * <li>- isManager</li>
   *
   * @param propertyName a {@link String} contaiing the name of the property for which the getter method names are being
   *                     returned
   * @return a {@link List} of potential getter method names that might correspond to the provided {@code propertyName}
   */
  protected static List<String> getGetterMethodNames( final String propertyName ) {

    // build a list of potential method names, using standard getter prefixes
    final List<String> methodNames = new ArrayList<String>();
    methodNames.add( propertyName );
    methodNames.add( "get" + StringUtils.capitalize( propertyName ) );
    methodNames.add( "is" + StringUtils.capitalize( propertyName ) );

    return methodNames;
  }

  /**
   * Given a {@code propertyName} checks the {@code object}s declared fields and methods, and attemps to return the
   * corresponding value.
   *
   * @param object       the {@link Object} whose property is being looked up
   * @param propertyName a {@link String} containing the field or method name
   * @return returns the {@link Object} value corresponding to a field or method with the name {@code propertyName} or
   * null, if no such field or method exists in the {@code object}s class.
   */
  public static Object getPropertyValueByReflection( final Object object, final String propertyName ) {
    if ( StringUtil.isEmpty( propertyName ) || object == null ) {
      return null;
    }

    // first try accessing they object's fields directly and check if any matches the provided propertyName
    Object propertyValue = null;
    try {
      final Field field = object.getClass().getDeclaredField( propertyName );
      field.setAccessible( true );
      propertyValue = field.get( object );
      // no exceptions means that the propertyName corresponds to a valid field, we can return the value
      return propertyValue;
    } catch ( final Exception e ) {
      // nothing to do, continue to see if the propertyName corresponds to a method
    }

    // if we're here, the propertyName did not correspond to a declared field, check if it's a method
    try {
      Method method = object.getClass().getDeclaredMethod( propertyName );
      propertyValue = method.invoke( object );
      // no exceptions means that the propertyName corresponds to a valid method, we can return the value
      return propertyValue;
    } catch ( final Exception e ) {
      // nothing to do, we may simply have the wrong method name, let's keep trying
    }
    return null;
  }
}
