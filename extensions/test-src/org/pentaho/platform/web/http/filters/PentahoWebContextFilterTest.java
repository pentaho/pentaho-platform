/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.mockito.Mockito.*;


public class PentahoWebContextFilterTest {

  @Test
  public void testWebContextCachedWaitSecondVariable() throws Exception {

    ICacheManager cacheManager = Mockito.mock( ICacheManager.class );

    PentahoWebContextFilter filter = new PentahoWebContextFilter();

    PentahoWebContextFilter.cache = cacheManager;

    when( cacheManager.getFromGlobalCache( PentahoSystem.WAIT_SECONDS ) ).thenReturn( null )
      .thenReturn( new Integer( 30 ) );

    filter.printRequireJsCfgStart( new ByteArrayOutputStream() );
    filter.printRequireJsCfgStart( new ByteArrayOutputStream() );

    verify( cacheManager, times( 2 ) ).getFromGlobalCache( eq( PentahoSystem.WAIT_SECONDS ) );
    verify( cacheManager, times( 1 ) ).putInGlobalCache( eq( PentahoSystem.WAIT_SECONDS ), anyObject() );

  }

}
