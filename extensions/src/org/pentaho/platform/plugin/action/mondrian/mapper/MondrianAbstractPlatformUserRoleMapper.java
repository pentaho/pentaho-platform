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
 * Created December 12, 2009
 * @author Marc Batchelor
 */
package org.pentaho.platform.plugin.action.mondrian.mapper;

import java.util.Arrays;

import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

/**
 * @author mbatchelor
 *
 */
public abstract class MondrianAbstractPlatformUserRoleMapper implements IConnectionUserRoleMapper {

  public MondrianAbstractPlatformUserRoleMapper() {
    
  }
  
  /**
   * Subclasses simply need to implement this one method to do the specific
   * mapping desired.
   * 
   * @param mondrianRoles Sorted list of roles defined in the catalog
   * @param platformRoles Sorted list of the roles defined in the catalog
   * @return
   */
  protected abstract String[] mapRoles(String[] mondrianRoles, String[] platformRoles);
  
  /**
   * This method returns the role names as found in the Mondrian schema. The returned names
   * must be ordered (sorted) or code down-stream will not work.
   * @param userSession Users' session
   * @param catalogName The name of the catalog
   * @return Array of role names from the schema file
   */
  protected String[] getMondrianRolesFromCatalog(IPentahoSession userSession, String context) {
    String[] rtn = null; 
    // Get the catalog service
    IMondrianCatalogService catalogService = PentahoSystem.get(IMondrianCatalogService.class);
    if (catalogService != null) {
      // Get the catalog by name
      MondrianCatalog catalog = catalogService.getCatalog(context, userSession);
      if (catalog != null) {
        // The roles are in the schema object
        MondrianSchema schema = catalog.getSchema();
        if (schema != null) {
          // Ask the schema for the role names array
          String[] roleNames = schema.getRoleNames();
          if ( (roleNames != null) && (roleNames.length>0) ) {
            // Return the roles from the schema
            Arrays.sort(roleNames);
            return roleNames;
          }
        }
      }
    }
    // Sort the returned list of roles.
    return rtn;
  }

  /**
   * This method returns the users' roles as specified in the Spring Security
   * authentication object. The role names returned must be sorted for other
   * code downstream to work properly.
   * @param session The users' session
   * @return Users' roles as defined in the authentication object
   */
  protected String[] getPlatformRolesFromSession(IPentahoSession session) {
    // Get the Spring Security authentication object
    Authentication auth = SecurityHelper.getAuthentication();
    String[] rtn = null;
    // Get the authorities
    GrantedAuthority[] gAuths = auth.getAuthorities();
    if ((gAuths != null) && (gAuths.length > 0) ) {
      // Copy role names out of the Authentication 
      rtn = new String[gAuths.length];
      for (int i=0; i<gAuths.length; i++) {
        rtn[i] = gAuths[i].getAuthority();
      }
      // Sort the returned list of roles
      Arrays.sort(rtn);
    }
    return rtn;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IConnectionUserRoleMapper#mapConnectionRoles(org.pentaho.platform.api.engine.IPentahoSession, java.lang.String)
   */
  public String[] mapConnectionRoles(IPentahoSession userSession, String connectionContext)
      throws PentahoAccessControlException {
    // The connectionContextName for this mapper is the Mondrian Catalog.
    String[] mondrianRoleNames = getMondrianRolesFromCatalog(userSession, connectionContext);
    String[] platformRoleNames = getPlatformRolesFromSession(userSession);
    String[] mappedResult = null;
    if ( (mondrianRoleNames != null) && (platformRoleNames != null) && 
        (mondrianRoleNames.length > 0) && (platformRoleNames.length >0) ) {
      mappedResult = mapRoles(mondrianRoleNames, platformRoleNames);
    }
    return mappedResult;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IConnectionUserRoleMapper#mapConnectionUser(org.pentaho.platform.api.engine.IPentahoSession, java.lang.String)
   */
  public Object mapConnectionUser(IPentahoSession userSession, String context)
      throws PentahoAccessControlException {
    throw new UnsupportedOperationException();
  }

}
