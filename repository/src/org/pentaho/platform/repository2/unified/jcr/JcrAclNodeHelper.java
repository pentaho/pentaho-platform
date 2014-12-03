package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

import java.util.concurrent.Callable;

/**
 * @author Andrey Khayrutdinov
 */
public class JcrAclNodeHelper implements IAclNodeHelper {
  private static final Log logger = LogFactory.getLog( JcrAclNodeHelper.class );

  private final IUnifiedRepository unifiedRepository;
  private final String aclNodeFolder;

  public JcrAclNodeHelper( IUnifiedRepository unifiedRepository,
                           String aclNodeFolder ) {
    this.unifiedRepository = unifiedRepository;
    this.aclNodeFolder = aclNodeFolder;
  }

  @Override public boolean hasAccess( String dataSourceName, DatasourceType type ) {
    try {
      FindAclNodeCommand command =
        new FindAclNodeCommand( unifiedRepository, getAclNodeFolder(), type.resolveName( dataSourceName ) );
      boolean nodeExists = SecurityHelper.getInstance().runAsSystem( command );

      return !nodeExists || command.call();
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public RepositoryFileAcl getAclFor( String dataSourceName, DatasourceType type ) {
    try {
      GetAclCommand command =
        new GetAclCommand( unifiedRepository, getAclNodeFolder(), type.resolveName( dataSourceName ) );
      return SecurityHelper.getInstance().runAsSystem( command );
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public void setAclFor( String dataSourceName, DatasourceType type,
                                   RepositoryFileAcl acl ) {
    try {
      SetAclCommand command =
        new SetAclCommand( unifiedRepository, getAclNodeFolder(), type.resolveName( dataSourceName ), acl );
      SecurityHelper.getInstance().runAsSystem( command );
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public void publishDatasource( String dataSourceName, DatasourceType type ) {
    setAclFor( dataSourceName, type, null );
  }

  @Override public void removeAclNodeFor( String dataSourceName, DatasourceType type ) {
    setAclFor( dataSourceName, type, null );
  }

  @Override public String getAclNodeFolder() {
    return aclNodeFolder;
  }
}

abstract class AbstractCommand<T> implements Callable<T> {
  final IUnifiedRepository repository;
  final String aclNodeFolder;
  final String resolvedDsName;

  public AbstractCommand( IUnifiedRepository repository, String aclNodeFolder, String resolvedDsName ) {
    this.repository = repository;
    this.aclNodeFolder = aclNodeFolder;
    this.resolvedDsName = resolvedDsName;
  }

  RepositoryFile getAclNode() {
    return repository.getFile( RepositoryFilenameUtils.normalize( aclNodeFolder + "/" + resolvedDsName ) );
  }

  RepositoryFile createAclNode() {
    RepositoryFile folder = repository.getFile( aclNodeFolder );
    if ( folder == null ) {
      folder = repository.createFolder(
        repository.getFile( "/" ).getId(),
        new RepositoryFile.Builder( resolvedDsName ).folder( true ).build(),
        ""
      );
    }

    return repository.createFile(
      folder.getId(),
      new RepositoryFile.Builder( resolvedDsName ).aclNode( true ).build(),
      new SampleRepositoryFileData( "", false, 0 ), ""
    );
  }
}

class FindAclNodeCommand extends AbstractCommand<Boolean> {
  public FindAclNodeCommand( IUnifiedRepository repository, String aclNodeFolder, String resolvedDsName ) {
    super( repository, aclNodeFolder, resolvedDsName );
  }

  @Override public Boolean call() throws Exception {
    return getAclNode() != null;
  }
}

class GetAclCommand extends AbstractCommand<RepositoryFileAcl> {
  public GetAclCommand( IUnifiedRepository repository, String aclNodeFolder, String resolvedDsName ) {
    super( repository, aclNodeFolder, resolvedDsName );
  }

  @Override public RepositoryFileAcl call() throws Exception {
    RepositoryFile aclNode = getAclNode();
    if ( aclNode == null ) {
      return null;
    }
    return repository.getAcl( aclNode.getId() );
  }
}

class SetAclCommand extends AbstractCommand<RepositoryFileAcl> {
  final RepositoryFileAcl acl;

  public SetAclCommand( IUnifiedRepository repository, String aclNodeFolder, String resolvedDsName,
                        RepositoryFileAcl acl ) {
    super( repository, aclNodeFolder, resolvedDsName );
    this.acl = acl;
  }

  @Override public RepositoryFileAcl call() throws Exception {
    RepositoryFile aclNode = getAclNode();

    if ( acl == null ) {
      if ( aclNode != null ) {
        repository.deleteFile( aclNode.getId(), true, "Removing the ACL node: " + aclNode.getPath() );
      }
    } else {
      if ( aclNode == null ) {
        aclNode = createAclNode();
      }
      RepositoryFileAcl existing = repository.getAcl( aclNode.getId() );
      RepositoryFileAcl updated = new RepositoryFileAcl.Builder( existing ).aces( acl.getAces() ).build();
      return repository.updateAcl( updated );
    }

    return null;
  }
}