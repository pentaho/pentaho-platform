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
