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


package org.pentaho.platform.uifoundation.component;

import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Steven Barkdull
 * 
 */
public class StaticFilterDefinition extends FilterDefinition {

  public StaticFilterDefinition( final Element node, final IPentahoSession session, final ILogger logger ) {
    super( node, session, logger );
  }

  @Override
  protected IPentahoResultSet getResultSet( final Map parameterProviders ) {
    List headers = node.selectNodes( "static-lov/headers/header" ); //$NON-NLS-1$
    List colHeaders = new LinkedList();
    for ( Iterator it = headers.iterator(); it.hasNext(); ) {
      Element header = (Element) it.next();
      String value = header.getStringValue();
      colHeaders.add( value );
    }

    List rows = node.selectNodes( "static-lov/rows/row" ); //$NON-NLS-1$
    List data = new LinkedList();
    for ( Iterator it = rows.iterator(); it.hasNext(); ) {
      Element rowItem = (Element) it.next();
      List items = rowItem.selectNodes( "item" );
      List row = new LinkedList();
      for ( Iterator itt = items.iterator(); itt.hasNext(); ) {
        Element item = (Element) itt.next();
        String value = item.getStringValue();
        row.add( value );
      }
      data.add( row );
    }

    IPentahoResultSet resultSet = MemoryResultSet.createFromLists( colHeaders, data );

    return resultSet;
  }
}
