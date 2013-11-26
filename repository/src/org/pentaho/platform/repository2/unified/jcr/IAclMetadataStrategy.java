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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import java.util.List;

public interface IAclMetadataStrategy {

  AclMetadata getAclMetadata( final Session session, final String path, final AccessControlList acList )
    throws RepositoryException;

  void setAclMetadata( final Session session, final String path, final AccessControlList acList,
      final AclMetadata aclMetadata ) throws RepositoryException;

  /**
   * If implementation uses ACE(s) to store metadata, this method must remove that metadata.
   */
  List<AccessControlEntry> removeAclMetadata( final List<AccessControlEntry> acEntries ) throws RepositoryException;

  /**
   * Immutable ACL metadata.
   */
  class AclMetadata {

    private final String owner;

    private final boolean entriesInheriting;

    public AclMetadata( String owner, boolean entriesInheriting ) {
      super();
      this.owner = owner;
      this.entriesInheriting = entriesInheriting;
    }

    public String getOwner() {
      return owner;
    }

    public boolean isEntriesInheriting() {
      return entriesInheriting;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( entriesInheriting ? 1231 : 1237 );
      result = prime * result + ( ( owner == null ) ? 0 : owner.hashCode() );
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
      AclMetadata other = (AclMetadata) obj;
      if ( entriesInheriting != other.entriesInheriting ) {
        return false;
      }
      if ( owner == null ) {
        if ( other.owner != null ) {
          return false;
        }
      } else if ( !owner.equals( other.owner ) ) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "AclMetadata [owner=" + owner + ", entriesInheriting=" + entriesInheriting + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

  }
}
