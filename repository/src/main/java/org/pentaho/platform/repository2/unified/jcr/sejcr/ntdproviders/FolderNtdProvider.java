/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import org.pentaho.platform.repository2.unified.jcr.sejcr.NodeTypeDefinitionProvider;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.version.OnParentVersionAction;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.NT;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;

// Equivalent CND:
// [pho_nt:pentahoFolder] > nt:folder, pho_nt:pentahoHierarchyNode
//     // pentahoInternalFolders are never versioned
//     + * (pho_nt:pentahoInternalFolder) ignore
public class FolderNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings( "unchecked" )
  @Override
  public NodeTypeDefinition getNodeTypeDefinition( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName( PHO_NT + "pentahoFolder" ); //$NON-NLS-1$
    t.setDeclaredSuperTypeNames( new String[] { NT + "folder", PHO_NT + "pentahoHierarchyNode" } ); //$NON-NLS-1$ //$NON-NLS-2$
    t.getNodeDefinitionTemplates().add( getInternalFolderNode( ntMgr, vFac ) );
    return t;
  }

  private NodeDefinitionTemplate getInternalFolderNode( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( "*" ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { PHO_NT + "pentahoInternalFolder" } ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.IGNORE );
    return t;
  }

}
