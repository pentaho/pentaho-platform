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

import java.util.ArrayList;
import java.util.Arrays;

public class MondrianOneToOneUserRoleListMapper extends MondrianAbstractPlatformUserRoleMapper implements
    InitializingBean {
  protected boolean failOnEmptyRoleList = true;

  /**
   * This mapper maps directly from a Pentaho to a Mondrian role. This is useful when your roles exist both in the
   * platform and identically named in the mondrian catalog.
   */
  protected String[] mapRoles( String[] mondrianRoles, String[] platformRoles ) throws PentahoAccessControlException {
    //
    // This class assumes that the platform roles list contains roles that
    // are defined in the mondrian schema. All roles that the user has
    // that are defined in the mondrian schema are returned.
    //
    // Note - this mapper doesn't need the mondrian catalog to do the mapping
    ArrayList<String> rtnRoles = new ArrayList<String>();
    int posn;
    // For each platform role...
    for ( int i = 0; i < platformRoles.length; i++ ) {
      // Find the role in the Mondrian roles for this catalog
      posn = Arrays.binarySearch( mondrianRoles, platformRoles[i] );
      if ( posn >= 0 ) {
        // For each one found, add it to the returned array of roles
        rtnRoles.add( mondrianRoles[posn] );
      }
    }
    if ( rtnRoles.size() > 0 ) {
      return rtnRoles.toArray( new String[rtnRoles.size()] );
    } else if ( failOnEmptyRoleList ) {
      throw new PentahoAccessControlException( Messages.getInstance().getErrorString(
          "MondrianOneToOneUserRoleListMapper.ERROR_001_NO_CORRESPONDENCE" ) ); //$NON-NLS-1$
    } else {
      return null;
    }
  }

  public void setFailOnEmptyRoleList( boolean failOnEmptyRoleList ) {
    this.failOnEmptyRoleList = failOnEmptyRoleList;
  }

  public boolean isFailOnEmptyRoleList() {
    return failOnEmptyRoleList;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // No op.
  }
}
