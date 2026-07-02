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


package org.pentaho.platform.config;

public interface IEnterpriseConsoleConfig extends IConsoleConfig {
  public Integer getMetricsChartHeight();

  public void setMetricsChartHeight( Integer height );

  public Integer getMetricsChartWidth();

  public void setMetricsChartWidth( Integer width );

  public Integer getMetricsSmallChartHeight();

  public void setMetricsSmallChartHeight( Integer height );

  public Integer getMetricsSmallChartWidth();

  public void setMetricsSmallChartWidth( Integer height );

  public Double getMetricsInterval();

  public void setMetricsInterval( Double height );

  public AnalysisView getMetricsView();

  public void setMetricsView( AnalysisView view );

  public Double getMetricsExecutionLimit();

  public void setMetricsExecutionLimit( Double limit );

  public Boolean getPdiOnly();

  public void setPdiOnly( Boolean pdiOnly );
}
