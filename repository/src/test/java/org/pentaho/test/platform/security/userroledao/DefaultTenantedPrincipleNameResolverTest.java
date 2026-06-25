/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.test.platform.security.userroledao;

import org.junit.Test;
import org.pentaho.platform.security.userroledao.DefaultTenantedPrincipleNameResolver;
import org.testng.Assert;

public class DefaultTenantedPrincipleNameResolverTest {

  @Test
  public void testIsValid() {
    DefaultTenantedPrincipleNameResolver resolver = new DefaultTenantedPrincipleNameResolver();
    resolver.setDelimeter( DefaultTenantedPrincipleNameResolver.ALTERNATE_DELIMETER );
    Assert.assertEquals( resolver.isValid( "pentaho_user" ), false );
  }
}
