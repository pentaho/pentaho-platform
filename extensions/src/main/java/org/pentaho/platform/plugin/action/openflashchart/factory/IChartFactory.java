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
