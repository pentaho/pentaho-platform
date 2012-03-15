package org.pentaho.platform.config;

public interface IEnterpriseConsoleConfig extends IConsoleConfig {
  public Integer getMetricsChartHeight();
  public void setMetricsChartHeight(Integer height);  
  public Integer getMetricsChartWidth();
  public void setMetricsChartWidth(Integer width);  
  public Integer getMetricsSmallChartHeight();
  public void setMetricsSmallChartHeight(Integer height);  
  public Integer getMetricsSmallChartWidth();
  public void setMetricsSmallChartWidth(Integer height);  
  public Double getMetricsInterval();
  public void setMetricsInterval(Double height);  
  public AnalysisView getMetricsView();
  public void setMetricsView(AnalysisView view);  
  public Double getMetricsExecutionLimit();
  public void setMetricsExecutionLimit(Double limit);  
  public Boolean getPdiOnly();
  public void setPdiOnly(Boolean pdiOnly);
}
