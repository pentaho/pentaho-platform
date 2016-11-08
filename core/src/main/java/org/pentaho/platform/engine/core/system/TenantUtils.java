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

package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;

/**
 * Utilities relating to multi-tenancy.
 * 
 * @author mlowery
 */
public class TenantUtils {

  /**
   * TODO mlowery make this configurable
   */
  public static final String TENANTID_SINGLE_TENANT = "tenant0"; //$NON-NLS-1$

  /**
   * Returns the tenant ID of the current user.
   */
  public static ITenant getCurrentTenant() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if ( pentahoSession == null ) {
      throw new IllegalStateException();
    }

    String tenantId = (String) pentahoSession.getAttribute( IPentahoSession.TENANT_ID_KEY );

    if ( tenantId == null ) {
      ITenantedPrincipleNameResolver tenantedUserNameUtils =
          PentahoSystem.get( ITenantedPrincipleNameResolver.class, "tenantedUserNameUtils", pentahoSession );
      if ( tenantedUserNameUtils != null ) {
        ITenant tenant = tenantedUserNameUtils.getTenant( pentahoSession.getId() );
        pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
        return new Tenant( tenant.getId(), true );
      }
    }

    return new Tenant( tenantId, true );
  }

  public static String getDefaultTenant() {
    return TENANTID_SINGLE_TENANT;
  }

  public static boolean isAccessibleTenant( ITenant tenant ) {
    ITenant currentTenant = TenantUtils.getCurrentTenant();
    try {
      return currentTenant.getId() == null
          || tenant.getRootFolderAbsolutePath().startsWith(
              currentTenant.getRootFolderAbsolutePath() + RepositoryFile.SEPARATOR ) || tenant.equals( currentTenant );
    } catch ( NullPointerException ex ) {
    }
    return false;
  }
}
