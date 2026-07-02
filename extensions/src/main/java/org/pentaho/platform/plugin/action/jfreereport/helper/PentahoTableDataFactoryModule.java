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


package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.pentaho.reporting.engine.classic.core.metadata.ElementMetaDataParser;
import org.pentaho.reporting.libraries.base.boot.AbstractModule;
import org.pentaho.reporting.libraries.base.boot.ModuleInitializeException;
import org.pentaho.reporting.libraries.base.boot.SubSystem;

/**
 * Defines the Module for PentahoTableDataFactory
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class PentahoTableDataFactoryModule extends AbstractModule {
  public PentahoTableDataFactoryModule() throws ModuleInitializeException {
    loadModuleInfo();
  }

  /**
   * Initializes the module. Use this method to perform all initial setup operations. This method is called only once in
   * a modules lifetime. If the initializing cannot be completed, throw a ModuleInitializeException to indicate the
   * error. The module will not be available to the system.
   * 
   * @param subSystem
   *          the subSystem.
   * @throws ModuleInitializeException
   *           if an error occurred while initializing the module.
   */
  public void initialize( final SubSystem subSystem ) throws ModuleInitializeException {
    ElementMetaDataParser
        .initializeOptionalDataFactoryMetaData( "org/pentaho/platform/plugin/action/jfreereport/helper/meta-datafactory.xml" ); //$NON-NLS-1$
  }
}
