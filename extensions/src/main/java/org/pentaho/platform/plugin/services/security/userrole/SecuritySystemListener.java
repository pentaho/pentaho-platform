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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
