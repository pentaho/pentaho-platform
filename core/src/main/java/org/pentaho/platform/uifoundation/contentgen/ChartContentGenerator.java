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
