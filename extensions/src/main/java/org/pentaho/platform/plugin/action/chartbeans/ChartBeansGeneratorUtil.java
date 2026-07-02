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
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Takes all inputs necessary to generate a chart and passes them to the ChartBeans engine (via an xaction).
 */
public class ChartBeansGeneratorUtil {

  private static IChartBeansGenerator generator;

  static {
    IChartBeansGenerator gen = PentahoSystem.get( IChartBeansGenerator.class );

    // If we are not running the PentahoSystem, at least give us a default generator...
    if ( gen == null ) {
      gen = new DefaultChartBeansGenerator();
    }
    generator = gen;
  }

  private ChartBeansGeneratorUtil() {
  }

  public static String createChartAsHtml( IPentahoSession userSession, Map<String, Object> parameterMap,
      String serializedChartDataDefinition, String serializedChartModel, int chartWidth, int chartHeight )
    throws IOException {

    return generator.createChartAsHtml( userSession, parameterMap, serializedChartDataDefinition, serializedChartModel,
      chartWidth, chartHeight );

  }

  public static String createChartAsHtml( IPentahoSession userSession, Map<String, Object> parameterMap,
      String serializedChartDataDefinition, String serializedChartModel, int chartWidth, int chartHeight,
      String contentLinkingTemplate ) throws IOException {
    // TODO: temp!

    return ( (DefaultChartBeansGenerator) generator ).createChartAsHtml( userSession, parameterMap,
      serializedChartDataDefinition, serializedChartModel, chartWidth, chartHeight, contentLinkingTemplate );

  }

  public static String mergeJFreeChartHtmlTemplate( File imageFile, String imageMap, String imageMapName,
      int chartWidth, int chartHeight, String contextPath ) {
    return generator.mergeJFreeChartHtmlTemplate( imageFile, imageMap, imageMapName, chartWidth, chartHeight,
      contextPath );
  }

  /**
   * Returns a complete HTML document that references a static image held in a temporary file on the server.
   * <p>
   * Only exposed for debugging (i.e. hosted mode) purposes.
   * </p>
   */
  public static String mergeStaticImageHtmlTemplate( String imageUrl ) {
    return generator.mergeStaticImageHtmlTemplate( imageUrl );
  }

  /**
   * Does this method belong in ChartBeansGeneratorUtil? ChartBeansGeneratorUtil may be more of a convenience for
   * executing the default ActionSequence, if this is to hold true, this method probably needs a new home more central
   * to the ChartBeans code. Returns a complete HTML document that references an Open Flash Chart SWF resource that
   * resides on the server along with the data that should be displayed in the chart (via a JavaScript function that
   * returns a JSON string).
   * <p>
   * Only exposed for debugging (i.e. hosted mode) purposes.
   * </p>
   */
  public static String mergeOpenFlashChartHtmlTemplate( String openFlashChartJson, String swfUrl ) {
    return generator.mergeOpenFlashChartHtmlTemplate( openFlashChartJson, swfUrl );
  }

  public static String buildEmptyOpenFlashChartHtmlFragment( String msg ) {
    return generator.buildEmptyOpenFlashChartHtmlFragment( msg );
  }

}
