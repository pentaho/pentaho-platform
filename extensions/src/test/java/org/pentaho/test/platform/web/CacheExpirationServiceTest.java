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


package org.pentaho.test.platform.web;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;
import java.util.Date;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.cache.ILastModifiedCacheItem;
import org.pentaho.platform.plugin.services.cache.CacheExpirationRegistry;
import org.pentaho.platform.web.servlet.CacheExpirationService;
import org.xml.sax.SAXException;

/**
 * User: rfellows Date: 10/25/11 Time: 2:42 PM
 */
public class CacheExpirationServiceTest {
  CacheExpirationRegistry registry;
  CacheExpirationService service;

  @Before
  public void setup() {
    registry = new CacheExpirationRegistry();
    service = new CacheExpirationService();
    service.setExpirationRegistry( registry );
  }

  @Test
  public void testGetXml() {
    TestCacheItem item = new TestCacheItem( "pentaho_cache_itemKey" );
    String lm = Long.toString( item.getLastModified() );

    registry.register( item );

    String xml = service.getXml();
    String expected =
        "<cache-expiration-items><cache-item><key>pentaho_cache_itemKey</key><last-modified>" + lm
            + "</last-modified></cache-item></cache-expiration-items>";
    assertEquals( expected, xml );
    assertEquals( 1, registry.getCachedItems().size() );

    // re-registering an item with the same key will just overwrite the old one
    registry.register( item );
    xml = service.getXml();
    assertEquals( expected, xml );
    assertEquals( 1, registry.getCachedItems().size() );

  }

  @Test
  public void testGetXml_MultipleItems() throws SAXException, IOException {
    TestCacheItem item = new TestCacheItem( "pentaho_cache_itemKey1" );
    String lm = Long.toString( item.getLastModified() );

    registry.register( item );

    item = new TestCacheItem( "pentaho_cache_itemKey2" );
    String lm2 = Long.toString( item.getLastModified() );

    registry.register( item );

    String xml = service.getXml();
    
    Diff myDiff = new Diff( "<cache-expiration-items><cache-item><key>pentaho_cache_itemKey1</key><last-modified>" + lm
          + "</last-modified></cache-item><cache-item><key>pentaho_cache_itemKey2</key><last-modified>" + lm2
          + "</last-modified></cache-item></cache-expiration-items>", xml);

    //This allows the xml elements to be ordered differently and still pass the equality check
    myDiff.overrideElementQualifier( new RecursiveElementNameAndTextQualifier( ) );
    assertXMLEqual(
        "But they are equal when an ElementQualifier controls which test element is compared with each control element",
        myDiff, true );

    assertEquals( 2, registry.getCachedItems().size() );

  }

  @Test
  public void testUnRegister() {
    TestCacheItem item = new TestCacheItem( "pentaho_cache_itemKey1" );
    String lm = Long.toString( item.getLastModified() );

    registry.register( item );
    assertEquals( 1, registry.getCachedItems().size() );

    registry.unRegister( item );
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
