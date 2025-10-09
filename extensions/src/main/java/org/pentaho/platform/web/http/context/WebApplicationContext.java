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


package org.pentaho.platform.web.http.context;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.web.http.messages.Messages;

public class WebApplicationContext extends StandaloneApplicationContext {

  private String fullyQualifiedServerUrl;

  public WebApplicationContext( final String solutionRootPath, final String fullyQualifiedServerUrl,
      final String applicationPath, final Object context ) {
    super( solutionRootPath, applicationPath, context );
    // TODO sbarkdull, do we need to consider path separators for windows?
    // assert !baseUrl.endsWith("\\") :
    // "Base URL in WebApplicationContext appears to be using Windows path separators.";

    this.fullyQualifiedServerUrl = fullyQualifiedServerUrl;
  }

  public WebApplicationContext( final String solutionRootPath, final String fullyQualifiedServerUrl,
      final String applicationPath ) {
    super( solutionRootPath, applicationPath );
    // TODO sbarkdull, do we need to consider path separators for windows?
    // assert !baseUrl.endsWith("\\") :
    // "Base URL in WebApplicationContext appears to be using Windows path separators.";

    this.fullyQualifiedServerUrl = fullyQualifiedServerUrl;
  }

  @Override
  public String getFullyQualifiedServerURL() {
    if ( !fullyQualifiedServerUrl.endsWith( "/" ) ) { //$NON-NLS-1$
      fullyQualifiedServerUrl = fullyQualifiedServerUrl + "/"; //$NON-NLS-1$
    }
    return fullyQualifiedServerUrl;
  }

  @Override
  public String getPentahoServerName() {
    return PentahoSystem
        .getSystemSetting( "name", Messages.getInstance().getString( "PentahoSystem.USER_SYSTEM_TITLE" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void setFullyQualifiedServerURL( final String fullyQualifiedServerUrl ) {
    this.fullyQualifiedServerUrl = fullyQualifiedServerUrl;
  }

}
