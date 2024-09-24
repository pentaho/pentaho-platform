/*!
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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.gwt.rpc.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains internal utility code that supports the implementation of the classes in the
 * {@link org.pentaho.platform.web.gwt.rpc} namespace.
 */
public class GwtRpcUtil {
  public static final String WEBAPP_ROOT_TOKEN = "WEBAPP_ROOT";
  private static final Pattern WEBAPP_ROOT_PATH_PATTERN = Pattern.compile( "^/.*/WEBAPP_ROOT/" );

  private GwtRpcUtil() {
  }

  /**
   * Returns a new string which is equal to that specified in the <code>path</code> argument,
   * except the first occurrence of <code>WEBAPP_ROOT</code> is replaced with the value of the
   * <code>appContextPath</code> argument.
   *
   * @param path           The path in which to replace the web app token.
   * @param appContextPath The value which replaces the web app token.
   * @return A new string with the web app token replaced.
   */
  public static String scrubWebAppRoot( String path, String appContextPath ) {
    if ( path.contains( WEBAPP_ROOT_TOKEN ) ) {
      Matcher matcher = WEBAPP_ROOT_PATH_PATTERN.matcher( path );
      if ( matcher.find() ) {
        String garbagePathPart = matcher.group();
        int index = path.indexOf( garbagePathPart );

        return new StringBuffer( path )
          .replace( index, index + garbagePathPart.length(), appContextPath )
          .toString();
      }
    }

    return path;
  }

  /**
   * Calls a <code>supplier</code> having a given class loader, <code>classLoader</code>, as the current class loader
   * of the current thread.
   * <p>
   * After calling the given supplier, the original current class loader of the current thread is restored,
   * even in case the supplier throws an exception.
   *
   * @param classLoader The class loader.
   * @param supplier    The supplier to call.
   * @return The value returned by the supplier.
   */
  public static <T> T withClassLoader( @NonNull ClassLoader classLoader, @NonNull Supplier<T> supplier ) {
    return withClassLoaderThrowing( classLoader, supplier::get );
  }

  /**
   * Calls a throwing supplier, <code>supplier</code>, having a given class loader, <code>classLoader</code>, as the
   * current class loader
   * of the current thread.
   * <p>
   * After calling the given supplier, the original current class loader of the current thread is restored,
   * even in case the supplier throws an exception.
   *
   * @param classLoader The class loader.
   * @param supplier    The supplier to call.
   * @return The value returned by the supplier.
   * @throws E The exception thrown by the supplier, when called.
   */
  public static <T, E extends Throwable> T withClassLoaderThrowing( @NonNull ClassLoader classLoader,
                                                                    @NonNull ThrowingSupplier<T, E> supplier )
    throws E {

    Objects.requireNonNull( classLoader );
    Objects.requireNonNull( supplier );

    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      // Temporarily swap out the context classloader to the target class loader if
      // the target has been loaded by one other than the context class loader.
      // This is necessary, so the RPC class can do a Class.forName and find the service
      // class specified in the request.
      if ( classLoader != originalClassLoader ) {
        Thread.currentThread().setContextClassLoader( classLoader );
      }

      return supplier.get();
    } finally {
      // Reset the context classloader, if necessary.
      if ( ( classLoader != originalClassLoader ) && originalClassLoader != null ) {
        Thread.currentThread().setContextClassLoader( originalClassLoader );
      }
    }
  }
}
