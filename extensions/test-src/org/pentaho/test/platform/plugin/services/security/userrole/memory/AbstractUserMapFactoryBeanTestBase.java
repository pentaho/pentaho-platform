/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.security.userrole.memory;

/**
 * Superclass of UserMap factory bean tests.
 * 
 * @author mlowery
 */
public class AbstractUserMapFactoryBeanTestBase {
  protected String userMapText;

  protected void setUp() throws Exception {
    StringBuffer buf = new StringBuffer();
    buf.append( "admin=password,ROLE_ADMINISTRATOR,ROLE_AUTHENTICATED\n" ) //$NON-NLS-1$
        .append( "suzy=password,ROLE_POWER_USER,ROLE_AUTHENTICATED\n" ) //$NON-NLS-1$
        .append( "pat=password,ROLE_BUSINESS_ANALYST,ROLE_AUTHENTICATED\n" ) //$NON-NLS-1$
        .append( "tiffany=password,ROLE_REPORT_AUTHOR,ROLE_AUTHENTICATED\n" ); //$NON-NLS-1$
    userMapText = buf.toString();
  }
}
