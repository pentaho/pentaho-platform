/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin;

//import java.text.SimpleDateFormat;
//
//import org.pentaho.commons.connection.IPentahoResultSet;
//import org.pentaho.commons.connection.PentahoDataTransmuter;
//import org.pentaho.platform.api.engine.IActionParameter;
//import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class PentahoDataTransmuterTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }
  
  public void testDummyTest() {}

  /*public void testTransmute() {
    startTest();

    IRuntimeContext context = run("test", "rules", "CrossTabTest.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    IActionParameter rtn = context.getOutputParameter("rule-result");//$NON-NLS-1$
    IPentahoResultSet resultSet = rtn.getValueAsResultSet();
    PentahoDataTransmuter transmuter = new PentahoDataTransmuter(resultSet);
    IPentahoResultSet resultSet1 = transmuter.getSourceResultSet();
    assertEquals(resultSet1, resultSet);
    IPentahoResultSet transmutedResultSet = PentahoDataTransmuter.transmute(resultSet, true);
    assertEquals(transmutedResultSet.getColumnCount(), resultSet.getRowCount());
    // TODO How should I display the results
    Integer[] columnsToInclude = new Integer[] { new Integer(1) };
    Integer[] rowsToInclude = new Integer[] { new Integer(1) };
    String[] columnForRowHeaders = new String[] { "COL1", "COL2", "COL3", "COL4" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    String[] rowForColumnHeaders = new String[] { "ROW1", "ROW1", "ROW1", "ROW1" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    IPentahoResultSet transmutedResultSet1 = PentahoDataTransmuter.transmute(resultSet, new Integer(2), new Integer(2),
        true);
    assertEquals(transmutedResultSet1.getColumnCount(), 7);

    IPentahoResultSet transmutedResultSet2 = PentahoDataTransmuter.transmute(resultSet, new Integer(1), new Integer(1),
        rowsToInclude, columnsToInclude, true);
    assertEquals(transmutedResultSet2.getRowCount(), 1);

    IPentahoResultSet transmutedResultSet3 = PentahoDataTransmuter.transmute(resultSet, rowsToInclude,
        columnsToInclude, true);
    assertEquals(transmutedResultSet3.getColumnCount(), 1);

    IPentahoResultSet transmutedResultSet4 = PentahoDataTransmuter.transmute(resultSet, columnForRowHeaders,
        rowForColumnHeaders, true);
    assertEquals(transmutedResultSet4.getColumnCount(), 7);

    PentahoDataTransmuter.transmute(resultSet, true);

    IPentahoResultSet crossTabResultSet = PentahoDataTransmuter.crossTab(resultSet, 3, 2, 2, new SimpleDateFormat(
        "yyyy.MM.dd G 'at' HH:mm:ss z"), new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"), true);//$NON-NLS-1$ //$NON-NLS-2$
    int count3 = crossTabResultSet.getRowCount();
    int count1 = crossTabResultSet.getColumnCount();
    int count2 = resultSet.getColumnCount();
    int count4 = resultSet.getRowCount();
    assertEquals(crossTabResultSet.getColumnCount(), resultSet.getColumnCount());
    IPentahoResultSet crossTabResultSet1 = PentahoDataTransmuter.crossTab(resultSet, 3, 2, new SimpleDateFormat(
        "yyyy.MM.dd G 'at' HH:mm:ss z"));//$NON-NLS-1$
    assertEquals(crossTabResultSet1.getColumnCount(), resultSet.getColumnCount());
    IPentahoResultSet crossTabResultSet2 = PentahoDataTransmuter.crossTab(resultSet, 3, 2, new SimpleDateFormat(
        "yyyy.MM.dd G 'at' HH:mm:ss z"), true);//$NON-NLS-1$
    assertEquals(crossTabResultSet2.getColumnCount(), resultSet.getColumnCount());

    //      IPentahoResultSet crossTabResultSet3 = PentahoDataTransmuter.crossTabOrdered(resultSet, 1, 1 , 2, new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z"), new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"), true, 2);//$NON-NLS-1$ //$NON-NLS-2$

    //      IPentahoResultSet crossTabResultSet4 = PentahoDataTransmuter.crossTabOrdered(resultSet, 1, 1 , new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z"));//$NON-NLS-1$      
    //      IPentahoResultSet crossTabResultSet5 = PentahoDataTransmuter.crossTabOrdered(resultSet, 1, 1 , new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z"), true);//$NON-NLS-1$      

    //IPentahoResultSet flattenedResultSet = PentahoDataTransmuter.flattenResultSet(resultSet, 4);
    //IPentahoResultSet pivotResultSet = PentahoDataTransmuter.pivot(resultSet);

    IPentahoResultSet transmutedResultSet11 = transmuter.transmute(rowsToInclude, columnsToInclude, true);
    assertEquals(transmutedResultSet11.getColumnCount(), 1);
    IPentahoResultSet transmutedResultSet5 = transmuter.transmute(columnForRowHeaders, rowForColumnHeaders, true);
    assertEquals(transmutedResultSet5.getColumnCount(), 2);
    IPentahoResultSet transmutedResultSet12 = transmuter.transmute(new Integer(1), new Integer(1), true);
    assertEquals(transmutedResultSet12.getColumnCount(), 1);

    finishTest();
  }*/

  public static void main(String[] args) {
    PentahoDataTransmuterTest test = new PentahoDataTransmuterTest();
    test.setUp();
    try {
//      test.testTransmute();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
