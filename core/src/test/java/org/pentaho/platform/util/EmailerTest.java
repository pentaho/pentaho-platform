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

package org.pentaho.platform.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EmailerTest {

  private Emailer emailer = new Emailer();

  @Test
  public void setTo_NullTest() {
    emailer.setTo( null );
    assertNull( emailer.getProperties().getProperty( "to" ) );
  }

  @Test
  public void setTo_ValidTest() {
    emailer.setTo( "to@domain.com" );
    assertEquals( "to@domain.com", emailer.getProperties().getProperty( "to" ) );
  }

  @Test
  public void setTo_ReplaceToCommaTest() {
    emailer.setTo( "to@domain.com; another_to@domain.com" );
    assertEquals( "to@domain.com, another_to@domain.com", emailer.getProperties().getProperty( "to" ) );
  }

  @Test
  public void setCc_NullTest() {
    emailer.setCc( null );
    assertNull( emailer.getProperties().getProperty( "cc" ) );
  }

  @Test
  public void setCc_Test() {
    emailer.setCc( "cc@domain.com" );
    assertEquals( "cc@domain.com", emailer.getProperties().getProperty( "cc" ) );
  }

  @Test
  public void setCc_ReplaceToCommaTest() {
    emailer.setCc( "cc@domain.com; another_cc@domain.com" );
    assertEquals( "cc@domain.com, another_cc@domain.com", emailer.getProperties().getProperty( "cc" ) );
  }

  @Test
  public void setBcc_NullTest() {
    emailer.setBcc( null );
    assertNull( emailer.getProperties().getProperty( "bcc" ) );
  }

  @Test
  public void setBcc_ValidTest() {
    emailer.setBcc( "bcc@domain.com" );
    assertEquals( "bcc@domain.com", emailer.getProperties().getProperty( "bcc" ) );
  }

  @Test
  public void setBcc_ReplaceToCommaTest() {
    emailer.setBcc( "bcc@domain.com; another_bcc@domain.com" );
    assertEquals( "bcc@domain.com, another_bcc@domain.com", emailer.getProperties().getProperty( "bcc" ) );
  }
}
