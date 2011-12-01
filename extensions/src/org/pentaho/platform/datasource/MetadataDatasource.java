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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * @created Nov 12, 2011
 * @author Ramaiz Mansoor
 */
package org.pentaho.platform.datasource;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;

public class MetadataDatasource implements IDatasource{

  private static final long serialVersionUID = 1L;
  private Domain datasource;
  private IDatasourceInfo datasourceInfo;

  public MetadataDatasource(Domain datasource, IDatasourceInfo datasourceInfo) {
    this.datasource = datasource;
    this.datasourceInfo = datasourceInfo;
  }
  @Override
  public Domain getDatasource() {
    return this.datasource;
  }
  @Override
  public IDatasourceInfo getDatasourceInfo() {
    return datasourceInfo;
  }

}
