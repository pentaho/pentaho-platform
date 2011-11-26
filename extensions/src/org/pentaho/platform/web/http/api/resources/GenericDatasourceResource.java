package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceServiceManager;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.datasource.GenericDatasourceInfo;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
@Path("/datasourcemgr/datasource")
public class GenericDatasourceResource extends AbstractJaxRSResource {

  protected IGenericDatasourceServiceManager datasourceServiceManager;
  
  public GenericDatasourceResource() {
    super();
    datasourceServiceManager = PentahoSystem.get(IGenericDatasourceServiceManager.class, PentahoSessionHolder.getSession());
  }

  @GET
  @Path("/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public List<GenericDatasourceInfo> getDatasources() throws PentahoAccessControlException {
    List<GenericDatasourceInfo> infoList = new ArrayList<GenericDatasourceInfo>();
    for(IGenericDatasourceInfo datasourceInfo:datasourceServiceManager.getIds()) {
      infoList.add(new GenericDatasourceInfo(datasourceInfo.getName(), datasourceInfo.getId(), datasourceInfo.getType())); 
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
