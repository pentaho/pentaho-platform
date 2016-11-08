/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.Privilege;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JcrAclMetadataStrategy implements IAclMetadataStrategy {

  private static final Log logger = LogFactory.getLog( JcrAclMetadataStrategy.class );

  /**
   * Actual privilege is not important.
   */
  private static final String PRIV = "jcr:retentionManagement"; //$NON-NLS-1$

  @Override
  public AclMetadata getAclMetadata( final Session session, final String path, final AccessControlList acList )
    throws RepositoryException {
    if ( session == null || path == null || acList == null ) {
      throw new IllegalArgumentException();
    }
    // special handling for root since root doesn't have metadata
    if ( session.getRootNode().getPath().equals( path ) ) {
      return null;
    }
    if ( acList.getAccessControlEntries().length == 0 ) {
      throw new IllegalArgumentException();
    }
    AccessControlEntry firstAce = acList.getAccessControlEntries()[0];
    if ( firstAce.getPrincipal() instanceof AclMetadataPrincipal ) {
      return ( (AclMetadataPrincipal) firstAce.getPrincipal() ).getAclMetadata();
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setAclMetadata( final Session session, final String path, final AccessControlList acList,
      final AclMetadata aclMetadata ) throws RepositoryException {
    if ( session == null || path == null || acList == null || aclMetadata == null ) {
      throw new IllegalArgumentException();
    }
    if ( session.getRootNode().getPath().equals( path ) ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "ignoring setAclMetadata on root node" ); //$NON-NLS-1$
      }
      return;
    }
    // acl must be clear of aces
    if ( acList.getAccessControlEntries().length != 0 ) {
      throw new IllegalArgumentException();
    }
    // some implementations will not allow an empty Privilege array
    if ( !( acList.addAccessControlEntry( new AclMetadataPrincipal( aclMetadata ), new Privilege[] { session
        .getAccessControlManager().privilegeFromName( PRIV ) } ) ) ) {
      throw new RepositoryException();
    }
  }

  @Override
  public List<AccessControlEntry> removeAclMetadata( List<AccessControlEntry> acEntries ) throws RepositoryException {
    List<AccessControlEntry> cleanedAcEntries = new ArrayList<AccessControlEntry>( acEntries.size() );
    for ( AccessControlEntry acEntry : acEntries ) {
      // removes acl metadata but also any magic aces that might leak out during getEffectivePolicies
      if ( !( acEntry.getPrincipal() instanceof IPentahoInternalPrincipal ) ) {
        cleanedAcEntries.add( acEntry );
      }
    }
    return cleanedAcEntries;
  }

  /**
   * Special principal used in ACEs that contains two pieces of metadata about the ACL as a whole:
   * 
   * <ul>
   * <li>Owner: Separate from all ACEs, what Principal is the owner? (Owners can be treated specially.)</li>
   * <li>Entries Inheriting: Whether or not the ACEs of this ACL apply or instead an ancestor.</li>
   * </ul>
   * 
   * @author mlowery
   */
  public static class AclMetadataPrincipal implements IPentahoInternalPrincipal {

    /**
     * Helps to guarantee uniqueness of this principal name so that it never matches a real principal.
     */
    public static final String PRINCIPAL_PREFIX = "org.pentaho.jcr"; //$NON-NLS-1$

    public static final char SEPARATOR = ':';

    private static final List<Character> RESERVED_CHARS = Arrays.asList( new Character[] { SEPARATOR } );

    private final AclMetadata aclMetadata;

    private final String encodedName;

    public AclMetadataPrincipal( final AclMetadata aclMetadata ) {
      super();
      this.aclMetadata = aclMetadata;
      // escape just in case owner name contains separator character
      this.encodedName =
          PRINCIPAL_PREFIX + SEPARATOR + RepositoryFilenameUtils.escape( aclMetadata.getOwner(), RESERVED_CHARS )
              + SEPARATOR + aclMetadata.isEntriesInheriting();
    }

    public AclMetadataPrincipal( final String encodedName ) {
      super();
      this.encodedName = encodedName;
      String[] tokens = encodedName.split( "\\" + SEPARATOR ); //$NON-NLS-1$
      if ( tokens.length != 3 ) {
        throw new IllegalArgumentException();
      }
      if ( !tokens[0].equals( PRINCIPAL_PREFIX ) ) {
        throw new IllegalArgumentException();
      }
      String owner = RepositoryFilenameUtils.unescape( tokens[1] );
      boolean entriesInheriting = Boolean.parseBoolean( tokens[2] );
      this.aclMetadata = new AclMetadata( owner, entriesInheriting );
    }

    @Override
    public String getName() {
      return encodedName;
    }

    public AclMetadata getAclMetadata() {
      return aclMetadata;
    }

    public static boolean isAclMetadataPrincipal( final String name ) {
      return name.startsWith( PRINCIPAL_PREFIX + SEPARATOR );
    }

    @Override
    public String toString() {
      return "AclMetadataPrincipal [aclMetadata=" + aclMetadata + ", encodedName=" + encodedName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( encodedName == null ) ? 0 : encodedName.hashCode() );
      return result;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( obj == null ) {
        return false;
      }
      if ( getClass() != obj.getClass() ) {
        return false;
      }
      AclMetadataPrincipal other = (AclMetadataPrincipal) obj;
      if ( encodedName == null ) {
        if ( other.encodedName != null ) {
          return false;
        }
      } else if ( !encodedName.equals( other.encodedName ) ) {
        return false;
      }
      return true;
    }

  }

}
