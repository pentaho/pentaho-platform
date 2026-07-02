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


package org.pentaho.platform.plugin.services.security.userrole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * This listener ensures that the Authentication system has been loaded. Security must be started before the repository.
 */
public class SecuritySystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog( SecuritySystemListener.class );

  @Override
  public boolean startup( IPentahoSession session ) {
    PentahoSystem.get( ProviderManager.class, "authenticationManager", session  );

    changeFilterChainProxyHttpFirewall(); // BACKLOG-22526

    return true;
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  /**
   * https://jira.pentaho.com/browse/BACKLOG-22526
   *
   * StrictHttpFirewall was added and made the default HTTPFirewall for FilterChainProxy. As per its javadoc, it is meant
   * to block requests that contains one of the following characters in the URL: period, forward slash, backslash, semicolon, percentage
   *
   * StrictHttpFirewall was added to address 3 different CVEs: CVE-2016-5007, CVE-2016-9879, CVE-2018-1199
   *
   * However, Pentaho's file/folder endpoint resources are passed as path params. And we do support file/folder names with the
   * aforementioned characters. In light of this, we are setting a more lenient HttpFirewall for FilterChainProxy
   *
   * @link https://github.com/spring-projects/spring-security/blob/4.1.5.RELEASE/web/src/main/java/org/springframework/security/web/firewall/StrictHttpFirewall.java
   */
  private void changeFilterChainProxyHttpFirewall() {

    StrictHttpFirewall notSoStrictHttpFirewall = new StrictHttpFirewall();

    notSoStrictHttpFirewall.setAllowSemicolon( true );
    notSoStrictHttpFirewall.setAllowUrlEncodedPercent( true );
    notSoStrictHttpFirewall.setAllowUrlEncodedPeriod( true );

    try {

      FilterChainProxy filterChainProxy = PentahoSystem.get( FilterChainProxy.class, "filterChainProxy", null );

      if ( filterChainProxy != null ) {
        logger.debug( "Changing FilterChainProxy's HttpFirewall to a more lenient one that allows for the passing "
            + "of semicolons, periods, and percentages signs in the URL path" ); //$NON-NLS-1$

        filterChainProxy.setFirewall( notSoStrictHttpFirewall );
      }

    } catch ( Throwable t ) {
      logger.error( t );
    }
  }
}
