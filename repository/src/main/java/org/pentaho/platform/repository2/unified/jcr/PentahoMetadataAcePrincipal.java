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

import org.pentaho.platform.repository.RepositoryFilenameUtils;

import java.util.Arrays;
import java.util.List;

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
public class PentahoMetadataAcePrincipal implements IPentahoInternalPrincipal {

  /**
   * Helps to guarantee uniqueness of this principal name so that it never matches a real principal.
   */
  public static final String PRINCIPAL_PREFIX = "org.pentaho.jcr"; //$NON-NLS-1$

  public static final char SEPARATOR = ':';

  private static final List<Character> RESERVED_CHARS = Arrays.asList( new Character[] { SEPARATOR } );

  private final boolean entriesInheriting;

  private final String owner;

  private final String encodedName;

  public PentahoMetadataAcePrincipal( final String owner, final boolean entriesInheriting ) {
    super();
    this.owner = owner;
    this.entriesInheriting = entriesInheriting;
    // escape just in case owner name contains separator character
    encodedName =
        PRINCIPAL_PREFIX + SEPARATOR + RepositoryFilenameUtils.escape( owner, RESERVED_CHARS ) + SEPARATOR
            + entriesInheriting;
  }

  @Override
  public String getName() {
    return encodedName;
  }

  public String getOwner() {
    return owner;
  }

  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  public static boolean isPentahoMetadataAcePrincipal( final String name ) {
    return name.startsWith( PRINCIPAL_PREFIX + SEPARATOR );
  }

}
