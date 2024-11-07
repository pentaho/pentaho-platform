/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.uifoundation.chart;

import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;

import java.util.Date;

public class PentahoChartURLTagFragmentGenerator extends StandardURLTagFragmentGenerator {

  private static final String SERIES_TAG = "series="; //$NON-NLS-1$

  private static final String CATEGORY_TAG = "category="; //$NON-NLS-1$

  private static final String ITEM_TAG = "item="; //$NON-NLS-1$

  String urlFragment;

  Dataset dataset;

  String parameterName;

  String seriesName;

  String urlTarget;

  boolean useBaseUrl;

  IPentahoResultSet data;

  int i = 0;

  public PentahoChartURLTagFragmentGenerator( final String urlFragment, final Dataset dataset,
      final String parameterName, final String outerParameterName ) {
    super();

    this.urlFragment = urlFragment;
    this.dataset = dataset;
    this.parameterName = parameterName;
    this.seriesName = outerParameterName;
    this.urlTarget = "pentaho_popup"; //$NON-NLS-1$ 
    this.useBaseUrl = true;
  }

  public PentahoChartURLTagFragmentGenerator( final String urlFragment, final String urlTarget,
      final boolean useBaseUrl, final Dataset dataset, final String parameterName, final String outerParameterName ) {
    super();
    this.urlFragment = urlFragment;
    this.dataset = dataset;
    this.parameterName = parameterName;
    this.seriesName = outerParameterName;
    this.urlTarget = urlTarget;
    this.useBaseUrl = useBaseUrl;
  }

  public PentahoChartURLTagFragmentGenerator( final String urlTemplate, final Dataset dataDefinition,
      final String paramName ) {
    this( urlTemplate, dataDefinition, paramName, "" ); //$NON-NLS-1$
  }

  @Override
  public String generateURLFragment( final String urlText ) {
    if ( urlFragment != null ) {

      String urlTemplate = " href=\""; //$NON-NLS-1$

      // do not add ase URL if script
      boolean isScript = urlFragment.startsWith( "javascript:" ); //$NON-NLS-1$ 

      // If isScript is true, ignore useBaseURL parameter...
      if ( !isScript ) {
        if ( useBaseUrl ) {
          urlTemplate += PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
        }
      }

      // Handle " in the urlFragment
      urlTemplate += urlFragment.replaceAll( "\"", "%22" ) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 

      String value = null;
      String itemValue = null;

      // Do we have a 'category=' as part of the urlText in? If so, grab the value from the urlText
      // this is the replacement value for the paramName parameter (when categorical).
      value = retrieveValue( PentahoChartURLTagFragmentGenerator.CATEGORY_TAG, urlText );

      if ( value != null ) {
        urlTemplate = TemplateUtil.applyTemplate( urlTemplate, parameterName, value ); // <paramName> replacement
                                                                                       // value
      }

      // Do we have a 'series=' as part of the urlText in? If so, grab the value from the urlText
      // this is the replacement value for the series-name parameter.
      value = retrieveValue( PentahoChartURLTagFragmentGenerator.SERIES_TAG, urlText );
      if ( value != null ) {

        if ( ( dataset instanceof CategoryDatasetChartDefinition )
            || ( dataset instanceof XYZSeriesCollectionChartDefinition ) ) {

          urlTemplate = TemplateUtil.applyTemplate( urlTemplate, seriesName, value ); // <series-name> replacement
                                                                                      // value

        } else if ( dataset instanceof XYDataset ) {

          XYDataset set = (XYDataset) dataset;
          Comparable<?> seriesKey = set.getSeriesKey( Integer.parseInt( value ) );
          urlTemplate = TemplateUtil.applyTemplate( urlTemplate, seriesName, seriesKey.toString() ); // <series-name>
                                                                                                     // replacement
                                                                                                     // value

          // Do we have an 'item=' as part of the urlText in? If so, grab the value from the urlText
          // this is the replacement value for the paramName parameter, when the chart is an x/y plot.
          itemValue = retrieveValue( PentahoChartURLTagFragmentGenerator.ITEM_TAG, urlText );

          if ( itemValue != null ) {

            int itemVal = Integer.parseInt( itemValue );
            int val = Integer.parseInt( value );

            Object x = null;
            Number xNum = set.getX( val, itemVal );
            x = ( xNum instanceof Long ) ? new Date( (Long) xNum ) : xNum;

            urlTemplate = TemplateUtil.applyTemplate( urlTemplate, parameterName, x.toString() ); // <paramName>
                                                                                                  // replacement
                                                                                                  // value

            // This value is NEW. We have never returned more than 2 parameters in the url-template.
            // A logical extension for x/y plots is to return the series, the x value and the y value.
            // However, the item value is not plumbed through to the chart definition yet.
            Object y = null;
            Number yNum = set.getY( val, itemVal );
            y = ( yNum instanceof Long ) ? new Date( (Long) yNum ) : yNum;

            urlTemplate = TemplateUtil.applyTemplate( urlTemplate, "ITEM", y.toString() ); // {ITEM} replacement
                                                                                           // value,
                                                                                           // in the
                                                                                           // url-template. There
                                                                                           // is no
                                                                                           // parameter
                                                                                           // plumbed for this.
          }
        }
      }

      if ( !isScript ) {
        urlTemplate = urlTemplate + " target=\"" + urlTarget + "\""; //$NON-NLS-1$//$NON-NLS-2$ 
      }

      return urlTemplate;
    } else {
      return super.generateURLFragment( urlText );
    }
  }

  private String retrieveValue( String tag, String urlText ) {
    String returnValue = null;
    int startIdx, endIdx;

    if ( urlText.contains( tag ) ) {
      startIdx = urlText.indexOf( tag ) + tag.length();

      if ( urlText.indexOf( '&', startIdx ) != -1 ) {
        endIdx = urlText.indexOf( '&', startIdx );
      } else {
        endIdx = urlText.length();
      }
      returnValue = urlText.substring( startIdx, endIdx ).trim();
    }
    return returnValue;
  }
}
