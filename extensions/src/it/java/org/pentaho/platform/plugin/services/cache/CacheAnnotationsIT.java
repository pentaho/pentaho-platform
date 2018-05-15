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

import java.io.File;

import org.junit.Assert;
import org.pentaho.platform.api.cache.IPlatformCache;
import org.pentaho.platform.api.cache.IPlatformCache.CacheScope;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

public class CacheAnnotationsIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution-annotated";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution-annotated";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";

  @Override public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH );
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH );
      return ALT_SOLUTION_PATH;
    }
  }

  public void testCache() {
    final IPlatformCache cache = PentahoSystem.get( IPlatformCache.class );
    Assert.assertEquals(
      "[org.pentaho.test.platform.plugin.services.cache.MockAnnotatedCacheRegion, Global, Session, FooBar]",
      ( ( PlatformCacheImpl ) cache ).scopesCaches.keySet().toString());
  }
}
