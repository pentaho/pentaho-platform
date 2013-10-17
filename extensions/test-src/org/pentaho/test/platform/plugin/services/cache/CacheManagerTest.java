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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.cache;

import junit.framework.Assert;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.math.BigDecimal;

@SuppressWarnings( "nls" )
public class CacheManagerTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/cache-solution";
  private static final String ALT_SOLUTION_PATH = "test-src/cache-solution";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH );
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH );
      return ALT_SOLUTION_PATH;
    }
  }

  // private final StringBuffer longString = new StringBuffer();

  public void testCache() {

    // Make sure we have a cache first...
    ICacheManager cacheManager = PentahoSystem.getCacheManager( null ); // TODO sbarkdull, need to get real session in
                                                                        // here
    Assert.assertNotNull( cacheManager );
    Assert.assertTrue( cacheManager.cacheEnabled() );

    // Test Session Based Caching
    StandaloneSession userSession1 = new StandaloneSession( "Standalone Session", "1234-5678-90" ); //$NON-NLS-1$ //$NON-NLS-2$
    StandaloneSession userSession2 = new StandaloneSession( "Standalone Session", "abc-def-ghi-jkl" ); //$NON-NLS-1$ //$NON-NLS-2$

    // ================================ Create Objects
    // User Objects

    // Cache any-old String...
    String user1StringObject = "User1's String Object"; //$NON-NLS-1$
    // Make sure we can cache these Document objects...
    Document user1Document = DocumentHelper.createDocument();
    Element user1RootNode = user1Document.addElement( "user1" ); //$NON-NLS-1$
    Element user1FileNode = user1RootNode.addElement( "file" ); //$NON-NLS-1$
    user1FileNode.addAttribute( "name", "test" ); //$NON-NLS-1$ //$NON-NLS-2$
    String user1CompareXMLOriginal = user1Document.asXML();

    // User2's Objects
    // Cache any-old String...
    String user2StringObject = "User2's String Object"; //$NON-NLS-1$
    Document user2Document = DocumentHelper.createDocument();
    Element user2RootNode = user2Document.addElement( "user2" ); //$NON-NLS-1$
    Element user2FileNode = user2RootNode.addElement( "folder" ); //$NON-NLS-1$
    user2FileNode.addAttribute( "name", "test2" ); //$NON-NLS-1$ //$NON-NLS-2$
    String user2CompareXMLOriginal = user2Document.asXML();

    // Global Objects
    Integer globalInt = new Integer( 372 );
    BigDecimal globalBigDecimal = new BigDecimal( "2342.123334444211" ); //$NON-NLS-1$
    StringBuffer globalStringBuffer = new StringBuffer();
    globalStringBuffer.append( "This is a really long string to stick in a string buffer" ); //$NON-NLS-1$

    // Ok - we now have some stuff to jam into the cache.
    cacheManager.putInSessionCache( userSession1, "StringObject", user1StringObject ); //$NON-NLS-1$
    cacheManager.putInSessionCache( userSession1, "repoDoc", user1Document ); //$NON-NLS-1$
    cacheManager.putInSessionCache( userSession2, "StringObject", user2StringObject ); //$NON-NLS-1$
    cacheManager.putInSessionCache( userSession2, "repoDoc", user2Document ); //$NON-NLS-1$

    // Get them back out
    Object user1CachedStringObject = cacheManager.getFromSessionCache( userSession1, "StringObject" ); //$NON-NLS-1$
    Assert.assertEquals( user1StringObject, (String) user1CachedStringObject );
    Object user1CachedDocument = cacheManager.getFromSessionCache( userSession1, "repoDoc" ); //$NON-NLS-1$
    String user1CompareXMLCached = ( (Document) user1CachedDocument ).asXML();
    Assert.assertEquals( user1CompareXMLOriginal, user1CompareXMLCached );

    Object user2CachedStringObject = cacheManager.getFromSessionCache( userSession2, "StringObject" ); //$NON-NLS-1$
    Assert.assertEquals( user2StringObject, (String) user2CachedStringObject );
    Object user2CachedDocument = cacheManager.getFromSessionCache( userSession2, "repoDoc" ); //$NON-NLS-1$
    String user2CompareXMLCached = ( (Document) user2CachedDocument ).asXML();
    Assert.assertEquals( user2CompareXMLOriginal, user2CompareXMLCached );

    // OK - We've verified that their objects are unique to each individual
    // user.

    // Test Removals from session only

    // Remove a single user-session based object.
    cacheManager.removeFromSessionCache( userSession1, "StringObject" ); //$NON-NLS-1$
    // Try to get it back anyway.
    Object notThere = cacheManager.getFromSessionCache( userSession1, "StringObject" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    // Make sure that User2 is unaffected
    Object shouldBeThere = cacheManager.getFromSessionCache( userSession2, "StringObject" ); //$NON-NLS-1$
    Assert.assertNotNull( shouldBeThere );

    // Kill user1's session
    cacheManager.killSessionCache( userSession1 );
    notThere = cacheManager.getFromSessionCache( userSession1, "repoDoc" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    // Make sure that User2 is still unaffected
    shouldBeThere = cacheManager.getFromSessionCache( userSession2, "StringObject" ); //$NON-NLS-1$
    Assert.assertNotNull( shouldBeThere );

    // Test Global Caching

    // Put stuff in
    cacheManager.putInGlobalCache( "globalIntegerKey", globalInt ); //$NON-NLS-1$
    cacheManager.putInGlobalCache( "globalBigDecimalKey", globalBigDecimal ); //$NON-NLS-1$
    cacheManager.putInGlobalCache( "globalStringBufferKey", globalStringBuffer ); //$NON-NLS-1$

    Object cachedGlobalInt = cacheManager.getFromGlobalCache( "globalIntegerKey" ); //$NON-NLS-1$
    Assert.assertEquals( globalInt, cachedGlobalInt );
    Object cachedGlobalBigDecimal = cacheManager.getFromGlobalCache( "globalBigDecimalKey" ); //$NON-NLS-1$
    Assert.assertEquals( globalBigDecimal, cachedGlobalBigDecimal );
    Object cachedGlobalStringBuffer = cacheManager.getFromGlobalCache( "globalStringBufferKey" ); //$NON-NLS-1$
    Assert.assertEquals( globalStringBuffer, cachedGlobalStringBuffer );

    // Test clear all session-based keys. This should leave the global stuff
    // alone.
    cacheManager.killSessionCaches();
    notThere = cacheManager.getFromSessionCache( userSession2, "StringObject" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    notThere = cacheManager.getFromSessionCache( userSession2, "repoDoc" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    shouldBeThere = cacheManager.getFromGlobalCache( "globalIntegerKey" ); //$NON-NLS-1$
    Assert.assertNotNull( shouldBeThere );

    // Totally clear out the cache.
    cacheManager.clearCache();
    notThere = cacheManager.getFromGlobalCache( "globalIntegerKey" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    notThere = cacheManager.getFromGlobalCache( "globalBigDecimalKey" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    notThere = cacheManager.getFromGlobalCache( "globalStringBufferKey" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    cacheManager.addCacheRegion( ICacheManager.GLOBAL );
    // Force cache overload - make sure it spools objects to disk...
    // Assumes cache size is set to 2000 objects maximum.
    for ( int i = 0; i < 10000; i++ ) {
      String someCachedString = "This is the string to cache " + i; //$NON-NLS-1$
      String someCachedKey = "SomeCachedKey" + i; //$NON-NLS-1$
      if ( ( i % 1000 ) == 0 ) {
        sleep( 5 );
      }
      cacheManager.putInGlobalCache( someCachedKey, someCachedString );
    }
    // Let cache stabalize, and decide what hasn't been used for a while.
    // 15 seconds should do it.
    sleep( 15 );
    // Get first item from the cache...
    shouldBeThere = cacheManager.getFromGlobalCache( "SomeCachedKey1" ); //$NON-NLS-1$
    Assert.assertEquals( shouldBeThere, "This is the string to cache 1" ); //$NON-NLS-1$
    // Get middle item from the cache...
    shouldBeThere = cacheManager.getFromGlobalCache( "SomeCachedKey5000" ); //$NON-NLS-1$
    Assert.assertEquals( shouldBeThere, "This is the string to cache 5000" ); //$NON-NLS-1$
    // Get last item from the cache...
    shouldBeThere = cacheManager.getFromGlobalCache( "SomeCachedKey999" ); //$NON-NLS-1$
    Assert.assertEquals( shouldBeThere, "This is the string to cache 999" ); //$NON-NLS-1$

    // Clear cache again...
    cacheManager.clearCache();

    // Make sure...
    notThere = cacheManager.getFromGlobalCache( "SomeCachedKey2" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    notThere = cacheManager.getFromGlobalCache( "SomeCachedKey5002" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    notThere = cacheManager.getFromGlobalCache( "SomeCachedKey998" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    // Done with tests.

  }

  private void sleep( final int time ) {
    try {
      System.out.println( "***** Sleeping for " + time + " seconds *****" ); //$NON-NLS-1$ //$NON-NLS-2$
      Thread.sleep( time * 1000 );
    } catch ( Exception ignored ) {
      //ignore
    }

  }

}
