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

package org.pentaho.platform.plugin.action.sql;

import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.actions.SqlDataAction;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceParameterMgr;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

public class SQLDdlComponent extends SQLLookupRule {

  private static final long serialVersionUID = 2100433825305016185L;

  @Override
  public IActionDefinition getActionDefinition() {

    IActionDefinition generic = super.getActionDefinition();

    ActionSequenceParameterMgr paramMgr = new ActionSequenceParameterMgr( getRuntimeContext(), getSession() );

    SqlDataAction action = new SqlDataAction( generic.getElement(), paramMgr );

    return action;
  }

  @Override
  public IPentahoResultSet
  doQuery( final SQLConnection sqlConnection, final String query, boolean forwardOnlyResultset ) throws Exception {

    MemoryResultSet resultSet = null;
    int n = ( (SQLConnection) connection ).execute( query );

    Object[][] columnHeaders = new Object[1][1];
    columnHeaders[0][0] = "result"; //$NON-NLS-1$
    IPentahoMetaData metadata = new MemoryMetaData( columnHeaders, null );

    resultSet = new MemoryResultSet( metadata );

    Object[] rowObjects = new Object[1];
    rowObjects[0] = new Integer( n );
    resultSet.addRow( rowObjects );
    return resultSet;

  }

}
