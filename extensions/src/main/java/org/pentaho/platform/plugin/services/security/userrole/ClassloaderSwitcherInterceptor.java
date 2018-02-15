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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
