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

package org.pentaho.platform.plugin.services.pluginmgr;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A plugin provider that does not rely on an the repository being configured
 * 
 * @author jamesdixon
 * 
 */
public class FileSystemXmlPluginProvider extends SystemPathXmlPluginProvider {

  @Override
  public List<IPlatformPlugin> getPlugins( IPentahoSession session ) throws PlatformPluginRegistrationException {
    List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

    // look in each of the system setting folders looking for plugin.xml files
    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath( "system" ); //$NON-NLS-1$
    File systemDir = new File( systemPath );
    if ( !systemDir.exists() || !systemDir.isDirectory() ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
        "PluginManager.ERROR_0004_CANNOT_FIND_SYSTEM_FOLDER" ) ); //$NON-NLS-1$
    }
    File[] kids = systemDir.listFiles();
    // look at each child to see if it is a folder
    for ( File kid : kids ) {
      if ( kid.isDirectory() ) {
        try {
          processDirectory( plugins, kid, session );
        } catch ( Throwable t ) {
          // don't throw an exception. we need to continue to process any remaining good plugins
          String msg =
              Messages.getInstance().getErrorString(
                  "SystemPathXmlPluginProvider.ERROR_0001_FAILED_TO_PROCESS_PLUGIN", kid.getAbsolutePath() ); //$NON-NLS-1$
          Logger.error( getClass().toString(), msg, t );
          PluginMessageLogger.add( msg );
        }
      }
    }

    return Collections.unmodifiableList( plugins );
  }

  @Override
  protected void processDirectory( List<IPlatformPlugin> plugins, File folder, IPentahoSession session )
    throws PlatformPluginRegistrationException {
    // see if there is a plugin.xml file
    FilenameFilter filter = new NameFileFilter( "plugin.xml", IOCase.SENSITIVE ); //$NON-NLS-1$
    File[] kids = folder.listFiles( filter );
    if ( kids == null || kids.length == 0 ) {
      return;
    }
    boolean hasLib = false;
    filter = new NameFileFilter( "lib", IOCase.SENSITIVE ); //$NON-NLS-1$
    kids = folder.listFiles( filter );
    if ( kids != null && kids.length > 0 ) {
      hasLib = kids[0].exists() && kids[0].isDirectory();
    }
    // we have found a plugin.xml file
    // get the file from the repository
    String path = "system" + File.separatorChar + folder.getName() + File.separatorChar + "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    Document doc;
    try {
      File f = new File( PentahoSystem.getApplicationContext().getSolutionPath( path ) );
      InputStream in = new FileInputStream( f );
      SAXReader reader = XMLParserFactoryProducer.getSAXReader( null );
      doc = reader.read( in );
      if ( doc != null ) {
        plugins.add( createPlugin( doc, session, folder.getName(), hasLib ) );
      }
    } catch ( Exception e ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ), e ); //$NON-NLS-1$
    }
    if ( doc == null ) {
      throw new PlatformPluginRegistrationException( Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path ) ); //$NON-NLS-1$
    }
  }
}
