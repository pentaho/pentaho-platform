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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp.tenantaware;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.beans.factory.InitializingBean;

/**
 * This reference implementation parses the users' login name to derive the Tenant ID. This can come in two forms:
 * 
 * 1- Tenant First - for example, tenantid\mbatchelor (an LDAP-like ID)
 * 
 * 2- Tenant Last - for example: mbatchelor@pentaho.com (an e-mail like ID)
 * 
 * @author mbatchelor
 * 
 */

public class TenantAwareLoginParsingDatasourceService extends AbstractTenantAwareDatasourceService implements
    InitializingBean {

  private String tenantSeparator = "@"; // The separator to split on
  private boolean tenantOnLeft; // Whether the tenant ID can be found on the left or the right of the string.

  @Override
  public String getTenantId() {
    // Retrieve the session and get the user id.
    IPentahoSession session = PentahoSessionHolder.getSession();
    String id = session.getName();
    String rtn = null;
    if ( id != null ) { // No ID - bail out here
      if ( id.indexOf( getTenantSeparator() ) >= 0 ) {
        // Only gets here if the userid is non-null and has the tenantSeparator
        String[] bits = id.split( getTenantSeparator() ); // split the field
        if ( isTenantOnLeft() ) { // get the 0th or the 1st element depending
          rtn = bits[0];
        } else {
          rtn = bits[1];
        }
      }
    }
    return rtn;
  }

  /******************** Getters and Setters ***************************/

  public void setTenantOnLeft( boolean value ) {
    this.tenantOnLeft = value;
  }

  public boolean isTenantOnLeft() {
    return this.tenantOnLeft;
  }

  public void setTenantSeparator( String value ) {
    this.tenantSeparator = value;
  }

  public String getTenantSeparator() {
    return this.tenantSeparator;
  }

}
