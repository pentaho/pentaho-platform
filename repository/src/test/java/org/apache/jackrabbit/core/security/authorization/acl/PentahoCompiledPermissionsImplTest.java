/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
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