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


package org.pentaho.platform.engine.security;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.security.messages.Messages;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Logger that uses AOP to log debugging information.
 * 
 * <p>
 * <strong>Do not use this in production! It logs passwords in plain text!</strong>
 * </p>
 * 
 * @author mlowery
 */
public class LoggingInterceptor implements MethodBeforeAdvice, AfterReturningAdvice, ThrowsAdvice {

  public void before( final Method method, final Object[] args, final Object target ) throws Throwable {
    Log logger = LogFactory.getLog( target.getClass() );
    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getString( "LoggingInterceptor.DEBUG_BEGIN_METHOD" ) ); //$NON-NLS-1$
      log( method, args, target );
    }
  }

  public void afterReturning( final Object returnValue, final Method method, final Object[] args, final Object target )
    throws Throwable {
    Log logger = LogFactory.getLog( target.getClass() );
    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getString( "LoggingInterceptor.DEBUG_END_METHOD" ) ); //$NON-NLS-1$
      log( method, args, target );
      logger.debug( Messages.getInstance().getString(
          "LoggingInterceptor.DEBUG_RETURN_VALUE", returnValue.getClass().getName(), toString( returnValue ) ) ); //$NON-NLS-1$
    }
  }

  public void afterThrowing( final Method method, final Object[] args,
                             final Object target, final Throwable exception ) {
    Log logger = LogFactory.getLog( target.getClass() );
    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getString( "LoggingInterceptor.DEBUG_EXCEPTION_IN_METHOD" ) ); //$NON-NLS-1$
      log( method, args, target );
      logger.debug( Messages.getInstance().getString(
          "LoggingInterceptor.DEBUG_EXCEPTION", exception.getClass().getName(), exception.getMessage() ) ); //$NON-NLS-1$
      logger.debug( Messages.getInstance().getString( "LoggingInterceptor.DEBUG_STACK_TRACE" ), exception ); //$NON-NLS-1$
    }
  }

  private void log( final Method method, final Object[] args, final Object target ) {
    Log logger = LogFactory.getLog( target.getClass() );
    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getString( "LoggingInterceptor.DEBUG_METHOD_NAME", method.getName() ) ); //$NON-NLS-1$
      logger.debug( Messages.getInstance().getString(
          "LoggingInterceptor.DEBUG_TARGET_OBJECT", target.getClass().getName(), toString( target ) ) ); //$NON-NLS-1$
      logger.debug( Messages.getInstance().getString( "LoggingInterceptor.DEBUG_METHOD_ARGS", arrayToString( args ) ) ); //$NON-NLS-1$
    }
  }

  /**
   * Returns a string representation of the given object. This is useful when third-party objects do not have
   * <code>toString()</code> implementations that meet your needs.
   */
  protected String toString( final Object object ) {
    /**
     * This impl uses reflection to print fields and it also skips sensitive fields.
     */
    return ( new ReflectionToStringBuilder( object ) {
      @Override
      protected boolean accept( final Field f ) {
        return super.accept( f ) && !f.getName().equals( "password" ) && !f.getName().equals( "credentials" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } ).toString();
  }

  /**
   * Returns a string representation of the given array. Instead of overriding this method, try overriding
   * <code>toString(object)</code> instead.
   */
  protected String arrayToString( final Object[] objects ) {
    StringBuffer buf = new StringBuffer();
    if ( null == objects ) {
      return "null"; //$NON-NLS-1$
    } else {
      for ( int i = 0; i < objects.length; i++ ) {
        if ( i > 0 ) {
          buf.append( ", " ); //$NON-NLS-1$
        }
        buf.append( toString( objects[i] ) );
      }
      return buf.toString();
    }
  }

}
