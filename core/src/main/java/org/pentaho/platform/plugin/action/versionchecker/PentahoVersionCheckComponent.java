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


package org.pentaho.platform.plugin.action.versionchecker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.services.solution.ComponentBase;

/**
 * Version Check Component This component makes a call to pentaho's server to see if a new version is a vailable.
 *
 * Uses reflection helper so that versioncheck.jar can be deleted without problems
 *
 * input param "ignoreExistingUpdates" - if true, ignore existing updates discovered
 *
 * @author Will Gorman
 *
 */
public class PentahoVersionCheckComponent extends ComponentBase {

  private static final long serialVersionUID = 8178713714323100555L;

  private static final String DOCUMENT = "document"; //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog( PentahoVersionCheckComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  public void done() {

  }

  @Override
  protected boolean executeAction() {
    getLogger().warn( "Version Checker has been DEPRECATED and will be deleted in a upcoming release." );
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

}
