package org.pentaho.platform.plugin.services.metadata;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

public class PentahoMondrianDomainRepository extends PentahoMetadataDomainRepository implements IMetadataDomainRepository {

  private static final Log logger = LogFactory.getLog(PentahoMondrianDomainRepository.class);
  
  public PentahoMondrianDomainRepository(IUnifiedRepository repository) {
    super(repository);
    // TODO Auto-generated constructor stub
  }

 
}
