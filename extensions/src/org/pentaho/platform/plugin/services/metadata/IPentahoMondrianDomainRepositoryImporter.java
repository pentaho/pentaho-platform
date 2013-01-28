package org.pentaho.platform.plugin.services.metadata;

import java.io.InputStream;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;

public interface IPentahoMondrianDomainRepositoryImporter {

  
  public void storeDomain(final InputStream inputStream, final String domainId, final boolean overwrite)
      throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException;;

}
