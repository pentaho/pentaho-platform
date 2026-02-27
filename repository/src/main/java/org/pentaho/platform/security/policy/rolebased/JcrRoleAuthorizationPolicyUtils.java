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
