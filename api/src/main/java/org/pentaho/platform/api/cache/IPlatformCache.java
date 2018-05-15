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
 * Copyright (c) 2018-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.cache;

import java.util.Map.Entry;
import java.util.Set;

import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * This is the Pentaho platform's cache API.
 *
 * <p>It is used by the platform's extensions, as well as some plugins.
 * It is a replacement to ICacheManager with equivalent methods. Some of
 * the requirements of the last interface have been simplified as well.
 * For example, there is no need to verify if regions exist, or to tell
 * the cache to create the desired regions from this client API.
 *
 * <p>The API resembles that of a Map. It has the same general CRUD methods,
 * with the exception of an added parameter, the {@link CacheScope}. The scope
 * defines the general namespace to which a collection of cached objects
 * belong to. See its javadoc for more details.
 *
 * <p>There are 3 general use scopes that exist.
 *
 * <p><b>Global</b>
 *
 * <p>This is the global cache space. All sessions and the system share
 * anything stored here.
 *
 * <pre>
 * {@code
 * IPlatformCache cache = IPentahoSystem.get( IPlatformCache.class );
 * cache.put( CacheScope.global(), key, value );
 * }
 * </pre>
 *
 * <p><b>Session</b>
 *
 * <p>This cache space allows you to store objects related to a user session.
 * The cache will only be accessible to sessions with the same session ID.
 * The cache will also take care to listen to the session's lifecycle events
 * and cleanup once the session is terminated.
 *
 * <pre>
 * {@code
 * IPlatformCache cache = IPentahoSystem.get( IPlatformCache.class );
 * cache.put(
 *   CacheScope.forSession(
 *     PentahoSessionHolder.getSession() ),
 *   key,
 *   value );
 * }
 * </pre>
 *
 * <p><b>Region</b>
 *
 * <p>This scope defines arbitrary regions identified by a unique
 * name, passed as a String.
 *
 * <pre>
 * {@code
 * IPlatformCache cache = IPentahoSystem.get( IPlatformCache.class );
 * cache.put(
 *   CacheScope.forRegion(
 *     "FooBar" ),
 *   key,
 *   value );
 * }
 * </pre>
 *
 * <p><b>Preconfigure caches with Spring beans</b>
 *
 * <p>Spring beans which require one or more regional caches can use the
 * {@link CacheRegionRequired} annotation on a bean. Such annotated classes
 * will be seen by the cache implementation when the Spring application
 * context is refreshed, and the cache regions will be preconfigured.
 *
 * <p><b>Cache implementation</b>
 *
 * <p>This interface is responsible of maintaining a thread-safe access to the caches,
 * as well as their creation as needed. It is safe to use concurrently and
 * will enforce a Read/Write access policy.
 *
 * <p>The implementation of this interface is not responsible of configuring
 * the underlying cache instance. This would be done by changing pentaho.xml
 * under cache-provider. Read the associated documentation to learn how the
 * cache is implemented behind the scenes.
 */
public interface IPlatformCache extends ILogoutListener {

  /**
   * <p>A CacheScope is the equivalent of a region. Each scope
   * corresponds to a cache instance as defined in pentaho.xml
   * under cache-provider.
   * <p>For the global cache scope, use {@link CacheScope#global()}.
   * <p>For the user session cache scope, use {@link CacheScope#forSession(IPentahoSession)}.
   * <p>For an arbitrary region cache scope, use {@link CacheScope#forRegion(String)}.
   */
  public static class CacheScope {
    final Scope scope;
    final String ident;

    public enum Scope {
      Session,
      Region,
      Global
    }

    CacheScope( Scope scope, String ident ) {
      this.scope = scope;
      this.ident = ident;
    }

    /**
     * Represents the cache scope of a user's session.
     */
    public static CacheScope forSession( IPentahoSession session ) {
      if ( session == null ) {
        return new CacheScope( Scope.Session, "null" );
      }
      return new CacheScope( Scope.Session, session.getId() );
    }

    /**
     * Represents the global cache scope.
     */
    public static CacheScope global() {
      return new CacheScope( Scope.Global, Scope.Global.name() );
    }

    /**
     * Represents a region in the cache with an arbitrary name.
     */
    public static CacheScope forRegion( String regionKey ) {
      return new CacheScope( Scope.Region, regionKey );
    }

    public CacheScope.Scope getScope() {
      return scope;
    }

    public String getKey() {
      return ident;
    }

    public int hashCode() {
      int h = 31 * ident.hashCode();
      h *= scope.name().hashCode();
      return h;
    }

    public boolean equals( Object obj ) {
      if ( obj == null || !( obj instanceof CacheScope ) ) {
        return false;
      }
      CacheScope cs = (CacheScope) obj;
      if ( cs.ident.equals( this.ident ) && cs.scope.equals( this.scope ) ) {
        return true;
      }
      return false;
    }
  }

  /**
   * Puts an element into the cache of the given scope and key.
   */
  void put( CacheScope scope, Object key, Object value );

  /**
   * Returns all cached entries for a given scope. The
   * returned collection may be empty.
   */
  @SuppressWarnings( "rawtypes" )
  Set<Entry> entrySet( CacheScope scope );

  /**
   * Returns all cached keys for a given scope. The
   * returned collection may be empty.
   */
  @SuppressWarnings( "rawtypes" )
  Set keySet( CacheScope scope );

  /**
   * Returns all cached elements for a given scope. The
   * returned collection may be empty.
   */
  @SuppressWarnings( "rawtypes" )
  Set values( CacheScope scope );

  /**
   * Returns the number of elements cached for a given scope.
   */
  int size( CacheScope scope );

  /**
   * Fetches an object from cache for the given scope and key.
   * Returns null if there is no such element in cache.
   */
  Object get( CacheScope scope, Object key );

  /**
   * Removed a cached object for the given scope and key.
   */
  void remove( CacheScope scope, Object key );

  /**
   * Clears all caches in all scopes.
   */
  void clear();

  /**
   * Clears the cache of a specified scope.
   */
  void clear( CacheScope scope );

  /**
   * Clears the cache of a specified scope. If delete is true
   * and this is a regional scope, the cache will be torn down entirely.
   */
  void clear( CacheScope scope, boolean delete );

  /**
   * This does not need to be called. The Pentaho platform will
   * call this through a system listener called PlatformCacheSystemListener.
   * It is declared in solutions/system/systemListeners.xml.
   */
  void stop();

  /**
   * This does not need to be called. The Pentaho platform will
   * call this through a system listener called PlatformCacheSystemListener.
   * It is declared in solutions/system/systemListeners.xml.
   */
  void start();

  /**
   * Tells whether the cache is enabled or not.
   */
  boolean isEnabled();
}
