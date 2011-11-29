/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * @created Nov 12, 2011
 * @author Ramaiz Mansoor
 */

package org.pentaho.platform.datasource;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class ActionBasedSecurityService {
  private IAuthorizationPolicy policy;

  private static final String ACTION_READ = "org.pentaho.repository.read"; //$NON-NLS-1$

  private static final String ACTION_CREATE = "org.pentaho.repository.create"; //$NON-NLS-1$

  private static final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity"; //$NON-NLS-1$

  public ActionBasedSecurityService() {
    policy = PentahoSystem.get(IAuthorizationPolicy.class);
  }
  public ActionBasedSecurityService(IAuthorizationPolicy policy) {
    this.policy = policy;
  }
  
  public void checkAdministratorAccess() throws PentahoAccessControlException{
    boolean access = policy.isAllowed(ACTION_READ) && policy.isAllowed(ACTION_CREATE)
    && policy.isAllowed(ACTION_ADMINISTER_SECURITY);
    if(!access) {
      throw new 
      PentahoAccessControlException("You have to be admin to perform this operation");
    }
  }
  
}
