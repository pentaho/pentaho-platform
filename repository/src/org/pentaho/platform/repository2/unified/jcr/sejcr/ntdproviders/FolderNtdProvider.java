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
