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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.datasource.IGenericDatasource;

public class JDBCDatasource extends GenericDatasourceInfo implements IGenericDatasource{

  private static final long serialVersionUID = 1L;
  private IDatabaseConnection datasource;
  
  public JDBCDatasource(IDatabaseConnection datasource, String name, String id, String type) {
    super(name, id, type);
    this.datasource = datasource;
  }
  @Override
  public IDatabaseConnection getDatasource() {
    return this.datasource;
  }
  @Override
  public void setDatasource(Object datasource) {
    if(datasource instanceof IDatabaseConnection) {
      this.datasource = (IDatabaseConnection)datasource;      
    }
  }
}
