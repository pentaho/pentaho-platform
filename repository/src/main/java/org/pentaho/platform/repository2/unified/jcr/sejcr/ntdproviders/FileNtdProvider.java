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
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.*;

// Equivalent CND:
// [pho_nt:pentahoFile] > nt:file, pho_nt:pentahoHierarchyNode
// strangely, nt:file's don't have lastModified; instead this attribute is on nt:resource; however not all pentaho
// files have an nt:resource; so we put the lastModified date on the nt:file node itself
//     - pho:lastModified (date) mandatory ignore
//     - pho:contentType (string) mandatory copy
//     - pho:fileSize (long) copy
public class FileNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings( "unchecked" )
  @Override
  public NodeTypeDefinition getNodeTypeDefinition( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName( PHO_NT + "pentahoFile" ); //$NON-NLS-1$
    t.setDeclaredSuperTypeNames( new String[] { NT + "file", PHO_NT + "pentahoHierarchyNode" } ); //$NON-NLS-1$ //$NON-NLS-2$
    t.getPropertyDefinitionTemplates().add( getLastModifiedProperty( ntMgr, vFac ) );
    t.getPropertyDefinitionTemplates().add( getContentTypeProperty( ntMgr, vFac ) );
    t.getPropertyDefinitionTemplates().add( getFileSizeProperty( ntMgr, vFac ) );
    return t;
  }

  private PropertyDefinitionTemplate getLastModifiedProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( PHO + "lastModified" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.DATE );
    t.setMandatory( true );
    t.setOnParentVersion( OnParentVersionAction.IGNORE );
    t.setMultiple( false );
    return t;
  }

  private PropertyDefinitionTemplate getContentTypeProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( PHO + "contentType" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.STRING );
    t.setMandatory( true );
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setMultiple( false );
    return t;
  }

  private PropertyDefinitionTemplate getFileSizeProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( PHO + "fileSize" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.LONG );
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setMultiple( false );
    return t;
  }

}
