/*
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
 * Copyright 2012 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources;

import java.io.File;
import javax.ws.rs.core.Response;

import org.pentaho.platform.plugin.services.email.EmailConfiguration;
import org.pentaho.platform.plugin.services.email.EmailService;

import junit.framework.TestCase;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmailResourceTest extends TestCase {
  private EmailResource emailResource = null;
  private File defaultConfigFile = null;
  private final int OK_STATUS = Response.ok().build().getStatus();
  private final EmailConfiguration BLANK_CONFIG = new EmailConfiguration();

  @Override
  protected void setUp() throws Exception {
    // Setup the temp email config file
    defaultConfigFile = File.createTempFile("email_config_", ".xml");
    this.emailResource = new EmailResource(new EmailService(defaultConfigFile));
  }

  public void testEmailConfig() throws Exception {
    {
      try {
        new EmailResource(null);
        fail("Exception should be thrown when a null EmailService is provided");
      } catch (IllegalArgumentException success) {
      }
    }

    {
      try {
        new EmailResource(new EmailService(new File(defaultConfigFile, "cannot.exist")));
        fail("Exception should be thrown when an invalid EmailService is provided");
      } catch (IllegalArgumentException success) {
      }
    }

    {
      final EmailConfiguration emptyConfig = emailResource.getEmailConfig();
      assertTrue(BLANK_CONFIG.equals(emptyConfig));
    }

    {
      final Response response = emailResource.setEmailConfig(new EmailConfiguration());
      assertEquals(OK_STATUS, response.getStatus());
      final EmailConfiguration emptyConfig = emailResource.getEmailConfig();
      assertTrue(BLANK_CONFIG.equals(emptyConfig));
    }

    {
      final EmailConfiguration emailConfigOriginal =
          new EmailConfiguration(true, false, "test@pentaho.com", null, new Short((short) 36), "",
              true, "user", null, false, true);
      final Response response = emailResource.setEmailConfig(emailConfigOriginal);
      assertEquals(OK_STATUS, response.getStatus());

      final EmailConfiguration emailConfigNew = emailResource.getEmailConfig();
      assertTrue(emailConfigOriginal.equals(emailConfigNew));
    }

  }

  public void testSendEmailTest() throws Exception {

  }
}
