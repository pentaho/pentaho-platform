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

package org.pentaho.platform.plugin.action.chartbeans;

import org.dom4j.Element;
import org.pentaho.chart.ChartBeanFactory;
import org.pentaho.chart.plugin.IChartPlugin;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChartBeansSystemListener implements IPentahoSystemListener {

  private String configFile = "chartbeans/chartbeans_config.xml"; //$NON-NLS-1$

  public ChartBeansSystemListener() {
  }

  public boolean startup( final IPentahoSession session ) {
    try {
      ChartBeanFactory.loadDefaultChartPlugins( initPlugins() );
      List<Element> nodes =
          PentahoSystem.getSystemSettings().getSystemSettings( configFile, "max-data-points-per-chart" ); //$NON-NLS-1$
      if ( nodes.size() > 0 ) {
        int maxDataPointsPerChart = Integer.parseInt( nodes.get( 0 ).getText() );
        if ( maxDataPointsPerChart > 0 ) {
          ChartBeanFactory.setMaxDataPointsPerChart( maxDataPointsPerChart );
        }
      }

    } catch ( Exception ex ) {
      Logger.warn( ChartBeansSystemListener.class.getName(), Messages.getInstance().getString(
        "ChartBeansSystemListener.ERROR_0004_LOAD_FAILED" ), //$NON-NLS-1$
        ex );
      return false;
    }
    return true;
  }

  /**
   * This methods looks in the chartbeans configuration file, and retrieves the prescribed list of chart plugins
   * (renderers). For each defined renderer, the method then looks to the PluginManager to see if that renderer has been
   * overridden. If it has, then the class defined in the PluginManager will be loaded IN PLACE OF the configuration
   * file's class.
   * 
   * @return list of available chart "plugin" (renderer) instances.
   * @throws Exception
   *           if no chart plugins (renderers) are found.
   */
  @SuppressWarnings( "unchecked" )
  private List<IChartPlugin> initPlugins() throws Exception {
    ArrayList<IChartPlugin> plugins = new ArrayList<IChartPlugin>();
    HashMap<String, Object> pluginMap = new HashMap<String, Object>();

    List<Element> nodes = PentahoSystem.getSystemSettings().getSystemSettings( configFile, "bean" ); //$NON-NLS-1$

    if ( nodes == null || nodes.size() == 0 ) {
      String msg = Messages.getInstance().getString( "ChartBeansSystemListener.ERROR_0001_CONFIG_MISSING" ); //$NON-NLS-1$
      Logger.warn( ChartBeansSystemListener.class.getName(), msg );
      throw new ChartSystemInitializationException( msg );
    }

    Element node;
    String id;
    Object plugin;
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    for ( int i = 0; i < nodes.size(); i++ ) {
      node = nodes.get( i );
      id = node.attribute( "id" ).getText(); //$NON-NLS-1$
      pluginMap.put( id, node.attribute( "class" ).getText() ); //$NON-NLS-1$

      // Now let's see if there is a plugin overriding this engine...
      if ( ( null != pluginManager ) && ( pluginManager.isBeanRegistered( id ) ) ) {
        try {
          plugin = pluginManager.getBean( id );
          pluginMap.put( id, plugin );
        } catch ( PluginBeanException e ) {
          Logger.warn( ChartBeansSystemListener.class.getName(), Messages.getInstance().getString(
              "ChartBeansSystemListener.ERROR_0002_PLUGINMANAGER_BEAN_MISSING", id ), //$NON-NLS-1$
              e );
        }
      }
    }

    for ( Object clazz : pluginMap.values() ) {

      try {
        if ( clazz instanceof String ) {
          plugins.add( (IChartPlugin) Class.forName( clazz.toString() ).newInstance() );
        } else {
          plugins.add( (IChartPlugin) clazz );
        }
      } catch ( Exception ex ) {
        Logger.warn( ChartBeansSystemListener.class.getName(), Messages.getInstance().getString(
            "ChartBeansSystemListener.ERROR_0003_CLASS_CREATION_PROBLEM" ) + clazz, ex ); //$NON-NLS-1$
      }
    }
    return plugins;
  }

  public void setConfigFile( String configFile ) {
    this.configFile = configFile;
  }

  public void shutdown() {
    // Nothing required
  }

}
