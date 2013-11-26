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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util.beans;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.messages.Messages;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Utility methods for processing Java Beans in a consistent manner across all Pentaho projects. This is not an
 * attempt to duplicate the behavior of commons-beanutils, rather, a central spot for common operations on beans so
 * we can ensure that same bean property binding functionality and logic anytime we need to work with Java Beans.
 * <p>
 * This utility is especially important in dealing with Pentaho Action beans {@link IAction}s. See
 * {@link ActionHarness} for an IAction-specific flavor of this utility.
 * 
 * @author aphillips
 * 
 * @see ActionHarness
 */
public class BeanUtil {

  private static final Log logger = LogFactory.getLog( BeanUtil.class );

  private PropertyUtilsBean propUtil = new PropertyUtilsBean();

  private BeanUtilsBean typeConvertingBeanUtil;

  protected Object bean;

  protected ValueSetErrorCallback defaultCallback;

  public void setDefaultCallback( ValueSetErrorCallback defaultCallback ) {
    this.defaultCallback = defaultCallback;
  }

  /**
   * Setup a new bean util for operating on the given bean
   * 
   * @param targetBean
   *          the bean on which to operate
   */
  public BeanUtil( final Object targetBean ) {
    this.bean = targetBean;
    //
    // Configure a bean util that throws exceptions during type conversion
    //
    ConvertUtilsBean convertUtil = new ConvertUtilsBean();
    convertUtil.register( true, true, 0 );

    typeConvertingBeanUtil = new BeanUtilsBean( convertUtil );
    setDefaultCallback( new EagerFailingCallback() );
  }

  public boolean isReadable( String propertyName ) {
    return propUtil.isReadable( bean, propertyName );
  }

