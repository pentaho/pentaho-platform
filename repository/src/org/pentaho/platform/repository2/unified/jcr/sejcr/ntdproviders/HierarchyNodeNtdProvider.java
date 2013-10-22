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

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.*;

// Equivalent CND:
// [pho_nt:pentahoHierarchyNode] > nt:hierarchyNode, mix:referenceable
//     + pho:title (pho_nt:localizedString) copy
//     + pho:description (pho_nt:localizedString) copy
//     + pho:metadata (nt:unstructured) copy
//     + pho:locales (pho_nt:locale) copy
//     - pho:hidden (boolean) = false copy
public class HierarchyNodeNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings( "unchecked" )
  @Override
  public NodeTypeDefinition getNodeTypeDefinition( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName( PHO_NT + "pentahoHierarchyNode" ); //$NON-NLS-1$
    t.setDeclaredSuperTypeNames( new String[] { NT + "hierarchyNode", MIX + "referenceable" } ); //$NON-NLS-1$//$NON-NLS-2$
    t.getNodeDefinitionTemplates().add( getTitleNode( ntMgr ) );
    t.getNodeDefinitionTemplates().add( getDescriptionNode( ntMgr ) );
    t.getNodeDefinitionTemplates().add( getMetadataNode( ntMgr ) );
    t.getNodeDefinitionTemplates().add( getLocaleNode( ntMgr ) );
    t.getPropertyDefinitionTemplates().add( getHiddenProperty( ntMgr, vFac ) );
    return t;
  }

  private PropertyDefinitionTemplate getHiddenProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( PHO + "hidden" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.BOOLEAN );
    t.setDefaultValues( new Value[] { vFac.createValue( false ) } );
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setMultiple( false );
    return t;
  }

  private NodeDefinitionTemplate getTitleNode( final NodeTypeManager ntMgr ) throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( PHO + "title" ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { PHO_NT + "localizedString" } ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setSameNameSiblings( false );
    return t;
  }

  private NodeDefinitionTemplate getDescriptionNode( final NodeTypeManager ntMgr ) throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( PHO + "description" ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { PHO_NT + "localizedString" } ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setSameNameSiblings( false );
    return t;
  }

  private NodeDefinitionTemplate getMetadataNode( final NodeTypeManager ntMgr ) throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( PHO + "metadata" ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { NT + "unstructured" } ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setSameNameSiblings( false );
    return t;
  }

  private NodeDefinitionTemplate getLocaleNode( final NodeTypeManager ntMgr ) throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( PHO + "locales" ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { PHO_NT + "locale" } ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setSameNameSiblings( false );
    return t;
  }
}
