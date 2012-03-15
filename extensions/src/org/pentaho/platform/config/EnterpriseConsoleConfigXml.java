package org.pentaho.platform.config;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;

public class EnterpriseConsoleConfigXml extends ConsoleConfigXml implements IEnterpriseConsoleConfig {

  private static final String INTERVAL_SETTING = ROOT_ELEMENT +"/metrics/atomic-interval"; //$NON-NLS-1$  
  private static final String VIEW_SETTING = ROOT_ELEMENT +"/metrics/view"; //$NON-NLS-1$
  private static final String LIMIT_SETTING = ROOT_ELEMENT +"/metrics/execution-limit";//$NON-NLS-1$
  private static final String CHART_HEIGHT_SETTING = ROOT_ELEMENT +"/metrics/charts/chart-height";//$NON-NLS-1$
  private static final String CHART_WIDTH_SETTING = ROOT_ELEMENT +"/metrics/charts/chart-width";//$NON-NLS-1$ 
  private static final String SMALL_CHART_HEIGHT_SETTING = ROOT_ELEMENT +"/metrics/charts/small-chart-height";//$NON-NLS-1$
  private static final String SMALL_CHART_WIDTH_SETTING = ROOT_ELEMENT +"/metrics/charts/small-chart-width";//$NON-NLS-1$
  private static final String PDI_ONLY_SETTING = ROOT_ELEMENT +"/pdiOnly";//$NON-NLS-1$
  
  
  public EnterpriseConsoleConfigXml(File consoleConfigXmlFile) throws IOException, DocumentException{
    super(consoleConfigXmlFile);
  }
  
  public EnterpriseConsoleConfigXml(String xml) throws DocumentException {
    super(xml);
  }
  
  public EnterpriseConsoleConfigXml(Document doc) throws DocumentException {
    super(doc);
  }
  
  public EnterpriseConsoleConfigXml() {
    super();
  }
  
  public Integer getMetricsChartHeight() {
    Integer height = null;
    try {
      height = Integer.parseInt(getValue(CHART_HEIGHT_SETTING));
    } catch (Exception ex) {
    }
    return height;
  }

  public Integer getMetricsChartWidth() {
    Integer width = null;
    try {
      width = Integer.parseInt(getValue(CHART_WIDTH_SETTING));
    } catch (Exception ex) {
    }
    return width;
  }

  public Double getMetricsExecutionLimit() {
    Double limit = null;
    try {
      limit = Double.parseDouble(getValue(LIMIT_SETTING));
    } catch (Exception ex) {
    }
    return limit;
  }

  public Double getMetricsInterval() {
    Double interval = null;
    try {
      interval = Double.parseDouble(getValue(INTERVAL_SETTING));
    } catch (Exception ex) {
    }
    return interval;
  }

  public Integer getMetricsSmallChartHeight() {
    Integer height = null;
    try {
      height = Integer.parseInt(getValue(SMALL_CHART_HEIGHT_SETTING));
    } catch (Exception ex) {
    }
    return height;
  }

  public Integer getMetricsSmallChartWidth() {
    Integer width = null;
    try {
      width = Integer.parseInt(getValue(SMALL_CHART_WIDTH_SETTING));
    } catch (Exception ex) {
    }
    return width;
  }


  public Boolean getPdiOnly() {
    Boolean pdiOnly = false;
    try {
      pdiOnly = Boolean.parseBoolean(getValue(PDI_ONLY_SETTING));
    } catch (Exception ex) {
    }
    return pdiOnly;
  }
  
  public AnalysisView getMetricsView() {
    AnalysisView analysisView = null;
    try {
      analysisView = Enum.valueOf(AnalysisView.class, getValue(VIEW_SETTING));
    } catch (Exception ex) {
      // Do nothing.
    }
    return analysisView;
  }

  public void setMetricsChartHeight(Integer height) {
    setValue(CHART_HEIGHT_SETTING, height != null ? height.toString() : ""); //$NON-NLS-1$
  }

  public void setMetricsChartWidth(Integer width) {
    setValue(CHART_WIDTH_SETTING, width != null ? width.toString() : ""); //$NON-NLS-1$
  }

  public void setMetricsExecutionLimit(Double limit) {
    setValue(LIMIT_SETTING, limit != null ? limit.toString() : ""); //$NON-NLS-1$
  }

  public void setMetricsInterval(Double interval) {
    setValue(INTERVAL_SETTING, interval != null ? interval.toString() : ""); //$NON-NLS-1$
  }

  public void setMetricsSmallChartHeight(Integer height) {
    setValue(SMALL_CHART_HEIGHT_SETTING, height != null ? height.toString() : ""); //$NON-NLS-1$
  }

  public void setMetricsSmallChartWidth(Integer width) {
    setValue(SMALL_CHART_WIDTH_SETTING, width != null ? width.toString() : ""); //$NON-NLS-1$
  }

  public void setMetricsView(AnalysisView view) {
    setValue(VIEW_SETTING, view != null ? view.toString() : ""); //$NON-NLS-1$
  }

  public void setPdiOnly(Boolean pdiOnly) {
    setValue(PDI_ONLY_SETTING, pdiOnly != null ? pdiOnly.toString() : "true"); //$NON-NLS-1$
  }
}
