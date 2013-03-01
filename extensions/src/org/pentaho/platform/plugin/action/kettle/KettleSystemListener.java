/*
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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created Jan 9, 2006 
 * @author mbatchel
 */
package org.pentaho.platform.plugin.action.kettle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class KettleSystemListener implements IPentahoSystemListener {

  /**
   * If {@code true}, send Kettle output to the platform log file (e.g. pentaho.log) in addition to its normal 
   * destinations.
   */
  private boolean usePlatformLogFile = true;
  
  public boolean startup(final IPentahoSession session) {
    
    if (usePlatformLogFile) {
      initLogging();
    }
    
    hookInDataSourceProvider();
    
    // Inform Kettle where the plugins should be loaded
    System.setProperty(Const.PLUGIN_BASE_FOLDERS_PROP, 
        Const.DEFAULT_PLUGIN_BASE_FOLDERS + "," +  //$NON-NLS-1$
        PentahoSystem.getApplicationContext().getSolutionPath("system/kettle/plugins")); //$NON-NLS-1$

    try {
      KettleSystemListener.environmentInit(session);
    } catch (Throwable t) {
      t.printStackTrace();
      Logger.error(KettleSystemListener.class.getName(), Messages.getInstance()
          .getErrorString("KettleSystemListener.ERROR_0001_PLUGIN_LOAD_FAILED")); //$NON-NLS-1$
    }
    
    try {
      String slaveServerConfigFilename = "system" + File.separator + "kettle" + File.separator + "slave-server-config.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      File slaveServerConfigFile = new File(PentahoSystem.getApplicationContext().getSolutionPath(slaveServerConfigFilename));
      if (slaveServerConfigFile.exists()) {
        InputStream is = new FileInputStream(slaveServerConfigFile);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        Node configNode = XMLHandler.getSubNode(document, SlaveServerConfig.XML_TAG);
        SlaveServerConfig config = new SlaveServerConfig(new LogChannel("Slave server config"), configNode);
        config.setFilename(slaveServerConfigFilename);
        SlaveServer slaveServer = new SlaveServer();
        config.setSlaveServer(slaveServer);
        CarteSingleton.setSlaveServerConfig(config);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      Logger.error(KettleSystemListener.class.getName(), t.getMessage());
    }
    
    return true;
  }

  /**
   * Sends Kettle's logs to the platform log as well.
   */
  @SuppressWarnings("unchecked")
  protected void initLogging() {
    // Find the platform file listener (if any) and make sure it gets data from Kettle.
    // We listen to the log records from Kettle and pass logging along
    //
    Enumeration<org.apache.log4j.Appender> appenders = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
    while (appenders.hasMoreElements()) {
      org.apache.log4j.Appender appender = appenders.nextElement();
      if (appender instanceof org.apache.log4j.FileAppender) {
        
        Log4jForwardingKettleLoggingEventListener listener = new Log4jForwardingKettleLoggingEventListener(appender);
        CentralLogStore.getAppender().addLoggingEventListener(listener);
      }
    }
  }
  
  private void hookInDataSourceProvider() {
    try {
      @SuppressWarnings("unused")
      Class clazz = Class.forName("org.pentaho.di.core.database.DataSourceProviderInterface"); //$NON-NLS-1$
      PlatformKettleDataSourceProvider.hookupProvider();
    } catch (Exception ignored) {
      // if here, then it's because we're running with an older
      // kettle.
    }
  }
  
  public static Map readProperties(final IPentahoSession session) {

    Properties props = new Properties();
    String kettlePropsFilename = "system" + File.separator + "kettle" + File.separator + "kettle.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    InputStream is = null;
    try {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class,session);

      if (!repository.resourceExists(kettlePropsFilename, ISolutionRepository.ACTION_EXECUTE)) {
        return props;
      }
      is = ActionSequenceResource.getInputStream(kettlePropsFilename, null);
      props.load(is);
    } catch (IOException ioe) {
      Logger.error(KettleSystemListener.class.getName(), Messages.getInstance()
          .getString("KettleSystemListener.ERROR_0003_PROPERTY_FILE_READ_FAILED") + ioe.getMessage(), ioe); //$NON-NLS-1$
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    props.put("pentaho.solutionpath", PentahoSystem.getApplicationContext().getFileOutputPath("")); //$NON-NLS-1$ //$NON-NLS-2$
    return props;

  }

  public static void environmentInit(final IPentahoSession session) throws KettleException {
    // init kettle without simplejndi
    KettleEnvironment.init(false);
  }

  public void shutdown() {
    // Nothing required
  }

  public void setUsePlatformLogFile(final boolean usePlatformLogFile) {
    this.usePlatformLogFile = usePlatformLogFile;
  }

}