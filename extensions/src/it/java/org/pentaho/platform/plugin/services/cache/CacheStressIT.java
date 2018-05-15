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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.pentaho.platform.api.cache.IPlatformCache;
import org.pentaho.platform.api.cache.IPlatformCache.CacheScope;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

public class CacheStressIT extends BaseTest {

  private final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed velit massa, facilisis ac massa id, sodales fringilla lorem. Integer malesuada, tortor sit amet pellentesque semper, mauris tortor rhoncus nisi, ac gravida est diam in sem. Praesent suscipit tellus eget elit gravida accumsan. Aenean aliquam aliquet diam, hendrerit gravida mauris tincidunt vel. Aliquam bibendum eros non nunc vulputate elementum. Integer feugiat dolor sed sapien varius gravida. Etiam non tellus dignissim, viverra dui vitae, pretium dui. Phasellus interdum ut dui eu dignissim. Nam id purus at est tempor facilisis. Suspendisse scelerisque dictum volutpat. In elementum erat vel nulla ullamcorper, eu vulputate orci blandit. Morbi orci lorem, fringilla non facilisis quis, faucibus varius magna. Donec sodales nisi quis mauris commodo, id accumsan tortor ultrices. Nulla id venenatis ipsum, non feugiat quam. Vestibulum vestibulum, sapien quis varius suscipit, dui tellus varius dui, dictum venenatis libero velit ac turpis. Cras nec hendrerit eros. Cras tempor nisi a felis dapibus, in ullamcorper ipsum fermentum. Integer a rhoncus sapien, vel molestie augue. Nunc elementum, dolor gravida molestie dictum, turpis leo semper nisl, aliquam porttitor quam ligula eget tortor. Vivamus aliquet libero ut velit volutpat malesuada. Donec ac turpis non metus rutrum venenatis. Vivamus leo diam, interdum et ex ut, sodales aliquet arcu. In non facilisis enim, et cursus neque. Curabitur sodales ante ut nibh vehicula aliquet. Proin aliquam at lorem non congue. Donec pretium diam in ullamcorper congue. Morbi mauris elit, mattis ac nulla at, posuere commodo purus. Nulla et purus nisl. Nulla ut nibh at lectus imperdiet dapibus ac ut risus. Vivamus interdum aliquam ipsum vitae sollicitudin. Sed quis volutpat lectus. Integer ac nisl nunc. Duis auctor sollicitudin nisi sed molestie. Sed et orci neque. Etiam scelerisque, velit sagittis congue rutrum, mauris ipsum tempus mauris, eget fermentum nisl tellus non tortor. Donec convallis massa a odio varius fringilla. Pellentesque at arcu tempor, gravida purus quis, fringilla turpis. Nunc in molestie dolor. Donec quis lorem vestibulum, semper dui at, elementum arcu. Suspendisse vitae viverra sapien. Integer convallis justo arcu, eget egestas quam lacinia non. Etiam sit amet velit dolor. Fusce urna lectus, tincidunt a blandit a, bibendum in augue. Mauris feugiat consequat leo. Ut euismod volutpat dictum. Quisque bibendum lobortis augue id dignissim. Quisque mattis pharetra diam, ut fringilla turpis consequat egestas. Duis a imperdiet felis. Phasellus eros nibh, dictum et feugiat nec, elementum at est. Quisque a nisi ut ipsum fermentum laoreet. Phasellus eleifend, purus a ultricies pulvinar, elit elit pretium elit, non lobortis libero mi et velit. Nullam vestibulum, nunc quis lobortis ultricies, ante nisl sollicitudin mauris, ut euismod ex lectus at lacus. Nullam viverra sem et eros maximus maximus. Aliquam ornare nunc in dui rhoncus, eget accumsan lectus hendrerit. Phasellus ornare nisi mauris, id convallis dui maximus sed. Vestibulum a suscipit ante. Vestibulum ut ipsum vitae dui accumsan maximus eu vitae lorem. Phasellus dapibus maximus maximus. Cras dolor odio, porttitor sit amet interdum nec, facilisis ac orci. Pellentesque a justo ac ipsum ultrices interdum. Aliquam sed lorem eget nibh sollicitudin fermentum. Mauris risus arcu, eleifend vitae aliquet vel, elementum vel arcu. Maecenas dapibus quis nibh sit amet eleifend. Proin et sagittis leo, non fermentum felis. Suspendisse mattis magna ut sapien aliquam semper. Maecenas sed dolor maximus, ultricies nulla eget, cursus elit. Donec placerat nulla id ultricies feugiat.";

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution-annotated";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution-annotated";
  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml";
  final AtomicBoolean isBaconated = new AtomicBoolean( true );

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

