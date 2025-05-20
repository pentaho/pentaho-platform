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

  public Util.PropertyList getConnectProperties() {
    return Util.parseConnectString( dataSourceInfo );
  }

  @Override
  public String toString() {
    return new ToStringBuilder( this ).append( "name", name ).append( "dataSourceInfo", dataSourceInfo ).append(
      //$NON-NLS-1$//$NON-NLS-2$
      "definition", definition ).append( "schema", schema ).toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
