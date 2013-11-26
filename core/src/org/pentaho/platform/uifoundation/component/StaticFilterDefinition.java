/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
