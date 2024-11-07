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

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.NT;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;

// Equivalent CND:
// [pho_nt:pentahoInternalFolder] > nt:base, mix:referenceable
// // copy and paste of nt:unstructed with the following changes:
// // 1. prohibit same-name siblings (indicated by absence of keyword 'multiple')
// // 2. internal folders never need to be versioned (indicated by 'ignore')
//     orderable
//     - * (undefined) multiple ignore
//     - * (undefined)
//     + * (nt:base) = nt:unstructured ignore
public class InternalFolderNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings( "unchecked" )
  @Override
  public NodeTypeDefinition getNodeTypeDefinition( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName( PHO_NT + "pentahoInternalFolder" ); //$NON-NLS-1$
    t.setOrderableChildNodes( true );
    t.getPropertyDefinitionTemplates().add( getWildcardMultipleProperty( ntMgr, vFac ) );
    t.getPropertyDefinitionTemplates().add( getWildcardProperty( ntMgr, vFac ) );
    t.getNodeDefinitionTemplates().add( getWildcardNode( ntMgr, vFac ) );
    return t;
  }

  private NodeDefinitionTemplate getWildcardNode( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( "*" ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { NT + "base" } ); //$NON-NLS-1$
    t.setDefaultPrimaryTypeName( NT + "unstructured" ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.IGNORE );
    t.setSameNameSiblings( false );
    return t;
  }

  private PropertyDefinitionTemplate getWildcardProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( "*" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.UNDEFINED );
    t.setOnParentVersion( OnParentVersionAction.IGNORE );
    t.setMultiple( true );
    return t;
  }

  private PropertyDefinitionTemplate getWildcardMultipleProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( "*" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.UNDEFINED );
    t.setOnParentVersion( OnParentVersionAction.IGNORE );
    t.setMultiple( false );
    return t;
  }

}
