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

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;

// Equivalent CND:
// [pho_nt:localizedString]
//     - pho:rootLocale (string) copy
//     - * (string) copy
public class LocalizedStringNtdProvider implements NodeTypeDefinitionProvider {

  @SuppressWarnings( "unchecked" )
  @Override
  public NodeTypeDefinition getNodeTypeDefinition( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName( PHO_NT + "localizedString" ); //$NON-NLS-1$
    t.getPropertyDefinitionTemplates().add( getRootLocaleProperty( ntMgr, vFac ) );
    t.getPropertyDefinitionTemplates().add( getWildcardStringProperty( ntMgr, vFac ) );
    return t;
  }

  private PropertyDefinitionTemplate getRootLocaleProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( PHO + "rootLocale" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.STRING );
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setMultiple( false );
    return t;
  }

  private PropertyDefinitionTemplate getWildcardStringProperty( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    PropertyDefinitionTemplate t = ntMgr.createPropertyDefinitionTemplate();
    t.setName( "*" ); //$NON-NLS-1$
    t.setRequiredType( PropertyType.STRING );
    t.setOnParentVersion( OnParentVersionAction.COPY );
    return t;
  }

}
