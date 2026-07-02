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


package org.pentaho.platform.plugin.action.chartbeans;

import org.pentaho.platform.api.engine.IPentahoSession;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface IChartBeansGenerator {

  /**
   * Convenience method that returns a complete HTML document containing the chart. Resource references point back to
   * the BI Server.
   */
  public String createChartAsHtml( IPentahoSession userSession, Map<String, Object> parameterMap,
      String serializedChartDataDefinition, String serializedChartModel, int chartWidth, int chartHeight )
    throws IOException;

  public String mergeStaticImageHtmlTemplate( String imageUrl );

  public String mergeOpenFlashChartHtmlTemplate( String openFlashChartJson, String swfUrl );

  public String buildEmptyOpenFlashChartHtmlFragment( String msg );

  public String buildOpenFlashChartHtmlFragment( String openFlashChartJson, String swfUrl, String chartWidth,
      String chartHeight );

  public String getHtmlTemplate();

  public String getFlashScriptFragment();

  public String getFlashObjectFragment();

  public String mergeJFreeChartHtmlTemplate( File imageFile, String imageMap, String imageMapName, int chartWidth,
      int chartHeight, String contextPath );
}
