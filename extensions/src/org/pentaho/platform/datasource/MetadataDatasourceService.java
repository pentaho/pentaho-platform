package org.pentaho.platform.datasource;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class MetadataDatasourceService implements IGenericDatasourceService{
  IMetadataDomainRepository metadataDomainRepository;
  public static final String TYPE = "Metadata";

  public  MetadataDatasourceService()  {
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class);
  }

  @Override
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException {
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
  public MetadataDatasource get(String id) {
    Domain domain = metadataDomainRepository.getDomain(id);
    if(domain != null) {
      return new MetadataDatasource(domain, domain.getId(), TYPE);      
    } else {
      return null;
    }

  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException {
    metadataDomainRepository.removeDomain(id);
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException {
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
  public List<IGenericDatasource> getAll() {
    
    List<IGenericDatasource> metadataDatasourceList = new ArrayList<IGenericDatasource>();
    for(String id:metadataDomainRepository.getDomainIds()) {
      metadataDatasourceList.add(get(id));
    }
    return metadataDatasourceList;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() {
    List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
    for(String id:metadataDomainRepository.getDomainIds()) {
      datasourceInfoList.add(new GenericDatasourceInfo(id, TYPE));
    }
    return datasourceInfoList;
  }
  
  @Override
  public String getType() {
    return TYPE;
  }

}
