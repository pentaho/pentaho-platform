package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;

import java.io.InputStream;

/**
 * This interface is a temporary solution created to keep backwards compatibility prior to 6.0<br><b>Note: This
 * interface will be removed in 6.0</b>
 * @author Andrey Khayrutdinov
 */
public interface IAclAwarePentahoMetadataDomainRepositoryImporter extends IPentahoMetadataDomainRepositoryImporter {

  void storeDomain( InputStream inputStream, String domainId, boolean overwrite, RepositoryFileAcl acl )
    throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException;

  void setAclFor( String domainId, RepositoryFileAcl acl );

  RepositoryFileAcl getAclFor( String domainId );

  boolean hasAccessFor( String domainId );
}
