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

import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.openflashchart.factory.PentahoOFC4JChartHelper;
import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.List;

public class OpenFlashChartComponentTest {

  @Test
  public void testAutoRange() throws Exception {
    // Line Chart
    JavaScriptResultSet query_result = new JavaScriptResultSet();

    List<String> cols = new ArrayList<String>();
    cols.add( "MESES" );
    cols.add( "ACTUAL" );
    cols.add( "ANTERIOR" );

    query_result.setMetaData( new MemoryMetaData( cols ) );
    query_result.addRow( new Object[] { "Enero", 0, 0 } );
    query_result.addRow( new Object[] { "Febrero", 0, 0 } );
    query_result.addRow( new Object[] { "Marzo", 0, 0 } );
    query_result.addRow( new Object[] { "Abril", 0, 0 } );
    query_result.addRow( new Object[] { "Mayo", 0, 0 } );
    query_result.addRow( new Object[] { "Junio", 0, 0 } );
    query_result.addRow( new Object[] { "Julio", 6, 3 } );

    Document chartDocument =
        XmlDom4JHelper.getDocFromString( "<chart><chart-type>LineChart</chart-type></chart>",
            new PentahoEntityResolver() );
    Node chartNode = chartDocument.selectSingleNode( "chart" );

    String json = PentahoOFC4JChartHelper.generateChartJson( chartNode, query_result, false, null );
    System.out.println( json );
    Assert.assertTrue( json.indexOf( "\"min\":0" ) >= 0 && json.indexOf( "\"max\":6" ) >= 0 );
  }
}
