package org.pentaho.platform.datasource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class MetadataDatasourceService implements IGenericDatasourceService{
  IMetadataDomainRepository metadataDomainRepository;
  IAuthorizationPolicy policy;
  ActionBasedSecurityService helper;
  public static final String TYPE = "Metadata";

  public  MetadataDatasourceService(IMetadataDomainRepository metadataDomainRepository, IAuthorizationPolicy policy) {
    this.metadataDomainRepository = metadataDomainRepository; 
    this.policy = policy;
    helper = new ActionBasedSecurityService(policy);
  }
  @Override
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    try {
      if(datasource instanceof MetadataDatasource) {
        MetadataDatasource metadataDatasource = (MetadataDatasource) datasource;
        metadataDomainRepository.storeDomain(metadataDatasource.getDatasource(), false);
      } else {
        throw new GenericDatasourceServiceException("Object is not of type MetadataDatasource");
      }

    } catch (DomainIdNullException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DomainAlreadyExistsException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DomainStorageException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public MetadataDatasource get(String id) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    Domain domain = metadataDomainRepository.getDomain(id);
    if(domain != null) {
      return new MetadataDatasource(domain, domain.getId(), TYPE);      
    } else {
      return null;
    }

  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    metadataDomainRepository.removeDomain(id);
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException, PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    try {
      if(datasource instanceof MetadataDatasource) {
        MetadataDatasource metadataDatasource = (MetadataDatasource) datasource;
        metadataDomainRepository.storeDomain(metadataDatasource.getDatasource(), true);
      } else {
        throw new GenericDatasourceServiceException("Object is not of type MetadataDatasource");
      }
    } catch (DomainIdNullException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DomainAlreadyExistsException e) {
      throw new GenericDatasourceServiceException(e);
    } catch (DomainStorageException e) {
      throw new GenericDatasourceServiceException(e);
    }
  }

  @Override
  public List<IGenericDatasource> getAll() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasource> metadataDatasourceList = new ArrayList<IGenericDatasource>();
    for(String id:metadataDomainRepository.getDomainIds()) {
      try {
        metadataDatasourceList.add(get(id));
      } catch (GenericDatasourceServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return metadataDatasourceList;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() throws PentahoAccessControlException  {
    helper.checkAdministratorAccess();
    List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
    for(String id:metadataDomainRepository.getDomainIds()) {
      try {
        if(isMetadataDatasource(id)) {
          datasourceInfoList.add(new GenericDatasourceInfo(id, TYPE));
        }
      } catch (GenericDatasourceServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return datasourceInfoList;
  }
  
  @Override
  public String getType() {
    return TYPE;
  }

  private boolean isMetadataDatasource(String id) throws GenericDatasourceServiceException{
    MetadataDatasource metadataDatasource = null;
    try {
      metadataDatasource = get(id);
    } catch (PentahoAccessControlException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Domain domain = metadataDatasource.getDatasource();
    List<LogicalModel> logicalModelList = domain.getLogicalModels();
    if(logicalModelList != null && logicalModelList.size() >= 1) {
      LogicalModel logicalModel = logicalModelList.get(0);
      Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
      if(property == null) {
        return true;    
      } 
    }
    return false;
  }
}
