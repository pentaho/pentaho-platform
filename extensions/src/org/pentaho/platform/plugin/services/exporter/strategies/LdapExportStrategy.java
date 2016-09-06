package org.pentaho.platform.plugin.services.exporter.strategies;

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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;

import java.util.List;

/**
 * Strategy that exports users' roles from LDAP
 *
 * @author Andrei Abramov
 */
public class LdapExportStrategy implements IExportStrategy {
  @Override
  public void exportUsersAndRoles( ExportManifest exportManifest ) {

    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    List<String> roles = userRoleListService.getAllRoles();

    ExportStrategyUtil.exportRoles( exportManifest, roles );
  }
}
