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
  public IPentahoResultSet doQuery( final SQLConnection sqlConnection, final String query, boolean forwardOnlyResultset ) throws Exception {

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
