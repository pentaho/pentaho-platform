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

package org.pentaho.platform.repository2.unified.jcr;

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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * User: pminutillo
 * Date: 2/12/13
 * Time: 4:10 PM
 */

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid.Type;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This default acl handler sets the default security to Authenticated ALL for specific shared paths specified by a
 * list in the configuration, defaulting to just databases at this time.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class SharedObjectsDefaultAclHandler extends InheritDefaultAclHandler {

  String authenticatedRoleName = "Authenticated";
  List<String> sharedObjectPaths; // "{0}/etc/pdi/databases"

  public SharedObjectsDefaultAclHandler() {
    sharedObjectPaths = new ArrayList<String>();
    sharedObjectPaths.add( "{0}/etc/pdi/databases" );
  }

  public SharedObjectsDefaultAclHandler( String authenticatedRoleName, List<String> sharedObjectPaths ) {
    this.authenticatedRoleName = authenticatedRoleName;
    this.sharedObjectPaths = sharedObjectPaths;
  }

  /**
   * Logic to determine if we should use default Authenticated permission vs. Inheriting permission. This is the
   * approach we take with Shared Objects in the DI Server.
   * 
   * @param file
   *          repository file to examine
   * @return whether this file is in a shared object path
   * 
   */
  protected boolean applyAuthRule( RepositoryFile file ) {
    ITenant tenant = JcrTenantUtils.getTenant();
    for ( String path : sharedObjectPaths ) {
      String substitutedPath = MessageFormat.format( path, tenant.getRootFolderAbsolutePath() );
      if ( file.getPath() != null && file.getPath().startsWith( substitutedPath ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine the correct default acls and return it.
   * 
   * @return default acls
   */
  @Override
  public RepositoryFileAcl createDefaultAcl( RepositoryFile repositoryFile ) {
    if ( applyAuthRule( repositoryFile ) ) {
      // if the auth name is not specified in the config, create an acl without an ace
      if ( authenticatedRoleName == null || authenticatedRoleName.trim().length() == 0 ) {
        return new RepositoryFileAcl.Builder( PentahoSessionHolder.getSession().getName() ).entriesInheriting( false )
            .build();
      } else {
        // if an auth is defined, create an acl with the ace
        RepositoryFileSid tenantAuthenticatedRoleSid = new RepositoryFileSid( authenticatedRoleName, Type.ROLE );
        return new RepositoryFileAcl.Builder( PentahoSessionHolder.getSession().getName() ).entriesInheriting( false )
            .ace( tenantAuthenticatedRoleSid, EnumSet.of( RepositoryFilePermission.ALL ) ).build();
      }
    } else {
      return super.createDefaultAcl( repositoryFile );
    }
  }
}
