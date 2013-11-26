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

package org.pentaho.platform.plugin.action.mondrian.catalog;

import mondrian.olap.Util;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Simplification of XMLA-specific DataSourcesConfig.Catalog. Should be immutable.
 * 
 * @author mlowery
 */
public class MondrianCatalog implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;

  private String dataSourceInfo; // optionally overrides this.dataSource.dataSourceInfo

  private String definition;

  private MondrianSchema schema;

  // We will hold the extra information not directly related to XMLA properties
  private MondrianCatalogComplementInfo mondrianCatalogComplementInfo;

  public MondrianCatalog( final String name, final String dataSourceInfo, final String definition,
      final MondrianSchema schema ) {
    this( name, dataSourceInfo, definition, schema, new MondrianCatalogComplementInfo() );
  }

  public MondrianCatalog( final String name, final String dataSourceInfo, final String definition,
      final MondrianSchema schema, final MondrianCatalogComplementInfo mondrianCatalogComplementInfo ) {
    this.name = name;
    this.dataSourceInfo = dataSourceInfo;
    this.definition = definition;
    this.schema = schema;
    this.mondrianCatalogComplementInfo = mondrianCatalogComplementInfo;
  }

  public String getName() {
    return name;
  }

  public String getDefinition() {
    return definition;
  }

  public String getDataSourceInfo() {
    return dataSourceInfo;
  }

  public boolean isJndi() {
    return getJndi() != null;
  }

  public String getJndi() {
    return Util.parseConnectString( dataSourceInfo ).get( "DataSource" );
  }

  public MondrianSchema getSchema() {
    return schema;
  }

  public MondrianCatalogComplementInfo getMondrianCatalogComplementInfo() {
    return mondrianCatalogComplementInfo;
  }

  @Override
  public String toString() {
    return new ToStringBuilder( this ).append( "name", name ).append( "dataSourceInfo", dataSourceInfo ).append(
      //$NON-NLS-1$//$NON-NLS-2$
      "definition", definition ).append( "schema", schema ).toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
