package org.pentaho.test.platform.web;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;
import org.pentaho.platform.plugin.services.cache.CacheExpirationRegistry;
import org.pentaho.platform.web.servlet.CacheExpirationService;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

/**
 * User: rfellows
 * Date: 10/25/11
 * Time: 2:42 PM
 */
public class CacheExpirationServiceTest {
    CacheExpirationRegistry registry;
    CacheExpirationService service;
    @Before
    public void setup() {
      registry = new CacheExpirationRegistry();
      service = new CacheExpirationService();
      service.setExpirationRegistry(registry);
    }

    @Test
    public void testGetXml() {
      TestCacheItem item = new TestCacheItem("pentaho_cache_itemKey");
      String lm = Long.toString(item.getLastModified());

      registry.register(item);

      String xml = service.getXml();
      String expected = "<cache-expiration-items><cache-item><key>pentaho_cache_itemKey</key><last-modified>" + lm + "</last-modified></cache-item></cache-expiration-items>";
      assertEquals(expected, xml);
      assertEquals(1, registry.getCachedItems().size());

      // re-registering an item with the same key will just overwrite the old one
      registry.register(item);
      xml = service.getXml();
      assertEquals(expected, xml);
      assertEquals(1, registry.getCachedItems().size());

    }

    @Test
    public void testGetXml_MultipleItems() {
      TestCacheItem item = new TestCacheItem("pentaho_cache_itemKey1");
      String lm = Long.toString(item.getLastModified());

      registry.register(item);

      item = new TestCacheItem("pentaho_cache_itemKey2");
      String lm2 = Long.toString(item.getLastModified());

      registry.register(item);

      String xml = service.getXml();
      String expected = "<cache-expiration-items><cache-item><key>pentaho_cache_itemKey1</key><last-modified>" + lm + "</last-modified></cache-item><cache-item><key>pentaho_cache_itemKey2</key><last-modified>" + lm2 + "</last-modified></cache-item></cache-expiration-items>";
      assertEquals(expected, xml);

      assertEquals(2, registry.getCachedItems().size());

    }

    @Test
    public void testUnRegister() {
      TestCacheItem item = new TestCacheItem("pentaho_cache_itemKey1");
      String lm = Long.toString(item.getLastModified());

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
