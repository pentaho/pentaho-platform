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
