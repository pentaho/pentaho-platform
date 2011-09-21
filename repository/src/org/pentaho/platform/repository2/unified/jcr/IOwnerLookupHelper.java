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
 */
package org.pentaho.platform.repository2.unified.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

/**
 * Provides a pluggable way to lookup the owner of a {@link RepositoryFile}. Typically, the owner of a file is stored
 * with the access control information for the file. This is because the owner can affect access control decisions. But
 * it's also a nice piece of metadata to store with the {@code RepositoryFile}. So implementations of this interface 
 * know how to fetch the owner from wherever it resides and convert it to the required {@link RepositoryFileSid}.
 * 
 * @author mlowery
 */
public interface IOwnerLookupHelper {
  RepositoryFileSid getOwner(final Session session, final PentahoJcrConstants pentahoJcrConstants, final Node node)
      throws RepositoryException;
}