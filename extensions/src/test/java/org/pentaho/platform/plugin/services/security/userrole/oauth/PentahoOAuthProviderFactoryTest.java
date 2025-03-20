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
  PentahoOAuthAzureHandler pentahoOAuthAzureHandler;

  @Mock
  PentahoOAuthOktaHandler pentahoOAuthOktaHandler;

  @Test
  public void testGetInstanceForAzure() {
    PentahoOAuthProviderFactory pentahoOAuthProviderFactory = new PentahoOAuthProviderFactory();
    pentahoOAuthProviderFactory.setPentahoOAuthAzureHandler( pentahoOAuthAzureHandler );

    Assert.assertEquals( pentahoOAuthProviderFactory.getInstance( "azure" ), pentahoOAuthAzureHandler );
  }

  @Test
  public void testGetInstanceForOkta() {
    PentahoOAuthProviderFactory pentahoOAuthProviderFactory = new PentahoOAuthProviderFactory();
    pentahoOAuthProviderFactory.setPentahoOAuthOktaHandler( pentahoOAuthOktaHandler );

    Assert.assertEquals( pentahoOAuthProviderFactory.getInstance( "okta" ), pentahoOAuthOktaHandler );
  }

  @Test
  public void testGetInstanceForRandomIdp() {
    PentahoOAuthProviderFactory pentahoOAuthProviderFactory = new PentahoOAuthProviderFactory();
    pentahoOAuthProviderFactory.setPentahoOAuthOktaHandler( pentahoOAuthOktaHandler );

    Assert.assertNull( pentahoOAuthProviderFactory.getInstance( "" ) );
    Assert.assertNull( pentahoOAuthProviderFactory.getInstance( "random" ) );
  }

}
