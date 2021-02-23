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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.security.userroledao;

import org.junit.Test;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.testng.Assert;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class DefaultTenantedPrincipleNameResolverTest {

  @Test
  public void testIsValid() {
    DefaultTenantedPrincipleNameResolver resolver = new DefaultTenantedPrincipleNameResolver();
    resolver.setDelimeter( DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );
    assertEquals( resolver.isValid( "pentaho_user" ), false );
  }

  @Test
  public void testGetPrincipleId() {
    DefaultTenantedPrincipleNameResolver resolver = new DefaultTenantedPrincipleNameResolver();
    ITenant tenant = mock( ITenant.class );
    String principalName = "PRINCIPAL_NAME";
    resolver.setDelimeter( DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );

    String principalId = resolver.getPrincipleId( tenant, principalName );
    assertEquals( principalId, principalName.toLowerCase() );


  }
}
