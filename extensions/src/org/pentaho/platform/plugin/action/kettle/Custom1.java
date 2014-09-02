/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
