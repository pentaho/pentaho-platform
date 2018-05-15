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

package org.pentaho.platform.plugin.services.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.pentaho.platform.api.cache.IPlatformCache;
import org.pentaho.platform.api.cache.IPlatformCache.CacheScope;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p><b>DEPRECATED</b>
 *
 * <p>This interface was deprecated in favor of {@link IPlatformCache}.
 *
 * <hr/>
 *
 * This class provides an access point for pluggable caching mechanisms. Right now, it only supports the caching
 * mechanisms implemented in <code>org.hibernate.cache</code>.
 * <p>
 * To use the cache manager, you need to include the following information in your <code>pentaho.xml</code>.
 * 
 * <pre>
 * 
 *  &lt;cache-provider&gt;
 *    &lt;class&gt;org.hibernate.cache.xxxxxxxx&lt;/class&gt;
 *    &lt;region&gt;pentahoCache&lt;/region&gt;
 *    &lt;properties&gt;
 *      &lt;property name=&quot;someProperty&quot;&gt;someValue&lt;/property&gt;
 *    &lt;/properties&gt;
 *  &lt;/cache-provider&gt;
 * </pre>
 * 
 * <p>
 * The specified class must implement the <code>org.hibernate.cache.CacheProvider</code> interface.
 * <p>
 * Each implementation of the <code>org.hibernate.cache.CacheProvider</code> has slightly different requirements with
 * respect to the required input parameters - so, please see the classes in that package for more information (available
 * from the Sourceforge Hibernate project). Also, some cache providers (notably the
 * <code>org.hibernate.cache.EhCacheProvider</code>) completely ignore the passed in properties, and only configure
 * themselves by locating a configuration file (e.g. ehcache.xml) on the classpath.
 * 
 * <p>
 * The cache manager supports session-based caching (that is, caching of data that is user-specific) as well as
 * global-based caching (that is, caching of data that is system-wide). To differentiate between session-based and
 * global-based caching, there are different methods that get called depending upon the storage type.
 * 
 * <p>
 * Data that is cached for user sessions require an <code>IPentahoSession</code> object to be passed in. The cache
 * manager uses the <code>IPentahoSession.getId()</code> to classify saved objects underneath a specific user session.
 * No information is actually stored in the user session object. For an example of this, see <code><br>
 * putInSessionCache(IPentahoSession session, String key, Object value)</code>
 * <p>
 * Data that is server-wide (i.e. global) uses different methods for storage/retrieval/management. For an example of
 * this, see <code><br> 
 * getFromGlobalCache(Object key)</code>
 * <p>
 * <b>Example Usage:</b>
 * <p>
 * 
 * <pre>
 * String globalCachable = &quot;String to cache&quot;;
 * String globalCacheKey = &quot;StringKey&quot;;
 * CacheManager cacheManager = PentahoSystem.getCacheManager();
 * cacheManager.putInGlobalCache( globalCacheKey, globalCachable );
 * </pre>
 * 
 * <p>
 * <b>Important Considerations</b>
 * <ul>
 * <li>Most caches require objects that go into the cache <i> as well as their respective object key</i> implement
 * Serializable. It is a safe assumption that both the Key and the Value should implement Serializable.</li>
 * <li>Some caches are read-only. Other caches are read-write. What does this mean? It means that once you put an object
 * in the cache, you can't put an object into the cache with the same key. You'd need to remove it first</li>
 * 
 * </ul>
 * 
 * <p>
 * 
 * @see org.hibernate.cache.CacheProvider
 * @see org.hibernate.cache.Cache
 * 
 * @author mbatchel
 * 
 */
@Deprecated
public class CacheManager implements ICacheManager, InitializingBean {

  private IPlatformCache delegate;

  public CacheManager() {
    // no op.
    delegate = PentahoSystem.get( IPlatformCache.class );
  }

  public void afterPropertiesSet() throws Exception {

  }

  public void cacheStop() {
    delegate.stop();
  }

  public void killSessionCache( IPentahoSession session ) {
    delegate.clear( CacheScope.forSession( session ) );
  }

  public void killSessionCaches() {
    // This is now a no-no.
  }

  public void putInSessionCache( IPentahoSession session, String key, Object value ) {
    delegate.put( CacheScope.forSession( session ), key, value );
  }

  public void clearCache() {
    delegate.clear();
  }

  public void removeFromSessionCache( IPentahoSession session, String key ) {
    delegate.remove( CacheScope.forSession( session ), key );
  }

  public Object getFromSessionCache( IPentahoSession session, String key ) {
    return delegate.get( CacheScope.forSession( session ), key );
  }

  public boolean cacheEnabled() {
    return delegate.isEnabled();
  }

  public void putInGlobalCache( Object key, Object value ) {
    delegate.put( CacheScope.global(), key, value );
  }

  public Object getFromGlobalCache( Object key ) {
    return delegate.get( CacheScope.global(), key );
  }

  public void removeFromGlobalCache( Object key ) {
    delegate.remove( CacheScope.global(), key );
  }

  public boolean cacheEnabled( String region ) {
    // All regions are loaded on the fly now.
    // For preloaded caches, use annotations.
    return true;
  }

  public void onLogout( IPentahoSession session ) {
    // The delegate will get his own message.
  }

  public boolean addCacheRegion( String region ) {
    // No need to do that anymore.
    return true;
  }

  public boolean addCacheRegion( String region, Properties cacheProperties ) {
    // No need to do that anymore.
    return true;
  }

  public void clearRegionCache( String region ) {
    delegate.clear( CacheScope.forRegion( region ) );
  }

  public void removeRegionCache( String region ) {
    delegate.clear( CacheScope.forRegion( region ), true );
  }

  public void putInRegionCache( String region, Object key, Object value ) {
    delegate.put( CacheScope.forRegion( region ), key, value );
  }

  public Object getFromRegionCache( String region, Object key ) {
    return delegate.get( CacheScope.forRegion( region ), key );
  }

  public Set getAllEntriesFromRegionCache( String region ) {
    return delegate.entrySet( CacheScope.forRegion( region ) );
  }

  public Set getAllKeysFromRegionCache( String region ) {
    return delegate.keySet( CacheScope.forRegion( region ) );
  }

  public List getAllValuesFromRegionCache( String region ) {
    return new ArrayList<>( delegate.values( CacheScope.forRegion( region ) ) );
  }

  public void removeFromRegionCache( String region, Object key ) {
    delegate.remove( CacheScope.forRegion( region ), key );
  }

  public long getElementCountInRegionCache( String region ) {
    return delegate.size( CacheScope.forRegion( region ) );
  }

  public long getElementCountInSessionCache() {
    return delegate.size( CacheScope.forSession( PentahoSessionHolder.getSession() ) );
  }

  public long getElementCountInGlobalCache() {
    return delegate.size( CacheScope.global() );
  }
}
