/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthProviderFactoryTest {

  @Mock
  AzurePentahoOAuthHandler azurePentahoOAuthHandler;

  @Mock
  OktaPentahoOAuthHandler oktaPentahoOAuthHandler;

  @Test
  public void testGetInstanceForAzure() {
    PentahoOAuthProviderFactory pentahoOAuthProviderFactory = new PentahoOAuthProviderFactory();
    pentahoOAuthProviderFactory.setAzurePentahoOAuthHandler( azurePentahoOAuthHandler );

    Assert.assertEquals( pentahoOAuthProviderFactory.getInstance( "azure" ), azurePentahoOAuthHandler );
  }

  @Test
  public void testGetInstanceForOkta() {
    PentahoOAuthProviderFactory pentahoOAuthProviderFactory = new PentahoOAuthProviderFactory();
    pentahoOAuthProviderFactory.setOktaPentahoOAuthHandler( oktaPentahoOAuthHandler );

    Assert.assertEquals( pentahoOAuthProviderFactory.getInstance( "okta" ), oktaPentahoOAuthHandler );
  }

  @Test
  public void testGetInstanceForRandomIdp() {
    PentahoOAuthProviderFactory pentahoOAuthProviderFactory = new PentahoOAuthProviderFactory();
    pentahoOAuthProviderFactory.setOktaPentahoOAuthHandler( oktaPentahoOAuthHandler );

    Assert.assertThrows( IllegalArgumentException.class, () -> pentahoOAuthProviderFactory.getInstance( "" ) );
    Assert.assertThrows( IllegalArgumentException.class, () -> pentahoOAuthProviderFactory.getInstance( "random" ) );
  }

}
