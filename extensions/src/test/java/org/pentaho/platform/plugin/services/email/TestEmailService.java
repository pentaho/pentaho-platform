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

package org.pentaho.platform.plugin.services.email;

import jakarta.mail.Provider;
import jakarta.mail.Session;

import java.io.File;

class TestEmailService extends EmailService {

  public TestEmailService( File file ) {
    super( file );
  }

  /**
   * Explicitly registers MockMail as the SMTP transport provider for testing.
   *
   * <p>While a {@code jakarta.mail.providers} file exists in test resources declaring MockMail,
   * the Jakarta Mail API's provider discovery mechanism may not reliably load it due to:
   * <ul>
   *   <li>Classpath ordering - the Eclipse Angus (default Jakarta Mail implementation) provider
   *       file is discovered first and may take precedence</li>
   *   <li>Provider merging behavior - when multiple provider files exist, the API may prioritize
   *       providers from JARs over test resources</li>
   *   <li>IDE test execution - test resources from dependency modules may not be visible on
   *       the classpath when running tests directly from the IDE</li>
   * </ul>
   *
   * <p>This explicit registration ensures MockMail is used consistently across all test scenarios,
   * preventing attempts to connect to a real SMTP server on localhost:25 during unit tests.
   *
   * @param session the Jakarta Mail session to configure with the MockMail provider
   */
  @Override
  protected void configureSession( Session session ) {
    // Explicitly register MockMail as the SMTP transport provider.
    try {
      Provider mockProvider = new Provider(
        Provider.Type.TRANSPORT,
        "smtp",
        "org.pentaho.platform.util.MockMail",
        "Pentaho Test Mock",
        "2.0"
      );

      session.setProvider( mockProvider );
    } catch ( Exception e ) {
      // Ignore
    }
  }
}

