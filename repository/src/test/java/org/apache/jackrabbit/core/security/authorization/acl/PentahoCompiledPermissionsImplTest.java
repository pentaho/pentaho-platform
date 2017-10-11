/*!
 * Copyright 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.AccessControlUtils;
import org.apache.jackrabbit.spi.Path;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.Collections;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Created by nbaker on 5/3/2017.
 */
public class PentahoCompiledPermissionsImplTest {
  /**
   * Verify that No caching is taking place between multiple calls to getResult.
   *
   * @throws Exception
   */
  @Test
  public void getResult() throws Exception {
    AbstractCompiledPermissions.Result res1 = mock( AbstractCompiledPermissions.Result.class );
    AbstractCompiledPermissions.Result res2 = mock( AbstractCompiledPermissions.Result.class );

    PentahoCompiledPermissionsImpl cp =
      new PentahoCompiledPermissionsImpl( Collections.emptySet(), mock( SessionImpl.class ),
        mock( EntryCollector.class ),
        mock( AccessControlUtils.class ), false ) {
        int numBuildResultCalls = 0;

        @Override protected Result buildResult( Path absPath ) throws RepositoryException {
          return ( ++numBuildResultCalls == 1 ) ? res1 : res2;
        }

        int numBuildRepoResultCalls = 0;

        @Override protected Result buildRepositoryResult() throws RepositoryException {
          return ( ++numBuildRepoResultCalls == 1 ) ? res1 : res2;
        }
      };

    // calls into builtResult
    AbstractCompiledPermissions.Result result1 = cp.getResult( mock( Path.class ) );
    AbstractCompiledPermissions.Result result2 = cp.getResult( mock( Path.class ) );
    assertSame( result1, res1 );
    assertSame( result2, res2 );


    // calls into BuildRepositoryResult
    result1 = cp.getResult( null );
    result2 = cp.getResult( null );
    assertSame( result1, res1 );
    assertSame( result2, res2 );

  }

}