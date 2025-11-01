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


package org.pentaho.platform.engine.security.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.LoggerListener;
import org.springframework.security.core.Authentication;

/**
 * org.pentaho.platform.engine.security.event.PentahoLoggerListener wraps org.springframework.security.authentication.event.LoggerListener
 * and safeguards onApplicationEvent() calls
 * <p/>
 * This is because on the later spring security, LoggerListener has changed its method signature from
 * <p/>
 * void onApplicationEvent( ApplicationEvent event )
 * <p/>
 * to
 * <p/>
 * void onApplicationEvent(AbstractAuthenticationEvent event)
 * <p/>
 * @see https://github.com/spring-projects/spring-security/blob/4.1.3.RELEASE/core/src/main/java/org/springframework/security/authentication/event/LoggerListener.java#L46
 * <p/>
 * But when the OrderedApplicationEventMulticaster multicasts a ApplicationEvent to all listeners
 * ( read: ApplicationListener interface ), it does so without checking if that particular ApplicationListener instance
 * does in fact support the ApplicationEvent instance
 * <p/>
 * listener.onApplicationEvent( event )
 * <p/>
 * and in cases such as a 'PublicInvocationEvent' or a 'ContextRefreshedEvent' are sent, the LoggerListener ended up
 * throwing a ClassCastException: event <instance> cannot be cast to AbstractAuthenticationEvent and actually halting
 * the operation at hand
 * <p/>
 * <p/>
 * We are instantiating org.springframework.security.authentication.event.LoggerListener internally in the constructor
 * and not via spring IoC ( as desired ) because spring.framework does a last-safeguard check on all beans
 * ( including inner beans ) where IF bean is instanceof ApplicationListener AND is a singleton-scoped bean, then
 * add if on-the-fly to the listeners list ( which would wound up bringing us back to the issue above )
 * @see https://github.com/spring-projects/spring-framework/blob/v4.1.5.RELEASE/spring-context/src/main/java/org/springframework/context/support/PostProcessorRegistrationDelegate.java#L349-L355
 */
public class PentahoLoggerListener implements ApplicationListener {

  LoggerListener loggerListener;

  public PentahoLoggerListener() {
    this.loggerListener = new LoggerListener();
  }

  public PentahoLoggerListener( LoggerListener loggerListener ) {
    this.loggerListener = loggerListener;
  }

  @Override public void onApplicationEvent( ApplicationEvent event ) {

    if ( event != null && AbstractAuthenticationEvent.class.isAssignableFrom( event.getClass() ) ) {
      loggerListener.onApplicationEvent( new WrappedAuthenticationEvent( (Authentication) event.getSource() ) );
    }
  }

  public boolean isLogInteractiveAuthenticationSuccessEvents() {
    return loggerListener.isLogInteractiveAuthenticationSuccessEvents();
  }

  public void setLogInteractiveAuthenticationSuccessEvents( boolean logInteractiveAuthenticationSuccessEvents ) {
    loggerListener.setLogInteractiveAuthenticationSuccessEvents( logInteractiveAuthenticationSuccessEvents );
  }

  private class WrappedAuthenticationEvent extends AbstractAuthenticationEvent {

    public WrappedAuthenticationEvent( Authentication authentication ) {
      super( authentication );
    }
  }
}
