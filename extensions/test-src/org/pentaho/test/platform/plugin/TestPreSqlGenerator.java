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

package org.pentaho.test.platform.plugin;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.query.impl.sql.SqlGenerator;
import org.pentaho.metadata.query.model.Selection;
import org.pentaho.pms.mql.dialect.SQLQueryModel;

import java.util.List;
import java.util.Map;

public class TestPreSqlGenerator extends SqlGenerator {

  @Override
  protected void preprocessQueryModel( SQLQueryModel query, List<Selection> selections,
      Map<LogicalTable, String> tableAliases, DatabaseMeta databaseMeta ) {

    System.out.println( "preprocessQueryModel was called..." );
    query.addWhereFormula( "LT.CUSTOMERNAME like 'Au%'", "AND" );

    super.preprocessQueryModel( query, selections, tableAliases, databaseMeta );
  }

}
