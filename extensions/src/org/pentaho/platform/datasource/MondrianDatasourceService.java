package org.pentaho.platform.datasource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

public class MondrianDatasourceService implements IGenericDatasourceService{
  public static final String TYPE = "Analysis";
  IMondrianCatalogService mondrianCatalogService;
  IMetadataDomainRepository metadataDomainRepository;
  IAuthorizationPolicy policy;
  ActionBasedSecurityService helper;
  
  public MondrianDatasourceService(IMondrianCatalogService mondrianCatalogService, IMetadataDomainRepository metadataDomainRepository, IAuthorizationPolicy policy) {
    this.mondrianCatalogService = mondrianCatalogService;
    this.metadataDomainRepository = metadataDomainRepository;
    this.policy = policy;
    helper = new ActionBasedSecurityService(policy);
  }
  @Override
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    if(datasource instanceof MondrianDatasource) {
      MondrianDatasource mondrianDatasource = (MondrianDatasource) datasource;
      mondrianCatalogService.addCatalog(mondrianDatasource.getDatasource(), false, PentahoSessionHolder.getSession());      
    } else {
      throw new GenericDatasourceServiceException("Object is not of type MondrianDatasource");
    }
  }

  @Override
  public MondrianDatasource get(String id) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    MondrianCatalog mondrianCatalog = mondrianCatalogService.getCatalog(id, PentahoSessionHolder.getSession());
    return new MondrianDatasource(mondrianCatalog, mondrianCatalog.getName(), TYPE);
  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    mondrianCatalogService.removeCatalog(id, PentahoSessionHolder.getSession());
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    if(datasource instanceof MondrianDatasource) {
      MondrianDatasource mondrianDatasource = (MondrianDatasource) datasource;
      mondrianCatalogService.addCatalog(mondrianDatasource.getDatasource(), true, PentahoSessionHolder.getSession());
    } else {
      throw new GenericDatasourceServiceException("Object is not of type MondrianDatasource");
    }
  }

  @Override
  public List<IGenericDatasource> getAll() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasource> mondrianDatasourceList = new ArrayList<IGenericDatasource>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true)) {
      mondrianDatasourceList.add(new MondrianDatasource(mondrianCatalog, mondrianCatalog.getName(), TYPE));
    }
    return mondrianDatasourceList;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true)) {
      Domain domain = metadataDomainRepository.getDomain(mondrianCatalog.getName());
      if(domain == null) {
        datasourceInfoList.add(new GenericDatasourceInfo(mondrianCatalog.getName(), TYPE));
      }
    }
    return datasourceInfoList;
  }
  
  @Override
  public String getType() {
    return TYPE;
  }
}
