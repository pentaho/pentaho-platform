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
public class CacheManagerWithRegionTest extends BaseTest {

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

  public void testCacheRegion() {

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

    cacheManager.putInRegionCache( userSession1.getId(), "StringObject", user1StringObject );
    cacheManager.putInRegionCache( userSession1.getId(), "repoDoc", user1Document ); //$NON-NLS-1$
    cacheManager.putInRegionCache( userSession2.getId(), "StringObject", user2StringObject ); //$NON-NLS-1$
    cacheManager.putInRegionCache( userSession2.getId(), "repoDoc", user2Document ); // $NON-N

    // Get them back out
    Object user1CachedStringObject = cacheManager.getFromRegionCache( userSession1.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertNull( user1CachedStringObject );
    Object user1CachedDocument = cacheManager.getFromRegionCache( userSession1.getId(), "repoDoc" ); //$NON-NLS-1$
    Assert.assertNull( user1CachedDocument );
    Object user2CachedStringObject = cacheManager.getFromRegionCache( userSession2.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertNull( user2CachedStringObject );
    Object user2CachedDocument = cacheManager.getFromRegionCache( userSession2.getId(), "repoDoc" ); //$NON-NLS-1$
    Assert.assertNull( user2CachedDocument );

    cacheManager.addCacheRegion( userSession1.getId() );
    cacheManager.addCacheRegion( userSession2.getId() );
    // Ok - we now have some stuff to jam into the cache.
    cacheManager.putInRegionCache( userSession1.getId(), "StringObject", user1StringObject );
    cacheManager.putInRegionCache( userSession1.getId(), "repoDoc", user1Document ); //$NON-NLS-1$
    cacheManager.putInRegionCache( userSession2.getId(), "StringObject", user2StringObject ); //$NON-NLS-1$
    cacheManager.putInRegionCache( userSession2.getId(), "repoDoc", user2Document ); //$NON-NLS-1$

    // Get them back out
    Object user1CachedStringObject1 = cacheManager.getFromRegionCache( userSession1.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertEquals( user1StringObject, (String) user1CachedStringObject1 );
    Object user1CachedDocument1 = cacheManager.getFromRegionCache( userSession1.getId(), "repoDoc" ); //$NON-NLS-1$
    String user1CompareXMLCached1 = ( (Document) user1CachedDocument1 ).asXML();
    Assert.assertEquals( user1CompareXMLOriginal, user1CompareXMLCached1 );

    Object user2CachedStringObject1 = cacheManager.getFromRegionCache( userSession2.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertEquals( user2StringObject, (String) user2CachedStringObject1 );
    Object user2CachedDocument1 = cacheManager.getFromRegionCache( userSession2.getId(), "repoDoc" ); //$NON-NLS-1$
    String user2CompareXMLCached1 = ( (Document) user2CachedDocument1 ).asXML();
    Assert.assertEquals( user2CompareXMLOriginal, user2CompareXMLCached1 );

    // OK - We've verified that their objects are unique to each individual
    // user.

    // Test Removals from session only

    // Remove a single user-session based object.
    cacheManager.removeFromRegionCache( userSession1.getId(), "StringObject" ); //$NON-NLS-1$
    // Try to get it back anyway.
    Object notThere = cacheManager.getFromRegionCache( userSession1.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    // Make sure that User2 is unaffected
    Object shouldBeThere = cacheManager.getFromRegionCache( userSession2.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertNotNull( shouldBeThere );

    // Kill user1's session
    cacheManager.removeRegionCache( userSession1.getId() );
    notThere = cacheManager.getFromRegionCache( userSession1.getId(), "repoDoc" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    // Make sure that User2 is still unaffected
    shouldBeThere = cacheManager.getFromRegionCache( userSession2.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertNotNull( shouldBeThere );

    // Test Global Caching
    cacheManager.addCacheRegion( "Global" );
    // Put stuff in
    cacheManager.putInRegionCache( "Global", "globalIntegerKey", globalInt ); //$NON-NLS-1$
    cacheManager.putInRegionCache( "Global", "globalBigDecimalKey", globalBigDecimal ); //$NON-NLS-1$
    cacheManager.putInRegionCache( "Global", "globalStringBufferKey", globalStringBuffer ); //$NON-NLS-1$

    Object cachedGlobalInt = cacheManager.getFromRegionCache( "Global", "globalIntegerKey" ); //$NON-NLS-1$
    Assert.assertEquals( globalInt, cachedGlobalInt );
    Object cachedGlobalBigDecimal = cacheManager.getFromRegionCache( "Global", "globalBigDecimalKey" ); //$NON-NLS-1$
    Assert.assertEquals( globalBigDecimal, cachedGlobalBigDecimal );
    Object cachedGlobalStringBuffer = cacheManager.getFromRegionCache( "Global", "globalStringBufferKey" ); //$NON-NLS-1$
    Assert.assertEquals( globalStringBuffer, cachedGlobalStringBuffer );

    // Test clear all session-based keys. This should leave the global stuff
    // alone.
    cacheManager.removeRegionCache( userSession2.getId() );
    notThere = cacheManager.getFromRegionCache( userSession2.getId(), "StringObject" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    notThere = cacheManager.getFromRegionCache( userSession2.getId(), "repoDoc" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    shouldBeThere = cacheManager.getFromRegionCache( "Global", "globalIntegerKey" ); //$NON-NLS-1$
    Assert.assertNotNull( shouldBeThere );

    // Totally clear out the cache.
    cacheManager.clearCache();
    notThere = cacheManager.getFromRegionCache( "Global", "globalIntegerKey" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    notThere = cacheManager.getFromRegionCache( "Global", "globalBigDecimalKey" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    notThere = cacheManager.getFromRegionCache( "Global", "globalStringBufferKey" ); //$NON-NLS-1$
    Assert.assertNull( notThere );
    cacheManager.addCacheRegion( "Global" );
    // Force cache overload - make sure it spools objects to disk...
    // Assumes cache size is set to 2000 objects maximum.
    for ( int i = 0; i < 10000; i++ ) {
      String someCachedString = "This is the string to cache " + i; //$NON-NLS-1$
      String someCachedKey = "SomeCachedKey" + i; //$NON-NLS-1$
      if ( ( i % 1000 ) == 0 ) {
        sleep( 5 );
      }
      cacheManager.putInRegionCache( "Global", someCachedKey, someCachedString );
    }
    // Let cache stabalize, and decide what hasn't been used for a while.
    // 15 seconds should do it.
    sleep( 15 );
    // Get first item from the cache...
    shouldBeThere = cacheManager.getFromRegionCache( "Global", "SomeCachedKey1" ); //$NON-NLS-1$
    Assert.assertEquals( shouldBeThere, "This is the string to cache 1" ); //$NON-NLS-1$
    // Get middle item from the cache...
    shouldBeThere = cacheManager.getFromRegionCache( "Global", "SomeCachedKey5000" ); //$NON-NLS-1$
    Assert.assertEquals( shouldBeThere, "This is the string to cache 5000" ); //$NON-NLS-1$
    // Get last item from the cache...
    shouldBeThere = cacheManager.getFromRegionCache( "Global", "SomeCachedKey999" ); //$NON-NLS-1$
    Assert.assertEquals( shouldBeThere, "This is the string to cache 999" ); //$NON-NLS-1$

    // Clear cache again...
    cacheManager.clearCache();

    // Make sure...
    notThere = cacheManager.getFromRegionCache( "Global", "SomeCachedKey2" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    notThere = cacheManager.getFromRegionCache( "Global", "SomeCachedKey5002" ); //$NON-NLS-1$
    Assert.assertNull( notThere );

    notThere = cacheManager.getFromRegionCache( "Global", "SomeCachedKey998" ); //$NON-NLS-1$
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
