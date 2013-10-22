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

package org.pentaho.platform.uifoundation.contentgen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.uifoundation.chart.CategoryDatasetChartComponent;
import org.pentaho.platform.uifoundation.chart.JFreeChartEngine;

import java.util.ArrayList;

public class ChartContentGenerator extends BaseXmlContentGenerator {

  private static final long serialVersionUID = 2272261269875005948L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ChartContentGenerator.class );
  }

  @Override
  public String getContent() throws Exception {

    int chartType = (int) requestParameters.getLongParameter( "ChartType", JFreeChartEngine.UNDEFINED_CHART_TYPE ); //$NON-NLS-1$
    String chartDefinitionPath = requestParameters.getStringParameter( "ChartDefinitionPath", null ); //$NON-NLS-1$

    ArrayList messages = new ArrayList();
    CategoryDatasetChartComponent barChart =
        new CategoryDatasetChartComponent( chartType, chartDefinitionPath, 600, 400, urlFactory, messages );

    String content = null;
    IPentahoConnection connection =
        PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, "SampleData", userSession, this ); //$NON-NLS-1$
    try {
      String query = "select department, actual, budget, variance from QUADRANT_ACTUALS"; //$NON-NLS-1$

      IPentahoResultSet results = connection.executeQuery( query );
      try {

        barChart.setValues( results );
        barChart.validate( userSession, null );

        barChart.setParameterProvider( IParameterProvider.SCOPE_REQUEST, requestParameters );
        barChart.setParameterProvider( IParameterProvider.SCOPE_SESSION, sessionParameters );

        content = barChart.getContent( "text/html" ); //$NON-NLS-1$
      } finally {
        results.close();
      }
    } finally {
      connection.close();
    }
    return content;
  }

}
