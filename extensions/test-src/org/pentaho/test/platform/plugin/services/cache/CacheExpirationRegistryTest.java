package org.pentaho.test.platform.plugin.services.cache;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;
import org.pentaho.platform.plugin.services.cache.CacheExpirationRegistry;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

/**
 * User: rfellows
 * Date: 10/26/11
 * Time: 9:20 AM
 */
public class CacheExpirationRegistryTest {
  CacheExpirationRegistry registry;
  @Before
  public void setup() {
    registry = new CacheExpirationRegistry();
  }

  @Test
  public void testRegister() {
    TestCacheItem item = new TestCacheItem("pentaho_cache_itemKey");
    registry.register(item);

    assertEquals(1, registry.getCachedItems().size());

    // re-registering an item with the same key will just overwrite the old one
    registry.register(item);
    assertEquals(1, registry.getCachedItems().size());

  }

  @Test
  public void testRegister_MultipleItems() {
    TestCacheItem item = new TestCacheItem("pentaho_cache_itemKey1");

    registry.register(item);

    item = new TestCacheItem("pentaho_cache_itemKey2");

    registry.register(item);

    assertEquals(2, registry.getCachedItems().size());

  }

  @Test
  public void testUnRegister() {
    TestCacheItem item = new TestCacheItem("pentaho_cache_itemKey1");

    registry.register(item);
    assertEquals(1, registry.getCachedItems().size());

    registry.unRegister(item);
    assertEquals(0, registry.getCachedItems().size());
  
  }

  class TestCacheItem implements ILastModifiedCacheItem {

    String key;
    long timestamp;

    TestCacheItem(String key) {
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
