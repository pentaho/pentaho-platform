/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

import java.io.InputStream;

/**
 * This interface is a temporary solution created to keep backwards compatibility prior to 6.0<br><b>Note: This
 * interface will be removed in 6.0</b>
 *
 * @author Andrey Khayrutdinov
 */
public interface IAclAwareMondrianCatalogService extends IMondrianCatalogService {

  /**
   * Pass the input stream directly from data access PUC and schema workbench
   *
   * @param inputStream           stream
   * @param catalog               catalog
   * @param overwriteInRepository flag, defining to overwrite existing or not
   * @param acl                   catalog ACL, <tt>null</tt> means no ACL
   * @param session               user session
   */
  void addCatalog( InputStream inputStream, MondrianCatalog catalog, boolean overwriteInRepository,
                   RepositoryFileAcl acl, IPentahoSession session );

  void setAclFor( String catalogName, RepositoryFileAcl acl );

  RepositoryFileAcl getAclFor( String catalogName );
}
