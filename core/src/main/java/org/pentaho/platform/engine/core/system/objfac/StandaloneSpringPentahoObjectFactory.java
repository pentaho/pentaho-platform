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

package org.pentaho.platform.engine.core.system.objfac;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.objfac.spring.PentahoBeanScopeValidatorPostProcessor;
import org.pentaho.platform.engine.core.system.objfac.spring.SpringScopeSessionHolder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.request.AbstractRequestAttributesScope;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This factory implementation creates and uses a self-contained Spring {@link ApplicationContext} which is not tied to
 * or accessible by any other parts of the application.
 *
 * <h3>Background information</h3>
 *
 * Beans published via `pen:publish`, in any "spring.xml" file which includes the special
 * `ApplicationContextPentahoSystemRegisterer` bean, are published to the PentahoSystem as a
 * `SpringPentahoObjectReference`, via `PentahoSystem.registerReference` -- this is the only use case for
 * `SpringPentahoObjectReference` at this time.
 * <p>
 * Currently, the following Spring application contexts include the bean, thus supporting `pen:publish`:
 * 1. the root Spring app context, of type `PentahoSolutionSpringApplicationContext`, which loads
 *    `pentaho-spring-beans.xml` (and has an  associated `WebSpringPentahoObjectFactory` object factory).
 * 2. the Spring app contexts created by `JAXRSServlet` and `GwtRpcProxyServlet`, of type
 *    `XmlWebApplicationContext`, and which load `pentahoServices.spring.xml`.
 * 3. any plugins which explicitly include the bean in their `plugin.spring.xml`.
 * <p>
 * The class `ApplicationContextPentahoSystemRegisterer` associates one `StandaloneSpringPentahoObjectFactory`
 * to each Spring application context which includes the bean (even if the app context already has some other
 * associated object factory; it's a many OF to one Spring AC relationship; in fact, some plugins end up with
 * two `StandaloneSpringPentahoObjectFactory`: one created by the plugin manager, and another created by
 * `ApplicationContextPentahoSystemRegisterer`).
 * <p>
 * When `StandaloneSpringPentahoObjectFactory` is initialized with its associated Spring application context, it
 * registers two special scopes, "request" and "session" in the application context, if these are not already
 * registered in it, using a custom scope implementation, `ThreadLocalScope`. This implementation expects/stores
 * "bean" objects in the Pentaho Session which is set in Pentaho's `SpringScopeSessionHolder.SESSION` holder, and
 * relies on it being up to date with the current/desired Pentaho session.
 * <p>
 * However, for the case of the root Spring application context, it already has these scopes previously registered
 * by Spring itself (with instances of `RequestScope` and `SessionScope`). These scopes work differently, as they
 * rely on Spring's `RequestContextHolder` to obtain the current HTTP request and session, to store objects in.
 * <p>
 * The problem with Spring's approach is that in some cases (such as when the server is starting up and importing
 * default content), there is no current HTTP request or HTTP session, and so any "request" or "session" beans
 * attempted to be resolved during that phase will fail (with the error
 * `org.springframework.beans.factory.support.ScopeNotActiveException`).
 * However, in these cases, there is a Pentaho session available (e.g. setup by `SecurityHelper.runAsSystem`).
 * This is (likely?) the reason why `StandaloneSpringPentahoObjectFactory` registers its own "request" and "session"
 * scope implementations, instead of relying on Spring's default implementations. It is not clear why this
 * same solution was not being used for the other Spring application contexts. (At the time of writing, trying to
 * obtain a "request" or "session" bean published by the root Spring application context at server startup fails.
 * Likely, this was the first time a "session" bean came up in the root app context which is used at startup.)
 * <p>
 * The main point is that `SpringPentahoObjectReference` needs to be able to handle beans from any of these Spring
 * application contexts, supporting both types of scopes implementation.
 * <p>
 * To support `StandaloneSpringPentahoObjectFactory` bound application contexts using `ThreadLocalScope`,
 * `SpringPentahoObjectReference` sets `SpringScopeSessionHolder.SESSION` to the current Pentaho session, to ensure
 * "session" and "request" beans can be resolved, regardless of the code calling this method.
 * <p>
 * To support the root application context, which uses Spring's `SessionScope` and `RequestScope`,
 * `StandaloneSpringPentahoObjectFactory` will now (at the time of writing start to) also register its own "request" and
 * "session" scopes, using the `ChildThreadLocalScope` implementation, which extends `ThreadLocalScope`. This
 * implementation checks if there is a current request attributes in `RequestContextHolder`, and if so, delegates to the
 * parent scope (i.e. Spring's `RequestScope` or `SessionScope`), otherwise it falls back to the Pentaho session, as
 * `ThreadLocalScope` does. This approach also ensures minimal possibility of breaking existing code, as the behavior is
 * unchanged when there is a current request attributes in `RequestContextHolder`.
 *
 * @author Aaron Phillips
 * @see AbstractSpringPentahoObjectFactory
 */
public class StandaloneSpringPentahoObjectFactory extends AbstractSpringPentahoObjectFactory {

  private static final String SCOPE_REQUEST = "request";
  private static final String SCOPE_SESSION = "session";

  private static Map<ApplicationContext, StandaloneSpringPentahoObjectFactory> factoryMap = new HashMap<>();

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

      configureScope( appCtx.getBeanFactory(), SCOPE_REQUEST );
      configureScope( appCtx.getBeanFactory(), SCOPE_SESSION );

      beanFactory = appCtx;
    } else {
      if ( !( context instanceof ConfigurableApplicationContext configAppCtx ) ) {
        String msg =
          Messages.getInstance()
            .getErrorString( "StandalonePentahoObjectFactory.ERROR_0001_CONTEXT_NOT_SUPPORTED", //$NON-NLS-1$
              getClass().getSimpleName(), "GenericApplicationContext", context.getClass().getName() ); //$NON-NLS-1$
        throw new IllegalArgumentException( msg );
      }

      configureScope( configAppCtx.getBeanFactory(), SCOPE_REQUEST );
      configureScope( configAppCtx.getBeanFactory(), SCOPE_SESSION );

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

  private void configureScope( @NonNull ConfigurableListableBeanFactory beanFactory, @NonNull String scopeName ) {
    Scope currentScope = beanFactory.getRegisteredScope( scopeName );

    Scope scope;
    if ( currentScope instanceof AbstractRequestAttributesScope parentSpringScope ) {
      // Wrap current scope in our own scope that knows how to fall back to the pentaho session
      // when needed.
      scope = new ChildThreadLocalScope( scopeName, parentSpringScope );
    } else {
      scope = new ThreadLocalScope( scopeName );
    }

    beanFactory.registerScope( scopeName, scope );
  }

  private static class ThreadLocalScope implements Scope {
    @NonNull
    private final String scopeName;

    public ThreadLocalScope( @NonNull String scopeName ) {
      this.scopeName = scopeName;
    }

    @Override
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

    @Override
    public Object remove( String name ) {
      IPentahoSession session = SpringScopeSessionHolder.SESSION.get();
      return session != null ? session.removeAttribute( name ) : null;
    }

    @Override
    public void registerDestructionCallback( String name, Runnable callback ) {
      logger.warn( "ThreadLocalScope does not support destruction callbacks. "
        + "Consider using a RequestScope in a Web environment." );
    }

    @Override
    public String getConversationId() {
      if ( scopeName.equals( SCOPE_REQUEST ) ) {
        return null;
      }

      var session = SpringScopeSessionHolder.SESSION.get();
      return session != null ? session.getId() : null;
    }

    @Override
    public Object resolveContextualObject( String key ) {
      return null;
    }
  }

  private static class ChildThreadLocalScope extends ThreadLocalScope {
    @NonNull
    private final AbstractRequestAttributesScope parentScope;

    public ChildThreadLocalScope( @NonNull String scopeName, @NonNull AbstractRequestAttributesScope parentScope ) {
      super( scopeName );

      this.parentScope = Objects.requireNonNull( parentScope );
    }

    @Override
    public Object get( String name, ObjectFactory objectFactory ) {

      // Detect whether the Spring RequestContextHolder has a current request context.
      // If it does, delegate to it.
      if ( hasRequestContext() ) {
        return parentScope.get( name, objectFactory );
      }

      return super.get( name, objectFactory );
    }

    @Override
    public Object remove( String name ) {
      if ( hasRequestContext() ) {
        return parentScope.remove( name );
      }

      return super.remove( name );
    }

    @Override
    public void registerDestructionCallback( String name, Runnable callback ) {
      if ( hasRequestContext() ) {
        parentScope.registerDestructionCallback( name, callback );
        return;
      }

      super.registerDestructionCallback( name, callback );
    }

    @Override
    public String getConversationId() {
      if ( hasRequestContext() ) {
        return parentScope.getConversationId();
      }

      return super.getConversationId();
    }

    @Override
    public Object resolveContextualObject( String key ) {
      if ( hasRequestContext() ) {
        return parentScope.resolveContextualObject( key );
      }

      return super.resolveContextualObject( key );
    }

    private boolean hasRequestContext() {
      return RequestContextHolder.getRequestAttributes() != null;
    }
  }
}
