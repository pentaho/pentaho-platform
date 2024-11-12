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


package org.pentaho.platform.api.mt;

import java.io.Serializable;

/**
 * A tenant of the Pentaho platform. Contains name, path and ID of a tenant.
 * This interface should be implemented if you need create your own tenant.
 * Tenants are used for implementing the security mechanism. 
 * 
 * @author rmansoor
 */

public interface ITenant extends Serializable {

  /**
   * The method is used for retrieving ID of the tenant.
   * @return Returns String representation of the tenant's ID. 
   */
  public String getId();

  /**
   * The method gets the absolute path to the tenant root folder.
   * Folders for the data protected by current tenant (including the users' home folders) should be
   *  located under the tenant's root folder. 
   * @return Returns the absolute path to the tenant's root folder. 
   */
  public String getRootFolderAbsolutePath();

  /**
   * This method gets the tenant's name.
   * @return Returns the tenant's name.
   */
  public String getName();

  /**
   * The method validates whether the tenant is enabled or not. 
   * @return Returns true if the tenant is enabled.
   */
  boolean isEnabled();

}
