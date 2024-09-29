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


package org.pentaho.platform.plugin.services.pluginmgr.servicemgr;

import org.pentaho.platform.plugin.services.webservices.AbstractAxisConfigurator;
import org.pentaho.platform.plugin.services.webservices.FileSystemAxisConfigurator;

/**
 * A web service manager that only uses the file system for configuration
 * 
 * @author jamesdixon
 * 
 */
public class FileSystemAxisWebServiceManager extends AxisWebServiceManager {

  private FileSystemAxisConfigurator configurator = new FileSystemAxisConfigurator();

  @Override
  protected AbstractAxisConfigurator getConfigurator() {
    return configurator;
  }

}
