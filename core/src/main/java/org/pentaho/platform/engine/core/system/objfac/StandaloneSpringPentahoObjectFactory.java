/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.objfac.spring.PentahoBeanScopeValidatorPostProcessor;
import org.pentaho.platform.engine.core.system.objfac.spring.SpringScopeSessionHolder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

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
      FileSystemXmlApplicationContext appCtx = new FileSystemXmlApplicationContext( configFile );
      appCtx.refresh();

      appCtx.addBeanFactoryPostProcessor( new PentahoBeanScopeValidatorPostProcessor() );
      Scope requestScope = new ThreadLocalScope();
      appCtx.getBeanFactory().registerScope( "request", requestScope );
      Scope sessionScope = new ThreadLocalScope();
      appCtx.getBeanFactory().registerScope( "session", sessionScope );

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

    @Override
    public Object resolveContextualObject( String arg0 ) {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
