/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

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
