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

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Spring implementation of {@link IPentahoObjectReference}
 * <p/>
 * {@inheritDoc}
 * <p/>
 * User: nbaker Date: 1/16/13
 */
public class SpringPentahoObjectReference<T> implements IPentahoObjectReference<T> {

  private final ConfigurableApplicationContext context;

  private final String name;

  private final Class<T> clazz;

  private final IPentahoSession session;

  private final SpringBeanAttributes attributes;

  private static final String PRIORITY = "priority";

  public SpringPentahoObjectReference( ConfigurableApplicationContext context, String name, Class<T> clazz,
                                       IPentahoSession session, BeanDefinition beanDef ) {
    this.context = context;
    this.name = name;
    this.clazz = clazz;
    this.session = session;
    this.attributes = new SpringBeanAttributes( beanDef, getContextOwnerPluginId( context ) );
  }

  @Override
  public Class<?> getObjectClass() {
    return clazz;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public T getObject() {

    IPentahoSession previousSession = SpringScopeSessionHolder.SESSION.get();
    IPentahoSession sessionToUse = session != null ? session : PentahoSessionHolder.getSession();
    SpringScopeSessionHolder.SESSION.set( sessionToUse );
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

      Object obj;
      try {
        obj = context.getBeanFactory().getBean( name );
      } finally {
        SpringScopeSessionHolder.SESSION.set( previousSession );
      }

      if ( obj instanceof IPentahoInitializer ) {
        ( (IPentahoInitializer) obj ).init( sessionToUse );
      }

      return (T) obj;
    } finally {
      Thread.currentThread().setContextClassLoader( originalClassLoader );
    }
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }

    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    @SuppressWarnings( "rawtypes" )
    SpringPentahoObjectReference that = (SpringPentahoObjectReference) o;

    if ( !clazz.equals( that.clazz ) ) {
      return false;
    }

    if ( !name.equals( that.name ) ) {
      return false;
    }

    if ( !attributes.equals( that.attributes ) ) {
      return false;
    }

    if ( !Objects.equals( session, that.session ) ) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + clazz.hashCode();
    result = 31 * result + ( session != null ? session.hashCode() : 0 );
    result = 31 * result + attributes.hashCode();
    return result;
  }

  @Override
  public int compareTo( IPentahoObjectReference<T> o ) {
    if ( o == null ) {
      return 1;
    }

    if ( o == this ) {
      return 0;
    }

    return Integer.compare( this.getRanking(), o.getRanking() );
  }

  @Override
  public Integer getRanking() {
    return getPriority();
  }

  private int getPriority() {
    if ( !this.getAttributes().containsKey( PRIORITY ) ) {
      // return default
      return IPentahoObjectFactory.DEFAULT_PRIORTIY;
    }

    try {
      return Integer.parseInt( this.getAttributes().get( PRIORITY ).toString() );
    } catch ( NumberFormatException e ) {
      // return default
      return IPentahoObjectFactory.DEFAULT_PRIORTIY;
    }
  }

  private static String getContextOwnerPluginId( ConfigurableApplicationContext context ) {
    try {
      return context.getBean( Const.OWNER_PLUGIN_ID_BEAN, String.class );
    } catch ( BeansException ignored ) {
      // Bean does not exist, or is not a String as expected.
      return null;
    }
  }

  /**
   * Hashmap backed by a Spring BeanDefinition
   */
  private static class SpringBeanAttributes extends HashMap<String, Object> {

    private static final long serialVersionUID = -5790844158879001752L;

    public SpringBeanAttributes( final BeanDefinition beanDef, final String ownerPluginId ) {
      for ( String s : beanDef.attributeNames() ) {
        this.put( s, beanDef.getAttribute( s ) );
      }

      this.put( "scope", StringUtils.defaultIfEmpty( beanDef.getScope(), "singleton" ) );

      if ( !StringUtil.isEmpty( ownerPluginId ) ) {
        this.put( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE, ownerPluginId );
      }
    }
  }
}
