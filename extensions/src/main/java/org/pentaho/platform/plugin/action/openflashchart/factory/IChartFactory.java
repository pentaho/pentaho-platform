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


package org.pentaho.platform.plugin.action.openflashchart.factory;

import org.apache.commons.logging.Log;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoResultSet;

/**
 * This interface allows generation of OpenFlashChart Json
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public interface IChartFactory {

  /**
   * set the result set to render
   * 
   * @param data
   *          result set
   */
  void setData( IPentahoResultSet data );

  /**
   * set the xml chart definition
   * 
   * @param chartNode
   *          chart definition
   */
  void setChartNode( Node chartNode );

  /**
   * set the logger
   * 
   * @param log
   *          logger
   */
  void setLog( Log log );

  /**
   * generate ofc json
   * 
   * @return json string
   */
  String convertToJson();
}
