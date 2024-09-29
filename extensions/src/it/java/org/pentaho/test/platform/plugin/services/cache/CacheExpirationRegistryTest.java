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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.plugin.services.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;
import org.pentaho.platform.plugin.services.cache.CacheExpirationRegistry;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * User: rfellows Date: 10/26/11 Time: 9:20 AM
 */
public class CacheExpirationRegistryTest {

  private static final int THREADS = 50; // setup to run 50 threads
  private static final Log logger = LogFactory.getLog( CacheExpirationRegistryTest.class );

  private CacheExpirationRegistry registry;

  @Before
  public void setup() {
    registry = new CacheExpirationRegistry();
  }

  @Test
  public void testRegister() {
    TestCacheItem item = new TestCacheItem( "pentaho_cache_itemKey" );

    registry.register( item );
    assertEquals( 1, registry.getCachedItems().size() );

    // re-registering an item with the same key will just overwrite the old one
    registry.register( item );
    assertEquals( 1, registry.getCachedItems().size() );
  }

  @Test
  public void testRegister_MultipleItems() {
    registry.register( new TestCacheItem( "pentaho_cache_itemKey1" ) );
    registry.register( new TestCacheItem( "pentaho_cache_itemKey2" ) );

    assertEquals( 2, registry.getCachedItems().size() );
  }

  @Test
  public void testUnRegister() {
    TestCacheItem item = new TestCacheItem( "pentaho_cache_itemKey1" );

    registry.register( item );
    assertEquals( 1, registry.getCachedItems().size() );

    registry.unRegister( item );
    assertEquals( 0, registry.getCachedItems().size() );
  }

  @Test
  public void testRegisterThreaded() throws InterruptedException {
    ExecutorService service = Executors.newFixedThreadPool( THREADS );
    long start = System.currentTimeMillis();

    IntStream.range( 0, THREADS ).forEach( t -> service.submit( () -> {
      try {
        IntStream.range( 0, 100000 ).forEach( ( i ) -> {
          registry.register( new TestCacheItem( "key" + i ) );
          registry.getCachedItems();
          registry.unRegister( new TestCacheItem( "key" + i ) );
          registry.getCachedItems();
        } );
      } catch ( Exception e ) {
        logger.error( "Error in thread " + t + " [" + e.getClass().getName() + "]: " + e.getMessage() + "." );
        fail();
      }

      logger.info( "Thread " + t + " finished." );
    } ) );

    service.shutdown();
    service.awaitTermination( 300, TimeUnit.SECONDS ); // kill the test if it takes more than 5 minutes to run

    long end = System.currentTimeMillis();
    logger.info( "Execution time in milliseconds: " + ( end - start ) );
    logger.info( "Size: " + registry.getCachedItems().size() );

    assertEquals( 0, registry.getCachedItems().size() );
  }

  class TestCacheItem implements ILastModifiedCacheItem {

    String key;
    long timestamp;

    TestCacheItem( String key ) {
      this.key = key;
      this.timestamp = new Date().getTime();
    }

    @Override
    public long getLastModified() {
      return timestamp;
    }

    @Override
    public String getCacheKey() {
      return key;
    }
  }
}
