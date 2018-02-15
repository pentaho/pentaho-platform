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

package org.pentaho.platform.plugin.action.mondrian;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.mdx.MDXLookupRule;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;

import mondrian.olap.CacheControl;
import mondrian.olap.Connection;
import mondrian.olap.Cube;

/**
 * @author William E. Seyler
 */
public class MondrianFlushCacheComponent extends ComponentBase {

  private static final long serialVersionUID = 5697771680582241114L;
  private static final Log logger = LogFactory.getLog( MondrianFlushCacheComponent.class );

  public boolean executeAction() {
    MDXLookupRule mdxLookupRule = getLookupRule();
    Connection conn = ( (MDXConnection) mdxLookupRule.shareConnection() ).getConnection();
    CacheControl cacheControl = conn.getCacheControl( null );
    Cube[] cubes = conn.getSchema().getCubes();
    for ( Cube cube : cubes ) {
      cacheControl.flush( cacheControl.createMeasuresRegion( cube ) );
    }
    cacheControl.flushSchema( conn.getSchema() );
    return true;
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  public MDXLookupRule getLookupRule() {
    return (MDXLookupRule) this.getInputValue( "shared_olap_connection" );
  }
}
