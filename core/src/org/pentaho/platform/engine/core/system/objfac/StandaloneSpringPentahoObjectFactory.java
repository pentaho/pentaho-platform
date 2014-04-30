/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.objfac.spring.SpringScopeSessionHolder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This factory implementation creates and uses a self-contained Spring {@link ApplicationContext} which is not tied to
 * or accesible by any other parts of the application.
 *
 * @author Aaron Phillips
 * @see AbstractSpringPentahoObjectFactory
 */
public class StandaloneSpringPentahoObjectFactory extends AbstractSpringPentahoObjectFactory {

  private static Map<ApplicationContext, StandaloneSpringPentahoObjectFactory> factoryMap =
    new HashMap<ApplicationContext, StandaloneSpringPentahoObjectFactory>();


  public StandaloneSpringPentahoObjectFactory() {
  }

  public StandaloneSpringPentahoObjectFactory( String name ) {
    super( name );
  }

  /**
   * Initializes this object factory by creating a self-contained Spring {@link ApplicationContext} if one is not passed
   * in.
   *
   * @param configFile the Spring bean definition XML file
   * @param context    the {@link ApplicationContext} object, if null, then this method will create one
   */
  public void init( String configFile, Object context ) {

    if ( context == null ) {
      // beanFactory = new FileSystemXmlApplicationContext(configFile);
      File f = new File( configFile );
      FileSystemResource fsr = new FileSystemResource( f );
      GenericApplicationContext appCtx = new GenericApplicationContext();

      Scope requestScope = new ThreadLocalScope();
      appCtx.getBeanFactory().registerScope( "request", requestScope );
      Scope sessionScope = new ThreadLocalScope();
      appCtx.getBeanFactory().registerScope( "session", sessionScope );

      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );
      xmlReader.loadBeanDefinitions( fsr );

      beanFactory = appCtx;
    } else {
      if ( !( context instanceof ConfigurableApplicationContext ) ) {
        String msg =
          Messages.getInstance()
            .getErrorString( "StandalonePentahoObjectFactory.ERROR_0001_CONTEXT_NOT_SUPPORTED", //$NON-NLS-1$
              getClass().getSimpleName(), "GenericApplicationContext", context.getClass().getName() ); //$NON-NLS-1$
        throw new IllegalArgumentException( msg );
      }

      ConfigurableApplicationContext configAppCtx = (ConfigurableApplicationContext) context;

      if ( configAppCtx.getBeanFactory().getRegisteredScope( "request" ) == null ) {
        Scope requestScope = new ThreadLocalScope();
        configAppCtx.getBeanFactory().registerScope( "request", requestScope );
      }
      if ( configAppCtx.getBeanFactory().getRegisteredScope( "session" ) == null ) {
        Scope sessionScope = new ThreadLocalScope();
        configAppCtx.getBeanFactory().registerScope( "session", sessionScope );
      }

      setBeanFactory( configAppCtx );
    }
  }

  /**
   * Factory method guaranteed to return the same instance for a given applicationContext.
   *
   * @param applicationContext
   * @return
   */
  public static StandaloneSpringPentahoObjectFactory getInstance( final ApplicationContext applicationContext ) {
    if ( applicationContext == null ) {
      throw new IllegalArgumentException( "ApplicationContext cannot be null" );
    }
    StandaloneSpringPentahoObjectFactory retVal = factoryMap.get( applicationContext );
    if ( retVal == null ) {
      retVal = new StandaloneSpringPentahoObjectFactory();
      retVal.init( null, applicationContext );
      factoryMap.put( applicationContext, retVal );
    }
    return retVal;
  }

  private static class ThreadLocalScope implements Scope {

    public Object get( String name, ObjectFactory objectFactory ) {
      IPentahoSession session = SpringScopeSessionHolder.SESSION.get();
      if ( session == null ) {
        return null;
      }
      Object object = session.getAttribute( name );
      if ( object == null ) {
        object = objectFactory.getObject();
        session.setAttribute( name, object );
      }
      return object;
    }

    public Object remove( String name ) {
      IPentahoSession session = SpringScopeSessionHolder.SESSION.get();
      return session.removeAttribute( name );
    }

    public void registerDestructionCallback( String name, Runnable callback ) {
      logger.warn( "SimpleThreadScope does not support descruction callbacks. "
        + "Consider using a RequestScope in a Web environment." );
    }

    public String getConversationId() {
      return SpringScopeSessionHolder.SESSION.get().getId();
    }

  }
}
