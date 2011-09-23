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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.util.Pair;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simplification of XMLA-specific DataSourcesConfig.DataSource. Should be immutable.
 * 
 * @author mlowery
 */
public class MondrianDataSource implements Serializable {
  
  private static final long serialVersionUID = 1L;

  private String name;

  private String description;

  /**
   * XMLA only. The unique path that shows where to invoke the XMLA methods for this data source.
   */
  private String url;

  private String dataSourceInfo;

  /**
   * XMLA only. The name of the provider behind the data source.
   */
  private String providerName;

  /**
   * XMLA only. 
   * <p />
   * <ul>
   * <li>PROVIDER_TYPE_TDP: tabular data provider</li>
   * <li>PROVIDER_TYPE_MDP: multidimensional data provider</li>
   * <li>PROVIDER_TYPE_DMP: data mining provider</li>
   * </ul>
   */
  private String providerType;

  /**
   * XMLA only.
   * <p />
   * <li>AUTH_MODE_UNAUTHENTICATED: no username or password required</li>
   * <li>AUTH_MODE_AUTHENTICATED: username and password required</li>
   * <li>AUTH_MODE_INTEGRATED: data source uses the underlying security</li>
   * </ul>
   */
  private String authenticationMode;

  private List<String> catalogNames;

  private Map<String, String> propertyList = new HashMap<String, String>();;

  public MondrianDataSource(final MondrianDataSource copy, final String overrideInfo) {
    this(copy.name, copy.description, copy.url, (null != overrideInfo ? overrideInfo : copy.dataSourceInfo),
        copy.providerName, copy.providerType, copy.authenticationMode, copy.catalogNames);
  }

  public MondrianDataSource(final String name, final String description, final String url,
      final String dataSourceInfo, final String providerName, final String providerType,
      final String authenticationMode, final List<String> catalogNames) {
    this.name = name;
    this.description = description;
    this.url = url;
    this.dataSourceInfo = dataSourceInfo;
    this.providerName = providerName;
    this.providerType = providerType;
    this.authenticationMode = authenticationMode;
    this.catalogNames = catalogNames;
    if (dataSourceInfo != null) {
      // convert datasource info over to map so that this object can be
      // serialized in ehcache
      PropertyList list = Util.parseConnectString(dataSourceInfo);
      Iterator<Pair<String, String>> iter = list.iterator();
      while (iter.hasNext()) {
        Pair<String, String> pair = iter.next();
        propertyList.put(pair.getKey(), pair.getValue());
      }
    }
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getUrl() {
    return url;
  }

  public String getDataSourceInfo() {
    return dataSourceInfo;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getProviderType() {
    return providerType;
  }

  public String getAuthenticationMode() {
    return authenticationMode;
  }

  protected List<String> getCatalogNames() {
    return catalogNames;
  }

  public boolean isJndi() {
    return propertyList.get("DataSource") != null; //$NON-NLS-1$
  }

  public String getJndi() {
    return propertyList.get("DataSource"); //$NON-NLS-1$
  }

  public String getJdbc() {
    return propertyList.get("Jdbc"); //$NON-NLS-1$
  }

  protected boolean isXmla() {
    return !"False".equals(propertyList.get("EnableXmla")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", name).append("description", description).append("url", url) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        .append("dataSourceInfo", dataSourceInfo).append("providerName", providerName).append("providerType", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            providerType).append("authenticationMode", authenticationMode).append("catalogNames", catalogNames) //$NON-NLS-1$ //$NON-NLS-2$
        .toString();
  }

}
