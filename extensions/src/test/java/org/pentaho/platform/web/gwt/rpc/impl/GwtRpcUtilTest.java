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

import org.junit.Test;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GwtRpcUtilTest {

  // region scrubWebAppRoot( path , contextPath )
  /*
   * NOTE: These tests were written to match the existing implementation, even though much of the way it works
   * doesn't seem to make much sense...
   */

  private static final String APP_CONTEXT = "/pentaho";

  private void testScrubOne( String path, String expectedResult ) {
    String result = GwtRpcUtil.scrubWebAppRoot( path, APP_CONTEXT );

    assertEquals( expectedResult, result );
  }

  @Test
  public void testScrubEmptyResultsInEmpty() {
    testScrubOne( "", "" );
  }

  @Test
  public void testScrubNotRootedPathReturnsSamePath() {
    testScrubOne( "WEBAPP_ROOT", "WEBAPP_ROOT" );
    testScrubOne( "WEBAPP_ROOT/", "WEBAPP_ROOT/" );
    testScrubOne( "abc/WEBAPP_ROOT/", "abc/WEBAPP_ROOT/" );
  }

  @Test
  public void testScrubRootedPathButNonTokenFolderReturnsSamePath() {
    testScrubOne( "/WEBAPP_ROOT", "/WEBAPP_ROOT" );
    testScrubOne( "/abc/WEBAPP_ROOT", "/abc/WEBAPP_ROOT" );
  }

  @Test
  public void testScrubFirstLevelRootedAndFolderTokenReturnsSamePath() {
    testScrubOne( "/WEBAPP_ROOT/", "/WEBAPP_ROOT/" );
    testScrubOne( "/WEBAPP_ROOT/abc", "/WEBAPP_ROOT/abc" );
  }

  @Test
  public void testScrubSecondLevelRootedAndFolderTokenReplacesToken() {
    testScrubOne( "//WEBAPP_ROOT/", APP_CONTEXT );
    testScrubOne( "//WEBAPP_ROOT//abc", APP_CONTEXT + "/abc" );

    testScrubOne( "//WEBAPP_ROOT/", APP_CONTEXT );
    testScrubOne( "/abc/WEBAPP_ROOT//cde", APP_CONTEXT + "/cde" );
  }
  // endregion scrubWebAppRoot

  // region withClassLoader( classLoader, supplier )
  public static class SupplierMock<T> implements Supplier<T> {

    private final T value;
    private final Runnable runnable;

    public SupplierMock( T value, Runnable runnable ) {
      this.value = value;
      this.runnable = runnable;
    }

    @Override
    public T get() {
      if ( runnable != null ) {
        runnable.run();
      }

      return value;
    }
  }

  @Test
  public void testWithClassLoaderSetsGivenClassLoaderInCurrentThread() {
    ClassLoader classLoaderMock = mock( ClassLoader.class );

    SupplierMock<Void> supplierSpy = spy( new SupplierMock<>( null,
      () -> assertEquals( classLoaderMock, Thread.currentThread().getContextClassLoader() ) ) );

    GwtRpcUtil.withClassLoader( classLoaderMock, supplierSpy );

    // Make sure it was called.
    verify( supplierSpy, times( 1 ) ).get();
  }

  @Test
  public void testWithClassLoaderRestoresOriginalClassLoaderInCurrentThreadIfSupplierSucceeds() {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    try {
      ClassLoader beforeClassLoaderMock = mock( ClassLoader.class );
      currentThread.setContextClassLoader( beforeClassLoaderMock );

      ClassLoader duringClassLoaderMock = mock( ClassLoader.class );

      // Cannot spy Supplier<T> directly.
      SupplierMock<Void> supplierSpy = spy( new SupplierMock<>( null, null ) );

      GwtRpcUtil.withClassLoader( duringClassLoaderMock, supplierSpy );

      // Make sure it was called.
      verify( supplierSpy, times( 1 ) ).get();

      assertEquals( beforeClassLoaderMock, currentThread.getContextClassLoader() );

    } finally {
      if ( originalClassLoader != null ) {
        currentThread.setContextClassLoader( originalClassLoader );
      }
    }
  }

  @Test
  public void testWithClassLoaderRestoresOriginalClassLoaderInCurrentThreadIfSupplierThrows() {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    try {
      ClassLoader beforeClassLoaderMock = mock( ClassLoader.class );
      currentThread.setContextClassLoader( beforeClassLoaderMock );

      ClassLoader duringClassLoaderMock = mock( ClassLoader.class );

      RuntimeException error = new RuntimeException();

      // Cannot spy Supplier<T> directly.
      SupplierMock<Void> supplierSpy = spy( new SupplierMock<>( null, () -> {
        throw error;
      } ) );

      try {
        GwtRpcUtil.withClassLoader( duringClassLoaderMock, supplierSpy );
      } catch ( RuntimeException ex ) {
        assertEquals( error, ex );
      }

      // Make sure it was called.
      verify( supplierSpy, times( 1 ) ).get();

      assertEquals( beforeClassLoaderMock, currentThread.getContextClassLoader() );

    } finally {
      if ( originalClassLoader != null ) {
        currentThread.setContextClassLoader( originalClassLoader );
      }
    }
  }

  @Test
  public void testWithClassLoaderReturnsSupplierValue() {
    ClassLoader classLoaderMock = mock( ClassLoader.class );
    Object supplied = new Object();
    Object result = GwtRpcUtil.withClassLoader( classLoaderMock, new SupplierMock<>( supplied, null ) );

    assertEquals( supplied, result );
  }
  // endregion withClassLoader

  // region withClassLoaderThrowing( classLoader, throwingSupplier)
  interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;
  }

  public static class ThrowingSupplierMock<T, E extends Throwable> implements ThrowingSupplier<T, E> {

    private final T value;
    private final ThrowingRunnable<E> runnable;

    public ThrowingSupplierMock( T value, ThrowingRunnable<E> runnable ) {
      this.value = value;
      this.runnable = runnable;
    }

    @Override
    public T get() throws E {
      if ( runnable != null ) {
        runnable.run();
      }

      return value;
    }
  }

  @Test
  public void testWithClassLoaderThrowingSetsGivenClassLoaderInCurrentThread() throws Exception {
    ClassLoader classLoaderMock = mock( ClassLoader.class );

    ThrowingSupplierMock<Void, Exception> supplierSpy = spy( new ThrowingSupplierMock<>( null,
      () -> assertEquals( classLoaderMock, Thread.currentThread().getContextClassLoader() ) ) );

    GwtRpcUtil.withClassLoaderThrowing( classLoaderMock, supplierSpy );

    // Make sure it was called.
    verify( supplierSpy, times( 1 ) ).get();
  }

  @Test
  public void testWithClassLoaderThrowingRestoresOriginalClassLoaderInCurrentThreadIfSupplierSucceeds()
    throws Exception {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    try {
      ClassLoader beforeClassLoaderMock = mock( ClassLoader.class );
      currentThread.setContextClassLoader( beforeClassLoaderMock );

      ClassLoader duringClassLoaderMock = mock( ClassLoader.class );

      // Cannot spy Supplier<T> directly.
      ThrowingSupplierMock<Void, Exception> supplierSpy = spy( new ThrowingSupplierMock<>( null, null ) );

      GwtRpcUtil.withClassLoaderThrowing( duringClassLoaderMock, supplierSpy );

      // Make sure it was called.
      verify( supplierSpy, times( 1 ) ).get();

      assertEquals( beforeClassLoaderMock, currentThread.getContextClassLoader() );

    } finally {
      if ( originalClassLoader != null ) {
        currentThread.setContextClassLoader( originalClassLoader );
      }
    }
  }

  @Test
  public void testWithClassLoaderThrowingRestoresOriginalClassLoaderInCurrentThreadIfSupplierThrows() throws Exception {
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    try {
      ClassLoader beforeClassLoaderMock = mock( ClassLoader.class );
      currentThread.setContextClassLoader( beforeClassLoaderMock );

      ClassLoader duringClassLoaderMock = mock( ClassLoader.class );

      Exception error = new Exception();

      // Cannot spy Supplier<T> directly.
      ThrowingSupplierMock<Void, Exception> supplierSpy = spy( new ThrowingSupplierMock<>( null, () -> {
        throw error;
      } ) );

      try {
        GwtRpcUtil.withClassLoaderThrowing( duringClassLoaderMock, supplierSpy );
      } catch ( Exception ex ) {
        assertEquals( error, ex );
      }

      // Make sure it was called.
      verify( supplierSpy, times( 1 ) ).get();

      assertEquals( beforeClassLoaderMock, currentThread.getContextClassLoader() );

    } finally {
      if ( originalClassLoader != null ) {
        currentThread.setContextClassLoader( originalClassLoader );
      }
    }
  }

  @Test
  public void testWithClassLoaderThrowingReturnsSupplierValue() throws Exception {
    ClassLoader classLoaderMock = mock( ClassLoader.class );
    Object supplied = new Object();
    Object result = GwtRpcUtil.withClassLoaderThrowing(
      classLoaderMock,
      new ThrowingSupplierMock<Object, Exception>( supplied, null ) );

    assertEquals( supplied, result );
  }
  // endregion
}
