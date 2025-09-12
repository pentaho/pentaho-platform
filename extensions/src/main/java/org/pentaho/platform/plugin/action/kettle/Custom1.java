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


package org.pentaho.platform.plugin.action.kettle;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class Custom1 extends KettleComponent {

  private static final long serialVersionUID = -3534575935705861245L;

  @Override
  protected boolean customizeTrans( Trans trans ) {
    // override this to customize the transformation before it runs
    // by default there is no transformation

    return true;
  }

  @SuppressWarnings ( "unused" )
  private void execSQL( TransMeta transMeta, String targetDatabaseName ) throws KettleStepException,
      KettleDatabaseException {

    // OK, What's the SQL we need to execute to generate the target table?
    String sql = transMeta.getSQLStatementsString();

    // Execute the SQL on the target table:
    Database targetDatabase =
        new Database( new LoggingObject( "Custom1" ), transMeta.findDatabase( targetDatabaseName ) );
    targetDatabase.connect();
    targetDatabase.execStatements( sql );

  }
}
