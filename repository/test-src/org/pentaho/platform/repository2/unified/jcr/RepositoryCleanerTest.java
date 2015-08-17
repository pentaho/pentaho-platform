/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.junit.Test;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import javax.jcr.Repository;

import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class RepositoryCleanerTest {

  @Test
  public void gc() throws Exception {
    GarbageCollector collector = mock( GarbageCollector.class );

    RepositoryImpl repository = mock( RepositoryImpl.class );
    when( repository.createDataStoreGarbageCollector() ).thenReturn( collector );

    MicroPlatform mp = new MicroPlatform();
    mp.defineInstance( Repository.class, repository );
    mp.defineInstance( "jcrRepository", repository );
    mp.start();

    try {
      RepositoryCleaner.gc();
    } finally {
      mp.stop();
    }

    verify( collector, times( 1 ) ).mark();
    verify( collector, times( 1 ) ).sweep();
    verify( collector, times( 1 ) ).close();
  }

}