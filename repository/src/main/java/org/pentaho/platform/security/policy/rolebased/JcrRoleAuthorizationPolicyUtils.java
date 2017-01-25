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

package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.NodeHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.util.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;

public class JcrRoleAuthorizationPolicyUtils {

  protected boolean isInNamespace( final String namespace, final String namespacedAction ) {
    if ( namespace == null ) {
      return true;
    }
    final String DOT = "."; //$NON-NLS-1$
    int lastIndexOfDot = namespacedAction.lastIndexOf( DOT );
    // there is no dot; therefore there is no namespace; return true if namespace is null or empty
    if ( lastIndexOfDot == -1 ) {
      return !StringUtils.hasText( namespace );
    } else {
      return namespace.equals( namespacedAction.substring( 0, lastIndexOfDot ) );
    }
  }

  public static void internalSetBindings( final PentahoJcrConstants pentahoJcrConstants,
      final Node runtimeRolesFolderNode, final String runtimeRoleNodeName, final List<String> logicalRoleNames, final String nodeNamePrefix )
    throws RepositoryException {
    Node runtimeRoleNode = null;
    if ( NodeHelper.hasNode( runtimeRolesFolderNode, nodeNamePrefix, runtimeRoleNodeName ) ) {
      runtimeRoleNode = NodeHelper.getNode( runtimeRolesFolderNode, nodeNamePrefix, runtimeRoleNodeName );
    } else {
      runtimeRoleNode = NodeHelper.addNode( runtimeRolesFolderNode, nodeNamePrefix, runtimeRoleNodeName );
    }
    // clear all existing properties
    if ( runtimeRoleNode.hasProperty( pentahoJcrConstants.getPHO_BOUNDROLES() ) ) {
      runtimeRoleNode.getProperty( pentahoJcrConstants.getPHO_BOUNDROLES() ).remove();
    }
    runtimeRoleNode.setProperty( pentahoJcrConstants.getPHO_BOUNDROLES(), logicalRoleNames.toArray( new String[0] ) );
  }

}
