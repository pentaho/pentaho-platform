/*!
 *
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
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.spring.AutowiredInjectResolver;
import org.glassfish.jersey.server.spring.SpringComponentProvider;
import org.jvnet.hk2.spring.bridge.api.SpringBridge;
import org.jvnet.hk2.spring.bridge.api.SpringIntoHK2Bridge;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.Supplier;

@Priority(value = 1)
public class PentahoSpringComponentProvider extends SpringComponentProvider {

  private static final Log logger = LogFactory.getLog( PentahoSpringComponentProvider.class );

  private static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "applicationContext.xml";
  private static final String PARAM_CONTEXT_CONFIG_LOCATION = "contextConfigLocation";
  private static final String PARAM_SPRING_CONTEXT = "contextConfig";

  private volatile InjectionManager injectionManager;
  private volatile ApplicationContext ctx;

  @Override
  public void initialize( InjectionManager injectionManager ) {
    this.injectionManager = injectionManager;

    ServletContext sc = injectionManager.getInstance( ServletContext.class );

    if ( sc != null ) {
      // servlet container
      Object attr = sc.getAttribute( "PLUGIN_CONTEXT" );
      if ( attr == null ) {
        attr = sc.getAttribute( WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE );
      }

      if ( attr == null ) {
        return;
      } else if ( attr instanceof RuntimeException ) {
        throw ( RuntimeException )attr;
      } else if ( attr instanceof Error ) {
        throw ( Error )attr;
      } else if ( attr instanceof Exception ) {
        throw new IllegalStateException( ( Exception )attr );
      } else if ( !( attr instanceof ApplicationContext ) ) {
        throw new IllegalStateException( "Context attribute is not of type WebApplicationContext: " + attr );
      } else {
        ctx = ( ApplicationContext ) attr;
      }
    } else {
      // non-servlet container
      ctx = createSpringContext();
    }
    if (ctx == null) {
      return;
    }

    // initialize HK2 spring-bridge

    ImmediateHk2InjectionManager hk2InjectionManager = ( ImmediateHk2InjectionManager ) injectionManager;
    SpringBridge.getSpringBridge().initializeSpringBridge( hk2InjectionManager.getServiceLocator() );
    SpringIntoHK2Bridge springBridge = injectionManager.getInstance( SpringIntoHK2Bridge.class );
    springBridge.bridgeSpringBeanFactory( ctx );

    try {
      // AutowiredInjectResolver  constructor can be used directly to instantiate it.
      // Since jersey-spring6 is a multi-version release jar, during compile time it is choosing default version of AutowiredInjectResolver instead of JDK-17 version of the same.
      // To get rid of this compiler error, we are using reflection to instantiate AutowiredInjectResolver.
      // During runtime JDK-17 version of AutowiredInjectResolver will be used.
      Class<?> autowiredInjector = Class.forName( "org.glassfish.jersey.server.spring.AutowiredInjectResolver" );
      injectionManager.register( Bindings.injectionResolver( ( AutowiredInjectResolver ) autowiredInjector.getConstructor( ApplicationContext.class ).newInstance( ctx ) ) );
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException |
             ClassNotFoundException e ) {
      throw new RuntimeException( e );
    }
    injectionManager.register( Bindings.service( ctx ).to( ApplicationContext.class ).named( "SpringContext" ) );
  }

  // detect JAX-RS classes that are also Spring @Components.
  // register these with HK2 ServiceLocator to manage their lifecycle using Spring.
  @Override
  public boolean bind( Class<?> component, Set<Class<?>> providerContracts ) {

    if ( ctx == null ) {
      return false;
    }

    String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors( ctx, component );
    if ( beanNames.length == 0 ) {
      return false;
    }

    String beanName = beanNames[0];

    Binding binding = Bindings.supplier( new SpringManagedBeanFactory( ctx, injectionManager, beanName ) )
            .to( component )
            .to( providerContracts );
    injectionManager.register( binding );

    return true;
  }

  private ApplicationContext createSpringContext() {
    ApplicationHandler applicationHandler = injectionManager.getInstance( ApplicationHandler.class );
    ApplicationContext springContext = ( ApplicationContext ) applicationHandler.getConfiguration()
            .getProperty( PARAM_SPRING_CONTEXT );
    if ( springContext == null ) {
      String contextConfigLocation = ( String ) applicationHandler.getConfiguration()
              .getProperty( PARAM_CONTEXT_CONFIG_LOCATION );
      springContext = createXmlSpringConfiguration( contextConfigLocation );
    }
    return springContext;
  }

  private ApplicationContext createXmlSpringConfiguration( String contextConfigLocation ) {
    if ( contextConfigLocation == null ) {
      contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;
    }
    return ctx = new ClassPathXmlApplicationContext( contextConfigLocation, "jersey-spring-applicationContext.xml" );
  }

  private static class SpringManagedBeanFactory implements Supplier {

    private final ApplicationContext ctx;
    private final InjectionManager injectionManager;
    private final String beanName;

    private SpringManagedBeanFactory( ApplicationContext ctx, InjectionManager injectionManager, String beanName ) {
      this.ctx = ctx;
      this.injectionManager = injectionManager;
      this.beanName = beanName;
    }

    @Override
    public Object get() {
      Object bean = ctx.getBean( beanName );
      if (bean instanceof Advised) {
        try {
          // Unwrap the bean and inject the values inside of it
          Object localBean = ( ( Advised ) bean ).getTargetSource().getTarget();
          injectionManager.inject( localBean );
        } catch ( Exception e ) {
          // Ignore and let the injection happen as it normally would.
          injectionManager.inject( bean );
        }
      } else {
        injectionManager.inject( bean );
      }
      return bean;
    }
  }

}
