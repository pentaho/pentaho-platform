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
