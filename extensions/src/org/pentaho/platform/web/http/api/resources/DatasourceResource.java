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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Nov 12, 2011
 * @author Ramaiz Mansoor
 * 
 */
package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceServiceManager;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.datasource.DatasourceInfo;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
@Path("/datasourcemgr/datasource")
public class DatasourceResource extends AbstractJaxRSResource {

  protected IDatasourceServiceManager datasourceServiceManager;
  
  public DatasourceResource() {
    super();
    datasourceServiceManager = PentahoSystem.get(IDatasourceServiceManager.class, PentahoSessionHolder.getSession());
  }

  @GET
  @Path("/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public List<DatasourceInfo> getDatasources() throws PentahoAccessControlException {
    List<DatasourceInfo> infoList = new ArrayList<DatasourceInfo>();
    for(IDatasourceInfo datasourceInfo:datasourceServiceManager.getIds()) {
      infoList.add(new DatasourceInfo(datasourceInfo.getName(), datasourceInfo.getId(), datasourceInfo.getType())); 
    }
    return infoList;
  }

  @GET
  @Path("/types")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getDatasourcesTypes() {
    return new JaxbList<String>(datasourceServiceManager.getTypes());
  }

  
}
