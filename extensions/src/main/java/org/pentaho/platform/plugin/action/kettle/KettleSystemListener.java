/*!
 *
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
 *
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.kettle;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class KettleSystemListener implements IPentahoSystemListener {

  /**
   * If {@code true}, send Kettle output to the platform log file (e.g. pentaho.log) in addition to its normal
   * destinations.
   */
  private boolean usePlatformLogFile = true;

  private org.apache.logging.log4j.Logger logger = LogManager.getLogger( getClass() );

  public boolean startup( final IPentahoSession session ) {

    // Default DI_HOME System Property if not set
    if ( StringUtils.isEmpty( System.getProperty( "DI_HOME" ) ) ) {
      String defaultKettleHomePath = PentahoSystem.getApplicationContext().getSolutionPath( "system" + File.separator
          + "kettle" );
      logger.error( "DI_HOME System Property not properly set. The default location of " + defaultKettleHomePath
          + " will be used." );
      System.setProperty( "DI_HOME", defaultKettleHomePath );
    }

    if ( usePlatformLogFile ) {
      KettleLogStore.init( false, false );
      initLogging();
    }

    hookInDataSourceProvider();

    try {
      KettleSystemListener.environmentInit( session );
    } catch ( Throwable t ) {
      t.printStackTrace();
      Logger.error( KettleSystemListener.class.getName(), Messages.getInstance().getErrorString(
        "KettleSystemListener.ERROR_0001_PLUGIN_LOAD_FAILED" ) ); //$NON-NLS-1$
    }

    try {
      String slaveServerConfigFilename =
          "system" + File.separator + "kettle" + File.separator + "slave-server-config.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      File slaveServerConfigFile =
          new File( PentahoSystem.getApplicationContext().getSolutionPath( slaveServerConfigFilename ) );
      if ( slaveServerConfigFile.exists() ) {
        try ( InputStream is = new FileInputStream( slaveServerConfigFile ) ) {
          Node configNode = getSlaveServerConfigNode( is );
          SlaveServerConfig config = new DIServerConfig( new LogChannel( "Slave server config" ), configNode );
          config.setFilename( slaveServerConfigFile.getAbsolutePath() );
          SlaveServer slaveServer = new SlaveServer();
          config.setSlaveServer( slaveServer );
          CarteSingleton.setSlaveServerConfig( config );
        }
      }
    } catch ( Throwable t ) {
      t.printStackTrace();
      Logger.error( KettleSystemListener.class.getName(), t.getMessage() );
    }

    return true;
  }

  /**
   * Sends Kettle's logs to the platform log as well.
   */
  @SuppressWarnings( "unchecked" )
  protected void initLogging() {
    // Find the platform file listener (if any) and make sure it gets data from Kettle.
    // We listen to the log records from Kettle and pass logging along
    //

    LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
    Configuration config = ctx.getConfiguration();
    Map<String, Appender> appenderMap = config.getLoggerConfig( LogManager.getRootLogger().getName() ).getAppenders();
    for ( Appender appender : appenderMap.values() ) {
      if ( appender instanceof org.apache.logging.log4j.core.appender.RollingFileAppender ) {
        Log4jForwardingKettleLoggingEventListener listener = new Log4jForwardingKettleLoggingEventListener();
        KettleLogStore.getAppender().addLoggingEventListener( listener );
      }
    }
  }

  private void hookInDataSourceProvider() {
    try {
      @SuppressWarnings( "unused" )
      Class<?> clazz = Class.forName( "org.pentaho.di.core.database.DataSourceProviderInterface" ); //$NON-NLS-1$
      PlatformKettleDataSourceProvider.hookupProvider();
    } catch ( Exception ignored ) {
      // if here, then it's because we're running with an older
      // kettle.
    }
  }

  public static Map readProperties( final IPentahoSession session ) {

    Properties props = new Properties();
    String kettlePropsFilename = "system" + File.separator + "kettle" + File.separator + "kettle.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    InputStream is = null;
    try {
      File f = new File( PentahoSystem.getApplicationContext().getSolutionPath( kettlePropsFilename ) );
      if ( !f.exists() ) {
        return props;
      }
      is = new FileInputStream( f );
      props.load( is );
    } catch ( IOException ioe ) {
      Logger.error( KettleSystemListener.class.getName(), Messages.getInstance().getString(
        "KettleSystemListener.ERROR_0003_PROPERTY_FILE_READ_FAILED" ) + ioe.getMessage(), ioe ); //$NON-NLS-1$
    } finally {
      if ( is != null ) {
        try {
          is.close();
        } catch ( IOException e ) {
          // ignore
        }
      }
    }

    props.put( "pentaho.solutionpath", "solution:" ); //$NON-NLS-1$ //$NON-NLS-2$
    return props;

  }

  public static void environmentInit( final IPentahoSession session ) throws KettleException {
    // init kettle without simplejndi
    KettleEnvironment.init( false );
  }

  public void shutdown() {
    // Nothing required
  }

  public void setUsePlatformLogFile( final boolean usePlatformLogFile ) {
    this.usePlatformLogFile = usePlatformLogFile;
  }

  @VisibleForTesting
  Node getSlaveServerConfigNode( InputStream is )
    throws SAXException, IOException, ParserConfigurationException {
    Document document = XMLParserFactoryProducer.createSecureDocBuilderFactory().newDocumentBuilder().parse( is );
    return XMLHandler.getSubNode( document, SlaveServerConfig.XML_TAG );
  }
}
