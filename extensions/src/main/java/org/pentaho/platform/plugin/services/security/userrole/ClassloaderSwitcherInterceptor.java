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


package org.pentaho.platform.plugin.services.security.userrole;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * <p>This interceptor changes the thread context classloader to the class' current classloader.
 *
 * <p>We need this because of the
 * {@link org.springframework.ldap.odm.typeconversion.impl.ConversionServiceConverterManager} which was introduced
 * in spring-security-2.0.0.
 *
 * <p>This manager uses {@link org.springframework.util.ClassUtils#getDefaultClassLoader()} which creates
 * new class instance using thread context classloader which differs from the current classloader of the caller class:
 * <ul>
 * <li> thread context classloader is the {@code org.apache.activemq.activemq-osgi}
 * <li> current classloader is the spring's {@code WebappClassLoader}
 * </ul>
 *
 * When {@code ConversionServiceConverterManager} tries to cast {@code clazz.newInstance()} to the
 * {@code GenericConversionService} it throws {@code ClassCastException} if the context classloader hadn't changed.
 *
 * <p>That's why we need to proxy all classes which might use the {@code ConversionServiceConverterManager}.
 *
 * @author Andrei Abramov
 */
public class ClassloaderSwitcherInterceptor implements MethodInterceptor {

  public Object invoke( MethodInvocation invocation ) throws Throwable {
    Object ret;
    ClassLoader currentThreadContextClassLoader = null;
    boolean classLoaderWasSet = false;
    try {
      currentThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
      classLoaderWasSet = true;
      ret = invocation.proceed();
    } finally {
      if ( classLoaderWasSet ) {
        Thread.currentThread().setContextClassLoader( currentThreadContextClassLoader );
      }
    }
    return ret;
  }
}
