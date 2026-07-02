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
import org.pentaho.platform.engine.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;

/**
 * This reference implementation retrieves the tenant ID from a variable in the users' session.
 * 
 * The only option here is the name of the session variable.
 * 
 * @author mbatchelor
 * 
 */

public class TenantAwareSessionVariableDatasourceService extends AbstractTenantAwareDatasourceService implements
    InitializingBean {

  private String tenantSessionVariableName;

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    // Double-check that the session variable name is set
    if ( this.tenantSessionVariableName == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "TenantAwareDatasourceService.ERROR_0003_SESSION_VARIABLE_NAME_REQUIRED" ) );
    }
  }

  @Override
  public String getTenantId() {
    IPentahoSession session = PentahoSessionHolder.getSession(); // get the session
    Object tenantId = session.getAttribute( getTenantSessionVariableName() ); // get the variable
    if ( tenantId != null ) {
      return tenantId.toString(); // Convert to string
    } else {
      return null; // Return null if not in the session.
    }
  }

  /******************** Getters and Setters ***************************/

  public void setTenantSessionVariableName( String value ) {
    this.tenantSessionVariableName = value;
  }

  public String getTenantSessionVariableName() {
    return this.tenantSessionVariableName;
  }

}
