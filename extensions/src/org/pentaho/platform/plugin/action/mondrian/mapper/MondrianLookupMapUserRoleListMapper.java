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

package org.pentaho.platform.plugin.action.mondrian.mapper;

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

import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MondrianLookupMapUserRoleListMapper extends MondrianAbstractPlatformUserRoleMapper implements
    InitializingBean {

  private Map<String, String> lookupMap;
  protected boolean failOnEmptyRoleList = true;

  /**
   * This version of the mapper uses a map that would be supplied in the spring configuration file. For every platform
   * role that the user has, it looks into the supplied map for the corresponding role name. Then, the resulting role
   * gets bounced against the roles in the given catalog and only those roles are returned.
   * 
   * Example: Platform Role Mondrian Role Mondrian Catalog(s) Sales M_SALES Accting_Cat, Sales_Cat Marketing M_MKTING
   * Sales_Cat CEO CTO Engineering M_ENG Eng_Cat, Presales_Cat Support M_ENG Presales_Cat, Services_Cat Services M_SVCS
   * Presales_Cat
   * 
   * If user admin has Sales , and CEO as roles, he connects to the Accting_Cat, or Sales_Cat catalogs, he will be
   * connected with the mondrian role M_SALES.
   * 
   * If user Tiffany is in the Engineering and Services roles, and connects to the Presales_Cat catalog, she will
   * receive the role M_ENG,M_SVCS (which mondrian will resolve additively).
   * 
   */
  protected String[] mapRoles( String[] mondrianRoles, String[] platformRoles ) throws PentahoAccessControlException {
    // This mapper doesn't need the mondrian catalog to do the mapping
    final ArrayList<String> mappedRolesList = new ArrayList<String>();
    if ( ( mondrianRoles != null ) && ( platformRoles != null ) ) {
      for ( int i = 0; i < platformRoles.length; i++ ) {
        final String aRole = lookupMap.get( platformRoles[i] ); // Find what the platform role maps to in all of
                                                                // mondrian
        if ( aRole != null ) { // OK, we found it.
          int foundIdx = Arrays.binarySearch( mondrianRoles, aRole ); // For this model, does the mapped entity exist?
          if ( foundIdx >= 0 ) { // >=0 means we have it.
            mappedRolesList.add( aRole );
          }
        }
      }
    }
    if ( mappedRolesList.size() > 0 ) {
      // We were able to map the roles - return the mappings.
      return mappedRolesList.toArray( new String[mappedRolesList.size()] );
    } else if ( failOnEmptyRoleList ) {
      throw new PentahoAccessControlException( Messages.getInstance().getErrorString(
          "MondrianOneToOneUserRoleListMapper.ERROR_001_NO_CORRESPONDENCE" ) ); //$NON-NLS-1$
    } else {
      return null;
    }
  }

  public void setLookupMap( Map<String, String> value ) {
    this.lookupMap = value;
  }

  public Map<String, String> getLookupMap() {
    return this.lookupMap;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( this.lookupMap );
  }

  public void setFailOnEmptyRoleList( boolean failOnEmptyRoleList ) {
    this.failOnEmptyRoleList = failOnEmptyRoleList;
  }

  public boolean isFailOnEmptyRoleList() {
    return failOnEmptyRoleList;
  }
}
