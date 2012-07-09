package org.pentaho.platform.plugin.services.metadata;

import java.io.InputStream;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public interface IPentahoMetadataDomainRepositoryImporter {
  public void storeDomain(final InputStream inputStream, final String domainId, final boolean overwrite)
      throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException;

  public void addLocalizationFile(final String domainId, final String locale, final InputStream inputStream,
                                  final boolean overwrite)
      throws DomainStorageException;

  public void removeDomain(String domainId);
}
