package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.io.InputStream;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;

/**
 * Implementations handle importing content into the repository.
 *
 * User: nbaker
 * Date: 5/29/12
 */
public interface IPlatformImportHandler {

  
  /**
   * Import the provided IPlatformImportBundle into the platform.
   *
   * @param bundle
   * @throws PlatformImportException
   * @throws IOException 
   * @throws DomainStorageException 
   * @throws DomainAlreadyExistsException 
   * @throws DomainIdNullException 
   *
   */
  void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException;
    
 }

