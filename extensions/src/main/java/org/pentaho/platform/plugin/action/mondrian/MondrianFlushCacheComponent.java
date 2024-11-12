/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
