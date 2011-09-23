package org.pentaho.platform.plugin.action.chartbeans;

import org.pentaho.chart.IChartLinkGenerator;

/**
 * Provides a collection of callback methods used by the chart beans API which
 * give the api user the ability link data points in the chart URLs or javascript. When the user
 * clicks on a bar, point, or pie slice the user will execute the link or javascript provided by 
 * implementors of this interface
 * @author arodriguez
 */
public class ChartLinkGenerator implements IChartLinkGenerator {

  private String urlTemplate;
  
  /**
   * Constructs a chart link generator. There are three placeholders that can be used
   * within the url template that will be replaced with the value at a given data point
   * on the chart. The placeholders are "{series}", "{domain}", and "{range}". For example if 
   * the template is http://www.weather.com?postalCode={domain} then at run time the string 
   * "{domain}" will be replaced the the domain value of each data point. Prefix all javascript 
   * function calls with "javascript:" (Ex. javascript:alert('hello world')). 
   */
  public ChartLinkGenerator(String urlTemplate) {
    this.urlTemplate = urlTemplate;
  }
  
  public String generateLink(String seriesName, String domainName, Number rangeValue) {
    // escape single quotes for javascript
    seriesName = resolveEscapeCharacters(seriesName);
    domainName = resolveEscapeCharacters(domainName);
    return urlTemplate.replaceAll("\\{series\\}", seriesName == null ? "" : seriesName).replaceAll("\\{domain\\}", domainName == null ? "" : domainName).replaceAll("\\{range\\}", rangeValue == null ? "" : rangeValue.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
  }

  private String resolveEscapeCharacters(String s) {
    if (this.urlTemplate.startsWith("javascript:") && s.indexOf("'") >= 0) {
      s = s.replaceAll("'", "\\\\\\\\'");
    }
    return s;
  }

  public String generateLink(String seriesName, Number domainValue, Number rangeValue) {
    // escape single quotes for javascript
    seriesName = resolveEscapeCharacters(seriesName);
    return urlTemplate.replaceAll("\\{series\\}", seriesName == null ? "" : seriesName).replaceAll("\\{domain\\}", domainValue == null ? "" : domainValue.toString()).replaceAll("\\{range\\}", rangeValue == null ? "" : rangeValue.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
  }

}