  public Object getValue( String propertyName ) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
    if ( logger.isTraceEnabled() ) {
      logger.trace( MessageFormat.format( "getting property \"{0}\" from bean \"{1}\"", propertyName, bean ) ); //$NON-NLS-1$
    }
    return propUtil.getSimpleProperty( bean, propertyName );
  }

  public Class<?> getPropertyType( String propertyName ) throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
    PropertyDescriptor desc = propUtil.getPropertyDescriptor( bean, propertyName );
    return desc.getPropertyType();
  }

  /**
   * Returns <code>true</code> if a bean property can be written to (i.e. there is an accessible and appropriately
   * typed setter method on the bean). Note: this method will check for both scalar (normal) and indexed properties
   * before returning <code>false</code>.
   * 
   * @param propertyName
   *          the name of the bean property to check for write-ability
   * @return <code>true</code> if the bean property can be written to
   */
  public boolean isWriteable( String propertyName ) {
    return propUtil.isWriteable( bean, propertyName )
        || ( propUtil.getResolver().isIndexed( propertyName ) && propUtil.isReadable( bean, propertyName ) );
  }

  /**
   * Set a bean property with a given value.
   * 
   * @param propertyName
   *          the bean property to set
   * @param value
   *          the value to set on the bean. If value is an instance of {@link ValueGenerator}, then the value will
   *          be derived by calling {@link ValueGenerator#getValue(String)}
   * 
   * @throws Exception
   *           if there was a problem setting the value on the bean
   * @see ValueGenerator
   */
  public void setValue( String propertyName, Object value ) throws Exception {
    setValue( propertyName, value, defaultCallback );
  }

  /**
   * Set a bean property with a given value, allowing the caller to respond to various error states that may be
   * encountered during the attempt to set the value on the bean.
   * 
   * @param propertyName
   *          the bean property to set
   * @param value
   *          the value to set on the bean. If value is an instance of {@link ValueGenerator}, then the value will
   *          be derived by calling {@link ValueGenerator#getValue(String)}
   * @param callback
   *          a structure that alerts the caller of any error states and enables the caller to fail, log, proceed,
   *          etc
   * 
   * @throws Exception
   *           if there was a problem setting the value on the bean
   * @see ValueGenerator
   * @see ValueSetErrorCallback
   */
  public void setValue( final String propertyName, Object value, ValueSetErrorCallback callback ) throws Exception {
    setValue( propertyName, value, callback, new PropertyNameFormatter[0] );
  }

  /**
   * Set a bean property with a given value, allowing the caller to respond to various error states that may be
   * encountered during the attempt to set the value on the bean. This method also allows the caller to specify
   * formatters that will modify the property name to match what the bean expects as the true name of the property.
   * This can be helpful when you are trying to map parameters from a source that follows a convention that is not
   * Java Bean spec compliant.
   * 
   * @param propertyName
   *          the bean property to set
   * @param value
   *          the value to set on the bean. If value is an instance of {@link ValueGenerator}, then the value will
   *          be derived by calling {@link ValueGenerator#getValue(String)}. Note: if value is <code>null</code>,
   *          we consciously bypass the set operation altogether since it leads to indeterminate behavior, i.e. it
   *          may fail or succeed.
   * @param callback
   *          a structure that alerts the caller of any error states and enables the caller to fail, log, proceed,
   *          etc
   * @param formatters
   *          a list of objects that can be used to modify the given property name prior to performing any
   *          operations on the bean itself. This new formatted property name will be used to identify the bean
   *          property. bean lookup and value setting
   * 
   * @throws Exception
   *           when something goes wrong (controlled by the callback object)
   * @see ValueGenerator
   * @see ValueSetErrorCallback
   * @see PropertyNameFormatter
   */
  public void setValue( String propertyName, Object value, PropertyNameFormatter... formatters ) throws Exception {
    setValue( propertyName, value, defaultCallback, formatters );
  }

  /**
   * Set a bean property with a given value, allowing the caller to respond to various error states that may be
   * encountered during the attempt to set the value on the bean. This method also allows the caller to specify
   * formatters that will modify the property name to match what the bean expects as the true name of the property.
   * This can be helpful when you are trying to map parameters from a source that follows a convention that is not
   * Java Bean spec compliant.
   * 
   * @param propertyName
   *          the bean property to set
   * @param value
   *          the value to set on the bean. If value is an instance of {@link ValueGenerator}, then the value will
   *          be derived by calling {@link ValueGenerator#getValue(String)}. Note: if value is <code>null</code>,
   *          we consciously bypass the set operation altogether since it leads to indeterminate behavior, i.e. it
   *          may fail or succeed.
   * @param callback
   *          a structure that alerts the caller of any error states and enables the caller to fail, log, proceed,
   *          etc
   * @param formatters
   *          a list of objects that can be used to modify the given property name prior to performing any
   *          operations on the bean itself. This new formatted property name will be used to identify the bean
   *          property. bean lookup and value setting
   * 
   * @throws Exception
   *           when something goes wrong (controlled by the callback object)
   * @see ValueGenerator
   * @see ValueSetErrorCallback
   * @see PropertyNameFormatter
   */
  public void setValue( String propertyName, Object value, ValueSetErrorCallback callback,
      PropertyNameFormatter... formatters ) throws Exception {
    if ( logger.isTraceEnabled() ) {
      logger.trace( MessageFormat.format( "setting property \"{0}\" on bean \"{1}\"", propertyName, bean ) ); //$NON-NLS-1$
    }

    if ( value == null ) {
      // we are ignoring (not setting) null values because we could wind up with a class cast / converter
      // exception downstream which would imply an error condition, when an error is typically not what
      // we want here. If we did let this null continue on, we would have indeterminate behavior for nulls,
      // i.e. sometimes they would work, sometimes they would trigger an error
      logger.info( MessageFormat.format(
          "value to set is null, skipping setting of \"{0}\" property on bean \"{1}\"", propertyName, bean ) ); //$NON-NLS-1$
      return;
    }

    String origPropertyName = propertyName;
    for ( PropertyNameFormatter formatter : formatters ) {
      propertyName = formatter.format( propertyName );
    }

    // here we check if we can set the input value on the bean. There are three ways that bean utils will go about
    // this
    // 1. use a simple property setter method
    // .. in the case of an indexed property there are two methods:
    // 2. if there is an indexed setter method bean utils will that (note: a simple getter is required as well
    // though it
    // will not be invoked)
    // 3. if there is an array-based getter like List<String> getNames(), bean utils will insert the new value into
    // the
    // array reference
    // it gets from the array getter.
    if ( isWriteable( propertyName ) ) {

      // we get the value at the latest point possible
      Object val = value;
      if ( value instanceof ValueGenerator ) {
        val = ( (ValueGenerator) value ).getValue( propertyName );
      }
      try {
        // trying our best to set the input value to the type specified by the action bean
        typeConvertingBeanUtil.copyProperty( bean, propertyName, val );
      } catch ( Exception e ) {
        String propertyType = ""; //$NON-NLS-1$
        try {
          propertyType = getPropertyType( propertyName ).getName();
        } catch ( Throwable t ) {
          // we are in a nested catch, we should never let an exception escape here
        }
        callback.failedToSetValue( bean, propertyName, val, propertyType, e );
      }
    } else {
      callback.propertyNotWritable( bean, origPropertyName );
    }
  }

  /**
   * Sets a number of bean properties based on given property-value map, where the key of the map is the bean
   * property and the value is the value to which to set that property.
   * 
   * @param propValueMap
   *          a map whose keys are property names and whose values are to be set on the associated property of the
   *          bean
   * @throws Exception
   *           if there was a problem setting the value on the bean
   */
  public void setValues( Map<String, Object> propValueMap ) throws Exception {
    for ( Map.Entry<String, Object> entry : propValueMap.entrySet() ) {
      setValue( entry.getKey(), entry.getValue() );
    }
  }

  public void setValues( Map<String, Object> propValueMap, ValueSetErrorCallback callback ) throws Exception {
    for ( Map.Entry<String, Object> entry : propValueMap.entrySet() ) {
      setValue( entry.getKey(), entry.getValue(), callback );
    }
  }

  public void setValues( Map<String, Object> propValueMap, PropertyNameFormatter... formatters ) throws Exception {
    for ( Map.Entry<String, Object> entry : propValueMap.entrySet() ) {
      setValue( entry.getKey(), entry.getValue(), formatters );
    }
  }

  public static class FeedbackValueGenerator {
    private Object value;

    public FeedbackValueGenerator( Object value ) {
      this.value = value;
    }

    public Object getValueToSet( String name ) throws Exception {
      return value;
    }
  };

  public static class EagerFailingCallback implements ValueSetErrorCallback {

    public void failedToSetValue( Object bean, String propertyName, Object value, String beanPropertyType,
        Throwable cause ) throws Exception {
      String valueType = ( value != null ) ? value.getClass().getName() : "[ClassNameNotAvailable]"; //$NON-NLS-1$
      String beanType = ( bean != null ) ? bean.getClass().getName() : "[ClassNameNotAvailable]"; //$NON-NLS-1$
      throw new InvocationTargetException( cause, Messages.getInstance().getErrorString(
          "BeanUtil.ERROR_0001_FAILED_TO_SET_PROPERTY", beanType, //$NON-NLS-1$
          propertyName, beanPropertyType, valueType ) );
    }

    public void propertyNotWritable( Object bean, String propertyName ) throws Exception {
      String beanType = ( bean != null ) ? bean.getClass().getName() : "[ClassNameNotAvailable]"; //$NON-NLS-1$
      throw new IllegalAccessException( Messages.getInstance().getErrorString(
          "BeanUtil.ERROR_0002_NO_METHOD_FOR_PROPERTY", beanType, propertyName ) ); //$NON-NLS-1$
    }
  }

}
