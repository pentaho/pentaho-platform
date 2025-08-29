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

import junit.framework.TestCase;

public class EmailConfigurationTest extends TestCase {

  public void testEqualsEmailConfig() {
    final EmailConfiguration emailConfigOriginal =
              new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.com", 36,
                      "SMTP", true, "user", "password", false, true );
    final EmailConfiguration emailConfigNew =
              new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.com", 36,
                      "SMTP", true, "user", "password", false, true );
    assertEquals( emailConfigOriginal.equals( emailConfigNew ), true );
    emailConfigNew.setAuthenticate( false );
    assertEquals( emailConfigOriginal.equals( emailConfigNew ), false );
  }

  public void testHashCodeEmailConfig() {
    final EmailConfiguration emailConfigOriginal =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.com", 36,
                    "SMTP", true, "user", "password", false, true );
    final EmailConfiguration emailConfigNew =
            new EmailConfiguration( true, false, "test@pentaho.com", "Pentaho Scheduler", "smtp.com", 36,
                    "SMTP", true, "user", "password", false, true );
    int emailConfigOriginalHashCode = emailConfigOriginal.hashCode();
    int emailConfigNewHashCode = emailConfigNew.hashCode();
    assertEquals( emailConfigOriginalHashCode == emailConfigNewHashCode, true );
    emailConfigNew.setAuthenticate( false );
    emailConfigNewHashCode = emailConfigNew.hashCode();
    assertEquals( emailConfigOriginalHashCode == emailConfigNewHashCode, false );
  }
}
