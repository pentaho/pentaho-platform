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

import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledOrJndiDatasourceService;
import org.pentaho.platform.engine.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.text.MessageFormat;

/**
 * This class provides the foundation for combining a users' tenant ID with the datasource name being requested at
 * runtime. The concept is this - - -
 * 
 * a- Build against a datasource like "Customers" or "Products"
 * 
 * b- At runtime, the users' tenant ID is substituted in a pattern to retrieve the Datasource with the Tenant ID
 * 
 * For example: User=admin, Tenant=ABC_COMPANY Requested Datasource: Customers Actual Returned Datasource:
 * ABC_COMPANY-Datasource
 * 
 * When admin runs a report that uses the datasource Customers, subclassers will use the tenant-ID and the
 * datasource name to fulfill the request -
 * 
 * @author mbatchelor
 * 
 */

public abstract class AbstractTenantAwareDatasourceService extends PooledOrJndiDatasourceService implements
    InitializingBean {

  private String datasourceNameFormat = "{0}-{1}"; // {0} is always the tenant ID, {1} is always the requested
                                                   // datasource.

  private boolean requireTenantId; // if tenant not found in session, throw an exception

  @Override
  public void afterPropertiesSet() throws Exception {
    // Check to see if the format is valid - must have a {0} and {1} in it.
    if ( ( this.datasourceNameFormat.indexOf( "{0}" ) < 0 ) || ( this.datasourceNameFormat.indexOf( "{1}" ) < 0 ) ) {
      throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
          "TenantAwareDatasourceService.ERROR_0001_NAME_FORMAT_ILLEGAL" ) );
    }
  }

  @Override
  public DataSource getDataSource( String dsName ) throws DBDatasourceServiceException {
    String tenantId = getTenantId();
    if ( tenantId == null ) { // If tenant ID is null, what should it do?
      if ( isRequireTenantId() ) {
        throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
            "TenantAwareDatasourceService.ERROR_0002_TENANT_ID_REQUIRED" ) );
      }
      return super.getDataSource( dsName ); // If no tenant ID found and it's not required, get originally
                                            // requested
                                            // datasource
    } else {
      return super.getDataSource( MessageFormat.format( getDatasourceNameFormat(), tenantId, dsName ) ); // Get the
                                                                                                         // tenant-ds
                                                                                                         // instead
    }
  }

  /**
   * This abstract method must be implemented by subclasses - this should return a string containing the tenant's
   * ID.
   * 
   * @return String ID of the Tenant
   */
  public abstract String getTenantId();

  /******************** Getters and Setters ***************************/

  public void setDatasourceNameFormat( String value ) {
    this.datasourceNameFormat = value;
  }

  public String getDatasourceNameFormat() {
    return this.datasourceNameFormat;
  }

  public void setRequireTenantId( boolean value ) {
    this.requireTenantId = value;
  }

  public boolean isRequireTenantId() {
    return this.requireTenantId;
  }

}
