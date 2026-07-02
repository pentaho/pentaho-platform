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


package org.pentaho.test.platform.plugin;

import org.pentaho.metadata.query.impl.sql.SqlGenerator;

public class TestPostSqlGenerator extends SqlGenerator {

  @Override
  protected String processGeneratedSql( String sql ) {

    System.out.println( "processGeneratedSql was called..." );
    sql = sql.replaceAll( "customername", "contactfirstname" );
    sql = sql.replaceAll( "CUSTOMERNAME", "CONTACTFIRSTNAME" );

    return super.processGeneratedSql( sql );
  }

}
