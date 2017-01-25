package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author Andrey Khayrutdinov
 * @author Nick Baker
 * @author Marc Batchelor
 */
public class JcrAclNodeHelper implements IAclNodeHelper {
  private static final Log logger = LogFactory.getLog( JcrAclNodeHelper.class );

  private static final String IS_ACL_NODE = "IS_ACL_NODE";
  private static final String TARGET = "TARGET";

  private final IUnifiedRepository unifiedRepository;

  public JcrAclNodeHelper( IUnifiedRepository unifiedRepository) {
    this.unifiedRepository = unifiedRepository;
  }
  // Set to protected for test access
  protected RepositoryFile getAclNode( final RepositoryFile file ) {
    try {
      return SecurityHelper.getInstance().runAsSystem( new Callable<RepositoryFile>() {
        @Override public RepositoryFile call() throws Exception {

          List<RepositoryFile> referrers = unifiedRepository.getReferrers( file.getId() );

          // Loop through nodes referring to the target file, return the first one designated as an ACL node
          int i = referrers.size();
          while ( i-- > 0 ) {
            RepositoryFile referrer = referrers.get( i );
            NodeRepositoryFileData dataForRead =
                unifiedRepository.getDataForRead( referrer.getId(), NodeRepositoryFileData.class );
            if ( dataForRead != null && dataForRead.getNode().hasProperty( IS_ACL_NODE ) ) {
              return referrer;
            }
          }

          // No ACL node found
          return null;
        }
      } );
    } catch ( Exception e ) {
      logger.error( "Error retrieving ACL Node", e );
      return null;
    }

  }

  @Override public boolean canAccess( final RepositoryFile repositoryFile,
                                      final EnumSet<RepositoryFilePermission> permissions ) {

    if ( repositoryFile == null ) {
      return false;
    }

    // Obtain a reference to ACL node as "system", guaranteed access
    final RepositoryFile aclNode = getAclNode( repositoryFile );
    
    // If no ACL node is present, it's a public resource
    // Removed redundant call to getAclNode via BISERVER-12780
    if ( aclNode == null ) {
      return true;
    }

    boolean notFound;
    try {
      // Check to see if user has READ access to file, this will return null if not.
      notFound = ( unifiedRepository.getFileById( aclNode.getId() ) == null );
    } catch ( Exception e ) {
      if ( logger.isWarnEnabled() ) {
        logger.warn( "Error checking access for file", e );
      }
      notFound = true;
    }
    if ( notFound ) {
      return false;
    }

    // if read passed, check the other permissions
    return unifiedRepository.hasAccess( aclNode.getPath(), permissions );

  }


  /**
   * {@inheritDoc}
   */
  @Override public RepositoryFileAcl getAclFor( final RepositoryFile repositoryFile ) {

    if ( repositoryFile == null ) {
      return null;
    }

    // Obtain a reference to ACL node as "system", guaranteed access
    final RepositoryFile aclNode = getAclNode( repositoryFile );

    // If no ACL node is present, it's a public resource
    // Removed redundant call to getAclNode via BISERVER-12780
    if ( aclNode == null ) {
      return null;
    }

    RepositoryFileAcl acl;
    try {
      acl = unifiedRepository.getAcl( aclNode.getId() );
    } catch ( Exception e ) {
      return null;
    }

    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder( acl.getId(), acl.getOwner().getName(),
        RepositoryFileSid.Type.ROLE );
    aclBuilder.aces( acl.getAces() );

    //add the Administrator role
    if( canAdminister() ) {
      String adminRoleName =
          PentahoSystem.get( String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession() );

      RepositoryFileAce adminGroup = new RepositoryFileAce( new RepositoryFileSid( adminRoleName,
          RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.ALL );
      aclBuilder.ace( adminGroup );
    }

    return aclBuilder.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void setAclFor( final RepositoryFile fileToAddAclFor, final RepositoryFileAcl acl ) {

    try {
      SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {
        @Override public Void call() throws Exception {
          RepositoryFile aclNode = getAclNode( fileToAddAclFor );

          if ( acl == null ) {
            if ( aclNode != null ) {
              unifiedRepository.deleteFile( aclNode.getId(), true,
                  Messages.getInstance().getString( "AclNodeHelper.WARN_0001_REMOVE_ACL_NODE", aclNode.getPath() ) );
            }
            // ignore if no ACL node is present.
          } else {
            if ( aclNode == null ) {
              // Create ACL Node with reference to given file.
              aclNode = createAclNode( fileToAddAclFor );
            }
            // Update ACL on file.
            RepositoryFileAcl existing = unifiedRepository.getAcl( aclNode.getId() );
            RepositoryFileAcl updated =
                new RepositoryFileAcl.Builder( existing )
                    .aces( acl.getAces() )
                    .build();
            unifiedRepository.updateAcl( updated );
          }
          return null;
        }
      } );
    } catch ( Exception e ) {
      logger.error( "Error setting ACL on node: " + fileToAddAclFor.getPath(), e );
    }
  }

  private RepositoryFile createAclNode( RepositoryFile fileToAddAclFor ) {

    DataNode dataNode = new DataNode( "acl node" );
    DataNodeRef dataNodeRef = new DataNodeRef( fileToAddAclFor.getId() );
    dataNode.setProperty( TARGET, dataNodeRef );
    dataNode.setProperty( IS_ACL_NODE, true );
    NodeRepositoryFileData nodeRepositoryFileData = new NodeRepositoryFileData( dataNode );

    return unifiedRepository.createFile(
        unifiedRepository.getFile( "/" ).getId(),
        new RepositoryFile.Builder( UUID.randomUUID().toString() ).aclNode( true ).build(),
        nodeRepositoryFileData, ""
    );

  }

  @Override public void removeAclFor( RepositoryFile file ) {
    setAclFor( file, null );
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }

}
