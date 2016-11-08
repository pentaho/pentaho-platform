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
