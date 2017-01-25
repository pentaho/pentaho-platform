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
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class PentahoOFC4JChartHelper {

  private static final Log logger = LogFactory.getLog( PentahoOFC4JChartHelper.class );

  private static final String CHART_TYPE_NODE_LOC = "chart-type"; //$NON-NLS-1$
  private static final String CHART_TYPE_DEFAULT = "BarChart"; //$NON-NLS-1$

  private static final String PLUGIN_BUNDLE_NAME =
      "org.pentaho.platform.plugin.action.openflashchart.factory.chartfactories"; //$NON-NLS-1$

  private static final String SOLUTION_PROPS = "system/openflashchart.properties"; //$NON-NLS-1$

  @SuppressWarnings( "unchecked" )
  static Map factories = null;

  @SuppressWarnings( "unchecked" )
  public static String generateChartJson( Node chartNode, IPentahoResultSet data, boolean byRow, Log log ) {
    String chartType = null;
    String factoryClassString = null;
    try {
      Node temp = chartNode.selectSingleNode( CHART_TYPE_NODE_LOC );
      if ( AbstractChartFactory.getValue( temp ) != null ) {
        chartType = AbstractChartFactory.getValue( temp );
      } else {
        // This should NEVER happen.
        chartType = CHART_TYPE_DEFAULT;
      }

      factoryClassString = (String) getChartFactories().get( chartType );
      if ( factoryClassString == null ) {
        throw new RuntimeException( Messages.getInstance().getErrorString(
          "PentahoOFC4JChartHelper.ERROR_0001_FACTORY_INIT", chartType, factoryClassString ) ); //$NON-NLS-1$
      } else {

        Class factoryClass = Class.forName( factoryClassString );

        // throw exception if factoryClass not found

        IChartFactory factory = (IChartFactory) factoryClass.getConstructor( new Class[0] )
          .newInstance( new Object[ 0 ] );
        factory.setChartNode( chartNode );
        factory.setLog( log );
        if ( byRow ) {
          factory.setData( PentahoDataTransmuter.pivot( data ) );
        } else {
          factory.setData( data );
        }
        return factory.convertToJson();
      }
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString(
        "PentahoOFC4JChartHelper.ERROR_0001_FACTORY_INIT", chartType, factoryClassString ), e ); //$NON-NLS-1$
      throw new RuntimeException( e );
    }
  }

  @SuppressWarnings( { "unchecked" } )
  protected static Map getChartFactories() {
    if ( factories == null ) {
      factories = Collections.synchronizedMap( createChartFactoryMap() );
    }
    return factories;
  }

  @SuppressWarnings( "unchecked" )
  private static Map createChartFactoryMap() {
    Properties chartFactories = new Properties();
    // First, get known chart factories...
    try {
      ResourceBundle pluginBundle = ResourceBundle.getBundle( PLUGIN_BUNDLE_NAME );
      if ( pluginBundle != null ) { // Copy the bundle here...
        Enumeration keyEnum = pluginBundle.getKeys();
        String bundleKey = null;
        while ( keyEnum.hasMoreElements() ) {
          bundleKey = (String) keyEnum.nextElement();
          chartFactories.put( bundleKey, pluginBundle.getString( bundleKey ) );
        }
      }
    } catch ( Exception ex ) {
      logger
          .warn( Messages.getInstance().getString( "PentahoOFC4JChartHelper.WARN_NO_CHART_FACTORY_PROPERTIES_BUNDLE"
      ) ); //$NON-NLS-1$
    }
    // Get overrides...
    //
    // Note - If the override wants to remove an existing "known" plugin,
    // simply adding an empty value will cause the "known" plugin to be removed.
    //
    if ( PentahoSystem.getApplicationContext() == null ) {
      return chartFactories;
    }
    File f = new File( PentahoSystem.getApplicationContext().getSolutionPath( SOLUTION_PROPS ) );
    if ( !f.exists() ) {
      // this is ok
      return chartFactories;
    }
    InputStream is = null;
    try {
      is = new FileInputStream( f );
      Properties overrideChartFactories = new Properties();
      overrideChartFactories.load( is );
      chartFactories.putAll( overrideChartFactories ); // load over the top of the known properties
    } catch ( FileNotFoundException ignored ) {
      logger.warn( Messages.getInstance().getString( "PentahoOFC4JChartHelper.WARN_NO_CHART_FACTORY_PROPERTIES" ) ); //$NON-NLS-1$
    } catch ( IOException ignored ) {
      logger.warn(
          Messages.getInstance().getString( "PentahoOFC4JChartHelper.WARN_BAD_CHART_FACTORY_PROPERTIES" ), ignored ); //$NON-NLS-1$
    } finally {
      try {
        if ( is != null ) {
          is.close();
        }
      } catch ( Exception e ) {
        //ignore
      }
    }

    return chartFactories;
  }
}
