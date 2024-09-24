/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;

/**
 * Copy-and-paste of {@code org.apache.jackrabbit.core.security.authorization.acl} in Jackrabbit 2.10.0.
 * This class is in {@code org.apache.jackrabbit.core.security.authorization.acl} package due to the scope of
 * collaborating classes.
 *
 * <p/>
 * <p/>
 * <p> Changes to original: </p> <ul> <li>{@code Entry} has a single private constructor, we changed the
 * scope to public {@code null} {@code nextId}.</li>
 * <p/>
 * </ul>
 */
public class PentahoEntryFilterImpl extends EntryFilterImpl implements PentahoEntryFilter {

  /**
   * logger instance
   */
  private static final Logger log = LoggerFactory.getLogger(PentahoEntryFilterImpl.class);

  private final Collection<String> principalNames;
  private final PathProvider pathProvider;

  private String itemPath;

  PentahoEntryFilterImpl(Collection<String> principalNames, final ItemId id, final SessionImpl sessionImpl) {
    super( principalNames , id , sessionImpl );
    this.principalNames = principalNames;
    this.pathProvider = new PathProvider() {
      public String getPath() throws RepositoryException {
        Path p = sessionImpl.getHierarchyManager().getPath(id);
        return sessionImpl.getJCRPath(p);
      }
    };
  }

  PentahoEntryFilterImpl(Collection<String> principalNames, final Path absPath, final PathResolver pathResolver) {
    super( principalNames, absPath, pathResolver  );
    this.principalNames = principalNames;
    this.pathProvider = new PathProvider() {
      public String getPath() throws RepositoryException {
        return pathResolver.getJCRPath(absPath);
      }
    };
  }

  /**
   * Separately collect the entries defined for the user and group
   * principals.
   *
   * @param entries
   * @param resultLists
   * @see EntryFilter#filterEntries(java.util.List, java.util.List[])
   */
  @Override
  public void filterEntries( List entries, List... resultLists ) {
    if ( resultLists.length == 2 ) {
      List<PentahoEntry> userAces = resultLists[0];
      List<PentahoEntry> groupAces = resultLists[1];

      int uInsertIndex = userAces.size();
      int gInsertIndex = groupAces.size();

      // first collect aces present on the given aclNode.
      for ( PentahoEntry ace : ( List<PentahoEntry> ) entries ) {
        // only process ace if 'principalName' is contained in the given set
        if ( matches( ace ) ) {
          // add it to the proper list (e.g. separated by principals)
          /**
           * NOTE: access control entries must be collected in reverse
           * order in order to assert proper evaluation.
           */
          if ( ace.isGroupEntry() ) {
            groupAces.add( gInsertIndex, ace );
          } else {
            userAces.add( uInsertIndex, ace );
          }
        }
      }
    } else {
      log.warn( "Filtering aborted. Expected 2 result lists." );
    }
  }

  private boolean matches( PentahoEntry entry ) {
    if ( principalNames == null || principalNames.contains( entry.getPrincipalName() ) ) {
      if ( !entry.hasRestrictions() ) {
        // short cut: there is no glob-restriction -> the entry matches
        // because it is either defined on the node or inherited.
        return true;
      } else {
        // there is a glob-restriction: check if the target path matches
        // this entry.
        try {
          return entry.matches(getPath());
        } catch (RepositoryException e) {
          log.error("Cannot determine ACE match.", e);
        }
      }
    }

    // doesn't match this filter -> ignore
    return false;
  }

  String getPath() throws RepositoryException {
    if (itemPath == null) {
      itemPath = pathProvider.getPath();
    }
    return itemPath;
  }

  //--------------------------------------------------------------------------
  /**
   * Interface for lazy calculation of the JCR path used for evaluation of ACE
   * matching in case of entries defining restriction(s).
   */
  private interface PathProvider {

    String getPath() throws RepositoryException;

  }

}
