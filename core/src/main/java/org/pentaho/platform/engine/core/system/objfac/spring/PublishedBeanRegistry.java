/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A Registry for published Spring beans. Also allows for looking up the names of all beans registered for a given type
 * in the given Spring bean factory
 * <p/>
 * User: nbaker Date: 3/27/13
 */
public class PublishedBeanRegistry {

  private static Map<Object, Map<Class<?>, List<String>>> classToBeanMap = Collections
      .synchronizedMap( new WeakHashMap<Object, Map<Class<?>, List<String>>>() );

  private static Map<ListableBeanFactory, Object> factoryMarkerCache = Collections
      .synchronizedMap( new WeakHashMap<ListableBeanFactory, Object>() );

  private static Map<ListableBeanFactory, List<IPentahoObjectRegistration>> handleRegistry =
      Collections.synchronizedMap( new WeakHashMap<ListableBeanFactory, List<IPentahoObjectRegistration>>() );

  /**
   * Register a bean for the given class. The factoryMarker is a UUID associated with the originating BeanFactory
   *
   * @param beanName
   * @param clazz
   * @param factoryMarker
   */
  public static void registerBean( String beanName, Class<?> clazz, Object factoryMarker ) {
    if ( beanName == null ) {
      throw new IllegalArgumentException( "Bean name cannot be null" );
    }
    if ( clazz == null ) {
      throw new IllegalArgumentException( "Class cannot be null" );
    }
    if ( factoryMarker == null ) {
      throw new IllegalArgumentException( "factoryMarker cannot be null" );
    }

    Map<Class<?>, List<String>> registryMap = classToBeanMap.get( factoryMarker );
    if ( registryMap == null ) {
      registryMap = new WeakHashMap<Class<?>, List<String>>();
      classToBeanMap.put( factoryMarker, registryMap );
    }
    List<String> beansImplementingType = registryMap.get( clazz );
    if ( beansImplementingType == null ) {
      beansImplementingType = Collections.synchronizedList( new ArrayList<String>() );
      registryMap.put( clazz, beansImplementingType );
    }
    beansImplementingType.add( beanName );
  }

  public static void registerFactory( ApplicationContext applicationContext ) {
    Object markerBean = null;
    try {
      // The marker may not be present if there are no published beans from this factory.
      markerBean = applicationContext.getBean( Const.FACTORY_MARKER );
    } catch ( NoSuchBeanDefinitionException ignored ) {
      // ignore
    }
    if ( markerBean == null ) {
      // The applicationContext has been declared to be registered, but no beans are actually published. Ignoring this
      // applicationContext
      return;
    }
    factoryMarkerCache.put( applicationContext, markerBean );

    final ConfigurableApplicationContext listableBeanFactory =
        (ConfigurableApplicationContext) applicationContext;

    List<IPentahoObjectRegistration> registrationList = new ArrayList<>();
    handleRegistry.put( listableBeanFactory, registrationList );
    Map<Class<?>, List<String>> classListMap = classToBeanMap.get( markerBean );

    for ( Map.Entry<Class<?>, List<String>> classListEntry : classListMap.entrySet() ) {
      Class<?> clazz = classListEntry.getKey();
      for ( String beanName : classListEntry.getValue() ) {
        IPentahoObjectRegistration iPentahoObjectRegistration = PentahoSystem.registerReference(
            new SpringPentahoObjectReference( listableBeanFactory, beanName, clazz, null,
                listableBeanFactory.getBeanFactory().getBeanDefinition( beanName ) ) );

        registrationList.add( iPentahoObjectRegistration );

      }
    }

    listableBeanFactory.addApplicationListener( new ApplicationListener() {
      @Override public void onApplicationEvent( ApplicationEvent applicationEvent ) {
        if ( applicationEvent instanceof ContextClosedEvent ) {
          for ( IPentahoObjectRegistration iPentahoObjectRegistration : handleRegistry.get( listableBeanFactory ) ) {
            iPentahoObjectRegistration.remove();
          }
        }
      }
    } );

  }

  /**
   * Return the list of registered BeanFactories
   *
   * @return list of factories
   */
  public static Set<ListableBeanFactory> getRegisteredFactories() {
    return factoryMarkerCache.keySet();
  }

  public static void reset() {
    factoryMarkerCache.clear();
  }
}
