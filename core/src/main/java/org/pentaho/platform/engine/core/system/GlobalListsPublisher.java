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


package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;

public class GlobalListsPublisher extends BasePublisher {

  private static final long serialVersionUID = -8247487696354924652L;

  private static final Log logger = LogFactory.getLog( GlobalListsPublisher.class );

  @Override
  public Log getLogger() {
    return GlobalListsPublisher.logger;
  }

  public String getName() {
    return Messages.getInstance().getString( "GlobalListsPublisher.USER_SYSTEM_SETTINGS" ); //$NON-NLS-1$
  }

  public String getDescription() {
    return Messages.getInstance().getString( "GlobalListsPublisher.USER_DESCRIPTION" ); //$NON-NLS-1$
  }

  @Override
  public String publish( final IPentahoSession localSession ) {
    try {
      PentahoSystem.clearGlobals();
      PentahoSystem.globalStartup( localSession );
    } catch ( Throwable t ) {
      error( Messages.getInstance().getErrorString( "GlobalListsPublisher.ERROR_0001_PUBLISH_FAILED" ), t ); //$NON-NLS-1$
      return Messages.getInstance().getString( "GlobalListsPublisher.USER_ERROR_PUBLISH_FAILED" ) + t.getLocalizedMessage(); //$NON-NLS-1$
    }
    return Messages.getInstance().getString( "GlobalListsPublisher.USER_SYSTEM_SETTINGS_UPDATED" ); //$NON-NLS-1$
  }

}