  public void testCacheUnderStress_20_200000_30() {
    final IPlatformCache cache = PentahoSystem.get( IPlatformCache.class );
    // This cache should easily be able to handle a million operations
    // over 100 clients in less than 30 seconds.
    runTest( cache, 100, 1000000, 30 );
  }

  private class MyRunnable implements Runnable {
    String defaultRegionName = "foo";
    String defaultKey = "yay";
    Throwable t = null;
    final IPlatformCache cache;
    final String name;
    boolean success = false;
    public MyRunnable( IPlatformCache cache, String name ) {
      this.cache = cache;
      this.name = name;
    }
    public void run() {
      try {
        if ( Math.random() <= 0.5 ) {
          // use a new random region
          defaultRegionName = "bar";
        }
        if ( Math.random() <= 0.5 ) {
          // use a new random region
          defaultRegionName = "nay";
        }
        if ( Math.random() <= 0.5 ) {
          //System.out.println( name + " put" );
          cache.put( CacheScope.forRegion( defaultRegionName ), defaultKey, LOREM_IPSUM );
        } else {
          //System.out.println( name + " get" );
          final Object object =
              cache.get( CacheScope.forRegion( defaultRegionName ), defaultKey );
          if ( object != null
              && !object.equals( LOREM_IPSUM ) ) {
            isBaconated.set( false );
          }
        }
        if ( Math.random() <= 0.05 ) {
          // delete why not
          //System.out.println( name + " clear" );
          cache.clear( CacheScope.forRegion( defaultRegionName ), true );
        }
        success = true;
      } catch ( Throwable t ) {
        this.t = t;
      }
    }
  }
  private void runTest( IPlatformCache cache, int nbThreads, int nbJobs, int timeout ) {
    final ThreadFactory factory =
        new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);
            public Thread newThread(Runnable r) {
                final Thread t =
                    Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                t.setName("CacheStressTestIT" + '_' + counter.incrementAndGet());
                return t;
            }
        };
    final ScheduledExecutorService exec = Executors.newScheduledThreadPool( nbThreads, factory );

    Map<Future,MyRunnable> futures = new ConcurrentHashMap<Future, CacheStressIT.MyRunnable>();
    for (int i = 0; i < nbJobs; i++ ) {
      MyRunnable mr = new MyRunnable( cache, String.valueOf( i ) );
      futures.put( exec.submit( mr ), mr );
    }

    try {
      exec.shutdown();
      exec.awaitTermination( timeout, TimeUnit.SECONDS );
      AtomicBoolean success = new AtomicBoolean( true );
      futures.entrySet().forEach( entry -> {
        try {
          entry.getKey().get();
        } catch ( InterruptedException e ) {
          success.set( false );
          fail("Interrupted future");
          e.printStackTrace();
        } catch ( ExecutionException e ) {
          success.set( false );
          fail("Execution exception");
          e.printStackTrace();
        }
        if ( !entry.getValue().success ) {
          success.set( false );
          entry.getValue().t.printStackTrace();
        }
      } );
      Assert.assertTrue( success.get() );
    } catch ( InterruptedException e ) {
      fail( "Did not complete." );
    }
    Assert.assertTrue( isBaconated.get() );
  }
}
