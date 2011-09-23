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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

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

  private MondrianDataSource dataSource;

  private MondrianSchema schema;


  // We will hold the extra information not directly related to XMLA properties
  private MondrianCatalogComplementInfo mondrianCatalogComplementInfo;

  private String whereCondition;


  public MondrianCatalog(final String name, final String dataSourceInfo, final String definition,
      final MondrianDataSource dataSource, final MondrianSchema schema) {

    this(name, dataSourceInfo, definition, dataSource, schema, new MondrianCatalogComplementInfo());
  }


  public MondrianCatalog(final String name, final String dataSourceInfo, final String definition,
                         final MondrianDataSource dataSource, final MondrianSchema schema,
                         final MondrianCatalogComplementInfo mondrianCatalogComplementInfo) {

    this.name = name;
    this.dataSourceInfo = dataSourceInfo;
    this.definition = definition;
    this.dataSource = dataSource;
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

  public MondrianSchema getSchema() {
    return schema;
  }

  public MondrianCatalogComplementInfo getMondrianCatalogComplementInfo() {
    return mondrianCatalogComplementInfo;
  }

  /**
   * Returns dataSource with overridden dataSourceInfo (if any). 
   */
  public MondrianDataSource getEffectiveDataSource() {
    if (null != dataSourceInfo) {
      return new MondrianDataSource(dataSource, dataSourceInfo);
    } else {
      return dataSource;
    }
  }

  public MondrianDataSource getDataSource() {
    return dataSource;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", name).append("dataSourceInfo", dataSourceInfo).append( //$NON-NLS-1$//$NON-NLS-2$
        "definition", definition).append("dataSource", dataSource).append("schema", schema).toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
